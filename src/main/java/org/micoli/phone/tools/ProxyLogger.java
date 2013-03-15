package org.micoli.phone.tools;

import net.sourceforge.peers.Logger;

public class ProxyLogger extends Logger {

	public ProxyLogger(String peersHome) {
		super(peersHome);
	}

	public void debug(String message) {
	}

	public void info(String message) {
	}

	public void error(String message) {
	}

	public void error(String message, Exception exception) {
	}

	private String genericLog(String message, String level) {
		return "";
	}

	public void traceNetwork(String message, String direction) {
		/*
		 * synchronized (networkMutex) { StringBuffer buf = new StringBuffer();
		 * buf.append(networkFormatter.format(new Date())); buf.append(" ");
		 * buf.append(direction); buf.append(" [");
		 * buf.append(Thread.currentThread().getName()); buf.append("]\n\n");
		 * buf.append(message); buf.append("\n");
		 * networkWriter.write(buf.toString()); networkWriter.flush(); }
		 */
	}

}
