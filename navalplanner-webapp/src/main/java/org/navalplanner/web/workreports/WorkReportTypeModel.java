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

import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workreports.daos.IWorkReportDAO;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link WorkReportType}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WorkReportTypeModel implements IWorkReportTypeModel {

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Autowired
    private IWorkReportDAO workReportDAO;

    private WorkReportType workReportType;

    private boolean editing = false;

    @Override
    public WorkReportType getWorkReportType() {
        return this.workReportType;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean thereAreWorkReportsFor(WorkReportType workReportType) {
        final List<WorkReport> workReports = workReportDAO.getAllByWorkReportType(workReportType);
        return (workReports != null && !workReports.isEmpty());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReportType> getWorkReportTypes() {
        return workReportTypeDAO.list(WorkReportType.class);
    }

    @Override
    public void prepareForCreate() {
        editing = false;
        this.workReportType = WorkReportType.create();
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(WorkReportType workReportType) {
        editing = true;
        Validate.notNull(workReportType);

        this.workReportType = getFromDB(workReportType);
    }

    private WorkReportType getFromDB(WorkReportType workReportType) {
        return getFromDB(workReportType.getId());
    }

    @Transactional(readOnly = true)
    private WorkReportType getFromDB(Long id) {
        try {
            WorkReportType result = workReportTypeDAO.find(id);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepareForRemove(WorkReportType workReportType) {
        this.workReportType = workReportType;
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        workReportTypeDAO.save(workReportType);
    }

    @Override
    @Transactional
    public void confirmRemove(WorkReportType workReportType) {
        try {
            workReportTypeDAO.remove(workReportType.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEditing() {
        return this.editing;
    }

}
