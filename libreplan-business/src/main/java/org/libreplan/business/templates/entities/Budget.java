/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.business.templates.entities;

import org.hibernate.validator.AssertTrue;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.scenarios.entities.Scenario;

/**
 * Marker class intended to differentiate two different kinds of OrderTemplate:
 * BudgetTemplate and Budget.
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
public class Budget extends OrderTemplate {

    private Order associatedOrder;

    public static Budget create() {
        Budget beingBuilt = new Budget();
        beingBuilt.setCode("default-code-for-budget");
        return create(beingBuilt);
    }

    public static Budget createFromTemplate(BudgetTemplate template) {
        Budget beingBuilt = new Budget();
        beingBuilt.setName(template.getName());
        beingBuilt.setCode("default-code-for-budget");
        for (Label label : template.getLabels()) {
            beingBuilt.addLabel(label);
        }
        for (QualityForm form : template.getQualityForms()) {
            beingBuilt.addQualityForm(form);
        }
        for (OrderElementTemplate child : template.getChildren()) {
            beingBuilt.add(child.createCopy());
        }
        return create(beingBuilt);
    }

    public Order createOrderLineElementsForAssociatedOrder(Scenario scenario) {
        associatedOrder.useSchedulingDataFor(scenario);
        for (OrderElementTemplate each : getChildren()) {
            each.convertBudgetIntoHours();
            each.createElement(associatedOrder);
        }
        return associatedOrder;
    }

    public Order getAssociatedOrder() {
        return associatedOrder;
    }

    public void setAssociatedOrder(Order associatedOrder) {
        this.associatedOrder = associatedOrder;
    }

    @AssertTrue(message = "template name is already being used")
    public boolean checkConstraintUniqueRootTemplateName() {
        // We are not interested in keeping the unicity constraint in the case
        // of Budget objects. We are setting them an auto-generated name so the
        // chances to save a repeated name are low.
        // TODO: We might have to review how we check this constraint in the
        // future, to have a better, more general solution.

        return true;
    }

}
