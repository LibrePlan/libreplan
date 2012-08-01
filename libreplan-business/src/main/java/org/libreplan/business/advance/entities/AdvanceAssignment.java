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

import org.hibernate.validator.NotNull;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLineGroup;

public abstract class AdvanceAssignment extends BaseEntity {

    private boolean reportGlobalAdvance;

    private OrderElement orderElement;

    private AdvanceType advanceType;

    public AdvanceAssignment() {
        this.reportGlobalAdvance = false;
    }

    protected AdvanceAssignment(boolean reportGlobalAdvance) {
        this.reportGlobalAdvance = reportGlobalAdvance;
    }

    public void setReportGlobalAdvance(boolean reportGlobalAdvance) {
        this.reportGlobalAdvance = reportGlobalAdvance;
        if (this.orderElement != null) {
            this.orderElement.markAsDirtyLastAdvanceMeasurementForSpreading();
        }
    }

    public boolean getReportGlobalAdvance() {
        return this.reportGlobalAdvance;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    public OrderElement getOrderElement() {
        return this.orderElement;
    }

    public void setAdvanceType(AdvanceType advanceType) {
        AdvanceType oldType = this.advanceType;
        if (advanceType != null) {
            this.advanceType = advanceType;
        }

        if (oldType != null && advanceType != null) {
            changeAdvanceTypeInParents(oldType, this.advanceType, this);
        }
    }

    @NotNull(message = "progress type not specified")
    public AdvanceType getAdvanceType() {
        return this.advanceType;
    }

    public void changeAdvanceTypeInParents(final AdvanceType oldType,
            AdvanceType newType, AdvanceAssignment advance) {
        if (getOrderElement() != null) {
            OrderLineGroup parent = getOrderElement().getParent();
            if (parent != null) {
                IndirectAdvanceAssignment oldIndirect = parent
                        .getIndirectAdvanceAssignment(oldType);
                if (oldIndirect != null) {
                    parent.removeIndirectAdvanceAssignment(oldType);
                    IndirectAdvanceAssignment newIndirect = advance
                            .createIndirectAdvanceFor(parent);
                    parent.addIndirectAdvanceAssignment(newIndirect);
                }
            }
        }
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

}
