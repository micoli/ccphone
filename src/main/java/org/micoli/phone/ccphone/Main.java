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

	inspired from Yohann Martineau Copyright 2010
 */

//mvn clean package;java -jar target/ccphone-0.1-SNAPSHOT-jar-with-dependencies.jar

package org.micoli.phone.ccphone;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sourceforge.peers.XmlConfig;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.micoli.phone.ccphone.registrations.Registration;
import org.micoli.phone.ccphone.remote.VertX;
import org.micoli.phone.tools.ProxyLogger;


// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 */
public class Main {
	static {
		System.setProperty("apple.awt.UIElement", "true");
	}
	
	/** The event manager. */
	private AsyncEventManager eventManager ;
	
	/** The registration. */
	private Registration registration;
	
	/** The logger. */
	private ProxyLogger logger;
	
	/** The tray. */
	private SystemTray tray = null;
	
	/** The tray icon. */
	private TrayIcon trayIcon;
	
	/** The peers home. */
	private String peersHome;
	
	/** The vert x. */
	VertX vertX;
	
	/** The config. */
	public XmlConfig config;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Main(args);
			}
		});
	}

	/**
	 * Instantiates a new main.
	 *
	 * @param args the args
	 */
	public Main(final String[] args) {

		peersHome = Utils.DEFAULT_PEERS_HOME;
		if (args.length > 0) {
			peersHome = args[0];
		}
		logger = new ProxyLogger(peersHome);
		config = new XmlConfig(peersHome + File.separator+ UserAgent.CONFIG_FILE, this.logger);

		launchThreads(args);

		initTray();
	}

	/**
	 * Launch threads.
	 *
	 * @param args the args
	 */
	private void launchThreads(final String[] args) {
		VertX.init(logger);
		VertX.run();

		Thread thread = new Thread(new Runnable() {
			public void run() {
				String peersHome = Utils.DEFAULT_PEERS_HOME;
				if (args.length > 0) {
					peersHome = args[0];
				}
				eventManager = new AsyncEventManager(Main.this, peersHome, logger);

				try {
					eventManager.register();
				} catch (SipUriSyntaxException e) {
					// statusLabel.setText(e.getMessage());
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

	/**
	 * Inits the tray.
	 */
	private void initTray() {
		if (tray != null) {
			return;
		}
		if (!SystemTray.isSupported()) {
			return;
		}
		final PopupMenu popup = new PopupMenu();
		URL url = getClass().getResource("/org/micoli/phone/phone-icon-gray.png");
		Image imageIcon = Toolkit.getDefaultToolkit().getImage(url);
		trayIcon = new TrayIcon(imageIcon, "test");
		tray = SystemTray.getSystemTray();

		// Create a popup menu components
		MenuItem aboutItem = new MenuItem("About");
		MenuItem exitItem = new MenuItem("Exit");

		popup.add(aboutItem);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			logger.error("TrayIcon could not be added.");
			return;
		}

		/*
		 * trayIcon.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) { JOptionPane.showMessageDialog(null,
		 * "This dialog box is run from System Tray"); } });
		 */

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "This dialog box is run from the About menu item");
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tray.remove(trayIcon);
				System.exit(0);
			}
		});
	}

	/**
	 * Window closed.
	 *
	 * @param e the e
	 */
	public void windowClosed(WindowEvent e) {
		eventManager.windowClosed();
	}

	/**
	 * Register failed.
	 *
	 * @param sipResponse the sip response
	 */
	public void registerFailed(SipResponse sipResponse) {
		registration.registerFailed();
	}

	/**
	 * Register successful.
	 *
	 * @param sipResponse the sip response
	 */
	public void registerSuccessful(SipResponse sipResponse) {
		registration.registerSuccessful();
	}

	/**
	 * Registering.
	 *
	 * @param sipRequest the sip request
	 */
	public void registering(SipRequest sipRequest) {
		registration.registerSent();
	}

	/**
	 * Socket exception on startup.
	 */
	public void socketExceptionOnStartup() {
		logger.error("peers SIP port " + "unavailable, exiting");
		System.exit(1);
	}

	/**
	 * Window activated.
	 *
	 * @param arg0 the arg0
	 */
	public void windowActivated(WindowEvent arg0) {
	}

	/**
	 * Window closing.
	 *
	 * @param arg0 the arg0
	 */
	public void windowClosing(WindowEvent arg0) {
	}

	/**
	 * Window deactivated.
	 *
	 * @param arg0 the arg0
	 */
	public void windowDeactivated(WindowEvent arg0) {
	}

	/**
	 * Window deiconified.
	 *
	 * @param arg0 the arg0
	 */
	public void windowDeiconified(WindowEvent arg0) {
	}

	/**
	 * Window iconified.
	 *
	 * @param arg0 the arg0
	 */
	public void windowIconified(WindowEvent arg0) {
	}

	/**
	 * Window opened.
	 *
	 * @param arg0 the arg0
	 */
	public void windowOpened(WindowEvent arg0) {
	}
}