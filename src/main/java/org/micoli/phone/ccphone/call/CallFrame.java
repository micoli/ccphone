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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.micoli.phone.ccphone.call.state.CallFrameState;
import org.micoli.phone.ccphone.call.state.CallFrameStateFailed;
import org.micoli.phone.ccphone.call.state.CallFrameStateInit;
import org.micoli.phone.ccphone.call.state.CallFrameStateRemoteHangup;
import org.micoli.phone.ccphone.call.state.CallFrameStateRinging;
import org.micoli.phone.ccphone.call.state.CallFrameStateSuccess;
import org.micoli.phone.ccphone.call.state.CallFrameStateTerminated;
import org.micoli.phone.ccphone.call.state.CallFrameStateUac;
import org.micoli.phone.ccphone.call.state.CallFrameStateUas;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class CallFrame {

	public static final String HANGUP_ACTION_COMMAND    = "hangup";
	public static final String PICKUP_ACTION_COMMAND    = "pickup";
	public static final String BUSY_HERE_ACTION_COMMAND = "busyhere";
	public static final String CLOSE_ACTION_COMMAND     = "close";

	private CallFrameState state;

	public final CallFrameState INIT;
	public final CallFrameState UAC;
	public final CallFrameState UAS;
	public final CallFrameState RINGING;
	public final CallFrameState SUCCESS;
	public final CallFrameState FAILED;
	public final CallFrameState REMOTE_HANGUP;
	public final CallFrameState TERMINATED;

	private SipRequest sipRequest;
	private CallFrameListener callFrameListener;

	CallFrame(String remoteParty, String id, Logger logger) {
		INIT = new CallFrameStateInit(id, this, logger);
		UAC = new CallFrameStateUac(id, this, logger);
		UAS = new CallFrameStateUas(id, this, logger);
		RINGING = new CallFrameStateRinging(id, this, logger);
		SUCCESS = new CallFrameStateSuccess(id, this, logger);
		FAILED = new CallFrameStateFailed(id, this, logger);
		REMOTE_HANGUP = new CallFrameStateRemoteHangup(id, this, logger);
		TERMINATED = new CallFrameStateTerminated(id, this, logger);
		state = INIT;
	}

	public void callClicked() {
		state.callClicked();
	}

	public void incomingCall() {
		state.incomingCall();
	}

	public void remoteHangup() {
		state.remoteHangup();
	}

	public void error(SipResponse sipResponse) {
		state.error(sipResponse);
	}

	public void calleePickup() {
		state.calleePickup();
	}

	public void ringing() {
		state.ringing();
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

	public void setState(CallFrameState state) {
		this.state.log(state);
		this.state = state;
	}

	public void setSipRequest(SipRequest sipRequest) {
		this.sipRequest = sipRequest;
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
