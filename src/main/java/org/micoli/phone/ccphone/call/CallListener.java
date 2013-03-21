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

import net.sourceforge.peers.sip.transport.SipRequest;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving call events.
 * The class that is interested in processing a call
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addCallListener<code> method. When
 * the call event occurs, that object's appropriate
 * method is invoked.
 *
 * @see CallEvent
 */
public interface CallListener {

	/**
	 * Hangup action.
	 *
	 * @param sipRequest the sip request
	 */
	public void hangupAction(SipRequest sipRequest);
	
	/**
	 * Pickup action.
	 *
	 * @param sipRequest the sip request
	 */
	public void pickupAction(SipRequest sipRequest);
	
	/**
	 * Busy here action.
	 *
	 * @param sipRequest the sip request
	 */
	public void busyHereAction(SipRequest sipRequest);
	
	/**
	 * Dtmf.
	 *
	 * @param digit the digit
	 */
	public void dtmf(char digit);

}
