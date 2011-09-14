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

import org.navalplanner.business.workingday.EffortDuration;

/**
 * This class will be used to implement methods that could be called from
 * hoursWoerkedPerWorkerReport.jrxml to make calculations over
 * {@link EffortDuration} elements.
 *
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 *
 */
public class HoursWorkedPerWorkerScriptlet extends JRAbstractScriptlet {

    private Set<EffortDuration> efforts = new HashSet<EffortDuration>();

    public String getEffort() throws JRScriptletException {
        EffortDuration effort = (EffortDuration) this.getFieldValue("effort");
        return effort.toFormattedString();
    }

    public String getSumEffort() throws JRScriptletException {
        return (String) this.getVariableValue("sumHoursPerDay");
    }

    public String getEffortWorker() throws JRScriptletException {
        return (String) this.getVariableValue("sumHoursPerWorker");
    }

    @Override
    public void afterDetailEval() throws JRScriptletException {
        // We use the set because elements could be processed twice depending on
        // the report
        EffortDuration current = (EffortDuration) this.getFieldValue("effort");
        if (!efforts.contains(current)) {
            // The effort of the worker is the sum of all efforts.
            EffortDuration effortWorker = EffortDuration.sum(current,
                    EffortDuration.parseFromFormattedString((String) this
                            .getVariableValue("sumHoursPerWorker")));
            this.setVariableValue("sumHoursPerWorker",
                    effortWorker.toFormattedString());
            // We calculate here the effort for a particular day
            EffortDuration effort = EffortDuration.sum(current, EffortDuration
                    .parseFromFormattedString((String) this
                            .getVariableValue("sumHoursPerDay")));
            this.setVariableValue("sumHoursPerDay", effort.toFormattedString());
            efforts.add(current);
        }
    }

    @Override
    public void afterColumnInit() throws JRScriptletException {

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
