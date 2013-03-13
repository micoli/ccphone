package org.micoli.phone.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.vertx.java.core.json.JsonObject;

public class JsonMapper {
	public static JsonObject sipRequest(String eventName,SipRequest sipRequest,HashMap<String,String> additional){
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", eventName);
		jsonObject.putString("method", sipRequest.getMethod());
		jsonObject.putString("requestUri", sipRequest.getRequestUri().toString());
		jsonObject.putString("requestUriHost", sipRequest.getRequestUri().getHost());
		jsonObject.putString("requestUriUserInfo", sipRequest.getRequestUri().getUserinfo());
		jsonObject.putNumber("requestUriport", sipRequest.getRequestUri().getPort());
		Iterator it = additional.entrySet().iterator();
		while (it.hasNext()) {
			@SuppressWarnings("unchecked")
			Map.Entry<String,String> pairs = (Map.Entry<String,String>)it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
			jsonObject.putString(pairs.getKey(),pairs.getValue());
		}
		return jsonObject;
	}
	public static JsonObject sipRequest(String eventName,SipRequest sipRequest){
		return JsonMapper.sipRequest(eventName,sipRequest,new HashMap<String,String>());
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
