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

package org.libreplan.business.costcategories.daos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.entities.Configuration;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.costcategories.entities.HourCost;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TypeOfWorkHoursDAO extends IntegrationEntityDAO<TypeOfWorkHours>
        implements
        ITypeOfWorkHoursDAO {

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Override
    public TypeOfWorkHours findUniqueByCode(TypeOfWorkHours typeOfWorkHours)
            throws InstanceNotFoundException {

        Validate.notNull(typeOfWorkHours);
        return findUniqueByCode(typeOfWorkHours.getCode());
    }

    @Override
    public TypeOfWorkHours findUniqueByCode(String code)
            throws InstanceNotFoundException {

        Criteria c = getSession().createCriteria(TypeOfWorkHours.class);
        c.add(Restrictions.eq("code", code));

        TypeOfWorkHours found = (TypeOfWorkHours) c.uniqueResult();
        if (found == null) {
            throw new InstanceNotFoundException(code, TypeOfWorkHours.class.getName());
        }
        return found;
    }

    @Override
    public List<TypeOfWorkHours> findActive() {

        Criteria c = getSession().createCriteria(TypeOfWorkHours.class);
        c.add(Restrictions.eq("enabled", true));

        List<TypeOfWorkHours> list = new ArrayList<TypeOfWorkHours>();
        list.addAll(c.list());
        return list;
    }

    @Override
    public boolean existsByCode(TypeOfWorkHours typeOfWorkHours) {
        try {
            return findUniqueByCode(typeOfWorkHours) != null;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly= true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsTypeWithCodeInAnotherTransaction(String code) {
        try {
            findUniqueByCode(code);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly= true, propagation = Propagation.REQUIRES_NEW)
    public TypeOfWorkHours findUniqueByCodeInAnotherTransaction(String code)
            throws InstanceNotFoundException {
        return findUniqueByCode(code);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public TypeOfWorkHours findUniqueByNameInAnotherTransaction(String name)
            throws InstanceNotFoundException {
        return findUniqueByName(name);
    }

    @Override
    public TypeOfWorkHours findUniqueByName(String name)
            throws InstanceNotFoundException {

        Criteria c = getSession().createCriteria(TypeOfWorkHours.class);
        c.add(Restrictions.eq("name", name.trim()).ignoreCase());

        TypeOfWorkHours found = (TypeOfWorkHours) c.uniqueResult();
        if (found == null) {
            throw new InstanceNotFoundException(name, TypeOfWorkHours.class
                    .getName());
        }
        return found;
    }

    @Override
    public List<TypeOfWorkHours> hoursTypeByNameAsc() {
        return getSession().createCriteria(TypeOfWorkHours.class)
                .addOrder(Order.asc("name")).list();
    }

    @Override
    public void checkIsReferencedByOtherEntities(TypeOfWorkHours type) throws ValidationException {
        checkHasHourCost(type);
        checkHasWorkReportLine(type);
        checkIsMonthlyTimesheetsTypeOfWorkHours(type);
    }

    private void checkHasWorkReportLine(TypeOfWorkHours type) {
        List workReportLines = getSession()
                .createCriteria(WorkReportLine.class)
                .add(Restrictions.eq("typeOfWorkHours", type)).list();
        if (!workReportLines.isEmpty()) {
            throw ValidationException
                    .invalidValue(
                            "Cannot delete type of work hours. It is being used at this moment in some timesheet line.",
                            type);
        }
    }

    private void checkHasHourCost(TypeOfWorkHours type) {
        List hoursCost = getSession().createCriteria(HourCost.class)
                .add(Restrictions.eq("type", type)).list();
        if (!hoursCost.isEmpty()) {
            throw ValidationException
                    .invalidValue(
                            "Cannot delete type of work hours. It is being used at this moment in some cost category.",
                            type);
        }
    }

    private void checkIsMonthlyTimesheetsTypeOfWorkHours(TypeOfWorkHours type) {
        Configuration configuration = configurationDAO.getConfiguration();
        if (configuration.getMonthlyTimesheetsTypeOfWorkHours().getId()
                .equals(type.getId())) {
            throw ValidationException
                    .invalidValue(
                            "Cannot delete the type of work hours. It is configured as type of work hours for monthly timesheets.",
                            type);
        }
    }

    @Override
    public boolean existsByName(TypeOfWorkHours typeOfWorkHours) {
        Criteria c = getSession().createCriteria(TypeOfWorkHours.class).add(
                Restrictions.eq("name", typeOfWorkHours.getName()));
        return c.uniqueResult() != null;
    }

}
