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

import java.net.SocketException;
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

import org.micoli.phone.ccphone.call.Call;
import org.micoli.phone.ccphone.remote.VertX;
import org.micoli.phone.tools.GUIAction;
import org.micoli.phone.tools.GUIActionManager;
import org.micoli.phone.tools.JsonMapper;
import org.micoli.phone.tools.ProxyLogger;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class AsyncEventManager implements SipListener {

	private UserAgent userAgent;
	private Map<String, Call> calls;
	private boolean closed;
	private ProxyLogger logger;
	Main main;

	public AsyncEventManager(Main main, String peersHome, ProxyLogger logger) {
		this.main = main;
		this.logger = logger;
		calls = Collections.synchronizedMap(new HashMap<String,Call>());
		closed = false;
		// create sip stack
		GUIActionManager.scan(this,logger);
		try {
			userAgent = new UserAgent(this, peersHome, logger);
		} catch (SocketException e) {
			logger.error("Peers sip port " + "unavailable, about to leave");
			System.exit(1);
		}
	}

	SipRequest getSipRequestFromCallId(String callId){
		SipRequest sipRequest = null;
		if(calls.containsKey(callId)){
			sipRequest = calls.get(callId).getSipRequest();
		}
		return sipRequest;
	}

	private Call getCall(SipMessage sipMessage) {
		String callId = Utils.getMessageCallId(sipMessage);
		return calls.get(callId);
	}

	/**
	 * sip registration vent
	 */
	public void registering(SipRequest sipRequest) {
		VertX.publishGui(JsonMapper.sipRequest("registering",sipRequest));
	}

	/**
	 * sip registration vent
	 */
	public synchronized void registerFailed(SipResponse sipResponse) {
		VertX.publishGui(JsonMapper.sipResponse("registerFailed",sipResponse));
	}

	/**
	 * sip registration vent
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
	 * sip event
	 */
	public synchronized void calleePickup(SipResponse sipResponse) {
		Call call = getCall(sipResponse);
		if (call != null) {
			call.calleePickup(sipResponse);
		}
	}

	/**
	 * sip event
	 */
	public synchronized void error(SipResponse sipResponse) {
		Call call = getCall(sipResponse);
		if (call != null) {
			call.error(sipResponse);
		}
	}

	/**
	 * sip event
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
	 * sip event
	 */
	public synchronized void remoteHangup(SipRequest sipRequest) {
		Call call = getCall(sipRequest);
		if (call != null) {
			call.remoteHangup();
			calls.remove(call.getCallid());
		}
	}

	/**
	 * sip event
	 */
	public synchronized void ringing(SipResponse sipResponse) {
		Call call = getCall(sipResponse);
		if (call != null) {
			call.ringing(sipResponse);
		}
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

	@GUIAction
	public synchronized void callAction(Message<JsonObject> message) {
		String uri = message.body.getString("uri");
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
			return;
		}
	}

	@GUIAction
	public synchronized void listCallsAction(Message<JsonObject> message) {
		JsonObject jsonList = new JsonObject();
		Iterator<Entry<String, Call>> it = calls.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,Call> pair = (Map.Entry<String,Call>)it.next();
			Call call = pair.getValue();
			jsonList.putObject(pair.getKey(),new JsonObject()
				.putString("callid",call.getCallid())
				.putString("state",call.getCallState())
				);
		}
		VertX.publishGui(new JsonObject().putObject("list", jsonList));
	}

	@GUIAction
	public synchronized void hangupAction(Message<JsonObject> message) {
		SipRequest sipRequest = getSipRequestFromCallId(message.body.getString("sipcallid"));
		userAgent.getUac().terminate(sipRequest);
	}

	@GUIAction
	public synchronized void pickupAction(Message<JsonObject> message) {
		//SipRequest sipRequest = new SipRequest(null, null);
		//sipRequest.getSipHeaders().add(new SipHeaderFieldName(RFC3261.HDR_CALLID),new SipHeaderFieldValue(msgCallId));
		SipRequest sipRequest = getSipRequestFromCallId(message.body.getString("sipcallid"));
		String callId = Utils.getMessageCallId(sipRequest);
		DialogManager dialogManager = userAgent.getDialogManager();
		Dialog dialog = dialogManager.getDialog(callId);
		userAgent.getUas().acceptCall(sipRequest, dialog);
	}

	@GUIAction
	public synchronized void busyHereAction(Message<JsonObject> message) {
		SipRequest sipRequest = getSipRequestFromCallId(message.body.getString("sipcallid"));
		userAgent.getUas().rejectCall(sipRequest);
	}

	@GUIAction
	public void dtmf(Message<JsonObject> message) {
		MediaManager mediaManager = userAgent.getMediaManager();
		mediaManager.sendDtmf(message.body.getString("dmtfDigit").charAt(0));
	}

}