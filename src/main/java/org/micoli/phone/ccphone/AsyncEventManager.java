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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.micoli.commands.Command;
import org.micoli.commands.CommandManager;
import org.micoli.phone.ccphone.call.Call;
import org.micoli.phone.ccphone.remote.VertX;
import org.micoli.phone.tools.JsonMapper;
import org.micoli.phone.tools.ProxyLogger;
import org.vertx.java.core.json.JsonObject;

// TODO: Auto-generated Javadoc
/**
 * The Class AsyncEventManager.
 */
public class AsyncEventManager implements SipListener {

	/** The user agent. */
	private UserAgent userAgent;

	/** The calls. */
	private Map<String, Call> calls;

	/** The closed. */
	private boolean closed;

	/** The logger. */
	private ProxyLogger logger;

	/** The main. */
	Main main;

	/**
	 * Instantiates a new async event manager.
	 *
	 * @param main the main
	 * @param peersHome the peers home
	 * @param logger the logger
	 */
	public AsyncEventManager(Main main, String peersHome, ProxyLogger logger) {
		this.main = main;
		this.logger = logger;
		calls = Collections.synchronizedMap(new HashMap<String,Call>());
		closed = false;
		// create sip stack
		CommandManager.scan(this,logger);
		try {
			String name;
			name = System.getProperty("user.name");
			System.out.println(name);
			System.out.println(peersHome);

			userAgent = new UserAgent(this, peersHome, logger);
		} catch (Exception e) {
			logger.error("Peers sip port " + "unavailable, about to leave");
			System.exit(1);
		}
	}

	/**
	 * Gets the sip request from call id.
	 *
	 * @param callId the call id
	 * @return the sip request from call id
	 */
	SipRequest getSipRequestFromCallId(String callId){
		SipRequest sipRequest = null;
		if(calls.containsKey(callId)){
			sipRequest = calls.get(callId).getSipRequest();
		}
		return sipRequest;
	}

	/**
	 * Gets the call.
	 *
	 * @param sipMessage the sip message
	 * @return the call
	 */
	private Call getCall(SipMessage sipMessage) {
		String callId = Utils.getMessageCallId(sipMessage);
		return calls.get(callId);
	}

	/**
	 * sip registration vent.
	 *
	 * @param sipRequest the sip request
	 */
	public void registering(SipRequest sipRequest) {
		VertX.publishGui(JsonMapper.sipRequest("registering",sipRequest));
	}

	/**
	 * sip registration vent.
	 *
	 * @param sipResponse the sip response
	 */
	public synchronized void registerFailed(SipResponse sipResponse) {
		VertX.publishGui(JsonMapper.sipResponse("registerFailed",sipResponse));
	}

	/**
	 * sip registration vent.
	 *
	 * @param sipResponse the sip response
	 */
	public synchronized void registerSuccessful(SipResponse sipResponse) {
		VertX.publishGui(JsonMapper.sipResponse("registerSuccessful",sipResponse));
		if (closed) {
			userAgent.close();
			System.exit(0);
			return;
		}
	}

	/**
	 * sip event.
	 *
	 * @param sipResponse the sip response
	 */
	public synchronized void calleePickup(SipResponse sipResponse) {
		Call call = getCall(sipResponse);
		if (call != null) {
			call.calleePickup(sipResponse);
		}
	}

	/**
	 * sip event.
	 *
	 * @param sipResponse the sip response
	 */
	public synchronized void error(SipResponse sipResponse) {
		Call call = getCall(sipResponse);
		if (call != null) {
			call.error(sipResponse);
		}
	}

	/**
	 * sip event.
	 *
	 * @param sipRequest the sip request
	 * @param provResponse the prov response
	 */
	public synchronized void incomingCall(final SipRequest sipRequest, SipResponse provResponse) {
		SipHeaderFieldValue from = sipRequest.getSipHeaders().get(new SipHeaderFieldName(RFC3261.HDR_FROM));
		String callId = Utils.getMessageCallId(sipRequest);

		Call call = new Call(from.getValue(), callId, logger);
		calls.put(callId, call);
		call.setSipRequest(sipRequest);
		call.incomingCall();
	}

	/**
	 * sip event.
	 *
	 * @param sipRequest the sip request
	 */
	public synchronized void remoteHangup(SipRequest sipRequest) {
		Call call = getCall(sipRequest);
		if (call != null) {
			call.remoteHangup();
			calls.remove(call.getCallid());
		}
	}

	/**
	 * sip event.
	 *
	 * @param sipResponse the sip response
	 */
	public synchronized void ringing(SipResponse sipResponse) {
		Call call = getCall(sipResponse);
		if (call != null) {
			call.ringing(sipResponse);
		}
	}

	// main frame events
	/**
	 * Register.
	 *
	 * @throws SipUriSyntaxException the sip uri syntax exception
	 */
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

	/**
	 * Window closed.
	 */
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

	/**
	 * Call action.
	 *
	 * @param message the message
	 */
	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] callAction(@Command("uri") String uri) {
		uri = RFC3261.SIP_SCHEME + RFC3261.SCHEME_SEPARATOR + uri + RFC3261.AT + main.config.getDomain();
		String callId = Utils.generateCallID(userAgent.getConfig().getLocalInetAddress());
		Call call = new Call(uri, callId, logger);
		SipRequest sipRequest;
		try {
			sipRequest = userAgent.getUac().invite(uri, callId);
			calls.put(callId, call);
			call.setSipRequest(sipRequest);
			call.callAction();
		} catch (SipUriSyntaxException e) {
			logger.error(e.getMessage(), e);
			VertX.publishGui(new JsonObject().putString("setSipRequestError", e.getMessage()));
			return new String[] { e.getMessage() };
		}
		return new String[] { "ok" };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] testAction(@Command("a1") String arg1) {
		return new String[] { "testAction SHELL test" + arg1.toString() };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] testAction2(@Command("a1") String arg1, @Command("a2") String arg2) {
		return new String[] { "testAction SHELL test " + arg1.toString() + " " + arg2.toString() };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] testAction3(@Command("a1") String arg1, @Command("a2") String arg2, @Command("a3") String arg3) {
		return new String[] { "testAction SHELL test " + arg1.toString() + " " + arg2.toString() + " " + arg3.toString() };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] muteAction() {
		// userAgent.getSoundManager().mute(true);
		return new String[] { "ok" };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] unmuteAction() {
		// userAgent.getSoundManager().mute(false);
		return new String[] { "ok" };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] listCallsAction() {
		String[] result = new String[calls.size()];
		int n = 0;
		JsonObject jsonList = new JsonObject();

		Iterator<Entry<String, Call>> it = calls.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,Call> pair = (Map.Entry<String,Call>)it.next();
			Call call = pair.getValue();
			jsonList.putObject(pair.getKey(), new JsonObject().putString("callid", call.getCallid()).putString("state", call.getCallState()));
			result[n] = String.format("%s=>%s", call.getCallid(), call.getCallState());
			n++;
		}
		VertX.publishGui(new JsonObject().putObject("list", jsonList));
		return result;
	}

	/**
	 * Hangup action.
	 *
	 * @param message the message
	 */
	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] hangupAction(@Command("sipcallid") String sipCallId) {
		SipRequest sipRequest = getSipRequestFromCallId(sipCallId);
		userAgent.getUac().terminate(sipRequest);
		return new String[] { "ok" };
	}

	/**
	 * Pickup action.
	 *
	 * @param message the message
	 */
	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] pickupAction(@Command("sipcallid") String sipCallId) {
		//SipRequest sipRequest = new SipRequest(null, null);
		//sipRequest.getSipHeaders().add(new SipHeaderFieldName(RFC3261.HDR_CALLID),new SipHeaderFieldValue(msgCallId));
		SipRequest sipRequest = getSipRequestFromCallId(sipCallId);
		String callId = Utils.getMessageCallId(sipRequest);
		DialogManager dialogManager = userAgent.getDialogManager();
		Dialog dialog = dialogManager.getDialog(callId);
		userAgent.getUas().acceptCall(sipRequest, dialog);
		return new String[] { "ok" };
	}

	/**
	 * Busy here action.
	 *
	 * @param message the message
	 */
	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] busyHereAction(@Command("sipcallid") String sipCallId) {
		SipRequest sipRequest = getSipRequestFromCallId(sipCallId);
		userAgent.getUas().rejectCall(sipRequest);
		return new String[] { "ok" };
	}

	/**
	 * Dtmf.
	 *
	 * @param message the message
	 */
	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public String[] dtmf(@Command("dtmfdigit") String dtmfDigit) {
		MediaManager mediaManager = userAgent.getMediaManager();
		mediaManager.sendDtmf(dtmfDigit.charAt(0));
		return new String[] { "ok" };
	}
}