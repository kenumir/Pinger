package com.wt.pinger.proto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DataFieldAnnotation {
	/**
	 * extra dane do zapytania CREATE TABLE ..., np: DEFAULT NULL
	 * @return
	 */
    String extra() default "";

	/**
	 * pole używane w zapytanich slect as/join itp
	 * pole fizycznie nie istnieje w bazie
	 * @return
	 */
	boolean isVirtualField() default false;

}
