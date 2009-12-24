package org.navalplanner.ws.common.api;

/**
 * {@link Exception} used in update process when two types are not compatible.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@SuppressWarnings(value = { "serial", "unchecked" })
public class IncompatibleTypeException extends Exception {

    private Object identifier;
    private Class expectedType;
    private Class actualType;

    public IncompatibleTypeException(Object identifier, Class expectedType,
            Class actualType) {
        super("Incompatible type (identifier = '" + identifier
                + "' - expectedType = '" + expectedType.getName()
                + "' - actualType = '" + actualType.getName() + "')");

        this.identifier = identifier;
        this.expectedType = expectedType;
        this.actualType = actualType;
    }

    public Object getIdentifier() {
        return identifier;
    }

    public Class getExpectedType() {
        return expectedType;
    }

    public Class getActualType() {
        return actualType;
    }

}
