import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

/**
 * Checks if inputted phone number is whitelisted
 * 
 * @author Mike
 *
 */
public class WhiteList {
	private static String[] whiteHash = { "444585", "442818" };

	/**
	 * 
	 * @param num
	 *            send-to number
	 * @return true if the send-to number is whitelisted
	 */
	public static boolean isWhiteListed(String num) {
		if (num.length() < 10)
			return false;
		String hashedNum = hash(num.substring(6, 10));
		for (int i = 0; i < whiteHash.length; i++) {
			if (hashedNum.equals(whiteHash[i]))
				return true;
		}
		return false;
	}

	/**
	 * Opens links to rick roll on YouTube
	 * 
	 * @param amt
	 *            amount of links to open
	 */
	public static void rickRoll(int amt) {
		try {
			for (int x = 0; x < amt; x++)
				Desktop.getDesktop().browse(
						new URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(null,
				"Don't try sending to a whitelisted number!", "Nope",
				JOptionPane.WARNING_MESSAGE);

	}

	/**
	 * Simple hash for whitelists
	 * 
	 * @param s
	 *            String to be hashed
	 * @return Hashed String
	 */
	private static String hash(String s) {
		int hash = 11;
		for (int i = 0; i < s.length(); i++) {
			hash = hash * 13 + s.charAt(i);
		}
		return String.valueOf(hash);
	}

	private WhiteList() {
	}

}
