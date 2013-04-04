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

import net.sourceforge.peers.Logger;

import org.micoli.phone.ccphone.remote.VertX;
import org.vertx.java.core.json.JsonObject;

/**
 * The Class Registration.
 */
public class Registration {

	/** The unregistered. */
	public final RegistrationState UNREGISTERED;

	/** The registering. */
	public final RegistrationState REGISTERING;

	/** The success. */
	public final RegistrationState SUCCESS;

	/** The failed. */
	public final RegistrationState FAILED;

	/** The label. */
	protected String label;

	/** The state. */
	private RegistrationState state;

	/**
	 * Instantiates a new registration.
	 *
	 * @param label the label
	 * @param logger the logger
	 */
	public Registration(String label, Logger logger) {
		this.label = label;

		String id = String.valueOf(hashCode());

		UNREGISTERED = new RegistrationStateUnregsitered(id, this, logger);
		state = UNREGISTERED;
		REGISTERING = new RegistrationStateRegistering(id, this, logger);
		SUCCESS = new RegistrationStateSuccess(id, this, logger);
		FAILED = new RegistrationStateFailed(id, this, logger);
	}

	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(RegistrationState state) {
		this.state = state;
	}

	/**
	 * Register sent.
	 */
	public synchronized void registerSent() {
		state.registerSent();
	}

	/**
	 * Register failed.
	 */
	public synchronized void registerFailed() {
		state.registerFailed();
	}

	/**
	 * Register successful.
	 */
	public synchronized void registerSuccessful() {
		state.registerSuccessful();
	}

	/**
	 * Display registering.
	 */
	protected void displayRegistering() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", "displayRegistering");
		jsonObject.putString("icon", "working.gif");
		jsonObject.putString("text", "Registering");
		VertX.publishGui(jsonObject);
	}
}
