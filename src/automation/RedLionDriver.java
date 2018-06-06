package automation;

import java.util.Calendar;
import javax.mail.Session;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RedLionDriver {
	
	static int urlFirstTimeFlag = 0;
	static int EastLine, WestLine, Rebagger;
	static int eastTmp, westTmp, rebaggerTmp;
	static WebDriver driver;
	static Session session;
	static String Subject, Message;
	
	public static void main(String[] args) throws InterruptedException {
		//rl = new RedLionAutomation();
		Calendar cal = Calendar.getInstance();
		//run continuously
		while (true) {
			//get the day of the week Sunday starts at 1, MON-2,TUES-3,WED-4,THURS-5,FRI-6,SAT-7
			int day = cal.get(Calendar.DAY_OF_WEEK);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			//CONTINUE AS LONG AS MON-FRI
			if (day >= 2 && day <= 6) {
				System.out.println(day);
				System.out.println(hour);
				//WORK DAY 7AM - 6PM
				if (hour >= 7 && hour < 18){
					//System.out.println(hour);
					System.out.println("here");
					launchChrome();
					System.out.println("here1");
					//after returning wait 30 seconds
					Thread.sleep(30000);
					System.out.println("here2");
				}
			}
			//during off hours sleep for a longer period of time
			else
				Thread.sleep(3600000);
		}
	}
public static void launchChrome()  {
		
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
			urlFirstTimeFlag++;
			eastTmp = westTmp = rebaggerTmp = 0;
		}
		else if (urlFirstTimeFlag > 0) {
			//refresh the web page to keep only one browser open.
			driver.navigate().refresh();
			//reads and converts the webelement to a readable integer.
			EastLine = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[2]/td[2]/font")).getText().trim());
			WestLine = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[3]/td[2]/font")).getText().trim());
			Rebagger = Integer.parseInt(driver.findElement(By.xpath("/html/body/div/table/tbody/tr[4]/td[2]/font")).getText().trim());
			System.out.println(EastLine + " " + WestLine + " " + Rebagger);
			//east line alarm count change
			if (EastLine != eastTmp) {
				Subject = "East Line Down!!!";
				Message = "East Line has been Down for 15 Minutes!!!";
				EastLine = eastTmp;
				SendEmailtoMAILJET(Subject, Message);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (WestLine != westTmp) {
				Subject = "West Line Down!!!";
				Message = "West Line has been Down for 15 Minutes!!!";
				WestLine = westTmp;
				SendEmailtoMAILJET(Subject, Message);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (Rebagger != rebaggerTmp) {
				Subject = "West Line Down!!!";
				Message = "West Line has been Down for 15 Minutes!!!";
				WestLine = westTmp;
				SendEmailtoMAILJET(Subject, Message);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
			//reset flag if something odd happens???
			urlFirstTimeFlag = 0;
		}
	}
public static void SendEmailtoMAILJET(String msg, String subj) {
	
	final String APIKey = "*******";
	final String SecretKey = "********";
	String From = "********";
	String To = "**************";
	
	Properties props = new Properties ();
	System.out.println("here1");
	//using Mailjet.com as SMTP Server.
	props.put ("mail.smtp.host", "in-v3.mailjet.com");
	props.put ("mail.smtp.socketFactory.port", "465");
	props.put ("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	props.put ("mail.smtp.auth", "true");
	props.put ("mail.smtp.port", "465");
	System.out.println("here2");
	Session session = Session.getDefaultInstance (props,
		new javax.mail.Authenticator ()
		{
			protected PasswordAuthentication getPasswordAuthentication ()
			{
				return new PasswordAuthentication (APIKey, SecretKey);
			}
		});
	System.out.println("here3");
	try
	{
		System.out.println("here4");
		Message message = new MimeMessage (session);
		message.setFrom (new InternetAddress (From));
		message.setRecipients (javax.mail.Message.RecipientType.TO, InternetAddress.parse(To));
		message.setSubject (subj);
		message.setText (msg);
		System.out.println("here5");
		Transport.send (message);

	}
	catch (MessagingException e)
	{
	
		throw new RuntimeException (e);
	}
}

}
