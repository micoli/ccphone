package org.micoli.phone.tools;

import net.sourceforge.peers.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class ProxyLogger.
 */
public class ProxyLogger extends Logger {

	/**
	 * Instantiates a new proxy logger.
	 *
	 * @param peersHome the peers home
	 */
	public ProxyLogger(String peersHome) {
		super(peersHome);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.peers.Logger#debug(java.lang.String)
	 */
	public void debug(String message) {
		System.out.println("DEBUG " + message);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.peers.Logger#info(java.lang.String)
	 */
	public void info(String message) {
		System.out.println("INFO " + message);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.peers.Logger#error(java.lang.String)
	 */
	public void error(String message) {
		System.out.println("ERROR " + message);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.peers.Logger#error(java.lang.String, java.lang.Exception)
	 */
	public void error(String message, Exception exception) {
		System.out.println("ERROR " + message);
		exception.printStackTrace();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.peers.Logger#traceNetwork(java.lang.String, java.lang.String)
	 */
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
