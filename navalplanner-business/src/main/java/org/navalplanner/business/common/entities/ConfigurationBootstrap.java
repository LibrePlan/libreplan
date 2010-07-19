/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
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

package org.navalplanner.business.common.entities;

import java.util.List;

import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.daos.IOrderSequenceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a default {@link Configuration} with default values. It also creates
 * a default {@link OrderSequence}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope("singleton")
public class ConfigurationBootstrap implements IConfigurationBootstrap {

    private static final String COMPANY_CODE = "COMPANY_CODE";

    private static final String PREFIX = "PREFIX";

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private IOrderSequenceDAO orderSequenceDAO;

    @Override
    @Transactional
    public void loadRequiredData() {
        List<Configuration> list = configurationDAO.list(Configuration.class);
        if (list.isEmpty()) {
            Configuration configuration = Configuration.create();
            configuration.setDefaultCalendar(getDefaultCalendar());
            configuration.setCompanyCode(COMPANY_CODE);
            configurationDAO.save(configuration);
        }

        createDefaultOrderSquenceIfNotExist();
    }

    private void createDefaultOrderSquenceIfNotExist() {
        List<OrderSequence> orderSequences = orderSequenceDAO.getAll();

        if (orderSequences.isEmpty()) {
            OrderSequence orderSequence = OrderSequence.create(PREFIX);
            orderSequence.setActive(true);
            orderSequenceDAO.save(orderSequence);
        }
    }

    private BaseCalendar getDefaultCalendar() {
        BaseCalendar calendar = BaseCalendar.create();

        calendar.setName("Default");

        calendar.setHours(Days.MONDAY, 8);
        calendar.setHours(Days.TUESDAY, 8);
        calendar.setHours(Days.WEDNESDAY, 8);
        calendar.setHours(Days.THURSDAY, 8);
        calendar.setHours(Days.FRIDAY, 8);
        calendar.setHours(Days.SATURDAY, 0);
        calendar.setHours(Days.SUNDAY, 0);

        baseCalendarDAO.save(calendar);

        return calendar;
    }

}
