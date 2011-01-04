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
package org.navalplanner.web.planner.order;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ICalendar;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IDetailItemModificator;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public final class BankHolidaysMarker implements
        IDetailItemModificator {

    private final ICalendar calendar;

    public BankHolidaysMarker(ICalendar calendar) {
        Validate.notNull(calendar);
        this.calendar = calendar;
    }

    public BankHolidaysMarker(BaseCalendar calendar) {
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
        return item;
    }
}