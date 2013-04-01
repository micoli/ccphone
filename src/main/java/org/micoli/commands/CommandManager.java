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

public class CommandManager {
	public static HashMap<String, CommandContainer> listCommand = new HashMap<String, CommandContainer>();
	public static ArrayList<String> listGUICommand = new ArrayList<String>();
	public static ArrayList<String> listShellCommand = new ArrayList<String>();

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

	public static ArrayList<String> runShellCommand(String commandName, Object args) {

		if (commandName.equalsIgnoreCase("help")) {
			return CommandManager.getCommandsHelp(args.toString());
		}

		if (commandName.equalsIgnoreCase("login")) {
			return CommandManager.getCommandsHelp(args.toString());
		}

		ArrayList<String> lines = new ArrayList<String>();
		if (listShellCommand.contains(commandName)) {
			Method method = CommandManager.listCommand.get(commandName).getMethod();
			Object container = CommandManager.listCommand.get(commandName).getContainer();
			JSAP jsap = CommandManager.listCommand.get(commandName).getJsap();
			int nbArg = CommandManager.listCommand.get(commandName).getNbArg();
			try {
				boolean isArgOk = true;
				CommandParameterMap config = new CommandParameterMap(jsap, args);

				int i=0;
				final Object[] param = new Object[nbArg];
				for (@SuppressWarnings("unchecked")
				Iterator<String> jsapIterator = jsap.getIDMap().idIterator(); jsapIterator.hasNext();) {
					String parameterName = jsapIterator.next();
					if (config.contains(parameterName)) {
						System.out.println(String.format("%d => %s", i, parameterName));
						param[i] = config.getString(parameterName);
						i++;
					} else {
						isArgOk = false;
						addLine("..", lines, CommandManager.listCommand.get(commandName) + " does not have " + parameterName + " parameter");
					}
				}

				if (isArgOk) {
					String[] result = (String[]) method.invoke(container, param);
					for (int l = 0; l < result.length; l++) {
						addLine("", lines, result[l]);
					}
				} else {
					lines.addAll(getCommandsHelp(commandName));
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				addLine("Error::",lines,getStackTrace(e));
			}
		}else{
			addLine("..", lines, String.format("Unknown shell command [%s], try help", commandName));
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

	public static ArrayList<String> getCommandsHelp(String command) {
		ArrayList<String> lines = new ArrayList<String>();
		if(!command.equals("")){
			if (listCommand.containsKey(command)) {
				addLine("::", lines, command + " " + listCommand.get(command).getJsap().getUsage());
				addLine("",lines,"");
			}else{
				addLine("",lines,"unknown command");
			}
		}else{
			Iterator<Entry<String, CommandContainer>> iterator = listCommand.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, CommandContainer> pairs = (Map.Entry<String, CommandContainer>)iterator.next();
				addLine(">",lines,pairs.getKey());
				addLine("", lines, pairs.getValue().getJsap().getUsage());
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
		boolean parametersOK;
		for (Method method : container.getClass().getMethods()) {
			if (method.isAnnotationPresent(Command.class)) {
				parametersOK = true;
				Command annotation = method.getAnnotation(Command.class);
				annotation.annotationType().toString();

				final String MethodName = method.getName();

				HashSet<Command.Type> commands = new HashSet<Command.Type>(Arrays.asList(annotation.type()));
				JSAP jsap = new JSAP();
				int nbJsapArg = 0;
				final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
				System.out.println(">method " + method.getName());
				for (int i = 0; i < parameterAnnotations.length; i++) {
					for (final Annotation cmdAnnotation : parameterAnnotations[i]) {
						if (annotation instanceof Command) {
							try {
								String parameterName = ((Command) cmdAnnotation).value();
								jsap.registerParameter(new FlaggedOption(parameterName).setLongFlag(parameterName));
								System.out.println(">prm Command " + parameterName);
								nbJsapArg++;
							} catch (JSAPException e) {
								System.out.println(getStackTrace(e));
							}
						}
					}
				}

				if (nbJsapArg != method.getParameterTypes().length) {
					parametersOK = false;
					System.out.println(String.format(">>>>Error, not enough command arguments anonation on %s.%s (%s!=%d)", container.getClass().getCanonicalName(), MethodName, nbJsapArg, method.getParameterTypes().length));
				}

				if (parametersOK) {
					listCommand.put(MethodName, new CommandContainer(method, container, jsap, nbJsapArg));
					if (commands.contains(Command.Type.SHELL)) {
						listShellCommand.add(MethodName);
					}

					if (commands.contains(Command.Type.GUI)) {
						listGUICommand.add(MethodName);
						logger.info("init guiaction." + method.getName());
						VertX.vertx.eventBus().registerHandler("guiaction." + method.getName(), new Handler<Message<JsonObject>>() {
							public void handle(Message<JsonObject> event) {
								try {
									logger.info("init guiaction." + MethodName + " " + event.body.encode());
									runShellCommand(MethodName, event.body.toMap());
									// CommandManager.listCommand.get(MethodName).getMethod().invoke(container,
									// event);
								} catch (IllegalArgumentException e) {
									logger.error("handle guiaction " + MethodName + " " + event.body.encode(), e);
								}
							}
						});
					}
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