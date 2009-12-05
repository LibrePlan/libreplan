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

package org.navalplanner.web.costcategories;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link ResourcesCostCategoryAssignment}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourcesCostCategoryAssignmentModel implements
        IResourcesCostCategoryAssignmentModel {

    Resource resource;

    @Autowired
    IResourceDAO resourceDAO;

    @Override
    public List<ResourcesCostCategoryAssignment> getCostCategoryAssignments() {
        List<ResourcesCostCategoryAssignment> list =
            new ArrayList<ResourcesCostCategoryAssignment>();
        if (resource != null) {
            list.addAll(resource.getResourcesCostCategoryAssignments());
        }
        return list;
    }

    @Override
    public void addCostCategory() {
        resource.addResourcesCostCategoryAssignment(
                ResourcesCostCategoryAssignment.create());
    }

    @Override
    public void removeCostCategoryAssignment(
            ResourcesCostCategoryAssignment assignment) {
        resource.removeResourcesCostCategoryAssignment(assignment);
    }

    private void forceLoadCostCategoryAssignments(Resource resource) {
        for (ResourcesCostCategoryAssignment assignment : resource.getResourcesCostCategoryAssignments()) {
            assignment.getCostCategory().getName();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void setResource(Resource resource) {
        resourceDAO.reattach(resource);
        forceLoadCostCategoryAssignments(resource);
        this.resource = resource;
    }
}
