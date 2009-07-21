package org.navalplanner.web.common;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.zkoss.zk.ui.Component;

/**
 * Defines the ways in which information messages can be shown to the user <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IMessagesForUser {
    public interface ICustomLabelCreator{
        public Component createLabelFor(InvalidValue invalidValue);
    }

    void invalidValue(InvalidValue invalidValue, ICustomLabelCreator customLabelCreator);

    void invalidValue(InvalidValue invalidValue);

    void showMessage(Level level, String message);

    void clearMessages();

    void showInvalidValues(ValidationException e);

    void showInvalidValues(ValidationException e, ICustomLabelCreator customLabelCreator);

}
