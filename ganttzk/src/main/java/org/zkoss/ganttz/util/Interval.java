/**
 *
 */
package org.zkoss.ganttz.util;

import java.util.Date;

public class Interval {
    private final Date start;

    private final Date finish;

    private final long lengthBetween;

    public Interval(Date start, Date finish) {
        if (start == null)
            throw new IllegalArgumentException("begin cannot be null");
        if (finish == null)
            throw new IllegalArgumentException("end cannot be null");
        if (start.compareTo(finish) > 0)
            throw new IllegalArgumentException("start must be prior to end");
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
        if (!isIncluded(date))
            throw new IllegalArgumentException("date " + date
                    + " must be between [" + start + "," + finish + "]");
        return ((double) date.getTime() - start.getTime()) / lengthBetween;
    }

    private boolean isIncluded(Date date) {
        return start.compareTo(date) <= 0 && finish.compareTo(date) >= 0;
    }
}