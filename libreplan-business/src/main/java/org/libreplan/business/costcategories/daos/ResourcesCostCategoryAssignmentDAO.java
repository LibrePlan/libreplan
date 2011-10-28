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

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ResourcesCostCategoryAssignmentDAO
    extends IntegrationEntityDAO<ResourcesCostCategoryAssignment>
    implements IResourcesCostCategoryAssignmentDAO {

    @Override
    public void remove(Long id) throws InstanceNotFoundException {
        try {
            ResourcesCostCategoryAssignment assignment = find(id);
            assignment.getResource().removeResourcesCostCategoryAssignment(assignment);
        }
        catch(InstanceNotFoundException e) {
            //it was already deleted from its parent
            //we do nothing
        }
        super.remove(id);
    }

    @Override
    public List<ResourcesCostCategoryAssignment> getResourcesCostCategoryAssignmentsByCostCategory(
            CostCategory costCategory) {
        return (List<ResourcesCostCategoryAssignment>)getSession().
            createCriteria(ResourcesCostCategoryAssignment.class)
            .add(Restrictions.eq("costCategory", costCategory)).list();
    }
}
