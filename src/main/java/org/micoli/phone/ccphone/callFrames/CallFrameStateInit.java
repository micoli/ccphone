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

package org.micoli.phone.ccphone.callFrames;

import javax.swing.JFrame;

import org.micoli.phone.ccphone.remote.Server;
import org.vertx.java.core.json.JsonObject;

import net.sourceforge.peers.Logger;

public class CallFrameStateInit extends CallFrameState {

	public CallFrameStateInit(String id, CallFrame callFrame, Logger logger) {
		super(id, callFrame, logger);
	}

	@Override
	public void callClicked() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", "callClicked");
		Server.publishGui(jsonObject);
		//callFrame.setState(callFrame.UAC);
		//JFrame frame = callFrame.getFrame();
		//callFrame.setCallPanel(callFrame.UAC.callPanel);
		//frame.setVisible(true);
	}

	@Override
	public void incomingCall() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", "incomingCall");
		Server.publishGui(jsonObject);
		//callFrame.setState(callFrame.UAS);
		//JFrame frame = callFrame.getFrame();
		//callFrame.setCallPanel(callFrame.UAS.callPanel);
		//frame.setVisible(true);
	}

}
