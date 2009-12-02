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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.costcategories.daos.ICostCategoryDAO;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.costcategories.entities.HourCost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link CostCategory}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CostCategoryModel implements ICostCategoryModel {

    private CostCategory costCategory;

    @Autowired
    private ICostCategoryDAO costCategoryDAO;

    @Override
    public List<CostCategory> getCostCategories() {
        return costCategoryDAO.list(CostCategory.class);
    }

    @Override
    public void initCreate() {
        costCategory = CostCategory.create();
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(CostCategory costCategory) {
        Validate.notNull(costCategory);
        this.costCategory = getFromDB(costCategory);
    }

    @Transactional(readOnly = true)
    private CostCategory getFromDB(CostCategory costCategory) {
        return getFromDB(costCategory.getId());
    }

    @Transactional(readOnly = true)
    private CostCategory getFromDB(Long id) {
        try {
            CostCategory result = costCategoryDAO.find(id);
            forceLoadEntities(result);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load entities that will be needed in the conversation
     *
     * @param costCategory
     */
    private void forceLoadEntities(CostCategory costCategory) {
        for (HourCost each : costCategory.getHourCosts()) {
            each.getInitDate();
            each.getCategory().getName();
            each.getType().getName();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<HourCost> getHourCosts() {
        Set<HourCost> hourCosts = new HashSet<HourCost>();
        if (costCategory != null) {
            hourCosts.addAll(costCategory.getHourCosts());
        }
        return hourCosts;
    }

    @Override
    public CostCategory getCostCategory() {
        return costCategory;
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        costCategoryDAO.save(costCategory);
    }

    @Override
    public void addHourCost() {
        costCategory.addHourCost(HourCost.create());
    }

    @Override
    public void removeHourCost(HourCost hourCost) {
        costCategory.removeHourCost(hourCost);
    }
}
