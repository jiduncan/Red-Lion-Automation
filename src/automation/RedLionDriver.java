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
 * web server @ 192.168.1.180. If the values change an email will be sent out to their
 * respective email address.
 * 
 * @author Johnny Duncan
 * @version: 1.2
 * @LastUpdated: 6/19/2018                                                             
 *****************************************************************************************/
public class RedLionDriver {
	
	static int urlFirstTimeFlag = 0;
	static int fileFlag = 1;
	static int EastLine, WestLine, Rebagger, emailTest;
	static int eastTmp, westTmp, rebaggerTmp, emailTestTmp;
	static WebDriver driver;
	static Session session;
	static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyy-MM-dd");
	//static Logger l = Logger.getLogger(RedLionDriver.class.getName());
	
	public static void main(String[] args) throws InterruptedException, FileNotFoundException {

		LocalDateTime now = LocalDateTime.now();
		//System.out.println() will now print to file instead of console.
		File dir = new File("C:\\Users\\johnny\\Desktop\\logs\\javalogs\\" + dtf.format(now) + ".txt");
		PrintStream o = new PrintStream(dir);
		System.setOut(o);
		
		//run continuously
		while (true) {
			Calendar cal = Calendar.getInstance();
			//get the day of the week Sun-1, MON-2,TUES-3,WED-4,THURS-5,FRI-6,SAT-7
			int day = cal.get(Calendar.DAY_OF_WEEK);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);
			int second = cal.get(Calendar.SECOND);
			//CONTINUE AS LONG AS MON-FRI
			if (day >= 2 && day <= 6) {
				System.out.println("Day = " + day + " Hour = " + hour + 
						" Minute = " + minute + " Second = " + second);
				
				//Start at 7am till lunch time; sleep for 30 seconds.
				if (hour >= 7 && (hour <=11 && minute <= 59)) {
					System.out.println("7-11:59am");
					launchChrome();
					Thread.sleep(30000);
				}	
				//if after lunch time and before close; sleep for 30 seconds
				else if (hour >= 13 && hour < 18) {
					System.out.println("1-6pm");
					launchChrome();
					Thread.sleep(30000);
				}
				//lunch-time sleep for 5 minute intervals
				else if (hour == 12 && minute <= 59) {
					System.out.println("lunch-time");
					Thread.sleep(300000);
				}
				//during off hours sleep for 1 hour periods
				//sets fileFlag to 0
				else {
					fileFlag = 0;
					System.out.println("off-hours");
					Thread.sleep(3600000);
				}
			}
			//sleep for an hour during off hours
			//will also create a new file at midnight when flag is 0
			else {
				System.out.println("outside work hours");
				
				if (hour==0 && fileFlag==0) {
					fileFlag = 1;
					dir = new File("C:\\Users\\johnny\\Desktop\\logs\\javalogs\\" 
							+ dtf.format(now) + ".txt");
				}
				Thread.sleep(3600000);
				return;
			}
		}
	}
	
	public static Timestamp timeStamp() {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		return ts;
	}
	
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
			System.out.println("Message Sent");
			Thread.sleep(5000);
		}
		catch (MessagingException e)
		{
			throw new RuntimeException (e);
		}
	}
	public static void launchChrome() throws InterruptedException  {
		
		String username = "*******";
		String password = "********";
		String credentials = username + ":" + password;
		String URL = "http://"+credentials+"@***.***.***.***";
		String exePath = "C:\\Users\\johnny\\eclipse-workspace\\downloadlocation\\chromedriver.exe";

		System.setProperty("webdriver.chrome.driver", exePath);
		String Message, Subject;
		//logs into url first time
		if (urlFirstTimeFlag == 0) {
			driver = new ChromeDriver();
			driver.get(URL);
			urlFirstTimeFlag++;
			//add current value then look to update after a change???
			EastLine = WestLine = Rebagger = emailTest = 0;
		}
		else if (urlFirstTimeFlag > 0) {
			//refresh the web page to keep only one browser open.
			driver.navigate().refresh();
			//reads and converts the webelement to a readable integer.
			eastTmp = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[2]/td[2]/font")).getText().trim());
			westTmp = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[3]/td[2]/font")).getText().trim());
			rebaggerTmp = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[4]/td[2]/font")).getText().trim());
			emailTestTmp = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[5]/td[2]/font")).getText().trim());
			System.out.println("East = " + EastLine + " West = " + WestLine + " Rebagger = " + Rebagger);
			//System.out.println("EmailTest = " + emailTest);
			Timestamp ts = timeStamp();
			//east line alarm count change
			if (EastLine != eastTmp) {
				System.out.println("East Line Down " + ts);
				Subject = "East Line Down " + ts;
				Message = "East Line has been Down for 15 Minutes!!!";
				EastLine = eastTmp;
				SendEmailtoMAILJET(Subject, Message);
				Thread.sleep(5000);	
			}
			if (WestLine != westTmp) {
				System.out.println("West Line Down " + ts);
				Subject = "West Line Down " + ts;
				Message = "West Line has been Down for 15 Minutes!!!";
				WestLine = westTmp;
				SendEmailtoMAILJET(Subject, Message);
				Thread.sleep(5000);	
			}
			if (Rebagger != rebaggerTmp) {
				System.out.println("Rebagger Line Down " + ts);
				Subject = "Rebagger is Down " + ts;
				Message = "Rebagger Line has been Down for 15 Minutes!!!";
				Rebagger = rebaggerTmp;
				SendEmailtoMAILJET(Subject, Message);
				Thread.sleep(5000);	
			}
			/*if (emailTest != emailTestTmp) {
				System.out.println("Email Test " + ts);
				Subject = "Email Test " + ts;
				Message = "Email Test Button was incremented";
				emailTest = emailTestTmp;
				SendEmailtoMAILJET(Subject, Message);
				automationPause();
			
			}*/
		}
		else {
			//reset flag if something odd happens???
			urlFirstTimeFlag = 0;
		}
		return;
	}
}
