package org.micoli.commands;

import java.lang.reflect.Method;

import com.martiansoftware.jsap.JSAP;

public class CommandContainer {
	private Object container;
	private Method method;
	private JSAP jsap;
	private int nbArg;

	public CommandContainer(Method method2, Object container2) {
		container = container2;
		method = method2;
	}

	public CommandContainer(Method method2, Object container2, JSAP jsap2, int nbArg2) {
		container = container2;
		method = method2;
		jsap = jsap2;
		nbArg = nbArg2;
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

	public int getNbArg() {
		return nbArg;
	}
}