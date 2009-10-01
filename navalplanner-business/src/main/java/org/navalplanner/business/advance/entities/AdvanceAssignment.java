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

package org.navalplanner.business.advance.entities;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.OrderElement;

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
        this.advanceType = advanceType;
    }

    public AdvanceType getAdvanceType() {
        return this.advanceType;
    }

}
