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

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
package org.navalplanner.business.qualityforms.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.NonUniqueResultException;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;

public class QualityForm extends BaseEntity {

    public static final String ADVANCE_TYPE_PREFIX = "QF: ";

    public static QualityForm create() {
        QualityForm qualityForm = new QualityForm();
        qualityForm.setNewObject(true);
        return qualityForm;
    }

    public static QualityForm create(String name, String description) {
        QualityForm qualityForm = new QualityForm(name, description);
        qualityForm.setNewObject(true);
        return qualityForm;
    }

    protected QualityForm() {

    }

    private QualityForm(String name, String description) {
        this.name = name;
        this.description = description;
    }

    private String name;

    private String description;

    private QualityFormType qualityFormType = QualityFormType.getDefault();

    private List<QualityFormItem> qualityFormItems = new ArrayList<QualityFormItem>();

    private Boolean reportAdvance = false;

    private AdvanceType advanceType;

    @NotEmpty(message = "quality form name not specified")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotNull(message = "quality form type not specified")
    public QualityFormType getQualityFormType() {
        return qualityFormType;
    }

    public void setQualityFormType(QualityFormType qualityFormType) {
        Validate.notNull(qualityFormType);
        if (changeFromByItemsToByPercentage(qualityFormType)) {
            updatePercentageByPercentage();
        }
        this.qualityFormType = qualityFormType;
        updateAndSortQualityFormItem();
    }

    @Valid
    public List<QualityFormItem> getQualityFormItems() {
        return Collections.unmodifiableList(qualityFormItems);
    }

    void setQualityFormItems(List<QualityFormItem> qualityFormItems) {
        this.qualityFormItems = qualityFormItems;
    }

    public boolean addQualityFormItemOnTop(QualityFormItem qualityFormItem) {
        if (qualityFormItem != null) {
            Integer position = 0;
            qualityFormItem.setPosition(position);
            qualityFormItems.add(position, qualityFormItem);
            updateAndSortQualityFormItem();
        }
        return false;
    }

    public void removeQualityFormItem(QualityFormItem qualityFormItem) {
        qualityFormItems.remove(qualityFormItem);
        updateAndSortQualityFormItem();
    }

    public void updateAndSortQualityFormItem() {
        if (qualityFormType != null) {
            if (this.qualityFormType.equals(QualityFormType.BY_PERCENTAGE)) {
                updateAndSortQualityFormItemPositionsByPercentage();
            } else {
                updateAndSortQualityFormItemPositionsByItems();
                updatePercentageByItems();
            }
        }
    }

    public void moveQualityFormItem(QualityFormItem qualityFormItem,
            Integer newPosition) {
        if (checkValidPosition(newPosition)) {
            qualityFormItems.remove(qualityFormItem);
            qualityFormItems.add(newPosition, qualityFormItem);
            updateAndSortQualityFormItemPositionsByItems();
        }
    }

    public QualityFormItem findQualityFormItemWithDuplicateName() {
        List<QualityFormItem> items = new ArrayList<QualityFormItem>(
                qualityFormItems);
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                if ((items.get(j).getName() != null)
                        && (items.get(i).getName() != null)
                        && (items.get(j).getName().equals(items.get(i)
                                .getName()))) {
                    return items.get(j);
                }
            }
        }
        return null;
    }

    public QualityFormItem findQualityFormItemWithDuplicatePercentage() {
        List<QualityFormItem> items = new ArrayList<QualityFormItem>(
                qualityFormItems);
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                if ((items.get(j).getPercentage() != null)
                        && (items.get(i).getPercentage() != null)
                        && (items.get(j).getPercentage().equals(items.get(i)
                                .getPercentage()))) {
                    return items.get(j);
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "Quality form name is already being used")
    public boolean checkConstraintUniqueQualityFormName() {
        IQualityFormDAO qualityFormDAO = Registry.getQualityFormDAO();
        if (isNewObject()) {
            return !qualityFormDAO.existsByNameAnotherTransaction(this);
        } else {
            try {
                QualityForm c = qualityFormDAO.findUniqueByName(name);
                return c.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            } catch (NonUniqueResultException e) {
                return false;
            }
        }
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "Quality form item name must be unique")
    public boolean checkConstraintUniqueQualityFormItemsName() {
        return (findQualityFormItemWithDuplicateName() == null);
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "The quality item positions must be unique and consecutive.")
    public boolean checkConstraintConsecutivesAndUniquesQualityFormItemPositions() {
        List<QualityFormItem> result = getListToNull(qualityFormItems);
        for (QualityFormItem qualityFormItem : qualityFormItems) {
            // Check if index is out of range
            Integer index = qualityFormItem.getPosition();

            if (index == null) {
                return false;
            }

            if ((index.compareTo(0) < 0)
                    || (index.compareTo(result.size()) >= 0)) {
                return false;
            }
            // Check if index is repeated
            if (result.get(index) != null) {
                return false;
            }
            result.set(index, qualityFormItem);
        }

        // Check if the indexs are consecutives
        for (QualityFormItem item : result) {
            if (item == null) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "The quality item positions must be correct in function to the percentage.")
    public boolean checkConstraintCorrectPositionsQualityFormItemsByPercentage() {
        // check the position is correct in function to the percentage.
        if ((qualityFormType != null)
                && (qualityFormType.equals(QualityFormType.BY_PERCENTAGE))) {
            for (QualityFormItem item : qualityFormItems) {
                if (item.getPosition() == null) {
                    return false;
                }
                if (!item.getPosition().equals(getCorrectPosition(item))) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "The quality form item porcentage must be unique if the quality form type is by percentage.")
    public boolean checkConstraintDuplicatesQualityFormItemPercentage() {
        if ((qualityFormType != null)
                && (qualityFormType.equals(QualityFormType.BY_PERCENTAGE))
                && (findQualityFormItemWithDuplicatePercentage() != null)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "percentage should be greater than 0% and less than 100%")
    public boolean checkConstraintQualityFormItemsPercentage() {
        if ((qualityFormItems.size() > 0) && (qualityFormType != null)
                && (qualityFormType.equals(QualityFormType.BY_ITEMS))) {
            BigDecimal sum = new BigDecimal(0);
            for (QualityFormItem item : qualityFormItems) {
                if (item.getPercentage() == null) {
                    return false;
                }
                sum = sum.add(item.getPercentage());
            }
            return (sum.compareTo(new BigDecimal(100).setScale(2)) == 0);
        }
        return true;
    }

    private Integer getCorrectPosition(QualityFormItem itemToFind) {
        Integer position = 0;
        for (QualityFormItem item : qualityFormItems) {
            if (itemToFind.getPercentage() == null) {
                return null;
            }
            if ((((!itemToFind.equals(item)) && (item.getPercentage() != null) && (itemToFind
                    .getPercentage().compareTo(item.getPercentage())) > 0))
                    || (item.getPercentage() == null)) {
                position++;
            }
        }
        return position;
    }

    private List<QualityFormItem> getListToNull(List<QualityFormItem> list) {
        List<QualityFormItem> result = new ArrayList<QualityFormItem>(list
                .size());
        for (int i = 0; i < list.size(); i++) {
            result.add(null);
        }
        return result;
    }

    private void updateAndSortQualityFormItemPositionsByPercentage() {
        List<QualityFormItem> result = getListToNull(qualityFormItems);
        int nulos = 0;
        for (QualityFormItem item : qualityFormItems) {
            Integer position = getCorrectPosition(item);
            if (position == null) {
                position = nulos;
                nulos++;
            }

            while (result.get(position) != null) {
                position++;
            }

            item.setPosition(position);
            result.set(position, item);
        }
        setQualityFormItems(result);
    }

    private void updateAndSortQualityFormItemPositionsByItems() {
        List<QualityFormItem> result = getListToNull(qualityFormItems);
        for (QualityFormItem item : qualityFormItems) {
            int position = qualityFormItems.indexOf(item);
            item.setPosition(position);
            result.set(position, item);
        }
        setQualityFormItems(result);
    }

    private boolean changeFromByItemsToByPercentage(
            QualityFormType qualityFormType) {
        return (this.qualityFormType.equals(QualityFormType.BY_ITEMS) && qualityFormType
                .equals(QualityFormType.BY_PERCENTAGE));
    }

    private void updatePercentageByPercentage() {
        BigDecimal sum = new BigDecimal(0);
        for (QualityFormItem item : qualityFormItems) {
            item.setPercentage(item.getPercentage().add(sum));
            sum = sum.add(new BigDecimal(1));
        }
    }

    private void updatePercentageByItems() {
        if (qualityFormItems.size() > 0) {
            BigDecimal percentageTotal = new BigDecimal(100).setScale(2);
            BigDecimal numItems = new BigDecimal(qualityFormItems.size())
                    .setScale(2);
            BigDecimal percentageByItem = percentageTotal.divide(numItems, 2,
                    BigDecimal.ROUND_DOWN);
            for (QualityFormItem item : qualityFormItems) {
                item.setPercentage(percentageByItem);
            }

            // Calculate the division remainder
            BigDecimal sumByItems = (percentageByItem.multiply(numItems))
                    .setScale(2);
            BigDecimal remainder = (percentageTotal.subtract(sumByItems))
                    .setScale(2);
            QualityFormItem lastItem = qualityFormItems
                .get(qualityFormItems.size() - 1);
            BigDecimal lastPercentage = (lastItem.getPercentage()
                    .add(remainder)).setScale(2);
            lastItem.setPercentage(lastPercentage);
        }
    }

    private boolean checkValidPosition(Integer position) {
        return (position >= 0 && position < qualityFormItems.size());
    }

    @NotNull(message = "report advance not specified")
    public Boolean isReportAdvance() {
        return BooleanUtils.toBoolean(reportAdvance);
    }

    public void setReportAdvance(Boolean reportAdvance) {
        this.reportAdvance = BooleanUtils.toBoolean(reportAdvance);
    }

    public AdvanceType getAdvanceType() {
        return advanceType;
    }

    public void setAdvanceType(AdvanceType advanceType) {
        this.advanceType = advanceType;
    }

    @AssertTrue(message = "advance type should not be null if report advance")
    public boolean checkConstraintAdvanceTypeIsNotNullIfReportAdvance() {
        if (advanceType == null) {
            return !isReportAdvance();
        } else {
            return isReportAdvance();
        }
    }

}
