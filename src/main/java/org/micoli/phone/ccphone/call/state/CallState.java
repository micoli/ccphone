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

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.AbstractState;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.micoli.phone.ccphone.call.Call;

public abstract class CallState extends AbstractState {

	protected Call call;

	public CallState(String id, Call call, Logger logger) {
		super(id, logger);
		this.call = call;
	}

	public void callClicked() {}
	public void incomingCall() {}
	public void calleePickup() {}
	public void error(SipResponse sipResponse) {}
	public void pickupClicked() {}
	public void busyHereClicked() {}
	public void hangupClicked() {}
	public void remoteHangup() {}
	public void closeClicked() {}
	public void ringing() {}

}
