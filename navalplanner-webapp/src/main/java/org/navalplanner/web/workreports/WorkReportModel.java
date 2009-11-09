/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
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

package org.navalplanner.web.workreports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.daos.IWorkReportDAO;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link WorkReport}.
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WorkReportModel implements IWorkReportModel {

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    private WorkReport workReport;

    private boolean editing = false;

    @Override
    public WorkReport getWorkReport() {
        return workReport;
    }

    @Override
    @Transactional(readOnly = true)
    public void initCreate(WorkReportType workReportType) {
        editing = false;
        workReport = WorkReport.create();
        workReport.setWorkReportType(workReportType);
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(WorkReport workReport) {
        editing = true;
        Validate.notNull(workReport);
        this.workReport = getFromDB(workReport);
    }

    @Transactional(readOnly = true)
    private WorkReport getFromDB(WorkReport workReport) {
        return getFromDB(workReport.getId());
    }

    @Transactional(readOnly = true)
    private WorkReport getFromDB(Long id) {
        try {
            WorkReport result = workReportDAO.find(id);
            forceLoadEntities(result);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load entities that will be needed in the conversation
     *
     * @param workReport
     */
    private void forceLoadEntities(WorkReport workReport) {
        // Load WorkReportType
        workReport.getWorkReportType().getName();

        // Load CriterionTypes
        for (CriterionType criterionType : workReport.getWorkReportType()
                .getCriterionTypes()) {
            criterionType.getName();
            // Load Criterions
            for (Criterion criterion : criterionType.getCriterions()) {
                criterion.getName();
            }
        }

        // Load WorkReportLines
        for (WorkReportLine workReportLine : workReport.getWorkReportLines()) {
            workReportLine.getNumHours();
            workReportLine.getResource().getDescription();
            workReportLine.getOrderElement().getName();

            // Load Criterions
            for (Criterion criterion : workReportLine.getCriterions()) {
                criterion.getName();
            }
        }
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        workReportDAO.save(workReport);
    }

    @Override
    @Transactional
    public OrderElement findOrderElement(String orderCode)
            throws InstanceNotFoundException {
        String[] parts = orderCode.split("-");

        OrderElement parent = orderElementDAO.findUniqueByCodeAndParent(null,
                parts[0]);
        for (int i = 1; i < parts.length && parent != null; i++) {
            OrderElement child = orderElementDAO.findUniqueByCodeAndParent(
                    parent, parts[i]);
            parent = child;
        }

        return parent;
    }

    @Override
    @Transactional
    public Worker findWorker(String nif) throws InstanceNotFoundException {
        return workerDAO.findUniqueByNif(nif);
    }

    @Override
    @Transactional
    public Worker asWorker(Resource resource) throws InstanceNotFoundException {
        return workerDAO.find(resource.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReport> getWorkReports() {
        List<WorkReport> result = new ArrayList<WorkReport>();
        for (WorkReport each : workReportDAO.list(WorkReport.class)) {
            each.getWorkReportType().getName();
            result.add(each);
        }
        return result;
    }

    @Override
    public boolean isEditing() {
        return editing;
    }

    @Override
    @Transactional(readOnly = true)
    public String getDistinguishedCode(OrderElement orderElement) throws InstanceNotFoundException {
        return orderElementDAO.getDistinguishedCode(orderElement);
    }

    @Override
    public WorkReportLine addWorkReportLine() {
        WorkReportLine workReportLine = WorkReportLine.create();
        workReport.addWorkReportLine(workReportLine);
        return workReportLine;
    }

    @Transactional
    public void remove(WorkReport workReport) {
        try {
            workReportDAO.remove(workReport.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeWorkReportLine(WorkReportLine workReportLine) {
        workReport.removeWorkReportLine(workReportLine);
    }

    @Override
    public List<WorkReportLine> getWorkReportLines() {
        List<WorkReportLine> result = new ArrayList<WorkReportLine>();
        if (getWorkReport() != null) {
            result.addAll(workReport.getWorkReportLines());
        }
        return result;
    }
}
