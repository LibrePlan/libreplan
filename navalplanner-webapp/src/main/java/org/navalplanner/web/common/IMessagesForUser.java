/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
