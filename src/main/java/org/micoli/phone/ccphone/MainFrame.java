/*
	This file is part of Peers, a java SIP softphone.

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

	Copyright 2010 Yohann Martineau
 */

//mvn clean package;java -jar target/ccphone-0.1-SNAPSHOT-jar-with-dependencies.jar  org.micoli.phone.ccphone.MainFrame

package org.micoli.phone.ccphone;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.micoli.phone.ccphone.registrations.Registration;
import org.micoli.phone.ccphone.remote.Server;
import org.micoli.phone.tools.DraggableWindow;


public class MainFrame implements WindowListener, ActionListener {

	private JFrame mainFrame;
	private JPanel mainPanel;
	private JPanel dialerPanel;
	private JTextField uri;
	private JButton actionButton;
	private JLabel statusLabel;

	private AsyncEventManager eventManager ;
	private Registration registration;
	private Logger logger;
	private SystemTray tray = null;
	private TrayIcon trayIcon;
	Server server;

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(args);
			}
		});
	}

	private static void createAndShowGUI(String[] args) {
		JFrame.setDefaultLookAndFeelDecorated(false);
		new MainFrame(args);
	}

	/*private void launchMsgPackServer() {
		final Controller controller = new Controller();
		final EventLoop loop = EventLoop.defaultEventLoop();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				Server svr = new Server();
				svr.serve(controller);
				try {
					System.out.println("to launch");
					svr.listen(1985);
					loop.join();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		System.out.println("ok launched");
		thread.start();

		try {
			while (eventManager == null) {
				Thread.sleep(50);
			}
		} catch (InterruptedException e) {
			return;
		}

	}*/


	public MainFrame(final String[] args) {
		String peersHome = Utils.DEFAULT_PEERS_HOME;
		if (args.length > 0) {
			peersHome = args[0];
		}
		logger = new Logger(peersHome);

		String lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
		// lookAndFeelClassName = "ch.randelshofer.quaqua.QuaquaLookAndFeel";
		// System.setProperty("Quaqua.tabLayoutPolicy", "scroll");
		// System.setProperty("apple.laf.useScreenMenuBar", "false");

		try {
			UIManager.setLookAndFeel(lookAndFeelClassName);
		} catch (Exception e) {
			logger.error("cannot change look and feel", e);
		}

		String title = "";
		if (!Utils.DEFAULT_PEERS_HOME.equals(peersHome)) {
			title = peersHome;
		}

		title += "/Peers: SIP User-Agent";

		mainFrame = new JFrame(title);
		// mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.addWindowListener(this);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

		dialerPanel = new JPanel();

		uri = new JTextField("", 15);
		uri.addActionListener(this);

		actionButton = new JButton("Call");
		actionButton.addActionListener(this);

		dialerPanel.add(uri);
		dialerPanel.add(actionButton);
		dialerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		statusLabel = new JLabel(title);
		statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		Border border = BorderFactory.createEmptyBorder(0, 2, 2, 2);
		statusLabel.setBorder(border);

		mainPanel.add(dialerPanel);
		mainPanel.add(statusLabel);

		Container contentPane = mainFrame.getContentPane();
		contentPane.add(mainPanel);

		registration = new Registration(statusLabel, logger);

		launchThreads(args);

		new DraggableWindow(mainFrame);
		// initializeWindowDragger();
		initializeWindow();
		initTray();
		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == uri) {
			if (uri.getText().equalsIgnoreCase("account")) {
				eventManager.openAccount();
			} else if (uri.getText().equalsIgnoreCase("quit")) {
				tray.remove(trayIcon);
				System.exit(0);
			} else {
				eventManager.callClicked("sip:" + uri.getText());
			}
		} else if (source == actionButton) {
			eventManager.callClicked("sip:" + uri.getText());
		}
	}

	private void launchThreads(final String[] args) {
		Server.run();

		Thread thread = new Thread(new Runnable() {
			public void run() {
				String peersHome = Utils.DEFAULT_PEERS_HOME;
				if (args.length > 0) {
					peersHome = args[0];
				}
				eventManager = new AsyncEventManager(MainFrame.this, peersHome, logger);

				try {
					eventManager.register();
				} catch (SipUriSyntaxException e) {
					statusLabel.setText(e.getMessage());
				}
			}
		});
		thread.start();

		try {
			while (eventManager == null) {
				Thread.sleep(50);
			}
		} catch (InterruptedException e) {
			return;
		}
	}

	private void initTray() {
		if (tray != null) {
			return;
		}
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		final PopupMenu popup = new PopupMenu();
		URL url = getClass().getResource("Settings-Phone-icon.png");
		Image imageIcon = Toolkit.getDefaultToolkit().getImage(url);
		trayIcon = new TrayIcon(imageIcon, "test");
		tray = SystemTray.getSystemTray();

		// Create a popup menu components
		MenuItem aboutItem = new MenuItem("About");
		Menu displayMenu = new Menu("Display");
		MenuItem errorItem = new MenuItem("Error");
		MenuItem warningItem = new MenuItem("Warning");
		MenuItem infoItem = new MenuItem("Info");
		MenuItem noneItem = new MenuItem("None");
		MenuItem exitItem = new MenuItem("Exit");

		// Add components to popup menu
		popup.add(aboutItem);
		popup.addSeparator();
		popup.add(displayMenu);
		displayMenu.add(errorItem);
		displayMenu.add(warningItem);
		displayMenu.add(infoItem);
		displayMenu.add(noneItem);
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}

		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "This dialog box is run from System Tray");
			}
		});

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "This dialog box is run from the About menu item");
			}
		});

		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MenuItem item = (MenuItem) e.getSource();
				// TrayIcon.MessageType type = null;
				System.out.println(item.getLabel());
				if ("Error".equals(item.getLabel())) {
					// type = TrayIcon.MessageType.ERROR;
					trayIcon.displayMessage("Sun TrayIcon Demo", "This is an error message", TrayIcon.MessageType.ERROR);

				} else if ("Warning".equals(item.getLabel())) {
					// type = TrayIcon.MessageType.WARNING;
					trayIcon.displayMessage("Sun TrayIcon Demo", "This is a warning message", TrayIcon.MessageType.WARNING);

				} else if ("Info".equals(item.getLabel())) {
					// type = TrayIcon.MessageType.INFO;
					trayIcon.displayMessage("Sun TrayIcon Demo", "This is an info message", TrayIcon.MessageType.INFO);

				} else if ("None".equals(item.getLabel())) {
					// type = TrayIcon.MessageType.NONE;
					trayIcon.displayMessage("Sun TrayIcon Demo", "This is an ordinary message", TrayIcon.MessageType.NONE);
				}
			}
		};

		errorItem.addActionListener(listener);
		warningItem.addActionListener(listener);
		infoItem.addActionListener(listener);
		noneItem.addActionListener(listener);

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tray.remove(trayIcon);
				System.exit(0);
			}
		});
	}

	private void initializeWindow() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setLocationByPlatform(true);
		mainFrame.setLocation((int) ((screenSize.getWidth() - mainFrame.getWidth()) / 2), 20);
		mainFrame.setUndecorated(true);
		mainFrame.setAlwaysOnTop(true);
		mainFrame.setLocationByPlatform(true);
		setOpacity(0.6f);
	}

	@SuppressWarnings("restriction")
	private void setOpacity(float opacity) {
		com.sun.awt.AWTUtilities.setWindowOpacity(mainFrame, opacity);
	}

	public void windowClosed(WindowEvent e) {
		eventManager.windowClosed();
	}

	public void setLabelText(String text) {
		statusLabel.setText(text);
		mainFrame.pack();
	}

	public void registerFailed(SipResponse sipResponse) {
		registration.registerFailed();
	}

	public void registerSuccessful(SipResponse sipResponse) {
		registration.registerSuccessful();
	}

	public void registering(SipRequest sipRequest) {
		registration.registerSent();
	}

	public void socketExceptionOnStartup() {
		JOptionPane.showMessageDialog(mainFrame, "peers SIP port " + "unavailable, exiting");
		System.exit(1);
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosing(WindowEvent arg0) {
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}
}