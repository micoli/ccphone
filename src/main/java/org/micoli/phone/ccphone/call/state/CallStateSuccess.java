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

import org.micoli.phone.ccphone.call.Call;

import net.sourceforge.peers.Logger;

public class CallStateSuccess extends CallState {

	public CallStateSuccess(String id, Call call, Logger logger) {
		super(id, call, logger);
	}

	@Override
	public void remoteHangup() {
		call.setState(call.REMOTE_HANGUP);
		//call.setCallPanel(call.REMOTE_HANGUP.callPanel);
	}

	@Override
	public void hangupClicked() {
		call.setState(call.TERMINATED);
		call.hangup();
	}

}