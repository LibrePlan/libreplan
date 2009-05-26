package org.navalplanner.web.common;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tells which is the base url <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Page {

    public String value();

}
