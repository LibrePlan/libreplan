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
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;


/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class IndirectCriterionRequirement extends CriterionRequirement{

   private DirectCriterionRequirement parent;

   private Boolean isValid = true;

    public static IndirectCriterionRequirement create(DirectCriterionRequirement
            parent,Criterion criterion) {
        IndirectCriterionRequirement result = new IndirectCriterionRequirement(criterion);
        result.setNewObject(true);
        result.setParent(parent);
        return result;
    }

    public static IndirectCriterionRequirement create(DirectCriterionRequirement
            parent, Criterion criterion,OrderElement orderElement,HoursGroup hoursGroup){
        IndirectCriterionRequirement result = new IndirectCriterionRequirement(parent,criterion,
                orderElement,hoursGroup);
        result.setNewObject(true);
        return result;
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public IndirectCriterionRequirement() {

    }

    public IndirectCriterionRequirement(Criterion criterion) {
        super(criterion);
    }

    public IndirectCriterionRequirement(DirectCriterionRequirement parent,Criterion criterion,
            OrderElement orderElement,HoursGroup hoursGroup){
        super(criterion,orderElement,hoursGroup);
    }

    @NotNull(message = "parent not specified")
    public DirectCriterionRequirement getParent() {
        return parent;
    }

    public void setParent(DirectCriterionRequirement
            directCriterionRequirement) {
        this.parent = directCriterionRequirement;
    }

    public boolean isIsValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }
}
