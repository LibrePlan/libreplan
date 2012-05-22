/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.web.users.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.UserUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for for "Monthly timesheets" area in the user dashboard window
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MonthlyTimesheetsAreaModel implements IMonthlyTimesheetsAreaModel {

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTimesheet> getMonthlyTimesheets() {
        User user = UserUtil.getUserFromSession();
        if (!user.isBound()) {
            return Collections.emptyList();
        }

        LocalDate activationDate = getActivationDate(user.getWorker());
        LocalDate currentDate = new LocalDate();
        return getMonthlyTimesheets(activationDate, currentDate.plusMonths(1));
    }

    private List<MonthlyTimesheet> getMonthlyTimesheets(
            LocalDate start, LocalDate end) {
        int months = Months.monthsBetween(start, end).getMonths();

        List<MonthlyTimesheet> result = new ArrayList<MonthlyTimesheet>();

        // In decreasing order to provide a list sorted with the more recent
        // monthly timesheets at the beginning
        for (int i = months; i >= 0; i--) {
            result.add(new MonthlyTimesheet(start.plusMonths(i)));
        }

        return result;
    }

    private LocalDate getActivationDate(Worker worker) {
        return worker.getCalendar().getFistCalendarAvailability()
                .getStartDate();
    }

}
