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

package org.navalplanner.business.resources.daos;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
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
    @SuppressWarnings("unchecked")
    public List<CriterionType> findByName(CriterionType criterionType) {
        Criteria criteria = searchByNameCriteria(criterionType.getName());
        return (List<CriterionType>) criteria.list();
    }

    private Criteria searchByNameCriteria(String name) {
        Criteria result = getSession().createCriteria(CriterionType.class);
        result.add(Restrictions.eq("name", name).ignoreCase());
        return result;
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
        CriterionType result = uniqueByName(name);
        if (result == null) {
              throw new InstanceNotFoundException(name,
                  CriterionType.class.getName());
          }
        return result;
    }

    /**
     * @return the single result of null
     */
    private CriterionType uniqueByName(String name) {
        Criteria criteria = searchByNameCriteria(name);
        return (CriterionType) criteria.uniqueResult();
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
        CriterionType found = uniqueByName(criterionType.getName());
        return found != null && criterionType != found;
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

    @SuppressWarnings("unchecked")
    @Override
    public List<CriterionType> getCriterionTypesByResources(
            Collection<ResourceEnum> resources) {
        return getSession().createCriteria(CriterionType.class).add(
                Restrictions.in("resource", resources)).list();
    }

}
