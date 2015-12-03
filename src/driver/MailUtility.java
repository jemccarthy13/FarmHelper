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
 * Utility class to help with email.
 */
public class MailUtility {

	static Session session;
	static Properties props;
	String from;
	String to;
	String from_password;

	/**
	 * Setup the email utility
	 * @param from_addr
	 * @param from_password
	 * @param to
	 */
	public void setup(String from_addr, String from_password, String to) {
		props = System.getProperties();
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

		final String authenticated_email = from_addr;
		final String authenticated_pass = from_password;

		this.from = from_addr;
		this.from_password = from_password;
		this.to = to;

		session = Session.getDefaultInstance(props, new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(authenticated_email, authenticated_pass);
			}
		});
	}

	/**
	 * Send an email message
	 * @param text - the body of the email
	 * @throws Exception
	 */
	public void sendMessage(String text) throws Exception {
		if (session == null) {
			throw new Exception("Failed to initialize mail utility.");
		}
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setText(text);
			Transport.send(message);
		} catch (Exception e) {
			throw new Exception("Failed to send message!");
		}
	}

}
