package org.micoli.phone.ccphone.remote;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.vertx.java.core.sockjs.SockJSServer;

public class Server {
	private static Logger log = LoggerFactory.getLogger("server");
	static Vertx vertx;
	static ObjectMapper mapper = new ObjectMapper();
	static EventBus eb;
	static String guiEventAddress = "topic";
	static String actionAddress = "action";

	public static void publishGui(JsonObject jsonObject){
		eb.publish(guiEventAddress, jsonObject);
	}

	public static void run() {
		log.info("start");
		vertx = Vertx.newVertx("localhost");
		eb = vertx.eventBus();

		Handler<Message<JsonObject>> myHandler = new Handler<Message<JsonObject>>() {
			public void handle(Message<JsonObject> message) {
				System.out.println("Server event due to registration : ["
						+ message.body.getString("text") + "]\n"
						+ message.body.toString());// + " "+
													// event.replyAddress);
			}
		};

		eb.registerHandler(guiEventAddress, myHandler);

		HttpServer server = vertx.createHttpServer();

		server.requestHandler(new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				System.out.println("path " + req.path);
				if (req.path.equals("/pub")) {
					eb.publish(guiEventAddress, "test message published");
					req.response.sendFile("src/main/resources/eventbus/null.html");
				}

				if (req.path.equals("/"))
					req.response.sendFile("src/main/resources/eventbus/index.html");

				if (req.path.equals("/vertxbusws.js"))
					req.response.sendFile("src/main/resources/eventbus/vertxbusws.js");

				if (req.path.equals("/indexws.html"))
					req.response.sendFile("src/main/resources/eventbus/indexws.html");

				if (req.path.endsWith("/vertxbus.js"))
					req.response.sendFile("src/main/resources/eventbus/vertxbus.js");

				if (req.path.endsWith("/ws.html"))
					req.response.sendFile("src/main/resources/eventbus/ws.html");
			}
		}).websocketHandler(new Handler<ServerWebSocket>() {
			public void handle(final ServerWebSocket ws) {
				if (ws.path.equals("/eventbus")) {
					ws.closedHandler(new Handler<Void>() {
						public void handle(Void event) {
							// removeDispatch(ws.binaryHandlerID, ws);
						}
					});
					ws.dataHandler(new Handler<Buffer>() {
						public void handle(Buffer data) {
							System.out.println("websocket " + data.toString());
							eb.publish(guiEventAddress, data);
							// ws.writeTextFrame(data.toString());
						}
					});
				} else {
					ws.reject();
				}
			}
		});

		JsonArray permitted = new JsonArray();
		permitted.add(new JsonObject());
		SockJSServer sockJSServer = vertx.createSockJSServer(server);
		sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"),
				permitted, permitted);

		server.listen(8080);
	}
}
