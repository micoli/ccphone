package org.micoli.commands;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.micoli.phone.ccphone.remote.VertX;
import org.micoli.phone.tools.ProxyLogger;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

public class CommandManager {
	public static HashMap<String, Method> listGUICommand = new HashMap<String, Method>();
	public static HashMap<String, CommandContainer> listShellCommand = new HashMap<String, CommandContainer>();

	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	private static void addLine(String prefix,ArrayList<String> lines,String line){
		System.out.println(line);
		lines.add(String.format("%s %s", prefix,line));
	}

	public static ArrayList<String> runShellCommand(String commandName, String args) {

		if (commandName.equalsIgnoreCase("help")) {
			return CommandManager.getShellCommands(args);
		}

		if (commandName.equalsIgnoreCase("login")) {
			return CommandManager.getShellCommands(args);
		}

		ArrayList<String> lines = new ArrayList<String>();
		if (listShellCommand.containsKey(commandName)) {
			Method method = CommandManager.listShellCommand.get(commandName).method;
			Object container = CommandManager.listShellCommand.get(commandName).container;
			try {
				JSAP jsap = listShellCommand.get(commandName).jsap;

				JSAPResult config = jsap.parse(args);
				int argCount = method.getParameterTypes().length;

				final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
				final Object[] param = new Object[parameterAnnotations.length];
				int numberMapped = 0;
				for (int i = 0; i < parameterAnnotations.length; i++) {
					param[i] = null;
					final Annotation[] annotations = parameterAnnotations[i];
					for (final Annotation annotation : annotations) {
						if (annotation instanceof Command) {
							if(config.contains(((Command) annotation).value())){
								param[i] = config.getString(((Command) annotation).value());
								numberMapped++;
							}
						}
					}
				}

				if(param.length!=argCount || numberMapped!=param.length){
					if(param.length<argCount){
						addLine("..",lines,CommandManager.listShellCommand.get(commandName)+" not enough Annonation parameters");
					}else{
						addLine("..",lines,CommandManager.listShellCommand.get(commandName)+" not enough parameters ");
					}
					lines.addAll(getShellCommands(commandName));
				}else{
					Object result = method.invoke(container,param).toString();
					addLine("",lines, result.toString());
				}

			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				addLine("Error::",lines,getStackTrace(e));
			}
		}else{
			addLine("..",lines,String.format("Unknown command [%s], try help",commandName));
		}
		return lines;
	}

	public static boolean runLoginCommand(String args) {
		String commands[] = args.split(" ");
		if(commands.length==2){
			if(commands[0].equals("root") && commands[1].equals("root")){
				return true;
			}
		}
		return false;
	}

	public static ArrayList<String> getShellCommands(String command) {
		ArrayList<String> lines = new ArrayList<String>();
		if(!command.equals("")){
			if(listShellCommand.containsKey(command)){
				addLine("::",lines,command+" "+listShellCommand.get(command).jsap.getUsage());
				addLine("",lines,"");
			}else{
				addLine("",lines,"unknown command");
			}
		}else{
			Iterator<Entry<String, CommandContainer>> iterator = listShellCommand.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, CommandContainer> pairs = (Map.Entry<String, CommandContainer>)iterator.next();
				addLine(">",lines,pairs.getKey());
				addLine("",lines,pairs.getValue().jsap.getUsage());
				addLine("",lines,"");
			}
		}

		return lines;
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
			if (method.isAnnotationPresent(Command.class)) {
				Command annotation = method.getAnnotation(Command.class);
				annotation.annotationType().toString();
				final String MethodName = method.getName();

				HashSet<Command.Type> commands = new HashSet<Command.Type>(
						Arrays.asList(annotation.type()));

				if (commands.contains(Command.Type.SHELL)) {
					JSAP jsap = new JSAP();
					final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
					for (int i = 0; i < parameterAnnotations.length; i++) {
						for (final Annotation cmdAnnotation : parameterAnnotations[i]) {
							if (annotation instanceof Command) {
								try {
									String parameterName = ((Command) cmdAnnotation).value();
									jsap.registerParameter(new FlaggedOption(parameterName).setLongFlag(parameterName));
								} catch (JSAPException e) {
									System.out.println(getStackTrace(e));
								}
							}
						}
					}
					listShellCommand.put(MethodName, new CommandContainer(method,container,jsap));
				}
				if (commands.contains(Command.Type.GUI)) {
					listGUICommand.put(MethodName, method);
					logger.info("init guiaction." + method.getName());
					VertX.vertx.eventBus().registerHandler("guiaction." + method.getName(),new Handler<Message<JsonObject>>() {
						public void handle(Message<JsonObject> event) {
							try {
								logger.info("init guiaction."+ MethodName + " "+ event.body.encode());
								CommandManager.listGUICommand.get(MethodName).invoke(container,event);
							} catch (IllegalAccessException
									| IllegalArgumentException
									| InvocationTargetException e) {
								logger.error("handle guiaction "+ MethodName + " "+ event.body.encode(),e);
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