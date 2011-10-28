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

package org.libreplan.web.common.converters;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.workreports.daos.IWorkReportTypeDAO;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkReportTypeConverter implements IConverter<WorkReportType> {

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Override
    @Transactional(readOnly = true)
    public WorkReportType asObject(String stringRepresentation) {
        long id = Long.parseLong(stringRepresentation);
        try {
            WorkReportType workReportType = workReportTypeDAO.find(id);
            return workReportType;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String asString(WorkReportType entity) {
        return entity.getId().toString();
    }

    @Override
    public String asStringUngeneric(Object entity) {
        return asString((WorkReportType) entity);
    }

    @Override
    public Class<WorkReportType> getType() {
        return WorkReportType.class;
    }

}
