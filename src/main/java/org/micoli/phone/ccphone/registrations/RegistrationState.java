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

package org.micoli.phone.ccphone.registrations;

import net.sourceforge.peers.sip.AbstractState;

import net.sourceforge.peers.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class RegistrationState.
 */
public abstract class RegistrationState extends AbstractState {

	/** The registration. */
	protected Registration registration;

	/**
	 * Instantiates a new registration state.
	 *
	 * @param id the id
	 * @param registration the registration
	 * @param logger the logger
	 */
	public RegistrationState(String id, Registration registration, Logger logger) {
		super(id, logger);
		this.registration = registration;
	}

	/**
	 * Register sent.
	 */
	public void registerSent() {}
	
	/**
	 * Register successful.
	 */
	public void registerSuccessful() {}
	
	/**
	 * Register failed.
	 */
	public void registerFailed() {}

}
