package org.zkoss.ganttz;

import java.util.Date;

public interface IDatesMapper {

    int toPixels(Date date);

    Date toDate(int pixel);

    int toPixels(long milliseconds);

    long toMilliseconds(int pixels);

}
