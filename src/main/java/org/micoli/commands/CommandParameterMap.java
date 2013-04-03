package org.micoli.commands;

import java.util.HashMap;
import java.util.Map;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.StringParser;

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

	public Object getParam(String key, String className) {
		if (type.equals("jsapResult")) {
			switch (className) {
			case "String":
				return jsapResult.getString(key);
			case "byte":
				return jsapResult.getByte(key);
			case "short":
				return jsapResult.getShort(key);
			case "int":
				return jsapResult.getInt(key);
			case "long":
				return jsapResult.getLong(key);
			case "float":
				return jsapResult.getFloat(key);
			case "double":
				return jsapResult.getDouble(key);
			case "boolean":
				return jsapResult.getBoolean(key);
			case "char":
				return jsapResult.getChar(key);
			case "InetAddress":
				return jsapResult.getInetAddress(key);
			case "URL":
				return jsapResult.getURL(key);
			default:
				return jsapResult.getString(key);
			}
		}
		if (type.equals("hashResult")) {
			return hashResult.get(key).toString();
		}
		return null;
	}

	static StringParser getStringParserFromClassName(String className) {
		switch (className) {
		case "String":
			return JSAP.STRING_PARSER;
		case "byte":
			return JSAP.BYTE_PARSER;
		case "short":
			return JSAP.SHORT_PARSER;
		case "int":
			return JSAP.INTEGER_PARSER;
		case "long":
			return JSAP.LONG_PARSER;
		case "float":
			return JSAP.FLOAT_PARSER;
		case "double":
			return JSAP.DOUBLE_PARSER;
		case "boolean":
			return JSAP.BOOLEAN_PARSER;
		case "char":
			return JSAP.CHARACTER_PARSER;
		case "InetAddress":
			return JSAP.INETADDRESS_PARSER;
		case "URL":
			return JSAP.URL_PARSER;
		default:
			return JSAP.STRING_PARSER;
		}
	}
}
