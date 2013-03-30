package org.micoli.phone.tools;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.micoli.phone.ccphone.remote.VertX;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

/**
 * The Class ActionManager.
 */
public class ActionManager {
	/** The list command. */
	public static HashMap<String, Method> listGUICommand = new HashMap<String, Method>();
	public static HashMap<String, Method> listShellCommand = new HashMap<String, Method>();
	public static HashMap<String, Object> listShellContainer = new HashMap<String, Object>();

	public static void runShellCommand(String commandName, String args) {
		if (listShellCommand.containsKey(commandName)) {
			Method method = ActionManager.listShellCommand.get(commandName);
			Object container = ActionManager.listShellContainer.get(commandName);
			try {
				JSAP jsap = new JSAP();
				Map<String, Object> map = new HashMap<String,Object>();
				if(commandName.equals("testAction")){
					args="--a1=baaaa1";
				}
				if(commandName.equals("testAction2")){
					args="--a1 baaaa1 --a2 baaaa2";
				}
				if(commandName.equals("testAction3")){
					args="--a2=baaaa1 --a1 baaaa2 --a3=baaaa3";
				}

				final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
				final Object[] param = new Object[parameterAnnotations.length];

				for (int i = 0; i < parameterAnnotations.length; i++) {
					for (final Annotation annotation : parameterAnnotations[i]) {
						if (annotation instanceof ActionParameter) {
							try {
								String parameterName = ((ActionParameter) annotation).value();
								jsap.registerParameter(new FlaggedOption(parameterName).setLongFlag(parameterName));
							} catch (JSAPException e) {
								e.printStackTrace();
							}
						}
					}
				}
				JSAPResult config = jsap.parse(args);
				int argNumber = method.getParameterTypes().length;

				System.out.println("SHELL1 >" + commandName+ " " +jsap.getUsage());
				System.out.println(String.format("SHELL1 >%s [%d]", args,argNumber));

				for (int i = 0; i < parameterAnnotations.length; i++) {
					param[i] = null;

					final Annotation[] annotations = parameterAnnotations[i];
					for (final Annotation annotation : annotations) {
						if (annotation instanceof ActionParameter) {
							param[i] = config.getString(((ActionParameter) annotation).value());
						}
					}
				}
				if(param.length<argNumber){
					System.out.println("SHELL2 >"+ ActionManager.listShellCommand.get(commandName)+" not enough Annonation parameters");
				}else if(param.length>argNumber){
					System.out.println("SHELL2 >"+ ActionManager.listShellCommand.get(commandName)+" not enough parameters "+map.keySet().toString());
				}else{
					System.out.println("SHELL2 >"+ ActionManager.listShellCommand.get(commandName));
					for(int j=0;j<param.length;j++){
						System.out.println(String.format("[%d] %s", j,param[j].toString()));
					}
					switch(argNumber){
						case 0: method.invoke(container); break;
						case 1: method.invoke(container,param[0]); break;
						case 2: method.invoke(container,param[0],param[1]); break;
						case 3: method.invoke(container,param[0],param[1],param[2]); break;
						case 4: method.invoke(container,param[0],param[1],param[2],param[3]); break;
						case 5: method.invoke(container,param[0],param[1],param[2],param[3],param[4]); break;
						case 6: method.invoke(container,param[0],param[1],param[2],param[3],param[4],param[5]); break;
						case 7: method.invoke(container,param[0],param[1],param[2],param[3],param[4],param[5],param[6]); break;
						case 8: method.invoke(container,param[0],param[1],param[2],param[3],param[4],param[5],param[6],param[7]); break;
						case 9: method.invoke(container,param[0],param[1],param[2],param[3],param[4],param[5],param[6],param[7],param[8]); break;
						case 10: method.invoke(container,param[0],param[1],param[2],param[3],param[4],param[5],param[6],param[7],param[8],param[9]); break;
						default:
							System.out.println("SHELL2 >"+ ActionManager.listShellCommand.get(commandName)+" more than 10 parameters");
						break;
					}
				}

			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Scan.
	 *
	 * @param container
	 *            the container
	 * @param logger
	 *            the logger
	 */
	public static void scan(final Object container, final ProxyLogger logger) {
		for (Method method : container.getClass().getMethods()) {
			if (method.isAnnotationPresent(Action.class)) {
				Action annotation = method.getAnnotation(Action.class);
				annotation.annotationType().toString();
				final String MethodName = method.getName();

				HashSet<Action.Type> actions = new HashSet<Action.Type>(
						Arrays.asList(annotation.type()));

				if (actions.contains(Action.Type.SHELL)) {
					listShellCommand.put(MethodName, method);
					listShellContainer.put(MethodName, container);
				}
				if (actions.contains(Action.Type.GUI)) {
					listGUICommand.put(MethodName, method);
					logger.info("init guiaction." + method.getName());
					VertX.vertx.eventBus().registerHandler(
							"guiaction." + method.getName(),
							new Handler<Message<JsonObject>>() {
								public void handle(Message<JsonObject> event) {
									try {
										logger.info("init guiaction."
												+ MethodName + " "
												+ event.body.encode());
										ActionManager.listGUICommand.get(
												MethodName).invoke(container,
												event);
									} catch (IllegalAccessException
											| IllegalArgumentException
											| InvocationTargetException e) {
										logger.error(
												"handle guiaction "
														+ MethodName + " "
														+ event.body.encode(),
												e);
									}
								}
							});
				}
			}
		}
	}
	@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
	private static String hashPP(final Map<String,Object> m, String... offset) {
		String retval = "";
		String delta = offset.length == 0 ? "" : offset[0];
		for( Map.Entry<String, Object> e : m.entrySet() ) {
			retval += delta + "["+e.getKey() + "] -> ";
			Object value = e.getValue();
			if( value instanceof Map ) {
				retval += "(Hash)\n" + hashPP((Map<String,Object>)value, delta + "  ");
			} else if( value instanceof List ) {
				retval += "{";
				for( Object element : (List)value ) {
					retval += element+", ";
				}
				retval += "}\n";
			} else {
				retval += "["+value.toString()+"]\n";
			}
		}
		return retval+"\n";
	}
}
