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

package org.navalplanner.web.qualityforms;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.QualityFormItem;

/**
 * Interface for {@link QualityFormModel}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IQualityFormModel {

    /**
     * Removes {@link QualityForm}
     * @param qualityForm
     */
    void confirmDelete(QualityForm qualityForm);

    /**
     * Ends conversation saving current {@link QualityForm}
     */
    void confirmSave() throws ValidationException;

    /**
     * Returns {@link QualityForm}
     * @return
     */
    QualityForm getQualityForm();

    /**
     * Returns all {@link QualityForm}
     * @return
     */
    List<QualityForm> getQualityForms(String predicate);

    /**
     * Starts conversation creating new {@link QualityForm}
     */
    void initCreate();

    /**
     * Starts conversation editing {@link QualityForm}
     */
    void initEdit(QualityForm qualityForm);

    /**
     * Returns all {@link QualityFormItem} for current {@link QualityForm}
     * @return
     */
    List<QualityFormItem> getQualityFormItems();

    /**
     * Add {@link QualityFormItem} to {@link QualityForm}
     */
    void addQualityFormItem();

    /**
     * @param qualityFormItem
     */
    void confirmDeleteQualityFormItem(QualityFormItem item);

    /**
     * Check if {@link QualityFormItem} name is unique
     * @param value
     */
    boolean checkConstraintUniqueQualityFormItemName();

    /**
     * Check if {@link QualityForm} name is unique
     * @param value
     */
    boolean checkConstraintUniqueQualityFormName();

    /**
     * Check if the {@link QualityFormItem } percentage is into range (0,100]
     * @param qualityFormItem
     */
    boolean checkConstraintOutOfRangeQualityFormItemPercentage(
            QualityFormItem item);

    /**
     * Check if exist other {@link QualityFormItem } with the same percentage
     * @param percentage
     */
    boolean checkConstraintUniqueQualityFormItemPercentage();

    /**
     * Changes the current {@link QualityFormItem } position to a greater
     * position and updates the others position {@link QualityFormItem } position
     * @param qualityFormItem
     */
    void downQualityFormItem(QualityFormItem qualityFormItem);

    /**
     * Changes the current {@link QualityFormItem } position to a lower position
     * and updates the others position {@link QualityFormItem } position
     * @param qualityFormItem
     */
    void upQualityFormItem(QualityFormItem qualityFormItem);

    /**
     * Check if exist any {@link QualityFormItem } of the current quality form with the 100 percentage
     */
    boolean hasItemWithTotalPercentage();

    /**
     * Check if the current {@link QualityFormItem } has a 100 percentage
     * @param qualityFormItem
     */
    Boolean isTotalPercentage(QualityFormItem item);
}
