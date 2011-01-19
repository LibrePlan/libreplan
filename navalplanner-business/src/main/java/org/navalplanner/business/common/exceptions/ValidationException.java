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

package org.navalplanner.business.common.exceptions;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.InvalidValue;

/**
 * Encapsulates some validation failure <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ValidationException extends RuntimeException {

    private InvalidValue[] invalidValues;

    public InvalidValue[] getInvalidValues() {
        return invalidValues.clone();
    }

    public ValidationException(InvalidValue invalidValue) {
        super();
        storeInvalidValues(toArray(invalidValue));
    }

    private InvalidValue[] toArray(InvalidValue invalidValue) {
        InvalidValue[] result = new InvalidValue[1];
        result[0] = invalidValue;
        return result;
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
