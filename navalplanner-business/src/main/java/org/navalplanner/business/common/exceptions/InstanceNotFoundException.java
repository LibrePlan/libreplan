package org.navalplanner.business.common.exceptions;

/**
 * An exception for modeling that no persistent instance can be located from
 * a given key.
 * 
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 *
 */
@SuppressWarnings("serial")
public class InstanceNotFoundException extends InstanceException {

    public InstanceNotFoundException(Object key, String className) {
        super("Instance not found", key, className);
    }
    
}

