import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

/**
 * Runnable that contacts GMail to send messages
 * 
 * @author Mike
 *
 */
public class MailSender implements Runnable {
	private SenderController sendController;
	private String username, password, subject, msg, address;
	private Properties props = new Properties();
	private Session session;
	private Random rand;

	/**
	 * Logs into GMail account
	 * 
	 * @param uN
	 *            username
	 * @param pW
	 *            password
	 * @param sC
	 *            sendercontroller parent
	 */
	public MailSender(String uN, String pW, SenderController sC) {
		sendController = sC;
		username = uN;
		password = pW;
		session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
	}

	/**
	 * Sets message headers
	 * 
	 * @param subj
	 *            subject
	 * @param msg
	 *            message
	 * @param addr
	 *            send-to address
	 */
	public void setSendingFields(String subj, String msg, String addr) {
		subject = subj;
		this.msg = msg;
		address = addr;
		rand = new Random();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
	}

	/**
	 * Sends one message
	 */
	public void run() {
		// System.out.println(Thread.currentThread().getName() + " sending ");
		Message message = new MimeMessage(session);
		try {

			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(address));
			message.setSubject(subject + rand.nextInt(1000));
			message.setText(msg + rand.nextInt(1000));

			Transport.send(message);

		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(null,
					"Error while sending mail on user " + username
							+ "\nEnsure that login credentials are correct"
							+ "\nExiting program");
			System.exit(0);
		}
		
		sendController.incrementSentAmount();
		sendController.updateGUI();
	}
}
