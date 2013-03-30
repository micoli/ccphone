package org.micoli.phone.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface QDCommand.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action {

	public static enum Type { GUI, SHELL, WEB };

	public Type[] type() default Type.GUI;

	/**
	 * Description.
	 *
	 * @return the string
	 */
	public String description() default "";

	/**
	 * Usage.
	 *
	 * @return the string
	 */
	public String usage() default "";

	/**
	 * Help.
	 *
	 * @return the string
	 */
	public String help() default "";

}
