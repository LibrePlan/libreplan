package org.navalplanner.web.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Marks a controller that redirects to the real controller <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
public @interface Redirecter {

}
