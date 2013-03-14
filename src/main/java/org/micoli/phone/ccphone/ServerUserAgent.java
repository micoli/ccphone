package org.micoli.phone.ccphone;

import java.net.SocketException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;

public class ServerUserAgent extends UserAgent {

	public ServerUserAgent(SipListener sipListener, String peersHome, Logger logger) throws SocketException {
		super(sipListener,  peersHome, logger);
	}

	public ServerUserAgent(SipListener sipListener, Config config, Logger logger) throws SocketException {
		super(sipListener, config, logger);
	}
}
