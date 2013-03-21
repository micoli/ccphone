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
import org.micoli.phone.tools.ProxyLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class CallStateInit.
 */
public class CallStateInit extends CallState {

	/**
	 * Instantiates a new call state init.
	 *
	 * @param id the id
	 * @param call the call
	 * @param logger the logger
	 */
	public CallStateInit(String id, Call call, ProxyLogger logger) {
		super(id, call, logger);
	}

	/* (non-Javadoc)
	 * @see org.micoli.phone.ccphone.call.state.CallState#callAction()
	 */
	@Override
	public void callAction() {
		call.setState(call.UAC);
		//JFrame frame = call.getFrame();
		//call.setCallPanel(call.UAC.callPanel);
		//frame.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see org.micoli.phone.ccphone.call.state.CallState#incomingCall()
	 */
	@Override
	public void incomingCall() {
		call.setState(call.UAS);
		//JFrame frame = call.getFrame();
		//call.setCallPanel(call.UAS.callPanel);
		//frame.setVisible(true);
	}

}
