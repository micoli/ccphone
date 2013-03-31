package org.micoli.phone.ccphone.remote;

import org.jboss.netty.channel.Channel;
import org.vertx.java.core.impl.Context;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.java.core.net.impl.DefaultNetSocket;

public class SecureNetSocket extends DefaultNetSocket{

	public SecureNetSocket(VertxInternal vertx, Channel channel, Context context) {
		super(vertx, channel, context);
	}


}
