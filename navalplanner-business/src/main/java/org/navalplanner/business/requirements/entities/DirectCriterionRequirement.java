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

import java.util.HashSet;
import java.util.Set;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class DirectCriterionRequirement extends CriterionRequirement{


    Set<IndirectCriterionRequirement> children =
            new HashSet<IndirectCriterionRequirement>();

    public static DirectCriterionRequirement create(){
        DirectCriterionRequirement result = new DirectCriterionRequirement();
        result.setNewObject(true);
        return result;
    }

    public static DirectCriterionRequirement create(Criterion criterion){
        DirectCriterionRequirement result = new DirectCriterionRequirement(criterion);
        result.setNewObject(true);
        return result;
    }

    public static DirectCriterionRequirement create(Criterion criterion,
            OrderElement orderElement,HoursGroup hoursGroup){
        DirectCriterionRequirement result = new  DirectCriterionRequirement(criterion,
                orderElement,hoursGroup);
        result.setNewObject(true);
        return result;
    }


    public DirectCriterionRequirement(){
    }

    public DirectCriterionRequirement(Criterion criterion,
            OrderElement orderElement,HoursGroup hoursGroup){
        super(criterion,orderElement,hoursGroup);
    }

    public DirectCriterionRequirement(Criterion criterion){
        super(criterion);
    }

    public Set<IndirectCriterionRequirement> getChildren() {
        return children;
    }

    public void setChildren(Set<IndirectCriterionRequirement>
            children) {
        this.children = children;
    }

}
