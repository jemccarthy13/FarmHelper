package driver;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Test mail functionality.
 */
public class MailTest {

	/**
	 * Main entry point for the test
	 * @param args
	 */
	public static void main(String[] args) {
		String to = "6034258822@vtext.com";
		String from = "jemccarthy13@gmail.com";
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", "smtp.gmail.com");

		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.auth", "true");
		props.put("mail.debug", "false");
		props.put("mail.store.protocol", "pop3");
		props.put("mail.transport.protocol", "smtp");
		final String username = "jemccarthy13@gmail.com";//
		final String password = "Legolas93";
		try {
			Session session = Session.getDefaultInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject("Farm script ran");
			message.setText("Ran farm script.");
			Transport.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
