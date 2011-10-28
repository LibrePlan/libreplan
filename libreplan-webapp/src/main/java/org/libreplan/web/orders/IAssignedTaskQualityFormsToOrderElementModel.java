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

package org.libreplan.web.orders;

import java.util.List;

import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.qualityforms.entities.QualityFormItem;
import org.libreplan.business.qualityforms.entities.TaskQualityForm;
import org.libreplan.business.qualityforms.entities.TaskQualityFormItem;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IAssignedTaskQualityFormsToOrderElementModel {

    /**
     * Assigns {@link TaskQualityForm} to {@link OrderElement}
     * @param @ QualityForm}
     */
    void assignTaskQualityForm(QualityForm qualityForm);

    /**
     * Delete {@link TaskQualityForm}
     * @param taskQualityForm
     */
    void deleteTaskQualityForm(TaskQualityForm taskQualityForm);

    /**
     * Gets all {@link TaskQualityForm} assigned to the current
     * {@link OrderElement}
     * @return
     */
    List<TaskQualityForm> getTaskQualityForms();

    /**
     * Returns all the unallocated {@link QualityForm} to the current
     * {@link OrderElement}
     * @return
     */
    List<QualityForm> getNotAssignedQualityForms();

    /**
     * Returns {@link OrderElement}
     * @return
     */
    OrderElement getOrderElement();

    void init(OrderElement orderElement);

    /**
     * Check whether {@link QualityForm} has been already assigned to
     * {@link OrderElement} or not
     * @param qualityForm
     */
    boolean isAssigned(QualityForm qualityForm);

    /**
     * Set {@link OrderElement}
     * @param orderElement
     */
    void setOrderElement(OrderElement orderElement);

    /**
     * @param orderModel
     */
    void setOrderModel(IOrderModel orderModel);

    /**
     * Update the date and the property passed of all the
     * {@link TaskQualityFormItem} of the {@ TaskQualityForm}
     * @param taskQualityForm
     */
    void updatePassedTaskQualityFormItems(TaskQualityForm taskQualityForm);

    /**
     * Check whether {@link QualityFormItem} the property passed must be
     * disabled
     * @param taskQualityForm
     *            ,item
     */
    boolean isDisabledPassedItem(TaskQualityForm taskQualityForm,
            TaskQualityFormItem item);

    /**
     * Check whether {@link QualityFormItem} date must be disabled
     * @param taskQualityForm
     *            ,item
     */
    boolean isDisabledDateItem(TaskQualityForm taskQualityForm,
            TaskQualityFormItem item);

    /**
     * Check whether {@link QualityFormItem} date is consecutive
     * @param taskQualityForm
     *            ,item
     */
    boolean isCorrectConsecutiveDate(TaskQualityForm taskQualityForm,
            TaskQualityFormItem item);

    /**
     * Check whether all {@link QualityForm} and its {@link QualityFormItem} are
     * valid.
     * @param
     */
    void validate();

    void addAdvanceAssignmentIfNeeded(TaskQualityForm taskQualityForm)
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException;

    void removeAdvanceAssignmentIfNeeded(TaskQualityForm taskQualityForm);

    void updateAdvancesIfNeeded();

}
