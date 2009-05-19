package org.navalplanner.web.common;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;

/**
 * Defines the ways in which information messages can be shown to the user <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IMessagesForUser {

    void invalidValue(InvalidValue invalidValue);

    void showMessage(Level level, String message);

    void clearMessages();

    void showInvalidValues(ValidationException e);

}
