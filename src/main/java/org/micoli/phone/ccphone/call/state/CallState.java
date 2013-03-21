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

package org.micoli.phone.ccphone.call.state;

import net.sourceforge.peers.sip.AbstractState;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.micoli.phone.ccphone.call.Call;
import org.micoli.phone.tools.ProxyLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class CallState.
 */
public abstract class CallState extends AbstractState {

	/** The call. */
	protected Call call;

	/**
	 * Instantiates a new call state.
	 *
	 * @param id the id
	 * @param call the call
	 * @param logger the logger
	 */
	public CallState(String id, Call call, ProxyLogger logger) {
		super(id, logger);
		this.call = call;
	}

	/**
	 * Call action.
	 */
	public void callAction() {}
	
	/**
	 * Incoming call.
	 */
	public void incomingCall() {}
	
	/**
	 * Callee pickup.
	 */
	public void calleePickup() {}
	
	/**
	 * Error.
	 *
	 * @param sipResponse the sip response
	 */
	public void error(SipResponse sipResponse) {}
	
	/**
	 * Pickup action.
	 */
	public void pickupAction() {}
	
	/**
	 * Busy here action.
	 */
	public void busyHereAction() {}
	
	/**
	 * Hangup action.
	 */
	public void hangupAction() {}
	
	/**
	 * Remote hangup.
	 */
	public void remoteHangup() {}
	
	/**
	 * Close action.
	 */
	public void closeAction() {}
	
	/**
	 * Ringing.
	 */
	public void ringing() {}

}
