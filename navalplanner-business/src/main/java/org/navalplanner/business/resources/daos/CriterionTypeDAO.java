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

package org.navalplanner.business.resources.daos;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.springframework.stereotype.Component;

/**
 * DAO implementation for Criterion. <br />
 * @author Diego Pino Garcia <dpino@igalia.com>
 */

@Component
public class CriterionTypeDAO extends GenericDAOHibernate<CriterionType, Long>
        implements ICriterionTypeDAO {

    @Override
    public List<CriterionType> findByName(CriterionType criterionType) {
        Criteria c = getSession().createCriteria(CriterionType.class);

        c.add(Restrictions.eq("name", criterionType.getName()).ignoreCase());

        return (List<CriterionType>) c.list();
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
          Criteria c = getSession().createCriteria(CriterionType.class);

          c.add(Restrictions.eq("name", name));

          return (CriterionType) c.uniqueResult();
    }

    @Override
    public boolean existsByName(CriterionType criterionType) {
        try {
            CriterionType t = findUniqueByName(criterionType);
            return t != null && t != criterionType;
        } catch (InstanceNotFoundException e) {
            return false;
        }
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
