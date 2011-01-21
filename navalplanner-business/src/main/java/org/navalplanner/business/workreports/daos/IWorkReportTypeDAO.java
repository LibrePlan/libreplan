/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.workreports.daos;

import java.util.List;

import org.hibernate.NonUniqueResultException;
import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link WorkReportType}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public interface IWorkReportTypeDAO extends
        IIntegrationEntityDAO<WorkReportType> {

    public WorkReportType findUniqueByName(WorkReportType workReportType)
            throws InstanceNotFoundException, NonUniqueResultException;

    public WorkReportType findUniqueByName(String name)
            throws InstanceNotFoundException, NonUniqueResultException;

    public boolean existsOtherWorkReportTypeByName(WorkReportType WorkReportType);

    public boolean existsByNameAnotherTransaction(WorkReportType WorkReportType);

    public WorkReportType findUniqueByCode(WorkReportType workReportType)
            throws InstanceNotFoundException, NonUniqueResultException;

    public WorkReportType findUniqueByCode(String code)
            throws InstanceNotFoundException, NonUniqueResultException;

    public boolean existsOtherWorkReportTypeByCode(WorkReportType WorkReportType);

    public boolean existsByCodeAnotherTransaction(WorkReportType WorkReportType);

    public List<WorkReportType> getWorkReportTypes();

}
