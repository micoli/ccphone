package org.micoli.commands;

import java.lang.reflect.Method;

import com.martiansoftware.jsap.JSAP;

public class CommandContainer {
	public Object container;
	public Method method;
	public JSAP jsap;

	public CommandContainer(Method method2, Object container2, JSAP jsap2) {
		container = container2;
		method = method2;
		jsap = jsap2;
	}

	public Object getContainer() {
		return container;
	}

	public Method getMethod() {
		return method;
	}

	public JSAP getJsap() {
		return jsap;
	}
}