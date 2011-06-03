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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.QualityFormItem;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/qualityforms/qualityForms.zul")
public class QualityFormModel implements IQualityFormModel {

    @Autowired
    private IQualityFormDAO qualityFormDAO;

    private QualityForm qualityForm;

    private List<QualityForm> listQualityForms = new ArrayList<QualityForm>();

    private final BigDecimal totalPercentage = new BigDecimal(100).setScale(2);

    public QualityFormModel() {

    }

    @Override
    @Transactional(readOnly=true)
    public List<QualityForm> getQualityForms(String predicate) {
        listQualityForms.clear();
        listQualityForms = qualityFormDAO.getAll();
        return listQualityForms;
    }

    public List<QualityForm> filterQualityForms(String predicate) {
        if ((predicate != null) && (!predicate.isEmpty())) {
            List<QualityForm> result = new ArrayList<QualityForm>();
            for (QualityForm qualityForm : listQualityForms) {
                if (qualityForm.getName().toLowerCase().contains(
                        predicate.toLowerCase())) {
                    result.add(qualityForm);
                }
            }
            return result;
        }
        return listQualityForms;
    }

    @Override
    @Transactional
    public void confirmDelete(QualityForm qualityForm) {
        try {
            qualityFormDAO.remove(qualityForm.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void initCreate() {
        qualityForm = QualityForm.create("", "");
    }

    @Override
    public QualityForm getQualityForm() {
        return qualityForm;
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        qualityFormDAO.save(qualityForm);
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(QualityForm qualityForm) {
        Validate.notNull(qualityForm);
        this.qualityForm = getFromDB(qualityForm);
    }

    private QualityForm getFromDB(QualityForm qualityForm) {
        return getFromDB(qualityForm.getId());
    }

    private QualityForm getFromDB(Long id) {
        try {
            QualityForm result = qualityFormDAO.find(id);
            reattachQualityFormItems(result);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void reattachQualityFormItems(QualityForm qualityForm) {
        for (QualityFormItem item : qualityForm.getQualityFormItems()) {
            item.getName();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityFormItem> getQualityFormItems() {
        // Safe copy
        List<QualityFormItem> items = new ArrayList<QualityFormItem>();
        if (qualityForm != null) {
            items.addAll(qualityForm.getQualityFormItems());
        }
        return items;
    }

    @Override
    public void addQualityFormItem() {
        QualityFormItem item = QualityFormItem.create();
        qualityForm.addQualityFormItemOnTop(item);
    }

    @Override
    public void confirmDeleteQualityFormItem(QualityFormItem item) {
        qualityForm.removeQualityFormItem(item);
    }

    @Override
    public boolean checkConstraintUniqueQualityFormItemName() {
        if (getQualityForm() != null) {
            return getQualityForm().checkConstraintUniqueQualityFormItemsName();
        }
        return true;
    }

    @Override
    public boolean checkConstraintUniqueQualityFormName() {
        if (getQualityForm() != null) {
            return getQualityForm().checkConstraintUniqueQualityFormName();
        }
        return true;
    }

    @Override
    public boolean checkConstraintOutOfRangeQualityFormItemPercentage(
            QualityFormItem item) {
        if ((getQualityForm() != null) && (item != null)) {
            return (!item.checkConstraintQualityFormItemPercentage());
        }
        return true;
    }

    @Override
    public boolean checkConstraintUniqueQualityFormItemPercentage() {
        if (getQualityForm() != null) {
            return getQualityForm()
                    .checkConstraintDuplicatesQualityFormItemPercentage();
        }
        return true;
    }

    public void downQualityFormItem(QualityFormItem qualityFormItem) {
        Integer newPosition = qualityFormItem.getPosition() + 1;
        this.getQualityForm().moveQualityFormItem(qualityFormItem, newPosition);
    }

    public void upQualityFormItem(QualityFormItem qualityFormItem) {
        Integer newPosition = qualityFormItem.getPosition() - 1;
        this.getQualityForm().moveQualityFormItem(qualityFormItem, newPosition);
    }

    @Override
    public boolean hasItemWithTotalPercentage() {
        // Check if the current quality form has any item with 100 percentage to
        // can report progress
        if (getQualityForm() != null) {
            for (QualityFormItem item : this.getQualityForm()
                    .getQualityFormItems()) {
                if (isTotalPercentage(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean isTotalPercentage(QualityFormItem item) {
        return (item.getPercentage() != null) ? (item.getPercentage()
                .equals(totalPercentage)) : false;
    }

}
