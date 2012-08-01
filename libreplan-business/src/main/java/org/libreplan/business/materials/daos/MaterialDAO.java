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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.materials.entities.Material;
import org.libreplan.business.materials.entities.MaterialAssignment;
import org.libreplan.business.materials.entities.MaterialCategory;
import org.libreplan.business.materials.entities.MaterialStatusEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link Material}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class MaterialDAO extends IntegrationEntityDAO<Material> implements
        IMaterialDAO {

    @Override
    public List<Material> getAll() {
        return list(Material.class);
    }

    @Override
    public List<Material> findMaterialsInCategoryAndSubCategories(String text,
            MaterialCategory materialCategory) {
        Set<MaterialCategory> materialCategories = new HashSet<MaterialCategory>();
        if (materialCategory != null) {
            materialCategories.add(materialCategory);
            materialCategories.addAll(getAllSubcategories(materialCategory));
        }
        return findMaterialsInCategories(text, materialCategories);
    }

    @Override
    public Set<MaterialCategory> getAllSubcategories(MaterialCategory materialCategory) {
        Set<MaterialCategory> result = new HashSet<MaterialCategory>();
        getAllSubcategories(result, materialCategory.getSubcategories());
        return result;
    }

    private void getAllSubcategories(Set<MaterialCategory> materialCategories, Set<MaterialCategory> subcategories) {
        for (MaterialCategory each: subcategories) {
            materialCategories.add(each);
            getAllSubcategories(materialCategories, each.getSubcategories());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Material> findMaterialsInCategories(String text,
            Set<MaterialCategory> categories) {
        Criteria criteria = this.getSession().createCriteria(Material.class);

        text = "%" + text + "%";
        criteria.add(Restrictions.or(Restrictions.ilike("code", text), Restrictions.ilike("description", text)));
        criteria.add(Restrictions.eq("disabled", false));
        if (categories != null && !categories.isEmpty()) {
            criteria.add(Restrictions.in("category", categories));
        }
        return criteria.list();
    }

    @Override
    @Transactional(readOnly= true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsMaterialWithCodeInAnotherTransaction(String code) {
        try {
            findUniqueByCode(code);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    private Material findUniqueByCode(String code)
            throws InstanceNotFoundException {
        if (code == null) {
            throw new InstanceNotFoundException(null, Material.class.getName());
        }

        Criteria criteria = getSession().createCriteria(Material.class);
        criteria.add(Restrictions.eq("code", code).ignoreCase());

        List<Material> list = criteria.list();
        if (list.size() != 1) {
            throw new InstanceNotFoundException(code, Material.class.getName());
        }
        return list.get(0);
    }

    @Override
    @Transactional(readOnly= true, propagation = Propagation.REQUIRES_NEW)
    public Material findUniqueByCodeInAnotherTransaction(String code)
            throws InstanceNotFoundException  {
        return findUniqueByCode(code);
    }

    public List<MaterialAssignment> getFilterMaterial(
            MaterialStatusEnum filterStatus, List<Order> orders,
            List<MaterialCategory> categories, List<Material> materials) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        boolean addedWhere = false;
        boolean addfilterCategory = false;
        boolean addfilterMaterial = false;
        if (categories != null && !categories.isEmpty()) {
            addfilterCategory = true;
        }
        if (materials != null && !materials.isEmpty()) {
            addfilterMaterial = true;
        }

        final List<OrderElement> allOrders = new ArrayList<OrderElement>();
        for (Order order : orders) {
            allOrders.add(order);
            allOrders.addAll(order.getAllOrderElements());
        }

        // Prepare query
        String strQuery = "SELECT materialAssignment "
                + "FROM MaterialAssignment materialAssignment "
                + "LEFT OUTER JOIN materialAssignment.orderElement orderElement "
                + "LEFT OUTER JOIN materialAssignment.materialInfo.material material ";

        if (filterStatus != null) {
            addedWhere = true;
            strQuery += "WHERE materialAssignment.status = :filterStatus ";
        }

        if (addedWhere) {
            strQuery += "AND orderElement IN (:allOrders) ";
        } else {
            addedWhere = true;
            strQuery += "WHERE orderElement IN (:allOrders) ";
        }

        if (categories != null && !categories.isEmpty()) {
            if (addedWhere) {
                strQuery += "AND ";
            } else {
                addedWhere = true;
                strQuery += "WHERE ";
            }
            if (addfilterMaterial) {
                strQuery += "( material.category IN (:categories) ";
            } else {
                strQuery += "material.category IN (:categories) ";
            }
        }
        if (materials != null && !materials.isEmpty()) {
            if (addedWhere) {
                if (addfilterCategory) {
                    strQuery += "OR material IN (:materials) )";
                } else {
                    strQuery += "AND material IN (:materials) ";
                }
            } else {
                strQuery += "WHERE material IN (:materials)";
            }
        }

        // Execute query
        Query query = getSession().createQuery(strQuery);
        if (filterStatus != null) {
            query.setParameter("filterStatus", filterStatus);
        }
        if (!allOrders.isEmpty()) {
            query.setParameterList("allOrders", allOrders);
        }
        if (categories != null && !categories.isEmpty()) {
            query.setParameterList("categories", categories);
        }
        if (materials != null && !materials.isEmpty()) {
            query.setParameterList("materials", materials);
        }
        return query.list();
    }

}
