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

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.micoli.phone.ccphone.remote.VertX;
import org.micoli.phone.tools.JsonMapper;
public class Call {

	/** The state. */
	private String state;

	/** The sip request. */
	private SipRequest sipRequest;

	/** The call id. */
	private String callId;

	/**
	 * Instantiates a new call.
	 *
	 * @param remoteParty the remote party
	 * @param id the id
	 * @param logger the logger
	 */
	public Call(String remoteParty, String id, Logger logger) {
		this.callId = id;
	}

	/**
	 * Call action.
	 */
	public void callAction() {
		setState("callAction");
		HashMap<String,String> additional = new HashMap<String,String>();
		additional.put("callId",callId );
		VertX.publishGui(JsonMapper.sipRequest("setSipRequest",getSipRequest(),additional));
	}

	/**
	 * Gets the callid.
	 *
	 * @return the callid
	 */
	public String getCallid() {
		return callId;
	}

	/**
	 * Gets the call state.
	 *
	 * @return the call state
	 */
	public String getCallState() {
		return state;
	}

	/**
	 * Incoming call.
	 */
	public void incomingCall() {
		setState("incomingCall");

		//SipHeaderFieldValue from = getSipRequest().getSipHeaders().get(new SipHeaderFieldName(RFC3261.HDR_FROM));
		//JsonObject jsonObject = JsonMapper.sipRequest("incomingCall",getSipRequest());
		//jsonObject.putString("fromValue",from.getValue());
		//jsonObject.putString("callId",Utils.getMessageCallId(getSipRequest()));
		//VertX.publishGui(jsonObject);
		VertX.publishGui(JsonMapper.sipRequest("incomingCall",getSipRequest()));
	}

	/**
	 * Remote hangup.
	 */
	public void remoteHangup() {
		setState("remoteHangup");

		VertX.publishGui(JsonMapper.sipRequest("remoteHangup",sipRequest));
	}

	/**
	 * Error.
	 *
	 * @param sipResponse the sip response
	 */
	public void error(SipResponse sipResponse) {
		setState("error");// (sipResponse);

		VertX.publishGui(JsonMapper.sipResponse("error",sipResponse));
	}

	/**
	 * Callee pickup.
	 *
	 * @param sipResponse the sip response
	 */
	public void calleePickup(SipResponse sipResponse) {
		setState("calleePickup");
		//JsonObject jsonObject = JsonMapper.sipResponse("calleePickup",sipResponse);
		//jsonObject.putString("callId",Utils.getMessageCallId(sipResponse));
		//VertX.publishGui(jsonObject);
		VertX.publishGui(JsonMapper.sipResponse("calleePickup",sipResponse));
	}

	/**
	 * Ringing.
	 *
	 * @param sipResponse the sip response
	 */
	public void ringing(SipResponse sipResponse) {
		setState("ringing");
		//JsonObject jsonObject = JsonMapper.sipResponse("ringing",sipResponse);
		//jsonObject.putString("callId",Utils.getMessageCallId(sipResponse));
		//VertX.publishGui(jsonObject);
		VertX.publishGui(JsonMapper.sipResponse("ringing",sipResponse));
	}

	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(String state) {
		// this.state.log(state);
		this.state = state;
	}

	/**
	 * Sets the sip request.
	 *
	 * @param sipRequest the new sip request
	 */
	public void setSipRequest(SipRequest sipRequest) {
		this.sipRequest = sipRequest;
	}


	/**
	 * Gets the sip request.
	 *
	 * @return the sip request
	 */
	public SipRequest getSipRequest() {
		return sipRequest;
	}

	/**
	 * Command performed.
	 *
	 * @param e the e
	 */
	public void actionPerformed(ActionEvent e) {
	}
}
