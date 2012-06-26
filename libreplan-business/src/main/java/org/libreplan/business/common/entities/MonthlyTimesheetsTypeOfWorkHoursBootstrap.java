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

package org.libreplan.business.common.entities;

import org.libreplan.business.BootstrapOrder;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.costcategories.entities.TypeOfWorkHoursBootstrap;
import org.libreplan.business.workreports.entities.PredefinedWorkReportTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fills the attribute {@link Configuration#monthlyTimesheetsTypeOfWorkHours}
 * with a default value.<br />
 *
 * If possible it uses the "Default" {@link TypeOfWorkHours}, but if it doesn't
 * exist, it uses the first {@link TypeOfWorkHours} found.<br />
 *
 * This bootstrap have to be executed after {@link ConfigurationBootstrap} and
 * {@link TypeOfWorkHoursBootstrap}, this is why it's marked with
 * {@link BootstrapOrder BootstrapOrder(1)}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope("singleton")
@BootstrapOrder(1)
public class MonthlyTimesheetsTypeOfWorkHoursBootstrap implements
        IMonthlyTimesheetsTypeOfWorkHoursBootstrap {

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Override
    @Transactional
    public void loadRequiredData() {
        Configuration configuration = configurationDAO.getConfiguration();

        // TypeOfWorkHoursBootstrap creates the TypeOfWorkHours objects
        // specified by PredefinedWorkReportTypes if there isn't any
        // TypeOfWorkHours in the database
        TypeOfWorkHours typeOfWorkHours;
        try {
            typeOfWorkHours = typeOfWorkHoursDAO
                    .findUniqueByName(PredefinedWorkReportTypes.DEFAULT
                            .getName());
        } catch (InstanceNotFoundException e) {
            typeOfWorkHours = typeOfWorkHoursDAO.findActive().get(0);
        }

        configuration.setMonthlyTimesheetsTypeOfWorkHours(typeOfWorkHours);
        configurationDAO.save(configuration);
    }

}
