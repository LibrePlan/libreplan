/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 - ComtecSF, S.L
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
package org.libreplan.web.reports;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import org.libreplan.business.workingday.EffortDuration;

/**
 * This class will be used to implement methods that could be called from
 * hoursWorkedPerWorkerReport.jrxml, completedEstimatedHours.jrxml and
 * workingProgressPerTask.jrxml to make calculations over {@link EffortDuration}
 * elements.
 *
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 *
 */
public class RealHoursScriptlet extends JRDefaultScriptlet {

    public String getRealHours() throws JRScriptletException {
        EffortDuration effort = (EffortDuration) this
                .getFieldValue("realHours");
        return effort.toFormattedString();
    }
}
