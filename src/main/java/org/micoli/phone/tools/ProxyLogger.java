package org.micoli.phone.tools;

import net.sourceforge.peers.Logger;

public class ProxyLogger extends Logger {

	public ProxyLogger(String peersHome) {
		super(peersHome);
	}

	public void debug(String message) {
		System.out.println("DEBUG " + message);
	}

	public void info(String message) {
		System.out.println("INFO " + message);
	}

	public void error(String message) {
		System.out.println("ERROR " + message);
	}

	public void error(String message, Exception exception) {
		System.out.println("ERROR " + message);
		exception.printStackTrace();
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
