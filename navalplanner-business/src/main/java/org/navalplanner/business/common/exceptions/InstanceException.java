package org.navalplanner.business.common.exceptions;

/**
 * An exception for modeling a problem with an instance of a persistent entity.
 * It contains a message, the key of the instance, and its class name.
 * 
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 *
 */
@SuppressWarnings("serial")
public abstract class InstanceException extends Exception {

    private Object key;
    private String className;
    
    protected InstanceException(String specificMessage, Object key, 
        String className) {
        
        super(specificMessage + " (key = '" + key + "' - className = '" + 
            className + "')");
        this.key = key;
        this.className = className;
        
    }
    
    public Object getKey() {
        return key;
    }
    
    public String getClassName() {
        return className;
    }

}
