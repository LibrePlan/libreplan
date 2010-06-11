/*
 * This file is part of NavalPlan
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
package org.zkoss.ganttz;

import java.util.Date;

import org.zkoss.ganttz.util.Interval;

public class DatesMapperOnInterval implements IDatesMapper {
    private final int horizontalSize;
    private final Interval stubInterval;
    private long millisecondsPerPixel;

    public DatesMapperOnInterval(int horizontalSize, Interval stubInterval) {
        this.horizontalSize = horizontalSize;
        this.stubInterval = stubInterval;
        this.millisecondsPerPixel = stubInterval.getLengthBetween()
                / horizontalSize;
    }

    @Override
    public Date toDate(int pixels) {
        return new Date(stubInterval.getStart().getTime()
                + millisecondsPerPixel * pixels);
    }

    @Override
    public int toPixels(Date date) {
        double proportion = stubInterval.getProportion(date);
        return (int) (horizontalSize * proportion);
    }

    @Override
    public int toPixels(long milliseconds) {
        Date date = new Date(stubInterval.getStart().getTime() + milliseconds);
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