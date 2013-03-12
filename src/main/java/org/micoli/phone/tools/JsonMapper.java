package org.micoli.phone.tools;

import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.vertx.java.core.json.JsonObject;

public class JsonMapper {
	public static JsonObject sipRequest(String eventName,SipRequest sipRequest){
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", eventName);
		jsonObject.putString("method", sipRequest.getMethod());
		jsonObject.putString("requestUri", sipRequest.getRequestUri().toString());
		jsonObject.putString("requestUriHost", sipRequest.getRequestUri().getHost());
		jsonObject.putString("requestUriUserInfo", sipRequest.getRequestUri().getUserinfo());
		jsonObject.putNumber("requestUriport", sipRequest.getRequestUri().getPort());
		return jsonObject;
	}

	public static JsonObject sipResponse(String eventName, SipResponse sipResponse) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", eventName);
		jsonObject.putString("reasonPhrase", sipResponse.getReasonPhrase());
		jsonObject.putString("sipVersion", sipResponse.getSipVersion());
		jsonObject.putString("sipHeaders", sipResponse.getSipHeaders().toString());
		jsonObject.putNumber("statusCode", sipResponse.getStatusCode());
		return jsonObject;
	}
}
