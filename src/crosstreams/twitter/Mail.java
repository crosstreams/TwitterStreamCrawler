/*
 * Class Sourced from http://www.javapractices.com/topic/TopicAction.do?Id=144
 * 8th Jan 2013
 */
package crosstreams.twitter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/*
# Configuration file for javax.mail
# If a value for an item is not provided, then
# system defaults will be used. These items can
# also be set in code.

# Host whose mail services will be used
# (Default value : localhost)
mail.host=mail.blah.com

# Return address to appear on emails
# (Default value : username@host)
mail.from=webmaster@blah.net

# Other possible items include:
# mail.user=
# mail.store.protocol=
# mail.transport.protocol=
# mail.smtp.host=
# mail.smtp.user=
# mail.debug= 
*/

public class Mail {

	public static void mail(String mailconfigfile, String fromAddress, String toaddress, String subject, String body) {
		Mail.refreshConfig(mailconfigfile);
		Mail emailer = new Mail();
	    emailer.sendEmail(fromAddress, toaddress, subject, body);
	}
	
	/**
	  * Send a single email.
	  */
	  public void sendEmail(
	    String aFromEmailAddr, String aToEmailAddr,
	    String aSubject, String aBody
	  ){
	    //Here, no Authenticator argument is used (it is null).
	    //Authenticators are used to prompt the user for user
	    //name and password.
	    Session session = Session.getDefaultInstance( fMailServerConfig, null );
	    MimeMessage message = new MimeMessage( session );
	    try {
	      //the "from" address may be set in code, or set in the
	      //config file under "mail.from" ; here, the latter style is used
	      //message.setFrom( new InternetAddress(aFromEmailAddr) );
	      message.addRecipient(
	        Message.RecipientType.TO, new InternetAddress(aToEmailAddr)
	      );
	      message.setSubject( aSubject );
	      message.setText( aBody );
	      Transport.send( message );
	    }
	    catch (MessagingException ex){
	      System.err.println("Cannot send email. " + ex);
	    }
	  }

	  /**
	  * Allows the config to be refreshed at runtime, instead of
	  * requiring a restart.
	  */
	  public static void refreshConfig(String conffile) {
	    fMailServerConfig.clear();
	    fetchConfig(conffile);
	  }

	  // PRIVATE //

	  private static Properties fMailServerConfig = new Properties();


	  /**
	  * Open a specific text file containing mail server
	  * parameters, and populate a corresponding Properties object.
	  */
	  private static void fetchConfig(String conffile) {
	    InputStream input = null;
	    try {
	      //If possible, one should try to avoid hard-coding a path in this
	      //manner; in a web application, one should place such a file in
	      //WEB-INF, and access it using ServletContext.getResourceAsStream.
	      //Another alternative is Class.getResourceAsStream.
	      //This file contains the javax.mail config properties mentioned above.
	      input = new FileInputStream( conffile );
	      fMailServerConfig.load( input );
	    }
	    catch ( IOException ex ){
	      System.err.println("Cannot open and load mail server properties file.");
	    }
	    finally {
	      try {
	        if ( input != null ) input.close();
	      }
	      catch ( IOException ex ){
	        System.err.println( "Cannot close mail server properties file." );
	      }
	    }
	  }
	
}
