package org.micoli.phone.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.vertx.java.core.json.JsonObject;

// TODO: Auto-generated Javadoc
/**
 * The Class JsonMapper.
 */
public class JsonMapper {
	
	/**
	 * Adds the additionnal.
	 *
	 * @param jsonObject the json object
	 * @param additional the additional
	 */
	private static void addAdditionnal(JsonObject jsonObject, HashMap<String, String> additional) {
		Iterator<Entry<String, String>> it = additional.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
			jsonObject.putString(pairs.getKey(), pairs.getValue());
		}

	}

	/**
	 * Sip request.
	 *
	 * @param eventName the event name
	 * @param sipRequest the sip request
	 * @return the json object
	 */
	public static JsonObject sipRequest(String eventName, SipRequest sipRequest) {
		return JsonMapper.sipRequest(eventName, sipRequest, new HashMap<String, String>());
	}

	/**
	 * Sip request.
	 *
	 * @param eventName the event name
	 * @param sipRequest the sip request
	 * @param additional the additional
	 * @return the json object
	 */
	public static JsonObject sipRequest(String eventName,SipRequest sipRequest,HashMap<String,String> additional){
		SipHeaderFieldValue from = sipRequest.getSipHeaders().get(new SipHeaderFieldName(RFC3261.HDR_FROM));
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", eventName);
		jsonObject.putString("method", sipRequest.getMethod());
		jsonObject.putString("requestUri", sipRequest.getRequestUri().toString());
		jsonObject.putString("requestUriHost", sipRequest.getRequestUri().getHost());
		jsonObject.putString("requestUriUserInfo", sipRequest.getRequestUri().getUserinfo());
		jsonObject.putNumber("requestUriport", sipRequest.getRequestUri().getPort());
		jsonObject.putString("callId",Utils.getMessageCallId(sipRequest));
		if(from!=null){
			jsonObject.putString("fromValue",from.getValue());
		}

		JsonMapper.addAdditionnal(jsonObject, additional);
		return jsonObject;
	}

	/**
	 * Sip response.
	 *
	 * @param eventName the event name
	 * @param sipResponse the sip response
	 * @return the json object
	 */
	public static JsonObject sipResponse(String eventName, SipResponse sipResponse) {
		return JsonMapper.sipResponse(eventName, sipResponse, new HashMap<String, String>());
	}

	/**
	 * Sip response.
	 *
	 * @param eventName the event name
	 * @param sipResponse the sip response
	 * @param additional the additional
	 * @return the json object
	 */
	public static JsonObject sipResponse(String eventName, SipResponse sipResponse, HashMap<String, String> additional) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.putString("eventName", eventName);
		jsonObject.putString("reasonPhrase", sipResponse.getReasonPhrase());
		jsonObject.putString("sipVersion", sipResponse.getSipVersion());
		jsonObject.putString("sipHeaders", sipResponse.getSipHeaders().toString());
		jsonObject.putNumber("statusCode", sipResponse.getStatusCode());
		jsonObject.putString("callId",Utils.getMessageCallId(sipResponse));
		JsonMapper.addAdditionnal(jsonObject, additional);
		return jsonObject;
	}
}
