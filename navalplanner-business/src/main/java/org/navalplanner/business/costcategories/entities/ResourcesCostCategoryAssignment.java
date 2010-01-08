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

import static org.navalplanner.business.i18n.I18nHelper._;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.CreateUnvalidatedException;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Resource;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class ResourcesCostCategoryAssignment extends BaseEntity {

    private LocalDate initDate;

    private LocalDate endDate;

    private CostCategory costCategory;

    private Resource resource;

    // Default constructor, needed by Hibernate
    protected ResourcesCostCategoryAssignment() {

    }

    public static ResourcesCostCategoryAssignment create() {
        return (ResourcesCostCategoryAssignment) create(new ResourcesCostCategoryAssignment());
    }

    public static ResourcesCostCategoryAssignment createUnvalidated(
        String costCategoryName, Resource resource, LocalDate initDate,
        LocalDate endDate) throws CreateUnvalidatedException {

        /* Get CostCategory. */
        if (StringUtils.isBlank(costCategoryName)) {
            throw new CreateUnvalidatedException(
                _("cost category name not specified"));
        }

        CostCategory costCategory = null;
        try {
            costCategory = Registry.getCostCategoryDAO().findUniqueByName(
                StringUtils.trim(costCategoryName));
        } catch (InstanceNotFoundException e) {
            throw new CreateUnvalidatedException(
               _("{0}: cost category does not exist", costCategoryName));
        }

        /* Create instance of ResourcesCostCategoryAssignment. */
        ResourcesCostCategoryAssignment assignment =
            create(new ResourcesCostCategoryAssignment());

        assignment.initDate = initDate;
        assignment.endDate = endDate;
        assignment.costCategory = costCategory;
        assignment.resource = resource;

        return assignment;

    }

    @NotNull(message="cost assignment's start date not specified")
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

    @NotNull(message="cost assignment's category not specified")
    public CostCategory getCostCategory() {
        return costCategory;
    }

    public void setCostCategory(CostCategory category) {
        this.costCategory = category;
    }

    @NotNull(message="cost assignment's resource not specified")
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

    @AssertTrue(message="cost assignment with end date less than start date")
    public boolean checkConstraintPositiveTimeInterval() {

        /* Check if it makes sense to check the constraint .*/
        if (!isInitDateSpecified()) {
            return true;
        }

        /* Check the constraint. */
        if (endDate == null) {
            return true;
        }

        return (endDate.isAfter(initDate) || initDate.equals(endDate));

    }

    public boolean isInitDateSpecified() {
        return initDate != null;
    }

}
