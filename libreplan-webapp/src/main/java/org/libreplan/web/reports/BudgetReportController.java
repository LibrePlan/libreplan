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

package org.libreplan.web.reports;

import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.libreplan.business.reports.dtos.BudgetElementDTO;
import org.zkoss.zk.ui.Component;

/**
 * Controller for UI operations of Budget report.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@SuppressWarnings("serial")
public class BudgetReportController extends LibrePlanReportController {

    private static final String REPORT_NAME = "budgetReport";

    private IBudgetReportModel budgetReportModel;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);
    }

    @Override
    protected String getReportName() {
        return REPORT_NAME;
    }

    @Override
    protected JRDataSource getDataSource() {
        List<BudgetElementDTO> dtos = budgetReportModel.getBudgetElementDTOs();

        if (dtos.isEmpty()) {
            return new JREmptyDataSource();
        }

        return new JRBeanCollectionDataSource(dtos);
    }

}
