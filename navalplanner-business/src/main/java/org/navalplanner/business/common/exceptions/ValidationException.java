/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import org.navalplanner.business.common.BaseEntity;

/**
 * Encapsulates some validation failure <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public class ValidationException extends RuntimeException {

    private static String getValidationErrorSummary(
            InvalidValue... invalidValues) {
        StringBuilder builder = new StringBuilder();
        for (InvalidValue each : invalidValues) {
            builder.append(summaryFor(each));
            builder.append("; ");
        }
        if (invalidValues.length > 0) {
            builder.delete(builder.length() - 2, builder.length());
        }
        return builder.toString();
    }

    private static String summaryFor(InvalidValue invalidValue) {
        return "at " + asString(invalidValue.getBean()) + " "
                + invalidValue.getPropertyPath() + ": "
                + invalidValue.getMessage();
    }

    private static String asString(Object bean) {
        if (bean == null) {
            // this shouldn't happen, just in case
            return "null";
        }
        if (bean instanceof BaseEntity) {
            BaseEntity entity = (BaseEntity) bean;
            return bean.getClass().getSimpleName() + " "
                    + entity.getExtraInformation();
        }
        return bean.toString();
    }

    public static ValidationException invalidValue(String message, Object value) {
        InvalidValue invalidValue = new InvalidValue(message, null, "", value,
                null);
        return new ValidationException(invalidValue);
    }

    private InvalidValue[] invalidValues;

    public InvalidValue[] getInvalidValues() {
        return invalidValues.clone();
    }

    public InvalidValue getInvalidValue() {
        return (invalidValues.length > 0) ? invalidValues.clone()[0] : null;
    }

    public ValidationException(InvalidValue invalidValue) {
        super(getValidationErrorSummary(invalidValue));
        storeInvalidValues(toArray(invalidValue));
    }

    private InvalidValue[] toArray(InvalidValue invalidValue) {
        InvalidValue[] result = new InvalidValue[1];
        result[0] = invalidValue;
        return result;
    }

    public ValidationException(InvalidValue[] invalidValues) {
        super(getValidationErrorSummary(invalidValues));
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
