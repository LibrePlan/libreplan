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

/**
 *
 */
package org.zkoss.ganttz.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.lang.Validate;

public class Interval {
    private final Date start;

    private final Date finish;

    private final long lengthBetween;

    public Interval(Date start, Date finish) {
        if (start == null) {
            throw new IllegalArgumentException("begin cannot be null");
        }
        if (finish == null) {
            throw new IllegalArgumentException("end cannot be null");
        }
        if (start.compareTo(finish) > 0) {
            throw new IllegalArgumentException("start must be prior to end");
        }
        this.start = start;
        this.finish = finish;
        lengthBetween = this.finish.getTime() - this.start.getTime();
    }

    public Date getStart() {
        return new Date(start.getTime());
    }

    public Date getFinish() {
        return new Date(finish.getTime());
    }

    public long getLengthBetween() {
        return lengthBetween;
    }

    public Date atProportion(double proportion) {
        // comparisons with doubles are dangerous, change it
        if (proportion > 1.0d) {
            throw new IllegalArgumentException(
                    "the proportion must be less or equal than one");
        }
        if (proportion < 0d) {
            throw new IllegalArgumentException(
                    "the proportion must be bigger than cero");
        }
        return new Date(start.getTime() + (int) (lengthBetween * proportion));
    }

    public double getProportion(Date date) {
        if (!isIncluded(date)) {
            throw new IllegalArgumentException("date " + date
                    + " must be between [" + start + "," + finish + "]");
        }
        return ((double) date.getTime() - start.getTime()) / lengthBetween;
    }

    private boolean isIncluded(Date date) {
        return start.compareTo(date) <= 0 && finish.compareTo(date) >= 0;
    }

    public Interval coalesce(Interval otherInterval) {
        Validate.notNull(otherInterval);
        return new Interval(Collections.min(Arrays.asList(start,
                otherInterval.start)), Collections.max(Arrays.asList(finish,
                otherInterval.finish)));
    }
}