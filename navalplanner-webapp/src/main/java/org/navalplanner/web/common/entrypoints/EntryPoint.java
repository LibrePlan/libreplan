package org.navalplanner.web.common.entrypoints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method that can be linked to using matrix parameters<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface EntryPoint {
    public String[] value();
}
