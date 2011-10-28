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

package org.libreplan.business.materials.daos;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.materials.entities.MaterialCategory;
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
public class MaterialCategoryDAO extends IntegrationEntityDAO<MaterialCategory>
        implements
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

    @Override
    @Transactional(readOnly = true)
    public MaterialCategory findUniqueByName(String name)
            throws InstanceNotFoundException {

        if (StringUtils.isBlank(name)) {
            throw new InstanceNotFoundException(null, getEntityClass()
                    .getName());
        }

        MaterialCategory materialCategory = (MaterialCategory) getSession()
                .createCriteria(MaterialCategory.class).add(
                        Restrictions.eq("name", name.trim()).ignoreCase())
                .uniqueResult();

        if (materialCategory == null) {
            throw new InstanceNotFoundException(name, getEntityClass().getName());
        } else {
            return materialCategory;
        }
    }

    @Override
    @Transactional(readOnly= true, propagation = Propagation.REQUIRES_NEW)
    public MaterialCategory findUniqueByNameInAnotherTransaction(String name)
            throws InstanceNotFoundException {
        return findUniqueByName(name);
    }

    @Override
    public List<MaterialCategory> findAll() {
        return getSession().createCriteria(MaterialCategory.class).add(
                Restrictions.isNull("parent")).addOrder(Order.asc("code"))
                .list();
    }
}
