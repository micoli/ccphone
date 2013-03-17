package org.micoli.phone.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.micoli.phone.ccphone.remote.VertX;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;


public class ActionManager {
	public static HashMap<String, Method> listCommand = new HashMap<String, Method>();

	public static void scan(final Object container, final ProxyLogger logger) {
		for (Method method : container.getClass().getMethods()) {
			if (method.isAnnotationPresent(Action.class)) {
				Action annotation = method.getAnnotation(Action.class);
				annotation.annotationType().toString();
				listCommand.put(method.getName(), method);
				final String MethodName = method.getName();
				logger.info("init guiaction." + method.getName());
				VertX.vertx.eventBus().registerHandler("guiaction."+method.getName(), new Handler<Message<JsonObject>>() {
					public void handle(Message<JsonObject> event) {
						try {
							logger.info("init guiaction." + MethodName + " " + event.body.encode());
							ActionManager.listCommand.get(MethodName).invoke(container, event);
						} catch (IllegalAccessException
								| IllegalArgumentException
								| InvocationTargetException e) {
							logger.error("handle guiaction " + MethodName + " " + event.body.encode(), e);
						}
					}
				});
			}
		}
	}
}
