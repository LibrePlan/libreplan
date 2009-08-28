package org.navalplanner.business.common.exceptions;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.InvalidValue;

/**
 * Encapsulates some validation failure <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ValidationException extends Exception {

    private InvalidValue[] invalidValues;

    public InvalidValue[] getInvalidValues() {
        return invalidValues;
    }

    public ValidationException(InvalidValue[] invalidValues) {
        super();
        storeInvalidValues(invalidValues);
    }

    private void storeInvalidValues(InvalidValue[] invalidValues) {
        Validate.noNullElements(invalidValues);
        this.invalidValues = invalidValues.clone();
    }

    public ValidationException(InvalidValue[] invalidValues, String message,
            Throwable cause) {
        super(message, cause);
        storeInvalidValues(invalidValues);
    }

    public ValidationException(InvalidValue[] invalidValues, String message) {
        super(message);
        storeInvalidValues(invalidValues);
    }

    public ValidationException(InvalidValue[] invalidValues, Throwable cause) {
        super(cause);
        storeInvalidValues(invalidValues);
    }

    public ValidationException(String message) {
        this(new InvalidValue[] {}, message);
    }

}
