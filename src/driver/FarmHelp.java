package driver;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * A helper to assist with farming.
 *
 */
public class FarmHelp {

	static StringBuilder b = new StringBuilder();
	static FirefoxDriver d;

	static MailUtility mailer = new MailUtility();

	/**
	 * Sleep for a given duration.
	 * @throws BotException when errors are encountered.
	 */
	public static void sleep() throws BotException {
		int start = 500;
		int end = 1000;
		try {
			if (d.findElementById("bot_check_image") != null) {
				d.close();
				try {
					mailer.sendMessage("Bot protection. Can't continue.");
				} catch (Exception e) {
					e.printStackTrace();
				}
				b.delete(0, b.length());
				throw new BotException();
			}
			long wait = (long) (start + (Math.random() * (end - start)));
			Thread.sleep(wait);

		} catch (NoSuchElementException | InterruptedException e) {

		} 
	}

	/**
	 * Reset the program for the next run.
	 * @param milliseconds - time to wait before reset
	 */
	public static void reset(int milliseconds) {
		int start = milliseconds / 2;
		int end = (int) (milliseconds * 1.5);
		try {
			long wait = (long) (start + (Math.random() * (end - start)));
			b.append("Will run again in " + wait / 1000 / 60 + " minutes.");

			System.out.println(b.toString());
			mailer.sendMessage(b.toString());
			b.delete(0, b.length());

			Thread.sleep(wait);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sleep for a given duration.
	 * @param milliseconds - the number of milliseconds to sleep for.
	 */
	public static void sleep(int milliseconds) {
		int start = milliseconds / 2;
		int end = (int) (milliseconds * 1.5);
		try {
			long wait = (long) (start + (Math.random() * (end - start)));
			Thread.sleep(wait);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static int villCount = 0;
	static ArrayList<Integer> vills_to_skip = new ArrayList<Integer>();

	/**
	 * Actually does the farming.
	 * 
	 * @throws BotException if any errors are encountered.
	 */
	public static void doFarming() throws BotException {
		if (vills_to_skip.contains(villCount)) {
			b.append("Skipping village: " + villCount + "\n");
			villCount += 1;
			return;
		}
		JavascriptExecutor js = ((JavascriptExecutor) d);
		js.executeScript("window.scrollTo(0, document.body.scrollHeight)");

		List<WebElement> pages = d.findElements(By.className("paged-nav-item"));
		if (pages.size() == 0) {
			villCount += 1;
			return;
		}

		sleep();

		WebElement lastpage = pages.get(pages.size() - 1);
		int num_times = Integer
				.parseInt(lastpage.getText().replace("[", "").replace("]", "").replace(">", "").replace("<", ""));

		System.out.println("Clicking A farm buttons for village " + villCount);
		for (int page = 0; page < num_times; page++) {
			List<WebElement> vills = d.findElementsByCssSelector(".farm_icon.farm_icon_a");
			List<WebElement> disabledVills = d.findElementsByCssSelector(".farm_icon.farm_icon_a.farm_icon_disabled");
			vills.removeAll(disabledVills);

			for (WebElement vill : vills) {
				vill.click();
				try {
					d.findElement(By.cssSelector(".autoHideBox.error"));
					break;
				} catch (Exception e) {
				}
				sleep();
			}
			sleep();
		}

		pages = d.findElements(By.className("paged-nav-item"));
		if (pages.size() == 0) {
			villCount += 1;
			return;
		}

		sleep();

		lastpage = pages.get(pages.size() - 1);
		num_times = Integer
				.parseInt(lastpage.getText().replace("[", "").replace("]", "").replace(">", "").replace("<", ""));

		System.out.println("Clicking B farm buttons for village " + villCount);
		for (int page = 0; page < num_times; page++) {
			List<WebElement> vills = d.findElementsByCssSelector(".farm_icon.farm_icon_b");
			List<WebElement> disabledVills = d.findElementsByCssSelector(".farm_icon.farm_icon_b.farm_icon_disabled");
			vills.removeAll(disabledVills);

			for (WebElement vill : vills) {
				vill.click();
				try {
					d.findElement(By.cssSelector(".autoHideBox.error"));
					break;
				} catch (Exception e) {
				}
				sleep();
			}
			sleep();
		}

		js.executeScript("window.scrollTo(0, 0)");

		System.out.println("Farmed village: " + villCount);
		villCount += 1;
	}

	/**
	 * Execute the script
	 * @param username
	 * @param password
	 * @param world
	 * @throws BotException
	 */
	public static void execute(String username, String password, String world) throws BotException {
		d = new FirefoxDriver();
		try {
			d.get("http://tribalwars.net");
			d.manage().window().maximize();
			WebElement x = d.findElementById("user");

			x.sendKeys(username);

			x = d.findElementById("password");
			x.sendKeys(password);

			x = d.findElementById("cookie");
			if (x.isSelected()) {
				x.click();
			}

			x = d.findElementByClassName("login_button");
			x.click();

			sleep(3000);

			List<WebElement> elements;
			elements = d.findElementsByClassName("world_button_active");

			for (WebElement elem : elements) {
				boolean isWorld = elem.getText().contains(world);
				if (isWorld) {
					b.append("World: " + world + "\n");
					elem.click();
					sleep();

					List<WebElement> villages_edit = d.findElementsByClassName("quickedit-vn");
					int num_villages = villages_edit.size();
					b.append("Villages: " + num_villages + "\n");
					sleep();

					x = d.findElement(By.cssSelector(".icon.header.village"));
					x.click();
					sleep();

					x = d.findElement(By.className("manager_icon"));
					x.click();
					sleep();

					while (villCount < num_villages) {
						sleep();
						doFarming();
						x = d.findElementById("village_switch_right");
						x.click();
						sleep();
					}

					break;

				}

			}
			d.close();

			b.append("Farmed: " + villCount + " villages.\n");

			villCount = 0;

			reset(900000);

		}catch (BotException b){
			throw b;
		}catch (Exception e) {
			e.printStackTrace();
			try {
				d.close();
				b.append("Unhandled exception... running again in ~60 seconds.");
				System.out.println(b.toString());
				mailer.sendMessage(b.toString());
				b.delete(0, b.length());
				Thread.sleep(60000);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Main entry point for the program.
	 * @param args
	 */
	public static void main(String[] args) {

		String gmail_address = args[0];
		String gmail_password = args[1];
		String authorized_sender = args[2];
		String username = args[3];
		String password = args[4];
		String world = args[5];

		mailer.setup(gmail_address, gmail_password, authorized_sender);

		while (true) {
			try {
				execute(username, password, world);
			} catch (BotException e) {
				return;
			}
		}
	}
	
	/**
	 * Start the helper
	 * @param args
	 * @throws BotException
	 */
	public static void startHelper(String[] args) throws BotException{
		String gmail_address = args[0];
		String gmail_password = args[1];
		String authorized_sender = args[2];
		String username = args[3];
		String password = args[4];
		String world = args[5];

		mailer.setup(gmail_address, gmail_password, authorized_sender);

		while (true) {
			execute(username, password, world);	
		}
	}
}
