package com.mastercard.www.library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface DataMine {

    /**
     * Preference file name , If not set will take class name as default name.
     *
     * @return Preference FileName
     */
    String fileName() default "";

    /**
     * Preference mode , if not set by default it will set Context.MODE_PRIVATE.
     *
     * @return Preference Mode
     */
    int mode();
}
