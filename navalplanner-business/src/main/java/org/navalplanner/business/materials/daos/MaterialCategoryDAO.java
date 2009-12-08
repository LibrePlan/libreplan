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

package org.navalplanner.business.materials.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link MaterialCategory}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class MaterialCategoryDAO extends GenericDAOHibernate<MaterialCategory, Long> implements
        IMaterialCategoryDAO {

    @Override
    public List<MaterialCategory> getAll() {
        return list(MaterialCategory.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<MaterialCategory> getAllRootMaterialCategories() {
        return getSession().createCriteria(MaterialCategory.class).add(Restrictions.isNull("parent")).list();
    }

    @Override
    @Transactional(readOnly= true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsMaterialCategoryWithNameInAnotherTransaction(
            String name) {
        try {
            findUniqueByName(name);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    private MaterialCategory findUniqueByName(String name)
            throws InstanceNotFoundException {
        Criteria criteria = getSession().createCriteria(MaterialCategory.class);
        criteria.add(Restrictions.eq("name", name).ignoreCase());

        List<MaterialCategory> list = criteria.list();
        if (list.size() != 1) {
            throw new InstanceNotFoundException(name, MaterialCategory.class.getName());
        }
        return list.get(0);
    }

    @Override
    @Transactional(readOnly= true, propagation = Propagation.REQUIRES_NEW)
    public MaterialCategory findUniqueByNameInAnotherTransaction(String name)
            throws InstanceNotFoundException {
        return findUniqueByName(name);
    }

}
