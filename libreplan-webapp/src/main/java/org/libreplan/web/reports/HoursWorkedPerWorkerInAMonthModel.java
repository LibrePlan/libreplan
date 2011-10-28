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

package org.libreplan.web.reports;

import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.libreplan.business.reports.dtos.HoursWorkedPerWorkerInAMonthDTO;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HoursWorkedPerWorkerInAMonthModel implements IHoursWorkedPerWorkerInAMonthModel {

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IWorkReportDAO workReportDAO;

    private boolean showReportMessage = false;

    @Override
    @Transactional(readOnly = true)
    public JRDataSource getHoursWorkedPerWorkerReport(Integer year, Integer month) {

        final List<HoursWorkedPerWorkerInAMonthDTO> workingHoursPerWorkerList = resourceDAO
                .getWorkingHoursPerWorker(year, month);

        if (workingHoursPerWorkerList != null
                && !workingHoursPerWorkerList.isEmpty()) {
            setShowReportMessage(false);
            return new JRBeanCollectionDataSource(workingHoursPerWorkerList);
        } else {
            setShowReportMessage(true);
            return new JREmptyDataSource();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void init() {

    }

    @Override
    @Transactional(readOnly = true)
    public int getBeginDisplayYears() {
        return workReportDAO.getFirstReportYear();
    }

    @Override
    @Transactional(readOnly = true)
    public int getEndDisplayYears() {
        return workReportDAO.getLastReportYear();
    }

   public void setShowReportMessage(boolean showReportMessage) {
        this.showReportMessage = showReportMessage;
    }

    @Override
    public boolean isShowReportMessage() {
        return showReportMessage;
    }

}
