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

import org.apache.commons.lang.math.Fraction;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.ReadableDuration;
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
    public LocalDate toDate(int pixels) {
        int daysInto = Fraction.getFraction(pixels, 1).divideBy(pixelsPerDay)
                .intValue();
        return getInterval().getStart().plusDays(daysInto);
    }

    @Override
    public int toPixels(LocalDate date) {
        return toPixels(getProportion(date));
    }

    private Fraction getProportion(LocalDate date) {
        return getProportion(date.toDateTimeAtStartOfDay());
    }

    private Fraction getProportion(DateTime dateTime) {
        return getInterval().getProportion(dateTime);
    }

    private int toPixels(Fraction proportion) {
        return proportion.multiplyBy(Fraction.getFraction(horizontalSize, 1))
                .intValue();
    }

    @Override
    public int toPixels(ReadableDuration duration) {
        DateTime end = getInterval().getStart().toDateTimeAtStartOfDay()
                .plus(duration);
        return toPixels(getProportion(end));
    }

    @Override
    public int toPixelsAbsolute(long milliseconds) {
        DateTime date = new DateTime(milliseconds);
        return this.toPixels(getProportion(date));
    }

    @Override
    public ReadableDuration toDuration(int pixels) {
        return new Duration(millisecondsPerPixel * pixels);
    }

    @Override
    public long getMilisecondsPerPixel() {
        return millisecondsPerPixel;
    }

    @Override
    public int getHorizontalSize() {
        return this.horizontalSize;
    }

    public Fraction getPixelsPerDay() {
        return pixelsPerDay;
    }

    public Interval getInterval() {
        return interval;
    }

}