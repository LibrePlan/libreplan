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

package org.navalplanner.business.requirements.entities;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CriterionRequirement extends BaseEntity{

    private HoursGroup hoursGroup;

    private OrderElement orderElement;

    private Criterion criterion;

    public CriterionRequirement(){

    }

    public CriterionRequirement(Criterion criterion){
        this.criterion = criterion;
    }

    public CriterionRequirement(Criterion criterion,
            OrderElement orderElement,HoursGroup hoursGroup){
        this.criterion = criterion;
        this.orderElement = orderElement;
        this.hoursGroup = hoursGroup;
    }

    @NotNull(message = "criterion not specified")
    public Criterion getCriterion() {
        return criterion;
    }

    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }

    public HoursGroup getHoursGroup() {
        return hoursGroup;
    }

    public void setHoursGroup(HoursGroup hoursGroup) {
        this.hoursGroup = hoursGroup;
    }

    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }
}
