/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.requirements.entities;

import java.util.HashSet;
import java.util.Set;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.templates.entities.OrderElementTemplate;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class DirectCriterionRequirement extends CriterionRequirement {

    private DirectCriterionRequirement origin;

    private Set<IndirectCriterionRequirement> children =
            new HashSet<IndirectCriterionRequirement>();

    public static DirectCriterionRequirement copyFrom(
            DirectCriterionRequirement criterionRequirement,
            OrderElementTemplate orderElementTemplate) {
        DirectCriterionRequirement result = copyFrom(criterionRequirement);
        result.setOrigin(criterionRequirement);
        result.setOrderElement(null);
        result.setOrderElementTemplate(orderElementTemplate);
        return result;
    }

    public static DirectCriterionRequirement copyFrom(
            DirectCriterionRequirement criterionRequirement,
            OrderElement orderElement) {
        DirectCriterionRequirement result = copyFrom(criterionRequirement);
        result.setOrigin(criterionRequirement);
        result.setOrderElement(orderElement);
        result.setOrderElementTemplate(null);
        return result;
    }

    public static DirectCriterionRequirement copyFrom(
            DirectCriterionRequirement criterionRequirement,
            HoursGroup hoursGroup) {
        DirectCriterionRequirement result = copyFrom(criterionRequirement);
        result.setOrigin(criterionRequirement);
        result.setHoursGroup(hoursGroup);
        result.setOrderElement(null);
        result.setOrderElementTemplate(null);
        return result;
    }

    public static DirectCriterionRequirement copyFrom(
            DirectCriterionRequirement criterionRequirement) {
        DirectCriterionRequirement result = DirectCriterionRequirement.create();
        result.setCriterion(criterionRequirement.getCriterion());
        result.setHoursGroup(criterionRequirement.getHoursGroup());
        return BaseEntity.create(result);
    }

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

    protected DirectCriterionRequirement() {

    }

    public DirectCriterionRequirement(Criterion criterion,
            OrderElement orderElement,HoursGroup hoursGroup){
        super(criterion,orderElement,hoursGroup);
    }

    public DirectCriterionRequirement(Criterion criterion){
        super(criterion);
    }

    public void addIndirectCriterionRequirement(IndirectCriterionRequirement indirect) {
        children.add(indirect);
    }

    public void removeIndirectCriterionRequirement(IndirectCriterionRequirement indirect) {
        children.remove(indirect);
    }

    public Set<IndirectCriterionRequirement> getChildren() {
        return children;
    }

    public void setChildren(Set<IndirectCriterionRequirement> children) {
        this.children = children;
    }

    public DirectCriterionRequirement getOrigin() {
        return origin;
    }

    public void setOrigin(DirectCriterionRequirement origin) {
        this.origin = origin;
    }

    @Override
    protected void ensureSpecificDataLoaded() {
        if (origin != null) {
            origin.ensureDataLoaded();
        }
        for (IndirectCriterionRequirement each : children) {
            each.ensureDataLoaded();
        }
    }

}
