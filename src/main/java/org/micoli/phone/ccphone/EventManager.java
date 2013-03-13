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

package org.micoli.phone.ccphone;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class EventManager implements SipListener, ActionListener {

	public static final String PEERS_URL = "http://peers.sourceforge.net/";
	public static final String PEERS_USER_MANUAL = PEERS_URL + "user_manual";

	public static final String ACTION_EXIT = "Exit";
	public static final String ACTION_ACCOUNT = "Account";
	public static final String ACTION_PREFERENCES = "Preferences";
	public static final String ACTION_ABOUT = "About";
	public static final String ACTION_DOCUMENTATION = "Documentation";

	private UserAgent userAgent;
	// private Map<String, CallFrame> callFrames;
	private boolean closed;
	private Logger logger;

	public EventManager(String peersHome, Logger logger) {
		this.logger = logger;
		// callFrames = Collections.synchronizedMap(new HashMap<String,
		// CallFrame>());
		closed = false;
		try {
			userAgent = new UserAgent(this, peersHome, logger);
		} catch (SocketException e) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					JOptionPane.showMessageDialog(null, "Peers sip port " + "unavailable, about to leave", "Error", JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
			});
		}
	}

	// sip events

	public void registering(SipRequest sipRequest) {
		// mainFrame.registering(sipRequest);
	}

	public synchronized void registerFailed(SipResponse sipResponse) {
		// mainFrame.setLabelText("Registration failed");
		// mainFrame.registerFailed(sipResponse);
	}

	public synchronized void registerSuccessful(SipResponse sipResponse) {
		if (closed) {
			userAgent.close();
			System.exit(0);
			return;
		}
		// mainFrame.registerSuccessful(sipResponse);
	}

	public synchronized void calleePickup(SipResponse sipResponse) {
		// CallFrame callFrame = getCallFrame(sipResponse);
		// if (callFrame != null) {
		// callFrame.calleePickup();
		// }
	}

	public synchronized void error(SipResponse sipResponse) {
		// CallFrame callFrame = getCallFrame(sipResponse);
		// if (callFrame != null) {
		// callFrame.error(sipResponse);
		// }
	}

	public synchronized void incomingCall(final SipRequest sipRequest, SipResponse provResponse) {
		SipHeaders sipHeaders = sipRequest.getSipHeaders();
		SipHeaderFieldName sipHeaderFieldName = new SipHeaderFieldName(RFC3261.HDR_FROM);
		SipHeaderFieldValue from = sipHeaders.get(sipHeaderFieldName);
		final String fromValue = from.getValue();
		String callId = Utils.getMessageCallId(sipRequest);
		// CallFrame callFrame = new CallFrame(fromValue, callId, this, logger);
		// callFrames.put(callId, callFrame);
		// callFrame.setSipRequest(sipRequest);
		// callFrame.incomingCall();
	}

	public synchronized void remoteHangup(SipRequest sipRequest) {
		// CallFrame callFrame = getCallFrame(sipRequest);
		// if (callFrame != null) {
		// callFrame.remoteHangup();
		// }
	}

	public synchronized void ringing(SipResponse sipResponse) {
		// CallFrame callFrame = getCallFrame(sipResponse);
		// if (callFrame != null) {
		// callFrame.ringing();
		// }
	}

	// main frame events

	public void register() throws SipUriSyntaxException {
		if (userAgent == null) {
			// if several peers instances are launched concurrently,
			// display error message and exit
			return;
		}
		Config config = userAgent.getConfig();
		if (config.getPassword() != null) {
			userAgent.getUac().register();
		}
	}

	public synchronized void callClicked(String uri) {
		String callId = Utils.generateCallID(userAgent.getConfig().getLocalInetAddress());
		// CallFrame callFrame = new CallFrame(uri, callId, this, logger);
		// callFrames.put(callId, callFrame);
		SipRequest sipRequest;
		try {
			sipRequest = userAgent.getUac().invite(uri, callId);
		} catch (SipUriSyntaxException e) {
			logger.error(e.getMessage(), e);
			return;
		}
		// callFrame.setSipRequest(sipRequest);
		// callFrame.callClicked();
	}

	public synchronized void windowClosed() {
		try {
			userAgent.getUac().unregister();
		} catch (Exception e) {
			logger.error("error while unregistering", e);
		}
		closed = true;
		Thread thread = new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(3 * RFC3261.TIMER_T1);
				} catch (InterruptedException e) {
				}
				System.exit(0);
			}
		});
		thread.start();
	}

	// call frame events

	public synchronized void hangupClicked(SipRequest sipRequest) {
		userAgent.getUac().terminate(sipRequest);
	}

	public synchronized void pickupClicked(SipRequest sipRequest) {
		String callId = Utils.getMessageCallId(sipRequest);
		DialogManager dialogManager = userAgent.getDialogManager();
		Dialog dialog = dialogManager.getDialog(callId);
		userAgent.getUas().acceptCall(sipRequest, dialog);
	}

	public synchronized void busyHereClicked(SipRequest sipRequest) {
		userAgent.getUas().rejectCall(sipRequest);
	}

	public void dtmf(char digit) {
		MediaManager mediaManager = userAgent.getMediaManager();
		mediaManager.sendDtmf(digit);
	}

	// private CallFrame getCallFrame(SipMessage sipMessage) {
	// String callId = Utils.getMessageCallId(sipMessage);
	// return callFrames.get(callId);
	// }

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		logger.debug("gui actionPerformed() " + action);
		Runnable runnable = null;
		if (ACTION_EXIT.equals(action)) {
			runnable = new Runnable() {

				public void run() {
					windowClosed();
				}
			};
		} else if (ACTION_ACCOUNT.equals(action)) {
		} else if (ACTION_PREFERENCES.equals(action)) {
			runnable = new Runnable() {

				public void run() {
					JOptionPane.showMessageDialog(null, "Not implemented yet");
				}
			};
		} else if (ACTION_ABOUT.equals(action)) {
			runnable = new Runnable() {

				public void run() {
					AboutFrame aboutFrame = new AboutFrame(userAgent.getPeersHome(), logger);
					aboutFrame.setVisible(true);
				}
			};
		} else if (ACTION_DOCUMENTATION.equals(action)) {
			runnable = new Runnable() {

				public void run() {
					try {
						URI uri = new URI(PEERS_USER_MANUAL);
						java.awt.Desktop.getDesktop().browse(uri);
					} catch (URISyntaxException e) {
						logger.error(e.getMessage(), e);
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}
			};
		}
		if (runnable != null) {
			SwingUtilities.invokeLater(runnable);
		}
	}
}
