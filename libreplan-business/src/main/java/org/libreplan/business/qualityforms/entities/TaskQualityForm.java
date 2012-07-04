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

package org.libreplan.business.qualityforms.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.orders.entities.OrderElement;

public class TaskQualityForm extends BaseEntity {

    public static TaskQualityForm create(OrderElement orderElement,
            QualityForm qualityForm) {
        return create(new TaskQualityForm(orderElement, qualityForm));
    }

    protected TaskQualityForm() {

    }

    private TaskQualityForm(OrderElement orderElement, QualityForm qualityForm) {
        this.orderElement = orderElement;
        this.qualityForm = qualityForm;
        createTaskQualityFormItems();
    }

    private OrderElement orderElement;

    private QualityForm qualityForm;

    private List<TaskQualityFormItem> taskQualityFormItems = new ArrayList<TaskQualityFormItem>();

    private Boolean reportAdvance = false;

    @Valid
    public List<TaskQualityFormItem> getTaskQualityFormItems() {
        return Collections.unmodifiableList(taskQualityFormItems);
    }

    public void setTaskQualityFormItems(
            List<TaskQualityFormItem> taskQualityFormItems) {
        this.taskQualityFormItems = taskQualityFormItems;
    }

    @NotNull(message = "task not specified")
    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @NotNull(message = "quality form not specified")
    public QualityForm getQualityForm() {
        return qualityForm;
    }

    public void setQualityForm(QualityForm qualityForm) {
        this.qualityForm = qualityForm;
    }

    private void createTaskQualityFormItems() {
        Validate.notNull(qualityForm);
        for (QualityFormItem qualityFormItem : qualityForm
                .getQualityFormItems()) {
            TaskQualityFormItem taskQualityFormItem = TaskQualityFormItem
                    .create(qualityFormItem);
            taskQualityFormItems.add(taskQualityFormItem);
        }
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "Each date must be greater than the dates of the previous task quality form items.")
    public boolean checkConstraintCorrectConsecutivesDate() {
        if (!isByItems()) {
            for (TaskQualityFormItem item : taskQualityFormItems) {
                if (!isCorrectConsecutiveDate(item)) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "items cannot be checked until the previous items are checked before.")
    public boolean checkConstraintConsecutivePassedItems() {
        if (!isByItems()) {
            for (TaskQualityFormItem item : taskQualityFormItems) {
                if (!isCorrectConsecutivePassed(item)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isCorrectConsecutivePassed(TaskQualityFormItem item) {
        if (item.getPassed()) {
            return (isPassedPreviousItem(item));
        }
        return true;
    }

    public boolean isCorrectConsecutiveDate(TaskQualityFormItem item) {
        if (item.getPassed()) {
            return ((isPassedPreviousItem(item)) && (isLaterToPreviousItemDate(item)));
        }
        return (item.getDate() == null);
    }

    public boolean isPassedPreviousItem(TaskQualityFormItem item) {
        Integer previousPosition = item.getPosition() - 1;
        if ((previousPosition >= 0)
                && (previousPosition < taskQualityFormItems.size())) {
            return taskQualityFormItems.get(previousPosition).getPassed();
        }
        return true;
    }

    public boolean isLaterToPreviousItemDate(TaskQualityFormItem item) {
        Integer previousPosition = item.getPosition() - 1;
        if ((previousPosition >= 0)
                && (previousPosition < taskQualityFormItems.size())) {
            Date previousDate = taskQualityFormItems.get(previousPosition)
                    .getDate();
            return ((previousDate != null) && (item.getDate() != null) && ((previousDate
                    .before(item.getDate())) || (previousDate.equals(item
                    .getDate()))));
        }
        return true;
    }

    public boolean isByItems() {
        if ((this.qualityForm != null)
                && (this.qualityForm.getQualityFormType() != null)) {
            return (this.qualityForm.getQualityFormType()
                    .equals(QualityFormType.BY_ITEMS));
        }
        return true;
    }

    @NotNull(message = "report progress not specified")
    public Boolean isReportAdvance() {
        return BooleanUtils.toBoolean(reportAdvance);
    }

    public void setReportAdvance(Boolean reportAdvance) {
        this.reportAdvance = BooleanUtils.toBoolean(reportAdvance);
    }

    public static TaskQualityForm copy(TaskQualityForm origin,
            OrderElement orderElement) {
        TaskQualityForm copy = TaskQualityForm.create(orderElement, origin
                .getQualityForm());
        copy.setTaskQualityFormItems(origin.getTaskQualityFormItems());
        copy.setReportAdvance(origin.isReportAdvance());
        return copy;
    }

}
