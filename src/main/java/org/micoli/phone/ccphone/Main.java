/**
	This file is an extends of Peers, a java SIP softphone.

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

	@author o.michaud
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.XmlConfig;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.micoli.commands.Command;
import org.micoli.commands.CommandManager;
import org.micoli.phone.ccphone.registrations.Registration;
import org.micoli.phone.ccphone.remote.VertX;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hazelcast.util.Base64;

/**
 * The Class Main.
 */
public class Main {
	static {
		System.setProperty("apple.awt.UIElement", "true");
	}

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Main(args);
			}
		});
	}

	/** The event manager. */
	private AsyncEventManager eventManager ;

	/** The registration. */
	private Registration registration;

	/** The logger. */
	private Logger logger;

	/** The tray. */
	private SystemTray tray = null;

	/** The tray icon. */
	private TrayIcon trayIcon;

	/** The config. */
	public XmlConfig config;

	public Boolean configOk = false;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	/**
	 * Instantiates a new main.
	 *
	 * @param args the args
	 */
	public Main(final String[] args) {
		logger = new Logger("ccPhone");
		setLogLevel(3);

		VertX.init(logger);

		CommandManager.scan(this, logger);
		try {
			CommandManager.setBannerCommand(this, getClass().getMethod("getBanner"));
			CommandManager.setAuthCommand(this, getClass().getMethod("authenticate", String.class, String.class));
		} catch (NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		}

		loadConfig();

		initTray();

		VertX.run();

		if (configOk) {
			initSIP();
		}
	}

	public boolean authenticate(String user, String password) {
		if (user.equals("root") && password.equals("root")) {
			return true;
		} else {
			return false;
		}

	}
	public ArrayList<String> getBanner() {
		ArrayList<String> banners = new ArrayList<>();
		banners.add("--------------");
		if (eventManager != null) {
			banners.add(String.format("Domain : %s", eventManager.getUserAgent().getDomain().toString()));
			if (eventManager.getUserAgent().getOutboundProxy() != null) {
				banners.add(String.format("outbound proxy : %s", eventManager.getUserAgent().getOutboundProxy()));
			}
			banners.add(String.format("username : %s", eventManager.getUserAgent().getUserpart().toString()));
			banners.add(String.format("rtp port : %s", eventManager.getUserAgent().getRtpPort()));
			banners.add(String.format("sip port : %s", eventManager.getUserAgent().getSipPort()));
			if (eventManager.getUserAgent().isRegistered()) {
				banners.add("Registered");
			} else {
				banners.add("Not Registered");
			}
		}
		banners.add("--------------");
		return banners;
	}

	private void initSIP() {
		logger.info("Init SIP");
		if (eventManager != null) {
			if (eventManager.getUserAgent().isRegistered()) {
				eventManager.getUserAgent().close();
			}
		}
		eventManager = new AsyncEventManager(Main.this, logger);
		try {
			eventManager.register();
		} catch (SipUriSyntaxException e) {
			logger.error("InitSIP SipUriSyntaxException", e);
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

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] restart() {
		try {
			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			File currentJar;
			currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());

			/* is it a jar file? */
			if (!currentJar.getName().endsWith(".jar"))
				return new String[] { "Not Launched from a jar" };

			/* Build command: java -jar application.jar */
			final ArrayList<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());

			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
			System.exit(0);
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		return new String[] { "Restarting" };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] setLogLevel(@Command("level") int level) {
		switch(level){
		case 0:
			logger.setLevel(Logger.Level.OFF);
			break;
		case 1:
			logger.setLevel(Logger.Level.INFO);
			break;
		case 2:
			logger.setLevel(Logger.Level.ERROR);
			break;
		case 3:
			logger.setLevel(Logger.Level.DEBUG);
			break;
		}
		return new String[] { "ok" };
	}
	/**
	 *
	 * <root>
	 *		<url>http://server/provisioning.php?user=%user%</url>
	 *		<user>xxx</user>
	 *		<password>yyy</password>
	 * </root>
	 */
	public Boolean loadConfig() {
		Document document;
		String provisionningUrl = null;
		String provisionningUser = null;
		String provisionningPassword = null;
		File provisionningServerFile = new File(System.getProperty("user.home") + File.separator + ".ccPhoneProvisionningUrl.txt");
		File provisionningLocalConfigFile = new File(System.getProperty("user.home") + File.separator + ".ccPhone.xml");
		String configFileName = System.getProperty("java.io.tmpdir") + File.separator + ".ccPhoneConfig.xml";

		configOk = false;

		if (provisionningServerFile.exists()) {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder;
			try {
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
				document = documentBuilder.parse(provisionningServerFile);
				provisionningUrl = getNodeValue(document, "//url//text()");
				provisionningUser = getNodeValue(document, "//user//text()");
				provisionningPassword = getNodeValue(document, "//password//text()");
				if (provisionningUrl != null) {
					provisionningUrl = provisionningUrl.replace("%user%", System.getProperty("user.name"));
					provisionningUrl = provisionningUrl.replace("%localhost%", java.net.InetAddress.getLocalHost().getHostName());
					logger.info("provisionningUrl " + provisionningUrl);
					try {
						String buffer = "";
						FileWriter fstream;
						InputStream inputStream;
						URL url = new URL(provisionningUrl);
						URLConnection uc = url.openConnection();
						if (provisionningUser != null && provisionningPassword != null) {
							String userpass = provisionningUser + ":" + provisionningPassword;
							String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes()));
							uc.setRequestProperty("Authorization", basicAuth);
							inputStream = uc.getInputStream();
						} else {
							inputStream = url.openStream();
						}
						fstream = new FileWriter(configFileName);
						BufferedWriter out = new BufferedWriter(fstream);
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
						while ((buffer = reader.readLine()) != null) {
							out.write(buffer + "\n");
						}
						out.close();
					} catch (IOException e) {
						logger.error("loadConfig Error using " + provisionningUrl, e);
						if (provisionningLocalConfigFile.exists()) {
							configFileName = provisionningLocalConfigFile.getAbsolutePath();
						} else {
							logger.error("provisionningLocalConfigFile does not exists");
							return false;
						}
					}
					logger.info("configFileName " + configFileName);
					config = new XmlConfig(configFileName, this.logger);
					configOk = true;
					return true;
				}
			} catch (UnknownHostException e) {
				logger.error("loadConfig UnknownHost exception ", e);
			} catch (SAXException e) {
				logger.error("loadConfig SAX exception " + provisionningServerFile.getPath(), e);
			} catch (ParserConfigurationException e) {
				logger.error("loadConfig Parser configuration exception", e);
			} catch (XPathExpressionException e) {
				logger.error("loadConfig XPath expression exception", e);
			} catch (IOException e) {
				logger.error("loadConfig IO exception expression exception", e);
			}
		}
		return false;
	}

	private String getNodeValue(Document document, String path) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile(path);
		Object result = expr.evaluate(document, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		if (nodes.getLength() == 1) {
			return nodes.item(0).getNodeValue();
		}
		return null;
	}

	/**
	 * Window closed.
	 *
	 * @param e
	 *            the event
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