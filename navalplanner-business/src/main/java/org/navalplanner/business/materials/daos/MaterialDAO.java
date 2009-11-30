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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link Material}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class MaterialDAO extends GenericDAOHibernate<Material, Long> implements
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
        criteria.add(Restrictions.or(Restrictions.ilike("code", text), Restrictions.eq("description", text)));
        criteria.add(Restrictions.eq("disabled", false));
        if (categories != null && !categories.isEmpty()) {
            criteria.add(Restrictions.in("category", categories));
        }
        return criteria.list();
    }

}
