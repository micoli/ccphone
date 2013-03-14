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

package org.micoli.phone.ccphone.call;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.micoli.phone.ccphone.call.state.CallState;
import org.micoli.phone.ccphone.call.state.CallStateFailed;
import org.micoli.phone.ccphone.call.state.CallStateInit;
import org.micoli.phone.ccphone.call.state.CallStateRemoteHangup;
import org.micoli.phone.ccphone.call.state.CallStateRinging;
import org.micoli.phone.ccphone.call.state.CallStateSuccess;
import org.micoli.phone.ccphone.call.state.CallStateTerminated;
import org.micoli.phone.ccphone.call.state.CallStateUac;
import org.micoli.phone.ccphone.call.state.CallStateUas;
import org.micoli.phone.ccphone.remote.Server;
import org.micoli.phone.tools.JsonMapper;
import org.vertx.java.core.json.JsonObject;

public class Call {

	public static final String HANGUP_ACTION_COMMAND    = "hangup";
	public static final String PICKUP_ACTION_COMMAND    = "pickup";
	public static final String BUSY_HERE_ACTION_COMMAND = "busyhere";
	public static final String CLOSE_ACTION_COMMAND     = "close";

	private CallState state;

	public final CallState INIT;
	public final CallState UAC;
	public final CallState UAS;
	public final CallState RINGING;
	public final CallState SUCCESS;
	public final CallState FAILED;
	public final CallState REMOTE_HANGUP;
	public final CallState TERMINATED;

	private SipRequest sipRequest;
	private CallListener callFrameListener;
	private String callid;

	public Call(String remoteParty, String id, Logger logger) {
		this.callid = id;
		INIT = new CallStateInit(id, this, logger);
		UAC = new CallStateUac(id, this, logger);
		UAS = new CallStateUas(id, this, logger);
		RINGING = new CallStateRinging(id, this, logger);
		SUCCESS = new CallStateSuccess(id, this, logger);
		FAILED = new CallStateFailed(id, this, logger);
		REMOTE_HANGUP = new CallStateRemoteHangup(id, this, logger);
		TERMINATED = new CallStateTerminated(id, this, logger);
		state = INIT;
	}

	public void callClicked() {
		state.callClicked();

		HashMap<String,String> additional = new HashMap<String,String>();
		additional.put("callId",callid );
		Server.publishGui(JsonMapper.sipRequest("setSipRequest",getSipRequest(),additional));
	}

	public void incomingCall() {
		state.incomingCall();

		SipHeaderFieldValue from = getSipRequest().getSipHeaders().get(new SipHeaderFieldName(RFC3261.HDR_FROM));
		JsonObject jsonObject = JsonMapper.sipRequest("incomingCall",getSipRequest());
		jsonObject.putString("fromValue",from.getValue());
		jsonObject.putString("callId",Utils.getMessageCallId(getSipRequest()));
		Server.publishGui(jsonObject);
	}

	public void remoteHangup() {
		state.remoteHangup();

		Server.publishGui(JsonMapper.sipRequest("remoteHangup",sipRequest));
	}

	public void error(SipResponse sipResponse) {
		state.error(sipResponse);

		Server.publishGui(JsonMapper.sipResponse("error",sipResponse));
	}

	public void calleePickup(SipResponse sipResponse) {
		state.calleePickup();

		Server.publishGui(JsonMapper.sipResponse("calleePickup",sipResponse));
	}

	public void ringing(SipResponse sipResponse) {
		state.ringing();

		Server.publishGui(JsonMapper.sipResponse("ringing",sipResponse));
	}

	public void hangup() {
		if (callFrameListener != null) {
			callFrameListener.hangupClicked(sipRequest);
		}
	}

	public void pickup() {
		if (callFrameListener != null && sipRequest != null) {
			callFrameListener.pickupClicked(sipRequest);
		}
	}

	public void busyHere(){
		if (callFrameListener != null && sipRequest != null) {
			callFrameListener.busyHereClicked(sipRequest);
			sipRequest = null;
		}
	}

	public void setState(CallState state) {
		this.state.log(state);
		this.state = state;
	}

	public void setSipRequest(SipRequest sipRequest) {
		this.sipRequest = sipRequest;
	}


	public SipRequest getSipRequest() {
		return sipRequest;
	}

	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		Runnable runnable = null;
		if (HANGUP_ACTION_COMMAND.equals(actionCommand)) {
			runnable = new Runnable() {
				@Override
				public void run() {
					state.hangupClicked();
				}
			};
		} else if (CLOSE_ACTION_COMMAND.equals(actionCommand)) {
			runnable = new Runnable() {
				@Override
				public void run() {
					state.closeClicked();
				}
			};
		} else if (PICKUP_ACTION_COMMAND.equals(actionCommand)) {
			runnable = new Runnable() {
				public void run() {
					state.pickupClicked();
				}
			};
		} else if (BUSY_HERE_ACTION_COMMAND.equals(actionCommand)) {
			runnable = new Runnable() {
				@Override
				public void run() {
					state.busyHereClicked();
				}
			};
		}
		if (runnable != null) {
			SwingUtilities.invokeLater(runnable);
		}
	}
}
