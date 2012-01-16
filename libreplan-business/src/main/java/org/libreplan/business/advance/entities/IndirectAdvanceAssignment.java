/*
 * This file is part of LibrePlan
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

package org.libreplan.business.advance.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.Valid;
import org.libreplan.business.orders.entities.OrderLineGroup;
import org.libreplan.business.planner.entities.consolidations.CalculatedConsolidation;

/**
 * Represents an {@link AdvanceAssignment} that is defined in some of the
 * children of this {@link OrderLineGroup}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class IndirectAdvanceAssignment extends AdvanceAssignment {

    @Valid
    private Set<CalculatedConsolidation> calculatedConsolidations = new HashSet<CalculatedConsolidation>();

    public static IndirectAdvanceAssignment create() {
        IndirectAdvanceAssignment indirectAdvanceAssignment = new IndirectAdvanceAssignment();
        indirectAdvanceAssignment.setNewObject(true);
        return indirectAdvanceAssignment;
    }

    public static IndirectAdvanceAssignment create(boolean reportGlobalAdvance) {
        IndirectAdvanceAssignment advanceAssignment = new IndirectAdvanceAssignment(
                reportGlobalAdvance);
        advanceAssignment.setNewObject(true);
        return advanceAssignment;
    }

    public IndirectAdvanceAssignment createIndirectAdvanceFor(OrderLineGroup parent) {
        IndirectAdvanceAssignment result = new IndirectAdvanceAssignment();
        result.setAdvanceType(getAdvanceType());
        result.setOrderElement(parent);
        result.setReportGlobalAdvance(noOtherGlobalReportingAdvance(parent));
        return create(result);
    }

    private boolean noOtherGlobalReportingAdvance(OrderLineGroup parent) {
        return parent.getReportGlobalAdvanceAssignment() == null;
    }

    public IndirectAdvanceAssignment() {
        super();
    }

    private IndirectAdvanceAssignment(boolean reportGlobalAdvance) {
        super(reportGlobalAdvance);
    }

    public void setCalculatedConsolidation(
            Set<CalculatedConsolidation> calculatedConsolidations) {
        this.calculatedConsolidations = calculatedConsolidations;
    }

    public Set<CalculatedConsolidation> getCalculatedConsolidation() {
        return calculatedConsolidations;
    }

    public boolean hasAnyConsolidationValue() {
        return !calculatedConsolidations.isEmpty();
    }

}
