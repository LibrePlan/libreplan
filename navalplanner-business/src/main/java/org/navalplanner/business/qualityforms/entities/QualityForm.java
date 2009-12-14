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

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
package org.navalplanner.business.qualityforms.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.NonUniqueResultException;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;

public class QualityForm extends BaseEntity {

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

    public QualityForm() {

    }

    private QualityForm(String name, String description) {
        this.name = name;
        this.description = description;
    }

    private String name;

    private String description;

    private QualityFormType qualityFormType = QualityFormType.getDefault();

    private List<QualityFormItem> qualityFormItems = new ArrayList<QualityFormItem>();

    @NotEmpty
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

    @NotNull
    public QualityFormType getQualityFormType() {
        return qualityFormType;
    }

    public void setQualityFormType(QualityFormType qualityFormType) {
        this.qualityFormType = qualityFormType;
    }

    @Valid
    public List<QualityFormItem> getQualityFormItems() {
        return Collections.unmodifiableList(qualityFormItems);
    }

    void setQualityFormItems(List<QualityFormItem> qualityFormItems) {
        this.qualityFormItems = qualityFormItems;
    }

    public boolean addQualityFormItemAtEnd(QualityFormItem qualityFormItem) {
        if (qualityFormItem != null) {
            Integer position = this.qualityFormItems.size();
            qualityFormItem.setPosition(position);
            this.qualityFormItems.add(qualityFormItem);
        }
        return false;
    }

    public void removeQualityFormItem(QualityFormItem qualityFormItem) {
        this.qualityFormItems.remove(qualityFormItem);
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
    @AssertTrue(message = "The quality item positions must be uniques, consecutives and corrects in function to the percentage.")
    public boolean validateTheQualityFormItemPositions() {
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

        // check the position is correct in function to the percentage.
        for (QualityFormItem item : qualityFormItems) {
            if (!item.getPosition().equals(getCorrectPosition(item))) {
                return false;
            }
        }
        return true;
    }

    private Integer getCorrectPosition(QualityFormItem itemToFind) {
        Integer position = 0;
        for (QualityFormItem item : qualityFormItems) {
            if ((!itemToFind.equals(item))
                    && (itemToFind.getPercentage().compareTo(item
                            .getPercentage())) > 0) {
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

}
