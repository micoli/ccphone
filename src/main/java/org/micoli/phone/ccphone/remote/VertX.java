package org.micoli.phone.ccphone.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.micoli.commands.CommandManager;
import org.micoli.phone.tools.ProxyLogger;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.core.sockjs.SockJSServer;

/**
 * The Class VertX.
 */
public class VertX {

	/** The logger. */
	static private ProxyLogger logger;

	/** The vertx. */
	static public Vertx vertx;

	/** The eb. */
	static private EventBus eb;

	/** The gui event address. */
	static private String guiEventAddress = "calls";

	/** The action address. */
	@SuppressWarnings("unused")
	static private String actionAddress = "guiaction";

	/** The http server. */
	static private HttpServer httpServer;

	/** The net server. */
	static private NetServer netServer;

	/**
	 * Publish gui.
	 *
	 * @param jsonObject the json object
	 */
	public static void publishGui(JsonObject jsonObject){
		eb.publish(guiEventAddress, jsonObject);
	}

	/**
	 * Inits the.
	 *
	 * @param prmLogger the prm logger
	 */
	public static void init(ProxyLogger prmLogger) {
		final HashMap <String,Boolean> logged = new HashMap <String,Boolean>();
		logger = prmLogger;

		vertx = Vertx.newVertx("localhost");
		eb = vertx.eventBus();

		Handler<Message<JsonObject>> myHandler = new Handler<Message<JsonObject>>() {
			public void handle(Message<JsonObject> message) {
				logger.info("VertX event due to registration : ["
						+ message.body.getString("text") + "]\n"
						+ message.body.toString());
			}
		};

		eb.registerHandler(guiEventAddress, myHandler);

		httpServer = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				logger.debug("HttpServerRequest path " + req.path);
				String internalPath = req.path;
				String documentRoot = "src/main/resources/eventbus";

				if (internalPath.equals("/pub")) {
					eb.publish(guiEventAddress, "test message published");
					req.response.sendFile("src/main/resources/eventbus/null.html");
				}
				if (internalPath.equals("/")){
					internalPath = "/index.html";
				}

				File file = new File(documentRoot+internalPath);
				if (!file.exists()){
					internalPath = "/404.html";
				}
				req.response.sendFile(documentRoot+internalPath);
			}
		});

		httpServer.websocketHandler(new Handler<ServerWebSocket>() {
			public void handle(final ServerWebSocket ws) {
				if (ws.path.equals("/eventbus")) {
					ws.closedHandler(new Handler<Void>() {
						public void handle(Void event) {
							// removeDispatch(ws.binaryHandlerID, ws);
						}
					});
					ws.dataHandler(new Handler<Buffer>() {
						public void handle(Buffer data) {
							eb.publish(guiEventAddress, data);
							// ws.writeTextFrame(data.toString());
						}
					});
				} else {
					ws.reject();
				}
			}
		});

		netServer = vertx.createNetServer().connectHandler(new Handler<NetSocket>() {
			public void handle(final NetSocket socket) {
				socket.write("> ");
				socket.dataHandler(new Handler<Buffer>() {
					public void handle(Buffer buffer) {
						String commandStr = buffer.toString().replace("\n", "").replace("\r", "");
						String commands[] = commandStr.split(" ");

						if (commands[0].equalsIgnoreCase("login")) {
							if(logged.containsKey(socket.writeHandlerID)){
								socket.write("Already Logged in\n");
							}else{
								if(CommandManager.runLoginCommand(commandStr.substring(commands[0].length()).trim())){
									logged.put(socket.writeHandlerID,true);
									socket.write("Logged in\n");
								}else{
									socket.write("Can not Logged in\n");
								}
							}
						} else if (commands[0].equalsIgnoreCase("exit")) {
							socket.close();
						} else {
							if(logged.containsKey(socket.writeHandlerID)){
								writeArrayListSocket(socket,CommandManager.runShellCommand(commands[0], commandStr.substring(commands[0].length()).trim()));
							}else{
								socket.write("Not Logged in\n");
							}
						}
						socket.write("> ");
					}
				});
			}
		});
		// .setSSL(true).setKeyStorePath("httpServer.jks").setKeyStorePassword("ccphone")

		JsonArray permitted = new JsonArray();
		permitted.add(new JsonObject());
		SockJSServer sockJSServer = vertx.createSockJSServer(httpServer);
		sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"),permitted, permitted);
	}

	private static void writeArrayListSocket(NetSocket socket,ArrayList<String> lines){
		Iterator<String> iterator = lines.iterator();
		while (iterator.hasNext()) {
			socket.write(iterator.next()+"\n");
		}
	}
	/**
	 * Run.
	 */
	public static void run(){
		logger.info("start");
		httpServer.listen(8080);
		netServer.listen(8081);
	}
}
