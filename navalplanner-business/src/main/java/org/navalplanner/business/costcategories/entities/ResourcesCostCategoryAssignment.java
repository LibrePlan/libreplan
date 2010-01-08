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

package org.navalplanner.business.costcategories.entities;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.Resource;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public class ResourcesCostCategoryAssignment extends BaseEntity {

    @NotNull
    private LocalDate initDate;

    private LocalDate endDate;

    @NotNull
    private CostCategory costCategory;

    @NotNull
    private Resource resource;

    // Default constructor, needed by Hibernate
    protected ResourcesCostCategoryAssignment() {

    }

    public static ResourcesCostCategoryAssignment create() {
        return (ResourcesCostCategoryAssignment) create(new ResourcesCostCategoryAssignment());
    }

    public LocalDate getInitDate() {
        return initDate;
    }

    public void setInitDate(LocalDate initDate) {
        this.initDate = initDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public CostCategory getCostCategory() {
        return costCategory;
    }

    public void setCostCategory(CostCategory category) {
        this.costCategory = category;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        Resource oldResource = this.resource;
        this.resource = resource;
        if(oldResource!=null)
            oldResource.removeResourcesCostCategoryAssignment(this);
        if(resource!=null &&
                !resource.getResourcesCostCategoryAssignments().contains(this)) {
            resource.addResourcesCostCategoryAssignment(this);
        }
    }

    @AssertTrue(message="The end date cannot be before the init date")
    public boolean checkPositiveTimeInterval() {
        if (endDate == null) {
            return true;
        }
        return (endDate.isAfter(initDate) || initDate.equals(endDate));
    }

}
