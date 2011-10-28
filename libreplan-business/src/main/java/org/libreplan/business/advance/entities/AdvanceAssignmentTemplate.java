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

import java.math.BigDecimal;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.templates.entities.OrderElementTemplate;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class AdvanceAssignmentTemplate extends BaseEntity {

    public static AdvanceAssignmentTemplate convert(
            OrderElementTemplate beingBuilt, DirectAdvanceAssignment each) {
        AdvanceAssignmentTemplate result = create(beingBuilt);
        result.setReportGlobalAdvance(each.getReportGlobalAdvance());
        result.setMaxValue(each.getMaxValue());
        result.setAdvanceType(each.getAdvanceType());
        return result;
    }

    private static AdvanceAssignmentTemplate create(
            OrderElementTemplate template) {
        return create(new AdvanceAssignmentTemplate(template));
    }

    private boolean reportGlobalAdvance;

    private AdvanceType advanceType;

    private OrderElementTemplate template;

    private BigDecimal maxValue;

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public AdvanceAssignmentTemplate() {
    }

    public AdvanceAssignmentTemplate(OrderElementTemplate template) {
        Validate.notNull(template);
        this.template = template;
    }

    public boolean isReportGlobalAdvance() {
        return reportGlobalAdvance;
    }

    @NotNull
    public AdvanceType getAdvanceType() {
        return advanceType;
    }

    public void setAdvanceType(AdvanceType advanceType) {
        this.advanceType = advanceType;
    }

    @NotNull
    public OrderElementTemplate getTemplate() {
        return template;
    }

    @NotNull
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public void setReportGlobalAdvance(boolean reportGlobalAdvance) {
        this.reportGlobalAdvance = reportGlobalAdvance;
    }

    public DirectAdvanceAssignment createAdvanceAssignment(
            OrderElement orderElement) {
        DirectAdvanceAssignment result = DirectAdvanceAssignment.create(
                reportGlobalAdvance, maxValue);
        result.setOrderElement(orderElement);
        result.setAdvanceType(advanceType);
        return result;
    }

}
