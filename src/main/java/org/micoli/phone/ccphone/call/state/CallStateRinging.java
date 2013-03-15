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

import net.sourceforge.peers.sip.transport.SipResponse;

import org.micoli.phone.ccphone.call.Call;
import org.micoli.phone.tools.ProxyLogger;

public class CallStateRinging extends CallState {

	public CallStateRinging(String id, Call call, ProxyLogger logger) {
		super(id, call, logger);
	}

	@Override
	public void hangupAction() {
		call.setState(call.TERMINATED);
		call.hangup();
	}

	@Override
	public void calleePickup() {
		call.setState(call.SUCCESS);
		//call.setCallPanel(call.SUCCESS.callPanel);
	}

	@Override
	public void error(SipResponse sipResponse) {
		call.setState(call.FAILED);
		//call.setCallPanel(call.FAILED.callPanel);
		//call.addPageEndLabel("Reason: "+ sipResponse.getReasonPhrase());
	}

}
