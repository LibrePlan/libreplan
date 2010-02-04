/*
 * This file is part of NavalPlan
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.AssertFalse;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public class CostCategory extends BaseEntity {

    @NotEmpty
    private String name;

    private boolean enabled = true;

    private Set<HourCost> hourCosts = new HashSet<HourCost>();

    // Default constructor, needed by Hibernate
    protected CostCategory() {

    }

    public static CostCategory create() {
        return (CostCategory) create(new CostCategory());
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

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Valid
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
        LocalDate initDate = hourCost.getInitDate();
        LocalDate endDate = hourCost.getEndDate();
        for(HourCost listElement:hourCosts) {
            if(listElement.getType().getId().equals(hourCost.getType().getId())) {
                if (endDate == null && listElement.getEndDate() == null) {
                    overlap = true;
                }
                else if((endDate == null && listElement.getEndDate().compareTo(initDate)>=0) ||
                        (listElement.getEndDate() == null && listElement.getInitDate().compareTo(endDate)<=0)) {
                    overlap = true;
                }
                else if((endDate != null && listElement.getEndDate() != null) &&
                        ((listElement.getEndDate().compareTo(initDate)>=0 &&
                        listElement.getEndDate().compareTo(endDate)<=0) ||
                        (listElement.getInitDate().compareTo(initDate)>=0 &&
                                listElement.getInitDate().compareTo(endDate)<=0))) {
                    overlap = true;
                }
            }
        }
        return !overlap;
    }

    @AssertFalse(message="Two hour costs with the same type overlap in time")
    public boolean checkHourCostsOverlap() {
        List<HourCost> listHourCosts = new ArrayList<HourCost>();
        listHourCosts.addAll(getHourCosts());
        for(int i=0; i<listHourCosts.size(); i++) {
            LocalDate initDate = listHourCosts.get(i).getInitDate();
            LocalDate endDate = listHourCosts.get(i).getEndDate();
            for(int j=i+1; j<listHourCosts.size(); j++) {
                HourCost listElement = listHourCosts.get(j);
                if (listElement.getType() == null || listHourCosts.get(i).getType() == null) {
                    //this is not exactly an overlapping but a
                    //problem with missing compulsory fields
                    return true;
                }
                if(listElement.getType().getId().equals(listHourCosts.get(i).getType().getId())) {
                    if (initDate == null || listElement.getInitDate() == null) {
                        //this is not exactly an overlapping but a
                        //problem with missing compulsory fields
                        return true;
                    }
                    if (endDate == null && listElement.getEndDate() == null) {
                        return true;
                    }
                    else if((endDate == null && listElement.getEndDate().compareTo(initDate)>=0) ||
                            (listElement.getEndDate() == null && listElement.getInitDate().compareTo(endDate)<=0)) {
                        return true;
                    }
                    else if((endDate != null && listElement.getEndDate() != null) &&
                            ((listElement.getEndDate().compareTo(initDate)>=0 &&
                            listElement.getEndDate().compareTo(endDate)<=0) ||
                            (listElement.getInitDate().compareTo(initDate)>=0 &&
                                    listElement.getInitDate().compareTo(endDate)<=0))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
