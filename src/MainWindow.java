import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;

/**
 * GUI
 * 
 * @author Mike
 *
 */
public class MainWindow extends JFrame {
	private final String STATUS_WAITING_ACC = "Status: waiting for accounts",
			STATUS_RUNNING = "Status: sending",
			STATUS_STOPPED = "Status: stopped",
			STATUS_WAITING_TO_SEND = "Status: sending in: ";
	private SenderController sendController = null;
	private JLabel statusPanelText;
	private JSpinner spinnerAmountToSend, spinnerSendEvery;
	private JTextArea textareaMessage;
	private JCheckBox checkBoxSendEvery;
	private JTextField textfieldSubject, textfieldSendToNumber;
	private JComboBox<String> comboBoxserviceProviders;
	private JProgressBar progressBarProgress;
	private JButton buttonSend, buttonStop;
	private int amtSent, amtToSend, percentSent, intervalToSend;

	public MainWindow() {
		super("SMS 1.2");

		sendController = SenderController.getSenderController(this);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);

		buildMenuBar();
		buildMainFrame();
		buildStatusBar();

		pack();
		setVisible(true);
	}

	/**
	 * Builds the menu bar
	 */
	private void buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		JMenuItem menuItemLoadAccounts = new JMenuItem("Load accounts");
		// Load accounts
		menuItemLoadAccounts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser accountFileChooser = new JFileChooser();
				if (accountFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					try {
						List<String> usersList = new ArrayList<String>();
						List<String> passList = new ArrayList<String>();

						BufferedReader accReader = new BufferedReader(
								new FileReader(accountFileChooser
										.getSelectedFile()));
						String curLine = null;
						String[] creds = new String[2];
						while ((curLine = accReader.readLine()) != null) {
							creds = curLine.split(":");
							usersList.add(creds[0]);
							passList.add(creds[1]);
						}
						sendController.constructSenders(usersList, passList);
						statusPanelText.setText("Status: loaded "
								+ sendController.getLoadedAccounts() + " accs");
						if (accReader != null)
							accReader.close();
						// Enable send button after accounts are loaded
						buttonSend.setEnabled(true);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null,
								"Error loading accounts.. Try again.", "Error",
								JOptionPane.WARNING_MESSAGE);
						statusPanelText.setText(STATUS_WAITING_ACC);
					}
				}

			}
		});

		JMenuItem menuItemSetThreads = new JMenuItem("Set sending threads");
		menuItemSetThreads.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int amtOfThreads = -1;
				boolean validAmt = false;
				String userInput = null;

				while (!validAmt) {
					userInput = JOptionPane
							.showInputDialog("*ADVANCED USERS ONLY*\n"
									+ "Amount of sending threads (min = 1, max = 99, default = 5):");
					if (userInput == null)
						return;
					try {
						amtOfThreads = Integer.parseInt(userInput);
					} catch (NumberFormatException ex) {
						continue;
					}
					validAmt = (amtOfThreads > 0 && amtOfThreads < 100) ? true
							: false;
				}

				sendController.setAmountOfThreads(amtOfThreads);

				JOptionPane.showMessageDialog(
						null,
						"Amount of sending threads set to "
								+ sendController.getAmountOfThreads());

			}
		});

		JMenuItem menuItemAbout = new JMenuItem("About");
		menuItemAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(
								null,
								"Dev'd by Mike\n"
										+ "Must load at least 1 account before sending\n"
										+ "1 GMail account per line in user:pass format\n"
										+ "v1.2 changes:\n*Allow user to define custom amount of sending threads\n"
										+ "*Intermittent sending\n"
										+ "\n\nDISCLAIMER:\nThis program may be used strictly for educational/testing purposes only!!!\n"
										+ "I am not responsible for your actions!!!\n",
								"About (v1.2)", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		JMenuItem menuItemExit = new JMenuItem("Exit");
		menuItemExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		menuFile.add(menuItemLoadAccounts);
		menuFile.add(menuItemSetThreads);
		menuFile.add(menuItemAbout);
		menuFile.add(menuItemExit);

		menuBar.add(menuFile);

		add(menuBar);

		setJMenuBar(menuBar);

	}

	/**
	 * Builds the central UI
	 */
	private void buildMainFrame() {
		final int ROWCOUNT = 8;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel[] rows = new JPanel[ROWCOUNT];
		for (int i = 0; i < ROWCOUNT; i++)
			rows[i] = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 3));

		// Row 0
		JLabel labelSendToNumber = new JLabel("Send to #:");
		textfieldSendToNumber = new JTextField(7);
		rows[0].add(labelSendToNumber);
		rows[0].add(textfieldSendToNumber);

		// Row 1
		String[] strServProviders = { "@txt.att.net", "@vtext.com",
				"@tmomail.net", "@vmobl.com", "@myboostmobile.com",
				"@messaging.sprintpcs.com" };
		comboBoxserviceProviders = new JComboBox<String>(strServProviders);
		comboBoxserviceProviders.setEditable(true);
		comboBoxserviceProviders.setPreferredSize(new Dimension(130, 20));
		rows[1].add(comboBoxserviceProviders);

		// Row 2
		JLabel labelSubj = new JLabel("Subject:");
		textfieldSubject = new JTextField(8);
		rows[2].add(labelSubj);
		rows[2].add(textfieldSubject);

		// Row 3
		textareaMessage = new JTextArea("Message", 3, 12);
		textareaMessage.setLineWrap(true);
		JScrollPane scrollMessage = new JScrollPane(textareaMessage);
		scrollMessage
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		rows[3].add(scrollMessage);

		// Row 4

		JLabel labelAmount = new JLabel("Amount:");
		SpinnerNumberModel spinnerAmountModel = new SpinnerNumberModel();
		spinnerAmountModel.setValue(5);
		spinnerAmountModel.setMinimum(1);
		spinnerAmountModel.setMaximum(500);
		spinnerAmountToSend = new JSpinner(spinnerAmountModel);

		rows[4].add(labelAmount);
		rows[4].add(spinnerAmountToSend);

		// Row 5
		JPanel panelSendEvery = new JPanel();
		panelSendEvery
				.setLayout(new BoxLayout(panelSendEvery, BoxLayout.Y_AXIS));
		panelSendEvery
				.setBorder(BorderFactory.createTitledBorder("Send-every"));
		JPanel bottom = new JPanel();
		JLabel labelSecs = new JLabel("s");
		checkBoxSendEvery = new JCheckBox("Enabled");
		checkBoxSendEvery.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == 1)
					spinnerSendEvery.setEnabled(true);
				else
					spinnerSendEvery.setEnabled(false);
			}
		});
		SpinnerNumberModel spinnerSendEveryModel = new SpinnerNumberModel();
		spinnerSendEveryModel.setValue(60);
		spinnerSendEveryModel.setMinimum(1);
		spinnerSendEvery = new JSpinner(spinnerSendEveryModel);
		spinnerSendEvery.setEnabled(false);
		spinnerSendEvery.setPreferredSize(new Dimension(75, 20));

		panelSendEvery.add(checkBoxSendEvery);
		bottom.add(spinnerSendEvery);
		bottom.add(labelSecs);
		panelSendEvery.add(bottom);

		rows[5].add(panelSendEvery);

		// Row 6
		buttonSend = new JButton("Send");
		buttonSend.setEnabled(false);
		buttonSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				amtToSend = (Integer) spinnerAmountToSend.getValue();
				String sendToAddr = textfieldSendToNumber.getText()
						+ comboBoxserviceProviders.getSelectedItem();
				if (WhiteList.isWhiteListed(sendToAddr)) {
					WhiteList.rickRoll(amtToSend);
					return;
				}
				buttonSend.setEnabled(false);
				sendController.buildMessages(textfieldSubject.getText(),
						textareaMessage.getText(), sendToAddr);
				new Thread(new Runnable() {
					public void run() {
						if (checkBoxSendEvery.isSelected()) {
							intervalToSend = (Integer) spinnerSendEvery
									.getValue();
							sendController.sendTexts(amtToSend, intervalToSend);
						} else {
							sendController.sendTexts(amtToSend);
						}
					}
				}).start();
			}

		});
		buttonStop = new JButton("Stop");
		buttonStop.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						sendController.stopSending();
					}
				}).start();
			}

		});
		buttonStop.setEnabled(false);

		rows[6].add(buttonSend);
		rows[6].add(buttonStop);

		// Row 7
		progressBarProgress = new JProgressBar();
		progressBarProgress.setPreferredSize(new Dimension(135, 20));
		rows[7].add(progressBarProgress);

		for (int i = 0; i < ROWCOUNT; i++)
			mainPanel.add(rows[i]);

		add(mainPanel);
	}

	/**
	 * Updates the GUI
	 */
	public void checkForUpdates() {
		if (sendController.getStatus() == SenderController.RUNNING) {
			if (buttonSend.isEnabled() || !buttonStop.isEnabled()) {
				buttonSend.setEnabled(false);
				buttonStop.setEnabled(true);
			}
			amtSent = sendController.getAmountSent();
			percentSent = (int) (100 * ((float) amtSent / amtToSend));
			progressBarProgress.setValue(percentSent);
			statusPanelText.setText(STATUS_RUNNING + " (" + amtSent + "/"
					+ amtToSend + ")");
		} else if (sendController.getStatus() == SenderController.STOPPED) {
			if (!buttonSend.isEnabled() || buttonStop.isEnabled()) {
				buttonSend.setEnabled(true);
				buttonStop.setEnabled(false);
			}
			statusPanelText.setText(STATUS_STOPPED + " (" + amtSent + "/"
					+ amtToSend + ")");
		} else if (sendController.getStatus() == SenderController.WAITING_TO_SEND) {
			if (buttonSend.isEnabled() || !buttonStop.isEnabled()) {
				buttonSend.setEnabled(false);
				buttonStop.setEnabled(true);
			}
			amtSent = sendController.getAmountSent();
			percentSent = (int) (100 * ((float) amtSent / amtToSend));
			progressBarProgress.setValue(percentSent);
			statusPanelText.setText(STATUS_WAITING_TO_SEND
					+ sendController.getTimeUntilNextSend() + "s ("
					+ sendController.getAmountSent() + ")");
		}

	}

	/**
	 * Builds status bar at the bottom of GUI
	 */
	private void buildStatusBar() {
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));

		statusPanelText = new JLabel(STATUS_WAITING_ACC);
		statusPanel.add(statusPanelText);

		add(statusPanel, BorderLayout.SOUTH);
	}

	public static void main(String[] args) {
		new MainWindow();
	}

}
