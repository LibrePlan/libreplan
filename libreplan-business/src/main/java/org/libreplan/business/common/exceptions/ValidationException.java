/*
 * This file is part of LibrePlan
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

package org.libreplan.business.common.exceptions;

import static java.util.Collections.singleton;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.Validate;
import org.libreplan.business.common.BaseEntity;

/**
 * Encapsulates some validation failure <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public class ValidationException extends RuntimeException {

    public static abstract class InvalidValue {

        public abstract String getMessage();

        public abstract String getPropertyPath();

        public abstract Object getInvalidValue();

        public abstract Object getRootBean();

    }

    private static class BasedOnConstraintViolation extends InvalidValue {

        private ConstraintViolation<?> violation;

        public BasedOnConstraintViolation(ConstraintViolation<?> violation) {
            Validate.notNull(violation);
            this.violation = violation;
        }

        @Override
        public String getMessage() {
            return violation.getMessage();
        }

        @Override
        public String getPropertyPath() {
            if (violation.getPropertyPath() == null) {
                return null;
            }
            return violation.getPropertyPath().toString();
        }


        @Override
        public Object getRootBean() {
            return violation.getRootBean();
        }

        @Override
        public Object getInvalidValue() {
            return violation.getInvalidValue();
        }
    }

    private static class InstantiatedInvalidValue extends InvalidValue {

        private final String message;
        private final String propertyPath;
        private final Object invalidValue;
        private final Object rootBean;

        private InstantiatedInvalidValue(String message, String propertyPath,
                Object invalidValue, Object rootBean) {
            super();
            this.message = message;
            this.propertyPath = propertyPath;
            this.invalidValue = invalidValue;
            this.rootBean = rootBean;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getPropertyPath() {
            return propertyPath;
        }

        @Override
        public Object getInvalidValue() {
            return invalidValue;
        }

        @Override
        public Object getRootBean() {
            return rootBean;
        }
    }

    private static String getValidationErrorSummary(
            Collection<? extends InvalidValue> violations) {
        StringBuilder builder = new StringBuilder();
        for (InvalidValue each : violations) {
            builder.append(summaryFor(each));
            builder.append("; ");
        }
        if (builder.length() > 0) {
            builder.delete(builder.length() - 2, builder.length());
        }
        return builder.toString();
    }

    private static String summaryFor(InvalidValue invalid) {
        Object bean = invalid.getRootBean();
        Object propertyPath = invalid.getPropertyPath();

        StringBuilder builder = new StringBuilder();
        if (bean != null) {
            builder = builder.append("at ").append(asString(bean));
            if (propertyPath != null) {
                builder = builder.append(" ").append(propertyPath).append(": ");
            }
        }
        return builder.append(invalid.getMessage()).toString();
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

    public static ValidationException invalidValueException(String message,
            Object bean) {
        return new ValidationException(invalidValue(message, bean));
    }

    private static InvalidValue invalidValue(String message, Object bean) {
        return invalidValue(message, null, null, bean);
    }

    public static InvalidValue invalidValue(String message,
            String propertyPath, Object invalidValue, Object rootBean) {
        return new InstantiatedInvalidValue(message, propertyPath,
                invalidValue, rootBean);
    }

    private final Set<? extends InvalidValue> invalidValues;

    public Set<? extends InvalidValue> getInvalidValues() {
        return invalidValues;
    }

    public InvalidValue getInvalidValue() {
        return invalidValues.isEmpty() ? null : invalidValues.iterator().next();
    }

    public ValidationException(ConstraintViolation<?> violation) {
        super(getValidationErrorSummary(convert(violation)));
        this.invalidValues = convert(violation);
    }

    public ValidationException(InvalidValue invalidValue) {
        super(getValidationErrorSummary(singleton(invalidValue)));
        this.invalidValues = singleton(invalidValue);
    }

    public ValidationException(Collection<? extends InvalidValue> invalidValues) {
        super(getValidationErrorSummary(invalidValues));
        this.invalidValues = new HashSet<InvalidValue>(invalidValues);
    }

    public ValidationException(String message, InvalidValue invalidValue) {
        super(message);
        this.invalidValues = singleton(invalidValue);
    }

    private static Set<? extends InvalidValue> convert(
            ConstraintViolation<?> violation) {
        return Collections.singleton(new BasedOnConstraintViolation(violation));
    }

    private static Set<? extends InvalidValue> convert(
            Collection<? extends ConstraintViolation<?>> violations) {
        Set<InvalidValue> result = new HashSet<InvalidValue>();
        for (ConstraintViolation<?> each : violations) {
            result.add(new BasedOnConstraintViolation(each));
        }
        return Collections.unmodifiableSet(result);
    }

    public ValidationException(Set<? extends ConstraintViolation<?>> violations) {
        super(getValidationErrorSummary(convert(violations)));
        this.invalidValues = convert(violations);
    }


    public ValidationException(
            Set<? extends ConstraintViolation<?>> violations, String message,
            Throwable cause) {
        super(message, cause);
        this.invalidValues = convert(violations);
    }

    public ValidationException(Set<? extends ConstraintViolation<?>> violations, String message) {
        super(message);
        this.invalidValues = convert(violations);
    }

    public ValidationException(
            Set<? extends ConstraintViolation<?>> violations, Throwable cause) {
        this(violations, getValidationErrorSummary(convert(violations)), cause);
    }

    public ValidationException(String message) {
        this(Collections.<ConstraintViolation<?>> emptySet(), message);
    }

}
