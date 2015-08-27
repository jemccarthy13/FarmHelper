package driver;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import org.openqa.selenium.remote.UnreachableBrowserException;

public class MailActivatedHelper {

	static class FarmHelpServer extends Thread {

		public FarmHelpServer() {
		}

		@Override
		public void run() {
			String[] args = { gmail_address, gmail_password, authorized_sender, username, password, world };
			FarmHelp.main(args);
		}
	}

	static Store store;
	static ArrayList<Integer> vills_to_skip = new ArrayList<Integer>();
	static FarmHelpServer runningServer;
	static String gmail_address = "";
	static String gmail_password = "";
	static String authorized_sender = "ImNotAnEmailAddr";
	static String username;
	static String password;
	static String world;

	static MailUtility mailer = new MailUtility();

	public static Message getMostRecentMessage() throws Exception {

		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		Session session = Session.getInstance(props, null);
		store = session.getStore();
		store.connect("imap.gmail.com", gmail_address, gmail_password);

		// open the inbox
		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);
		Message msg = inbox.getMessage(inbox.getMessageCount());

		return msg;
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		Date previous_msg = null;
		Message most_recent;

		if (args.length < 3) {
			System.err.println("Missing arguments...");
			System.err.println("Usage: MailActivatedHelper gmail_address gmail_password authorized_sender");
			System.exit(0);
		}
		gmail_address = args[0];
		gmail_password = args[1];
		authorized_sender = args[2];

		mailer.setup(gmail_address, gmail_password, authorized_sender);

		try {
			most_recent = getMostRecentMessage();
			previous_msg = most_recent.getSentDate();

			System.out.println("Most recent message: ");
			System.out.println("SENT DATE:" + previous_msg);

			store.close();

		} catch (Exception e) {
			System.out.println("Unable to open inbox.");
		}

		while (true) {
			try {

				Message msg = getMostRecentMessage();

				Date current_date = msg.getSentDate();
				Multipart mp = (Multipart) msg.getContent();
				BodyPart bp = mp.getBodyPart(0);

				if (previous_msg == null || current_date.after(previous_msg)) {
					System.out.println("Received new message!");
					previous_msg = current_date;

					Address[] in = msg.getFrom();
					for (Address address : in) {
						System.out.println("FROM:" + address.toString());
						if (!address.toString().equals(authorized_sender)) {
							throw new Exception("Incorrect sender: " + address.toString());
						}
					}

					System.out.println("SUBJECT:" + msg.getSubject());
					System.out.println("CONTENT:" + bp.getContent());

					String command = bp.getContent().toString().toLowerCase();
					if (command.contains("run tw") || command.contains("tw start")) {

						command = command.replace("run tw", "").replace("tw start", "").trim();
						String[] configuration = command.split(" ");
						username = configuration[0];
						password = configuration[1];
						world = configuration[2];

						String pattern = ".*(skip ([0-9]+ *)*)";
						Pattern r = Pattern.compile(pattern);
						Matcher m = r.matcher(command);

						vills_to_skip = new ArrayList<Integer>();

						if (m.matches()) {
							String[] villages = m.group(1).replace("skip", "").trim().split(" ");
							for (String vill : villages) {
								vills_to_skip.add(Integer.parseInt(vill));
							}

							System.out.println("Skipping villages: " + vills_to_skip);
						}
						FarmHelp.vills_to_skip = vills_to_skip;
						if (runningServer != null && runningServer.isAlive()) {
							runningServer.stop();
						}

						runningServer = new FarmHelpServer();
						runningServer.start();

						mailer.sendMessage("Started TW helper server.");
					}
					if (command.contains("tw quit")) {
						if (runningServer != null) {
							runningServer.stop();
						}
						try {
							if (FarmHelp.d != null)
								FarmHelp.d.quit();
						} catch (UnreachableBrowserException e) {
						}
						FarmHelp.b.delete(0, FarmHelp.b.length());

						mailer.sendMessage("Quit TW helper server.");
					}
					if (command.contains("tw restart")) {
						if (runningServer != null) {
							runningServer.stop();
						}
						try {
							if (FarmHelp.d != null)
								FarmHelp.d.quit();
						} catch (UnreachableBrowserException e) {
						}
						FarmHelp.b.delete(0, FarmHelp.b.length());
						String pattern = ".*(skip ([0-9]+ *)*)";
						Pattern r = Pattern.compile(pattern);
						Matcher m = r.matcher(command);

						vills_to_skip = new ArrayList<Integer>();

						if (m.matches()) {
							String[] villages = m.group(1).replace("skip", "").trim().split(" ");
							for (String vill : villages) {
								vills_to_skip.add(Integer.parseInt(vill));
							}

							System.out.println("Skipping villages: " + vills_to_skip);
						}
						FarmHelp.vills_to_skip = vills_to_skip;

						runningServer = new FarmHelpServer();
						runningServer.start();

						mailer.sendMessage("Retarted helper server.");
					}

				}
				store.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
