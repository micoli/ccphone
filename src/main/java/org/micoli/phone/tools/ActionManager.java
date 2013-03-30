package org.micoli.phone.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.micoli.phone.ccphone.remote.VertX;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;


/**
 * The Class ActionManager.
 */
public class ActionManager {
	/** The list command. */
	public static HashMap<String, Method> listGUICommand = new HashMap<String, Method>();
	public static HashMap<String, Method> listShellCommand = new HashMap<String, Method>();
	public static HashMap<String, Object> listShellContainer = new HashMap<String, Object>();

	public static void runShellCommand(String commandName, String args) {
		if(listShellCommand.containsKey(commandName)){
			try {
				System.out.println("SHELL >" + commandName);
				System.out.println("SHELL >" + ActionManager.listShellCommand.get(commandName));
				ActionManager.listShellCommand.get(commandName).invoke(ActionManager.listShellContainer.get(commandName), args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Scan.
	 *
	 * @param container the container
	 * @param logger the logger
	 */
	public static void scan(final Object container, final ProxyLogger logger) {
		for (Method method : container.getClass().getMethods()) {
			if (method.isAnnotationPresent(Action.class)) {
				Action annotation = method.getAnnotation(Action.class);
				annotation.annotationType().toString();
				final String MethodName = method.getName();

				HashSet<Action.Type> actions = new HashSet<Action.Type> (Arrays.asList(annotation.type()));

				if (actions.contains(Action.Type.SHELL)) {
					listShellCommand.put(MethodName, method);
					listShellContainer.put(MethodName, container);
				}
				if (actions.contains(Action.Type.GUI)) {
					listGUICommand.put(MethodName, method);
					logger.info("init guiaction." + method.getName());
					VertX.vertx.eventBus().registerHandler("guiaction." + method.getName(), new Handler<Message<JsonObject>>() {
						public void handle(Message<JsonObject> event) {
							try {
								logger.info("init guiaction." + MethodName + " " + event.body.encode());
								ActionManager.listGUICommand.get(MethodName).invoke(container, event);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								logger.error("handle guiaction " + MethodName + " " + event.body.encode(), e);
							}
						}
					});
				}
			}
		}
	}
}
