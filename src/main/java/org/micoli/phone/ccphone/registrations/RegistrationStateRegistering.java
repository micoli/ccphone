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

import org.micoli.phone.ccphone.remote.Server;
import org.vertx.java.core.json.JsonObject;

public class RegistrationStateRegistering extends RegistrationState {

	public RegistrationStateRegistering(String id, Registration registration, Logger logger) {
		super(id, registration, logger);
	}

	@Override
	public void registerSuccessful() {
		registration.setState(registration.SUCCESS);
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", "registerSuccessful");
		jsonObject.putString("icon", "green.png");
		jsonObject.putString("text", "Registered");
		Server.publishGui(jsonObject);
	}

	@Override
	public void registerFailed() {
		registration.setState(registration.FAILED);
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", "registerFailed");
		jsonObject.putString("icon", "red.png");
		jsonObject.putString("text", "Registration failed");
		Server.publishGui(jsonObject);
	}

}
