package automation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import javax.mail.Session;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
/******************************************************************************************
 * The program uses a WebDriver to scrape information off of the Red Lion PTV
 * web server @ 192.168.1.180. If the values change an email will be sent out to the
 * respective email address.
 * 
 * @author Johnny Duncan
 * @version: 2.0
 * @LastUpdated: 7/6/2018                                                             
 *****************************************************************************************/
public class RedLionDriver {
	
	static int urlFirstTimeFlag = 0;
	static int fileFlag = 0;
	static int alarmResetFlag = 0;
	static int totalBagFlag = 0;
	static int eastQuotaFlag = 0;
	static int westQuotaFlag = 0;
	static int rebagQuotaFlag = 0;
	static int EODFlag = 0;
	static int EastLine, WestLine, Rebagger, emailTest;
	static int eastTmp, westTmp, rebaggerTmp, emailTestTmp;
	static int eLineCount, wLineCount, rebagLineCount, totalLineCount;
	static int eastToggle, westToggle, rebagToggle, programToggle;
	static int firstBreak, lunchBreak, secondBreak;
	static int firstBreakFlag = 0, lunchBreakFlag = 0, secondBreakFlag = 0; 
	final static int eastDailyQuota = 2700;
	final static int westDailyQuota = 2700;
	final static int rebagDailyQuota = 2700;
	static WebDriver driver;
	static Timestamp ts;
	static Session session;
	static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	//static Logger l = Logger.getLogger(RedLionDriver.class.getName());
	
	public static void main(String[] args) throws InterruptedException, FileNotFoundException {

		LocalDateTime now = LocalDateTime.now();
		//System.out.println() will now print to file instead of console.
		File dir = new File("C:\\Users\\johnny\\Dropbox\\RL_Logs\\java_Logs\\" + dtf.format(now) + ".txt");
		PrintStream o = new PrintStream(dir);
		System.setOut(o);
		
		//run continuously
		while (true) {
			Calendar cal = Calendar.getInstance();
			
			//get the day of the week Sun-1, MON-2,TUES-3,WED-4,THURS-5,FRI-6,SAT-7
			int day = cal.get(Calendar.DAY_OF_WEEK);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);
			//int second = cal.get(Calendar.SECOND);
			//CONTINUE AS LONG AS MON-FRI
			if (day >= 2 && day <= 6) {
				System.out.println(timeStamp());
				//create a new file at midnight when flag is 0
				duringWorkHours(hour, minute);
			}
			//sleep for an hour during off hours resets alarm flags
			//resets variables
			//**********possibly just use system.exit() instead?
			else {
				checkKillSwitch();
				if (alarmResetFlag == 1) {
					alarmResetFlag = 0;
					EastLine = WestLine = Rebagger = emailTest = 0;
					eastTmp = westTmp = rebaggerTmp = emailTestTmp = 0;
				}
				System.out.println("outside work hours");
				Thread.sleep(3600000);
				return;
			}
		}
	}
	/////////////////////////////////////////////////////////////////////////////////
	/********************************************************************************
	* duringWorkHours method receives the hour and minute in military time to       *
	* determine which category to look into to.                                     *
	* the times are:															    *
	* 6:45am - 11:59 am  (morning)									                *
	* 12:00 pm - 12:59pm (lunch break)                                              *
	* 1:00pm - 6:15pm    (afternoon)                                                *
	* 6:16pm -> shutdown (after hours)                                              *
	* 
	* @param hour = the hour received from main method in 24 hour format.           *
	* @param minute = the minute received from main method.                         *
	* @param alarmResetFlag = flag that signals on/off for when to reset alarm      *
	* 			alarm counters.                                                     *
	* @param EODFlag = end of day flag that signals once a day to send email        *
	* 			containing the end of day line totals.                              *
	* @param fileFlag = signals the creation of a new file each day.                *	
	********************************************************************************/
	/////////////////////////////////////////////////////////////////////////////////
	public static void duringWorkHours(int hour, int minute ) throws InterruptedException {
		
		//Start at 7am till lunch time; sleep for 30 seconds.
		if (hour >= 6 && (hour <= 11 && minute <= 59)) {
			System.out.println("6:45am-11:59am");
			launchChrome();
			Thread.sleep(30000);
			if (alarmResetFlag == 0) 
				alarmResetFlag = 1;
		}	
		else if ((hour == 18 && minute >= 15) && EODFlag == 0) {
			String subject = "End of Day Report " + timeStamp();
			EODFlag = 1;
			System.out.println("operation closing for the day");
			printTotals(subject);
			System.out.println("off-hours shutting down chromedriver");
			driver.quit();
			Thread.sleep(30000);
		}
		//if after lunch time and before close; sleep for 30 seconds
		else if (hour >= 13 && hour <= 19 ) {
			System.out.println("1-6:15pm");
			launchChrome();
			Thread.sleep(30000);
		}
		//lunch-time sleep for 5 minute intervals
		else if (hour == 12 && minute <= 59) {
			System.out.println("lunch-time");
			launchChrome();
			Thread.sleep(300000);
		}
		//during off hours sleep for 1 hour periods
		//sets fileFlag to 0
		else {
			fileFlag = 0;
			System.out.println("off-hours shutting down chromedriver");
			//kills chromedriver
			driver.quit();
			Thread.sleep(3600000);
		}
	}
	/////////////////////////////////////////////////////////////////////////////////
	/********************************************************************************
	* Driver method used to retrieve values from Red Lion's web server.             *
	* @param username = user login for the red lion web server.                     *
	* @param password = user password for the red lion web server.                  *
	* @param credentials = is the combination of both username and password into one*
	* 			variable to send when navigating to the url.                        *
	* @param URL = the web address to the Red Lion Web Server.                      *
	* @param exePath = is the directory path to execute the chromedriver.exe        *
	* @param urlFirstTimeFlag = flag that signals first time this program is run.   *
	* @param driver = driver for the chromedriver.exe                               *
	* @param EastLine = holds the alarm count for the East Line                     *
	* @param WestLine = holds the alarm count for the West Line                     *
	* @param Rebagger = holds the alarm count for the Rebagger Line                 *
	********************************************************************************/
	/////////////////////////////////////////////////////////////////////////////////
	public static void launchChrome() throws InterruptedException  {
		
		String username = "*******";
      String password = "********";
      String credentials = username + ":" + password;
      String URL = "http://"+credentials+"@***.***.***.***";
      String exePath = "C:\\Users\\johnny\\eclipse-workspace\\downloadlocation\\chromedriver.exe";		
      System.setProperty("webdriver.chrome.driver", exePath);
		
      //logs into url first time
		if (urlFirstTimeFlag == 0) {
			driver = new ChromeDriver();
			driver.get(URL);
			driver.manage().window().setSize(new Dimension(225,575));
			urlFirstTimeFlag = 1;
			//add current value then look to update after a change???
			getWebValues();
			printCurrentValues();
			checkKillSwitch();
			if (EastLine != 0) {
				checkEastLine();
			}
			if (WestLine != 0) {
				checkWestLine();
			}
			if (Rebagger != 0) {
				checkRebagLine();
			}
			checkBreakTimes();
		}
		else if (urlFirstTimeFlag > 0) {
			//refresh the web page to keep only one browser open.
			driver.navigate().refresh();
			getWebValues();
			printCurrentValues();
			ts = timeStamp();
			checkKillSwitch();
			
			checkEastLine();
			checkWestLine();
			checkRebagLine();
			checkBreakTimes();
		}
		else {
			//reset flag if something odd happens???
			urlFirstTimeFlag = 0;
		}
	}	
	/////////////////////////////////////////////////////////////////////////////////
	/********************************************************************************
	* checkEastLine method looks to see if the toggle is in the "on" position       *
	* then checks to see if the previous alarm count matches the current alarm count*
	* a change occurred previous alarm count is updated and email is sent. While    *
	* toggle is on current alarm count will be checked against the daily quota.     *
	* if reached during work hours an email will be sent out.                       *
	* If the toggle is in the off position then current alarm count will be stored  *
	* into the previous count variable.                                             *
	*																				* 
	* @param Subject = holds the subject matter for the email.						*
	* @param Message = holds the body content of the the email.                     *
	* @param EastLine = previous alarm count for the east line.                     *  
	* @param eastTmp = current alarm count for the east line.				        *
	* @param eastLineCount = current bag count for the east line                    *
	* @param eastDailyQuota = is the Daily quota of bags the east line must reach   *
	* 			by end of day.														*
	* @param eastQuotaFlag = flag variable to only fall into this if statement      *
	* 			once.																*
	********************************************************************************/
	/////////////////////////////////////////////////////////////////////////////////
	public static void checkEastLine() throws InterruptedException {
		String Subject, Message;
		if (eastToggle == 1) {
			if (EastLine != eastTmp) {
				System.out.println(ts + " East Line Down ");
				Subject = "East Line Down " + ts;
				Message = "East Line has been Down for 15 Minutes!!!";
				EastLine = eastTmp;
				SendEmailtoMAILJET(Subject, Message);
				Thread.sleep(5000);
			}
			if (eLineCount >= eastDailyQuota && eastQuotaFlag == 0) {
				System.out.println(ts + " East Line Quota Reached");
				eastQuotaFlag = 1;
				Subject = "East Line quota of " + eastDailyQuota + " has been reached " + ts;
				Message = "";
				SendEmailtoMAILJET(Subject,Message);
				Thread.sleep(5000);
			}
		}
		else {
			EastLine = eastTmp;
			System.out.println("East Line is OFF");
		}
	}
	//////////////////////////////////////////////////////////////////////////////////
	/*********************************************************************************
	 * checkWestLine method looks to see if the toggle is in the "on" position       *
	 * then checks to see if the previous alarm count matches the current alarm count*
	 * a change occurred previous alarm count is updated and email is sent. While    *
	 * toggle is on current alarm count will be checked against the daily quota.     *
	 * if reached during work hours an email will be sent out.                       *
	 * If the toggle is in the off position then current alarm count will be stored  *
	 * into the previous count variable.                                             *
	 *																				 * 
	 * @param Subject = holds the subject matter for the email.						 *
	 * @param Message = holds the body content of the the email.                     *
	 * @param WestLine = previous alarm count for the west line.                     *  
	 * @param westTmp = current alarm count for the west line.				         *
	 * @param westLineCount = current bag count for the west line                    *
	 * @param westDailyQuota = is the Daily quota of bags the west line must reach   *
	 * 			by end of day.														 *
	 * @param westQuotaFlag = flag variable to only fall into this if statement      *
	 * 			once.																 *
	 ********************************************************************************/
	//////////////////////////////////////////////////////////////////////////////////
	public static void checkWestLine() throws InterruptedException {
		String Subject, Message;
		if (westToggle == 1) {
			if (WestLine != westTmp) {
				System.out.println(ts + " West Line Down ");
				Subject = "West Line Down " + ts;
				Message = "West Line has been Down for 15 Minutes!!!";
				WestLine = westTmp;
				SendEmailtoMAILJET(Subject, Message);
				Thread.sleep(5000);	
			}
			if (wLineCount >= westDailyQuota && westQuotaFlag == 0) {
				System.out.println("West Line Quota Reached" + ts);
				westQuotaFlag = 1;
				Subject = "West Line quota of " + westDailyQuota + " has been reached " + ts;
				Message = "";
				SendEmailtoMAILJET(Subject,Message);
				Thread.sleep(5000);
			}
		}
		else {
			WestLine = westTmp;
			System.out.println("West Line is OFF");
		}	
	}
	/////////////////////////////////////////////////////////////////////////////////
	/********************************************************************************
	 * checkRebagLine method looks to see if the toggle is in the "on" position     *
	 * then checks to see if the previous line count matches the current line count.*
	 * a change occurred previous line count is updated and email is sent. While    *
	 * toggle is on current line count will be checked against the daily quota      *
	 * if reached during work hours an email will be sent out.                      *
	 * If the toggle is in the off position then current line count will be stored  *
	 * into the previous count variable.                                            *
	 *																				* 
	 * @param Subject = holds the subject matter for the email.						*
	 * @param Message = holds the body content of the the email.                    *
	 * @param Rebagger = previous alarm count for the rebagger line.                *
	 * @param rebaggerTmp = current alarm count for the rebagger line.				*
	 * @param rebagLineCount = current bag count for the rebagger line              *
	 * @param rebagDailyQuota = is the Daily quota of bags the rebagger must reach  *
	 * 			by end of day.														*
	 * @param rebagQuotaFlag = flag variable to only fall into this if statement    *
	 * 			once.																*
	 *******************************************************************************/
	/////////////////////////////////////////////////////////////////////////////////
	public static void checkRebagLine() throws InterruptedException {
		String Subject, Message;
		if (rebagToggle == 1) {
			if (Rebagger != rebaggerTmp) {
				System.out.println(ts + " Rebagger Line Down ");
				Subject = "Rebagger is Down " + ts;
				Message = "Rebagger Line has been Down for 15 Minutes!!!";
				Rebagger = rebaggerTmp;
				SendEmailtoMAILJET(Subject, Message);
				Thread.sleep(5000);	
			}
			if (rebagLineCount >= rebagDailyQuota && rebagQuotaFlag == 0) {
				System.out.println("Rebagger Line Quota Reached" + ts);
				rebagQuotaFlag = 1;
				Subject = "Rebagger Line quota of " + rebagDailyQuota + " has been reached " + ts;
				Message = "";
				SendEmailtoMAILJET(Subject,Message);
				Thread.sleep(5000);
			}
		}
		else {
			Rebagger = rebaggerTmp;
			System.out.println("Rebagger Line is OFF");
		}
	}
	//////////////////////////////////////////////////////////////////////////////
	/*****************************************************************************
	 * getWebValues retrieves all the current values from Red Lion's Web Server  *
	 * and stores them into integer variables using an html scraper (selenium web*
	 * driver).		                                                             *
	 * 															 			     *
	 * @param eLineCount = current east line count							     *
	 * @param wLineCount = current west line count                               *
	 * @param rebagLineCount = current rebagger line count                       *
	 * @param totalLineCount = current total of all lines                        *
	 * @param EastLine = current alarm count for the east line                   *
	 * @param WestLine = current alarm count for the west line                   *
	 * @param Rebagger = current alarm count for the rebagger line               *
	 * @param emailTestTmp = current on/off value for sending email.             *
	 * @param eastToggle = holds the current on/off state of the east line       *
	 * @param westToggle = holds the current on/off state of the west line       *
	 * @param rebagToggle = holds the current on/off state of the Rebagger line  *
	 ****************************************************************************/
	//////////////////////////////////////////////////////////////////////////////
	public static void getWebValues() {	
		
		//reads and converts the webelement to a readable integer.
		eLineCount = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[2]/td[2]/font")).getText().trim());
		wLineCount = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[3]/td[2]/font")).getText().trim());
		rebagLineCount = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[4]/td[2]/font")).getText().trim());
		totalLineCount = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[5]/td[2]/font")).getText().trim());
		eastTmp = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[6]/td[2]/font")).getText().trim());
		westTmp = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[7]/td[2]/font")).getText().trim());
		rebaggerTmp = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[8]/td[2]/font")).getText().trim());
		emailTestTmp = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[9]/td[2]/font")).getText().trim());
		eastToggle = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[9]/td[2]/font")).getText().trim());
		westToggle = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[10]/td[2]/font")).getText().trim());
		rebagToggle = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[11]/td[2]/font")).getText().trim());
		programToggle = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[12]/td[2]/font")).getText().trim());
		firstBreak = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[13]/td[2]/font")).getText().trim());
		lunchBreak = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[14]/td[2]/font")).getText().trim());
		secondBreak = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[15]/td[2]/font")).getText().trim());
	}
	/////////////////////////////////////////////////////////////////////////////
	/****************************************************************************
	 * Using Mailjet as a transactional email service with a gmail email address*
	 * To compensate for gmails email from landing in spam, a 5 second program  *
	 * wait has been added in between each sent email.                          *
	 * @param APIKey = holds the apikey for mailjet							    *
	 * @param SecretKey = holds the passcode to the apikey						*
	 * @param From = holds the address of the sendee                            *
	 * @param To = holds the addresses that will receive emails from "From"     *
	 * @param props = holds properties attributes for web connectivity          *
	 * @param session = holds current session to mailjet servers.               *
	 * @param message = holds the message content to sent in the email          *
	 ***************************************************************************/
     ////////////////////////////////////////////////////////////////////////////
	public static void SendEmailtoMAILJET(String subj, String msg) throws InterruptedException {
		
		System.out.println("Connecting to Email Server");
		final String APIKey = "*******";
      final String SecretKey = "********";
      String From = "********";
      String To = "**************";
		Properties props = new Properties ();
			
		//using Mailjet.com as SMTP Server.
		//SSL wrapping
		props.put ("mail.smtp.host", "in-v3.mailjet.com");
		props.put ("mail.smtp.socketFactory.port", "465");
		props.put ("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put ("mail.smtp.auth", "true");
		props.put ("mail.smtp.port", "465");
		//creating a session to the Email Server and sending credentials
		Session session = Session.getDefaultInstance (props,
				new javax.mail.Authenticator ()
		{
			protected PasswordAuthentication getPasswordAuthentication ()
			{
				return new PasswordAuthentication (APIKey, SecretKey);
			}
		});
		System.out.println("Login Successful to Email");
		try
		{
			System.out.println("Creating Message");
			Message message = new MimeMessage (session);
			message.setFrom (new InternetAddress (From));
			message.setRecipients (javax.mail.Message.RecipientType.TO, InternetAddress.parse(To));
			message.setSubject (subj);
			message.setText (msg);
			Transport.send (message);
			System.out.println("Message Sent to " + To);
			Thread.sleep(5000);
		}
		catch (MessagingException e)
		{
			throw new RuntimeException (e);
		}
	}
	////////////////////////////////////////////////////////////////////////////////
	/*******************************************************************************
	 * Checks the Web server for flags in the "ON" position to determine whether or*
	 * not it is time for break. If so method will send the subject string and then*
	 * calls printTotals(). For each break period there is another flag that will  *
	 * be set to "1" when it falls into its particular if statement not allowing   *
	 * it to fall in there again.                                                  *
	 * @param firstBreak = Holds Value pulled from WebServer for 10am Break        *
	 * @param lunchBreak = Holds Value pulled from WebServer for lunch Break       *
	 * @param secondBreak = Holds Value pulled from WebServer for 3pm Break        *
	 * @param firstBreakFlag = Set to "1" when first If statement is entered       *
	 * @param lunchBreakFlag = Set to "1" when second if statement is entered      *
	 * @param secondBreakFlag = Set to "1" when third If statement is entered      *
	 * @throws InterruptedException                                                *
	 ******************************************************************************/
	////////////////////////////////////////////////////////////////////////////////
	public static void checkBreakTimes() throws InterruptedException {
		String subject = "";
		if (firstBreak == 1 && firstBreakFlag == 0) {
			firstBreakFlag = 1;
			subject = timeStamp() + " Morning Break Bag Count Report";
			printTotals(subject);
		}
		if (lunchBreak == 1 && lunchBreakFlag == 0) {
			lunchBreakFlag = 1;
			subject = timeStamp() + " Lunch Break Bag Count Report";
			printTotals(subject);
		}
		if (secondBreak == 1 && secondBreakFlag == 0) {
			secondBreakFlag = 1;
			subject = timeStamp() + " Afternoon Break Bag Count Report";
			printTotals(subject);
		}
	}
	///////////////////////////////////////////////////////////////////////////////
	/******************************************************************************
	 * printTotals prepares message to be sent through email holding the current  *
	 * count of all variables at the end of the day then sleeps for 5 seconds.    *
	 * @param subj holds the subject string for the email                         *
	 * @param msg holds the current count or all values to be sent through email  *
	 * @param eLineCount = current east line count							      *
	 * @param wLineCount = current west line count                                *
	 * @param rebagLineCount = current rebagger line count                        *
	 * @param totalLineCount = current total of all lines                         *
	 * @param EastLine = current alarm count for the east line                    *
	 * @param WestLine = current alarm count for the west line                    *
	 * @param Rebagger = current alarm count for the rebagger line                *
	 *****************************************************************************/
	///////////////////////////////////////////////////////////////////////////////
	public static void printTotals(String subj) throws InterruptedException {
		
		System.out.println(subj);
		printCurrentValues();		
		String msg = "East = " + eLineCount + 
				"\nWest = " + wLineCount +    
				"\nRebagger = " +rebagLineCount + 
				"\nTotal = " + totalLineCount +
				"\nEast ALarm = " + EastLine + 
				"\nWest ALarm = " + WestLine + 
				"\nRebagger ALarm = " + Rebagger;
		SendEmailtoMAILJET(subj,msg);
		Thread.sleep(5000);
		
	}
	//////////////////////////////////////////////////////////////////////////////
	/*****************************************************************************
	 * printCurrentValues will print all line counts and alarm counts into log.  *
	 * @param eLineCount = current east line count								 *
	 * @param wLineCount = current west line count                               *
	 * @param rebagLineCount = current rebagger line count                       *
	 * @param totalLineCount = current total of all lines                        *
	 * @param EastLine = current alarm count for the east line                   *
	 * @param WestLine = current alarm count for the west line                   *
	 * @param Rebagger = current alarm count for the rebagger line               *
	 ****************************************************************************/
	//////////////////////////////////////////////////////////////////////////////
	public static void printCurrentValues() {
		
		System.out.println("  East = " + eLineCount);
		System.out.println("  West = " + wLineCount);
		System.out.println("  Rebagger = " +rebagLineCount);
		System.out.println("  Total = " + totalLineCount);
		System.out.println("  East Alarm = " + EastLine); 
		System.out.println("  West Alarm = " + WestLine); 
		System.out.println("  Rebagger Alarm = " + Rebagger);

	}
	/////////////////////////////////////////////////////////////////////////////
	/****************************************************************************
	 * checkKillSwitch checks for a 1 or 0 from Red Lion's Web Server. If equal *
	 * 0 the program will exit, otherwise continue.								*
	 * @param programToggle holds the value retreived from Red Lion's Web Server*
	 ***************************************************************************/
	/////////////////////////////////////////////////////////////////////////////
	public static void checkKillSwitch() {
		
		if (programToggle == 0) {
			System.out.println(timeStamp() + " Kill Switch Clicked.");
			System.exit(0);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	/***************************************************************************
	 * timeStamp will find the current time.								   *
	 * @return the current time												   *
	 **************************************************************************/
	////////////////////////////////////////////////////////////////////////////
	public static Timestamp timeStamp() {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		return ts;
	}
}
