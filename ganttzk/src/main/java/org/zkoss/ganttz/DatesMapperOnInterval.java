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

/**
 *
 */
package org.zkoss.ganttz;

import java.util.Date;

import org.apache.commons.lang.math.Fraction;
import org.joda.time.DateTime;
import org.zkoss.ganttz.util.Interval;

public class DatesMapperOnInterval implements IDatesMapper {
    private final int horizontalSize;
    private final Interval interval;
    private long millisecondsPerPixel;
    private Fraction pixelsPerDay;

    public DatesMapperOnInterval(int horizontalSize, Interval interval) {
        this.horizontalSize = horizontalSize;
        this.interval = interval;
        this.millisecondsPerPixel = interval.getLengthBetween().getMillis()
                / horizontalSize;
        this.pixelsPerDay = Fraction.getFraction(horizontalSize, interval
                .getDaysBetween().getDays());
    }

    @Override
    public Date toDate(int pixels) {
        int daysInto = Fraction.getFraction(pixels, 1).divideBy(pixelsPerDay)
                .intValue();
        return interval.getStart().plusDays(daysInto).toDateTimeAtStartOfDay()
                .toDate();
    }

    @Override
    public int toPixels(Date date) {
        Fraction proportion = interval.getProportion(new DateTime(date
                .getTime()));
        return proportion.multiplyBy(Fraction.getFraction(horizontalSize, 1))
                .intValue();
    }

    @Override
    public int toPixels(long milliseconds) {
        Date date = new Date(interval.getStart().toDateTimeAtStartOfDay()
                .getMillis()
                + milliseconds);
        return this.toPixels(date);
    }

    @Override
    public int toPixelsAbsolute(long milliseconds) {
        Date date = new Date(milliseconds);
        return this.toPixels(date);
    }

    @Override
    public long toMilliseconds(int pixels) {
        return millisecondsPerPixel * pixels;
    }

    @Override
    public long getMilisecondsPerPixel() {
        return millisecondsPerPixel;
    }

    @Override
    public int getHorizontalSize() {
        return this.horizontalSize;
    }

}