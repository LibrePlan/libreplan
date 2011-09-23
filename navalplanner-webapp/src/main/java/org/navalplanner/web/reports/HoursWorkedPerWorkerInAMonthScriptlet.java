/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.reports;

import java.util.HashSet;
import java.util.Set;

import net.sf.jasperreports.engine.JRAbstractScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import org.navalplanner.business.reports.dtos.HoursWorkedPerWorkerInAMonthDTO;
import org.navalplanner.business.workingday.EffortDuration;

/**
 * This class will be used to implement methods that could be called from jrxml
 * to make calculations over {@link EffortDuration} elements.
 *
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 *
 */
public class HoursWorkedPerWorkerInAMonthScriptlet extends JRAbstractScriptlet {

    private Set<HoursWorkedPerWorkerInAMonthDTO> dtos = new HashSet<HoursWorkedPerWorkerInAMonthDTO>();

    public String getSumNumHours() throws JRScriptletException {
        return (String) this.getVariableValue("sumNumHours");
    }

    public String getNumHours() throws JRScriptletException {
        return ((EffortDuration) this.getFieldValue("numHours"))
                .toFormattedString();
    }

    @Override
    public void afterColumnInit() throws JRScriptletException {

    }

    @Override
    public void afterDetailEval() throws JRScriptletException {
        EffortDuration current = (EffortDuration) this
                .getFieldValue("numHours");
        HoursWorkedPerWorkerInAMonthDTO dto = (HoursWorkedPerWorkerInAMonthDTO) this
                .getFieldValue("self");
        if (!dtos.contains(dto)) {
            EffortDuration effort = EffortDuration.sum(EffortDuration
                    .parseFromFormattedString((String) this
                            .getVariableValue("sumNumHours")), current);
            this.setVariableValue("sumNumHours", effort.toFormattedString());
            dtos.add(dto);
        }
    }

    @Override
    public void afterGroupInit(String arg0) throws JRScriptletException {

    }

    @Override
    public void afterPageInit() throws JRScriptletException {

    }

    @Override
    public void afterReportInit() throws JRScriptletException {

    }

    @Override
    public void beforeColumnInit() throws JRScriptletException {

    }

    @Override
    public void beforeDetailEval() throws JRScriptletException {

    }

    @Override
    public void beforeGroupInit(String arg0) throws JRScriptletException {

    }

    @Override
    public void beforePageInit() throws JRScriptletException {

    }

    @Override
    public void beforeReportInit() throws JRScriptletException {

    }
}
