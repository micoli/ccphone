package org.micoli.phone.ccphone.remote;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.micoli.phone.ccphone.remote.messages.PingPong.Ping;
import org.micoli.phone.ccphone.remote.messages.PingPong.PingPongService;
import org.micoli.phone.ccphone.remote.messages.PingPong.PingPongService.BlockingInterface;
import org.micoli.phone.ccphone.remote.messages.PingPong.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BlockingService;
import com.google.protobuf.ByteString;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientBootstrap;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerBootstrap;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutExecutor;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutExecutor;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;

public class Server {
	private static Logger log = LoggerFactory.getLogger("server");

	public static void run() {
		String serverHostname = "localhost";// args[1];
		int serverPort = 9001;// Integer.parseInt(args[2]);
		boolean secure = false;// "Y".equals(args[3]);
		boolean nodelay = true;// "Y".equals(args[4]);
		long runDuration = 0;

		log.info("DuplexPingPongServer " + serverHostname + ":"+ serverPort +" ssl=" + (secure?"Y":"N") + " nodelay=" + (nodelay?"Y":"N"));

		PeerInfo serverInfo = new PeerInfo(serverHostname, serverPort);

		RpcServerCallExecutor executor = new ThreadPoolCallExecutor(3, 200);

		DuplexTcpServerBootstrap bootstrap = new DuplexTcpServerBootstrap(serverInfo, new NioServerSocketChannelFactory(Executors.newCachedThreadPool(new RenamingThreadFactoryProxy("boss", Executors.defaultThreadFactory())), Executors.newCachedThreadPool(new RenamingThreadFactoryProxy("worker", Executors.defaultThreadFactory()))));
		bootstrap.setRpcServerCallExecutor(executor);

		/*if (secure) {
			RpcSSLContext sslCtx = new RpcSSLContext();
			sslCtx.setKeystorePassword("changeme");
			sslCtx.setKeystorePath("./lib/server.keystore");
			sslCtx.setTruststorePassword("changeme");
			sslCtx.setTruststorePath("./lib/truststore");
			sslCtx.init();

			bootstrap.setSslContext(sslCtx);
		}*/
		bootstrap.setOption("sendBufferSize", 1048576);
		bootstrap.setOption("receiveBufferSize", 1048576);
		bootstrap.setOption("child.receiveBufferSize", 1048576);
		bootstrap.setOption("child.sendBufferSize", 1048576);
		bootstrap.setOption("tcpNoDelay", nodelay);

		RpcTimeoutExecutor timeoutExecutor = new TimeoutExecutor(1, 5);
		RpcTimeoutChecker timeoutChecker = new TimeoutChecker();
		timeoutChecker.setTimeoutExecutor(timeoutExecutor);
		timeoutChecker.startChecking(bootstrap.getRpcClientRegistry());

		CleanShutdownHandler shutdownHandler = new CleanShutdownHandler();
		shutdownHandler.addResource(bootstrap);
		shutdownHandler.addResource(executor);
		shutdownHandler.addResource(timeoutChecker);
		shutdownHandler.addResource(timeoutExecutor);

		// setup a RPC event listener - it just logs what happens
		RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();
		RpcConnectionEventListener listener = new RpcConnectionEventListener() {

			public void connectionReestablished(RpcClientChannel clientChannel) {
				log.info("connectionReestablished " + clientChannel);
			}

			public void connectionOpened(RpcClientChannel clientChannel) {
				log.info("connectionOpened " + clientChannel);
			}

			public void connectionLost(RpcClientChannel clientChannel) {
				log.info("connectionLost " + clientChannel);
			}

			public void connectionChanged(RpcClientChannel clientChannel) {
				log.info("connectionChanged " + clientChannel);
			}
		};
		rpcEventNotifier.setEventListener(listener);
		bootstrap.registerConnectionEventListener(rpcEventNotifier);

		BlockingService bPingService = PingPongService.newReflectiveBlockingService(new PingPongService.BlockingInterface() {
			@Override
			public Pong ping(RpcController controller, Ping request) throws ServiceException {
				Pong.Builder pongBuilder = Pong.newBuilder();
				byte[] payload = new byte[4];
				for (int i = 0; i < payload.length; i++) {
					payload[i] = (byte) (i % 10);
				}
				pongBuilder.setPongData(ByteString.copyFrom(payload));
				return pongBuilder.build();
			}

			@Override
			public Pong fail(RpcController controller, Ping request) throws ServiceException {
				return null;
			}
		});
		bootstrap.getRpcServiceRegistry().registerBlockingService(bPingService);

		bootstrap.bind();

		System.out.println("Serving " + serverInfo);

		PeerInfo client = new PeerInfo("clientHostname", 1234);
		ThreadPoolCallExecutor executor2 = new ThreadPoolCallExecutor(3, 10);
		DuplexTcpClientBootstrap bootstrap2 = new DuplexTcpClientBootstrap(client, new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		RpcClientChannel channel;
		try {
			channel = bootstrap2.peerWith(serverInfo);

			BlockingInterface pingpongService = PingPongService.newBlockingStub(channel);
			RpcController controller = channel.newRpcController();
			if (runDuration > 0) {
				try {
					Thread.sleep(runDuration);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.exit(0);
			} else {
				while (true) {
					try {
						System.out.println("Sleeping 60s before retesting clients.");
						// System.out.println(serverInfo);
						Thread.sleep(1000);

						Ping.Builder pingBuilder = Ping.newBuilder();
						pingBuilder.setProcessingTime(200);
						pingBuilder.setPongDataLength(100);
						Ping ping = pingBuilder.build();
						Pong pong = pingpongService.ping(controller, ping);

						System.out.println(pong);
						System.out.println("eeeeeee");
						//new ShortTests().execute(bootstrap.getRpcClientRegistry());
					} catch (Throwable e) {
						System.out.println(e);
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
