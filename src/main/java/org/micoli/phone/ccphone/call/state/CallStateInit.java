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

import org.micoli.phone.ccphone.call.Call;

public class CallStateInit extends CallState {

	public CallStateInit(String id, Call call, Logger logger) {
		super(id, call, logger);
	}

	@Override
	public void callAction() {
		call.setState(call.UAC);
		//JFrame frame = call.getFrame();
		//call.setCallPanel(call.UAC.callPanel);
		//frame.setVisible(true);
	}

	@Override
	public void incomingCall() {
		call.setState(call.UAS);
		//JFrame frame = call.getFrame();
		//call.setCallPanel(call.UAS.callPanel);
		//frame.setVisible(true);
	}

}
