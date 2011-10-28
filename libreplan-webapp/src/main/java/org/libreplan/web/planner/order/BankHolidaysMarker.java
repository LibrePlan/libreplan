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
package org.libreplan.web.planner.order;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.ICalendar;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.web.calendars.BaseCalendarModel;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IDetailItemModificator;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public final class BankHolidaysMarker implements
        IDetailItemModificator {

    public static BankHolidaysMarker create(BaseCalendar calendar) {
        BaseCalendarModel.forceLoadBaseCalendar(calendar);
        return new BankHolidaysMarker(calendar);
    }

    private final ICalendar calendar;

    private static final int WEEK_LEVEL_SHADE_WIDTH = 8;

    /**
     * <strong>Important: </strong>Make sure that the provided calendar has all
     * its associated data loaded.
     *
     * @param calendar
     */
    public BankHolidaysMarker(ICalendar calendar) {
        Validate.notNull(calendar);
        this.calendar = calendar;
    }

    @Override
    public DetailItem applyModificationsTo(DetailItem item, ZoomLevel z) {
        if (z == ZoomLevel.DETAIL_FIVE && calendar != null) {
            PartialDay day = PartialDay.wholeDay(item.getStartDate()
                    .toLocalDate());
            if (calendar.getCapacityOn(day).isZero()) {
                item.markBankHoliday();
            }
        }
        if ((calendar != null)
                && (z == ZoomLevel.DETAIL_THREE || z == ZoomLevel.DETAIL_FOUR)) {
            LocalDate day = item.getStartDate().toLocalDate();
            boolean notWorkable;
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                notWorkable = calendar.getCapacityOn(PartialDay.wholeDay(day))
                        .isZero();
                day = day.plusDays(1);
                result.append(notWorkable ? i * WEEK_LEVEL_SHADE_WIDTH
                        : -WEEK_LEVEL_SHADE_WIDTH);
                result.append("px 0");
                if (i != 6) {
                    result.append(",");
                }
            }
            item.markBankHolidayWeek(result.toString());
        }
        return item;
    }
}
