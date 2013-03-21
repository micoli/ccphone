package org.micoli.phone.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO: Auto-generated Javadoc
/**
 * The Interface QDCommand.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action {

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

	/**
	 * Permissions.
	 *
	 * @return the string[]
	 */
	//public String[] permissions();

}
