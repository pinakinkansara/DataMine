package com.datamine.www.library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by e064173 on 09/11/17.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface DataKey {

    /**
     * Preference key for the data to be stored & retrieved.
     * @return key
     */
    String key();
}
