package org.micoli.phone.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.vertx.java.core.json.JsonObject;

public class JsonMapper {
	private static void addAdditionnal(JsonObject jsonObject, HashMap<String, String> additional) {
		Iterator<Entry<String, String>> it = additional.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
			jsonObject.putString(pairs.getKey(), pairs.getValue());
		}

	}

	public static JsonObject sipRequest(String eventName, SipRequest sipRequest) {
		return JsonMapper.sipRequest(eventName, sipRequest, new HashMap<String, String>());
	}

	public static JsonObject sipRequest(String eventName,SipRequest sipRequest,HashMap<String,String> additional){
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", eventName);
		jsonObject.putString("method", sipRequest.getMethod());
		jsonObject.putString("requestUri", sipRequest.getRequestUri().toString());
		jsonObject.putString("requestUriHost", sipRequest.getRequestUri().getHost());
		jsonObject.putString("requestUriUserInfo", sipRequest.getRequestUri().getUserinfo());
		jsonObject.putNumber("requestUriport", sipRequest.getRequestUri().getPort());
		JsonMapper.addAdditionnal(jsonObject, additional);
		return jsonObject;
	}

	public static JsonObject sipResponse(String eventName, SipResponse sipResponse) {
		return JsonMapper.sipResponse(eventName, sipResponse, new HashMap<String, String>());
	}

	public static JsonObject sipResponse(String eventName, SipResponse sipResponse, HashMap<String, String> additional) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", eventName);
		jsonObject.putString("reasonPhrase", sipResponse.getReasonPhrase());
		jsonObject.putString("sipVersion", sipResponse.getSipVersion());
		jsonObject.putString("sipHeaders", sipResponse.getSipHeaders().toString());
		jsonObject.putNumber("statusCode", sipResponse.getStatusCode());
		JsonMapper.addAdditionnal(jsonObject, additional);
		return jsonObject;
	}
}
