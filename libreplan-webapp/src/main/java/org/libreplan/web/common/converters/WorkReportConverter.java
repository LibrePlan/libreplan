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

package org.libreplan.web.common.converters;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.entities.WorkReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A {@link IConverter} for {@link WorkReport}
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkReportConverter implements IConverter<WorkReport> {

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Override
    public Class<WorkReport> getType() {
        return WorkReport.class;
    }

    @Override
    public String asString(WorkReport entity) {
        return entity.getCode();
    }

    @Override
    public WorkReport asObject(String stringRepresentation) {
        try {
            return workReportDAO.findByCode(stringRepresentation);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String asStringUngeneric(Object entity) {
        return asString((WorkReport) entity);
    }

}
