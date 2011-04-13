/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.resources.daos;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.IntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for Criterion. <br />
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */

@Component
public class CriterionTypeDAO extends IntegrationEntityDAO<CriterionType>
    implements ICriterionTypeDAO {

    @Override
    public CriterionType findByName(String name) {
        return (CriterionType) getSession().createCriteria(CriterionType.class)
                .add(Restrictions.eq("name", name).ignoreCase()).uniqueResult();
    }

    @Override
    public CriterionType findUniqueByName(CriterionType criterionType)
                throws InstanceNotFoundException {
        Validate.notNull(criterionType);
        return findUniqueByName(criterionType.getName());
    }

    @Override
    public CriterionType findUniqueByName(String name)
            throws InstanceNotFoundException {
        CriterionType result = findByName(name);
        if (result == null) {
              throw new InstanceNotFoundException(name,
                  CriterionType.class.getName());
          }
        return result;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public CriterionType findUniqueByNameAnotherTransaction(String name)
        throws InstanceNotFoundException {

        return findUniqueByName(name);

    }

    @Override
    public boolean existsOtherCriterionTypeByName(CriterionType criterionType) {
        Validate.notNull(criterionType);
        CriterionType found = findByName(criterionType.getName());
        return found != null && criterionType != found;
    }

    @Override
    public boolean existsPredefinedType(CriterionType criterionType) {
        Validate.notNull(criterionType);
        Validate.notNull(criterionType.getPredefinedTypeInternalName());
        if (existsOtherCriterionTypeByName(criterionType)) {
            return true;
        }
        return uniqueByInternalName(criterionType) != null;
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean hasDiferentTypeSaved(Long id, ResourceEnum resource) {
        try {
            CriterionType type = find(id);
            return (!(type.getResource().equals(resource)));
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean checkChildrenAssignedToAnyResource(
            CriterionType criterionType) {
        Validate.notNull(criterionType);

        String strQuery = "SELECT COUNT(*) "
                + "FROM Criterion criterion, CriterionSatisfaction satisfaction "
                + "LEFT OUTER JOIN satisfaction.criterion sat_crit "
                + "WHERE sat_crit.id = criterion.id "
                + "AND criterion.type = :criterionType ";

        Query query = getSession().createQuery(strQuery);

        if (criterionType != null) {
            query.setParameter("criterionType", criterionType);
        }

        if ((Long) query.uniqueResult() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public CriterionType findPredefined(CriterionType criterionType) {
        Validate.notNull(criterionType);
        Validate.notNull(criterionType.getPredefinedTypeInternalName());
        CriterionType result = findByName(criterionType.getName());
        if (result != null) {
            return result;
        }
        return uniqueByInternalName(criterionType);
    }

    private Criteria byInternalName(String predefinedTypeInternalName) {
        Criteria result = getSession().createCriteria(CriterionType.class);
        result.add(Restrictions.eq("predefinedTypeInternalName",
                predefinedTypeInternalName).ignoreCase());
        return result;
    }

    private CriterionType uniqueByInternalName(CriterionType criterionType) {
        Criteria c = byInternalName(criterionType
                .getPredefinedTypeInternalName());
        return (CriterionType) c.uniqueResult();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByNameAnotherTransaction(CriterionType criterionType) {
        return existsOtherCriterionTypeByName(criterionType);
    }

    @Override
    public void removeByName(CriterionType criterionType) {
        try {
            CriterionType reloaded = findUniqueByName(criterionType);
            remove(reloaded.getId());
        } catch (InstanceNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<CriterionType> getCriterionTypes() {
        return list(CriterionType.class);
    }

    @Override
    public List<CriterionType> getSortedCriterionTypes() {
        return getSession().createCriteria(CriterionType.class).addOrder(
                Order.asc("name")).list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CriterionType> getCriterionTypesByResources(
            Collection<ResourceEnum> resources) {
        return getSession().createCriteria(CriterionType.class).add(
                Restrictions.in("resource", resources)).addOrder(
                Order.asc("name")).list();
    }

}
