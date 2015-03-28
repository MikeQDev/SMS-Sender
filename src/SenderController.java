import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controls the sending actions
 * 
 * @author Mike
 *
 */
public class SenderController {
	public static int RUNNING = 1, STOPPED = 0, WAITING_TO_SEND = 2;
	private static MainWindow parent = null;
	private static SenderController sendControl;
	private ExecutorService eS;
	private List<MailSender> listMailSenders = new ArrayList<MailSender>();
	private int status = 0, amountOfThreads = 5, attemptedSends, amtToSend,
			timeToNextSend;
	private boolean stopRequested, intermittenSending;
	private Random randAccChooser = new Random();

	/**
	 * 
	 * @param par
	 *            parent window, used to signal changes
	 * @return instance of SenderController
	 */
	public static SenderController getSenderController(MainWindow par) {
		if (sendControl == null)
			sendControl = new SenderController();
		if (parent == null)
			parent = par;
		return sendControl;
	}

	/**
	 * Sends the texts
	 * 
	 * @param amt
	 *            amount of texts to send
	 */
	public void sendTexts(int amt) {

		amtToSend = amt;

		if (intermittenSending == false)
			attemptedSends = 0;

		eS = Executors.newFixedThreadPool(amountOfThreads);

		if (amtToSend == 1) {
			eS.execute(listMailSenders.get(randAccChooser
					.nextInt(listMailSenders.size())));
		} else {
			for (int i = 0; i < (amtToSend + 1) / listMailSenders.size(); i++) {
				for (MailSender s : listMailSenders) {
					eS.execute(s);
				}
			}
		}

		eS.shutdown();

		while (!eS.isTerminated()) {
			if (status != RUNNING)
				status = RUNNING;
		}

		status = STOPPED;

		updateGUI();
	}

	/**
	 * Sends texts at an interval
	 * 
	 * @param amt
	 *            amount of texts to send
	 * @param interval
	 *            how often to send texts (in seconds)
	 */
	public void sendTexts(int amt, int interval) {
		int t;
		attemptedSends = 0;
		stopRequested = false;
		intermittenSending = true;
		while (!stopRequested) {
			sendTexts(amt);
			try {
				t = interval;
				while (t > 0) {
					if (status != WAITING_TO_SEND)
						status = WAITING_TO_SEND;
					timeToNextSend = --t;
					updateGUI();
					if (stopRequested)
						break;
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		status = STOPPED;
		updateGUI();
	}

	/**
	 * 
	 * @param subj
	 *            message subject
	 * @param msg
	 *            message content
	 * @param addr
	 *            send-to phone number
	 */
	public void buildMessages(String subj, String msg, String addr) {
		for (MailSender m : listMailSenders)
			m.setSendingFields(subj, msg, addr);
	}

	/**
	 * Creates a list of mail senders
	 * 
	 * @param userNames
	 *            gmail account names
	 * @param passwords
	 *            gmail account passwords
	 */
	public void constructSenders(List<String> userNames, List<String> passwords) {
		if (listMailSenders.size() != 0)
			listMailSenders.clear();
		for (int i = 0; i < userNames.size(); i++) {
			listMailSenders.add(new MailSender(userNames.get(i), passwords
					.get(i), this));
		}
	}

	/**
	 * Terminates sending
	 */
	public void stopSending() {
		stopRequested = true;
		intermittenSending = false;
		eS.shutdownNow();
	}

	private SenderController() {
	}

	/**
	 * Increment the amount of attempted sends
	 */
	public synchronized void incrementSentAmount() {
		attemptedSends++;
	}

	/**
	 * Signal GUI to check for updates
	 */
	public void updateGUI() {
		parent.checkForUpdates();
	}

	/**
	 * 
	 * @return amount of accounts inputed.. Not all accounts may contain
	 *         correct credentials!
	 */
	public int getLoadedAccounts() {
		return listMailSenders.size();
	}

	/**
	 * 
	 * @return amount of texts to send
	 */
	public int getAmountToSend() {
		return amtToSend;
	}

	/**
	 * 
	 * @return amount of texts attempted to send
	 */
	public int getAmountSent() {
		return attemptedSends;
	}

	/**
	 * 
	 * @return status of the sender (stopped/running)
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Sets the amount of threads to be used
	 * 
	 * @param a
	 *            amount of threads to use
	 */
	public void setAmountOfThreads(int a) {
		amountOfThreads = a;
	}

	/**
	 * 
	 * @return amount of time (in seconds) until next send
	 */
	public int getTimeUntilNextSend() {
		return timeToNextSend;
	}

	/**
	 * 
	 * @return amount of texts sent since beginning of intervalled sending
	 */
	// public int getAmountSentIntermittently(){
	// return amtSentIntermittently;
	// }

	/**
	 * 
	 * @return amount of sending threads
	 */
	public int getAmountOfThreads() {
		return amountOfThreads;
	}

}
