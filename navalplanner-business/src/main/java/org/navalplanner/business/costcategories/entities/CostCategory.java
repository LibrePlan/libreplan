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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.BaseEntity;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public class CostCategory extends BaseEntity {

	@NotEmpty
    private String name;

	private Set<HourCost> hourCosts = new HashSet<HourCost>();

    // Default constructor, needed by Hibernate
    protected CostCategory() {

    }

    public static CostCategory create(String name) {
        return (CostCategory) create(new CostCategory(name));
    }

    protected CostCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<HourCost> getHourCosts() {
        return hourCosts;
    }

    public void addHourCost(HourCost hourCost) {
        hourCosts.add(hourCost);
        if(hourCost.getCategory()!=this)
            hourCost.setCategory(this);
    }

    public void removeHourCost(HourCost hourCost) {
        hourCosts.remove(hourCost);
        if(hourCost.getCategory()==this)
            hourCost.setCategory(null);
    }

    public boolean canAddHourCost(HourCost hourCost) {
        boolean overlap = false;
        Date initDate = hourCost.getInitDate();
        Date endDate = hourCost.getEndDate();
        for(HourCost listElement:hourCosts) {
            if((listElement.getEndDate().compareTo(initDate)>=0 && listElement.getEndDate().compareTo(endDate)<=0) ||
               (listElement.getInitDate().compareTo(initDate)>=0 && listElement.getInitDate().compareTo(endDate)<=0))
                overlap = true;
        }
        return !overlap;
    }

    @AssertTrue
    public boolean HourCostNotOverlapping() {
        //TODO: implement a method to validate HourCost time intervals
        //complementary with canAddHourCost(), this method is run when calling DAO.save()
        return true;
    }
}
