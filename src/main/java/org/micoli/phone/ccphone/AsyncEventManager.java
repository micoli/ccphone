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
import java.util.Map;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.micoli.phone.ccphone.call.Call;
import org.micoli.phone.ccphone.remote.VertX;
import org.micoli.phone.tools.JsonMapper;
import net.sourceforge.peers.Logger;

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
	private Logger logger;

	/** The main. */
	Main main;

	AsyncCommandManager asyncCommandManager;

	/**
	 * Instantiates a new async event manager.
	 *
	 * @param main the main
	 * @param logger the logger
	 */
	public AsyncEventManager(Main main, Logger logger) {
		this.main = main;
		this.logger = logger;
		setCalls(Collections.synchronizedMap(new HashMap<String,Call>()));
		closed = false;
		try {
			this.userAgent = new UserAgent(this, main.config, logger);

			asyncCommandManager= new AsyncCommandManager(this,logger);

		} catch (Exception e) {
			logger.error("Peers sip port " + "unavailable, about to leave");
			System.exit(1);
		}
	}

	public Map<String, Call> getCalls() {
		return calls;
	}

	public void setCalls(Map<String, Call> calls) {
		this.calls = calls;
	}

	public UserAgent getUserAgent() {
		return userAgent;
	}

	/**
	 * Gets the sip request from call id.
	 *
	 * @param callId the call id
	 * @return the sip request from call id
	 */
	SipRequest getSipRequestFromCallId(String callId){
		SipRequest sipRequest = null;
		if(getCalls().containsKey(callId)){
			sipRequest = getCalls().get(callId).getSipRequest();
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
		return getCalls().get(callId);
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
			getUserAgent().close();
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
		getCalls().put(callId, call);
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
			getCalls().remove(call.getCallid());
		}
	}

	/**
	 * sip event.
	 *
	 * @param sipRequest the sip request
	 */
	public synchronized void hangup(SipRequest sipRequest) {
		Call call = getCall(sipRequest);
		if (call != null) {
			VertX.publishGui(JsonMapper.sipRequest("close",sipRequest));
			getCalls().remove(call.getCallid());
		}
	}

	/**
	 * sip event.
	 *
	 * @param sipRequest the sip request
	 */
	public synchronized void busyHere(SipRequest sipRequest) {
		Call call = getCall(sipRequest);
		if (call != null) {
			VertX.publishGui(JsonMapper.sipRequest("close",sipRequest));
			getCalls().remove(call.getCallid());
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
		if (getUserAgent() == null) {
			// if several peers instances are launched concurrently,
			// display error message and exit
			return;
		}
		Config config = getUserAgent().getConfig();
		if (config.getPassword() != null) {
			getUserAgent().getUac().register();
		}
	}

	/**
	 * Window closed.
	 */
	public synchronized void windowClosed() {
		try {
			getUserAgent().getUac().unregister();
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
}