package org.navalplanner.business.common.exceptions;

/**
 * An exception for modeling an attempt to create a persistent instance with
 * the same key than another existent instance.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 *
 */
@SuppressWarnings("serial")
public class DuplicateInstanceException extends InstanceException {

    public DuplicateInstanceException(Object key, String className) {
        super("Duplicate instance", key, className);
    }

}

