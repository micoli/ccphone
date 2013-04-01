package org.micoli.commands;

import java.util.HashMap;
import java.util.Map;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;

public class CommandParameterMap {
	private String type;
	private JSAP jsap;
	private JSAPResult jsapResult;
	private HashMap<String, Object> hashResult;

	@SuppressWarnings("unchecked")
	public CommandParameterMap(JSAP jsap, Object args) {
		this.jsap = jsap;
		if (args instanceof String) {
			this.jsap = jsap;
			type = "jsapResult";
			jsapResult = this.jsap.parse((String) args);
		}
		if (args instanceof Map<?, ?>) {
			type = "hashResult";
			this.hashResult = (HashMap<String, Object>) args;
		}
	}

	public boolean contains(String key) {
		if (type.equals("jsapResult")) {
			return jsapResult.contains(key);
		}
		if (type.equals("hashResult")) {
			return hashResult.containsKey(key);
		}
		return false;
	}

	public String getString(String key) {
		if (type.equals("jsapResult")) {
			return jsapResult.getString(key);
		}
		if (type.equals("hashResult")) {
			return hashResult.get(key).toString();
		}
		return null;
	}
}
