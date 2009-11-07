/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
package org.zkoss.ganttz.data.constraint;

import java.util.Date;

/**
 * A constraint applied to {@link Date dates} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class DateConstraint extends Constraint<Date> {

    public static Constraint<Date> biggerOrEqualThan(Date date) {
        if (date == null) {
            return Constraint.voidConstraint();
        }
        return new BiggerOrEqualThan(date);
    }

    private long value;

    protected long getValue() {
        return value;
    }

    protected DateConstraint(Date date) {
        this.value = asMilliseconds(date);
    }

    @Override
    protected Date applyConstraintTo(Date currentValue) {
        return new Date(applyConstraintTo(asMilliseconds(currentValue)));
    }

    private Long asMilliseconds(Date date) {
        return date != null ? date.getTime() : null;
    }

    protected abstract long applyConstraintTo(Long time);

    @Override
    public boolean isSatisfiedBy(Date value) {
        return isSatisfiedBy(asMilliseconds(value));
    }

    protected abstract boolean isSatisfiedBy(Long time);

    static class BiggerOrEqualThan extends DateConstraint {

        protected BiggerOrEqualThan(Date date) {
            super(date);
        }

        @Override
        protected long applyConstraintTo(Long time) {
            if (time == null) {
                return getValue();
            }
            return Math.max(getValue(), time);
        }

        @Override
        protected boolean isSatisfiedBy(Long time) {
            return time >= getValue();
        }

    }

}

