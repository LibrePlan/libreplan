/**
 *
 */
package org.zkoss.ganttz;

import java.util.Date;

import org.zkoss.ganttz.util.Interval;

public class DatesMapperOnInterval implements DatesMapper {
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
    public long toMilliseconds(int pixels) {
        return millisecondsPerPixel * pixels;
    }
}