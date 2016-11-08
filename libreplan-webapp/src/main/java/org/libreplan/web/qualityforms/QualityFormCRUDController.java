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

package org.libreplan.web.qualityforms;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.List;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.qualityforms.entities.QualityFormItem;
import org.libreplan.business.qualityforms.entities.QualityFormType;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.ext.Sortable;

/**
 * CRUD Controller for {@link QualityForm}.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class QualityFormCRUDController extends BaseCRUDController<QualityForm> {

    private final String CANNOT_BE_EMPTY = "cannot be empty";

    private IQualityFormModel qualityFormModel;

    private Grid gridQualityForms;

    private Grid gridQualityFormItems;

    private String predicate;

    private Textbox txtFilter;

    public QualityFormCRUDController() {
        if ( qualityFormModel == null ) {
            qualityFormModel = (IQualityFormModel) SpringUtil.getBean("qualityFormModel");
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        txtFilter = (Textbox) listWindow.getFellowIfAny("txtFilter");
        gridQualityFormItems = (Grid) editWindow.getFellowIfAny("gridQualityFormItems");
        gridQualityForms = (Grid) listWindow.getFellowIfAny("qualityFormsList");
    }

    /**
     * Return all {@link QualityForm}.
     *
     * @return {@link List<QualityForm>}
     */
    public List<QualityForm> getQualityForms() {
        return qualityFormModel.getQualityForms(predicate);
    }

    /**
     * Return current {@link QualityForm}.
     *
     * @return {@link QualityForm}
     */
    public QualityForm getQualityForm() {
        return qualityFormModel.getQualityForm();
    }

    /**
     * Return all {@link QualityFormItem} assigned to the current {@link QualityForm}.
     *
     * @return {@link List<QualityFormItem>}
     */
    public List<QualityFormItem> getQualityFormItems() {
        return qualityFormModel.getQualityFormItems();
    }

    @Override
    protected void beforeSaving() {
        super.beforeSaving();
        validateReportProgress();
    }

    @Override
    protected void save() {
        qualityFormModel.confirmSave();
    }

    public void createQualityFormItem() {
        qualityFormModel.addQualityFormItem();
        Util.reloadBindings(gridQualityFormItems);

        // After adding a new row, model might be disordered, so we force it to
        // sort again respecting previous settings
        forceSortGridQualityFormItems();
    }

    /**
     * Sorts {@link Grid} model by first column, respecting sort order.
     *
     * FIXME: This is a temporary solution
     * There should be a better/smarter way Preserving order in the Grid every time a new element is added to its model.
     */
    private void forceSortGridQualityFormItems() {
        Column column = (Column) gridQualityFormItems.getColumns().getChildren().get(2);
        Sortable model = (Sortable) gridQualityFormItems.getModel();
        if ("ascending".equals(column.getSortDirection())) {
            model.sort(column.getSortAscending(), true);
        }
        if ("descending".equals(column.getSortDirection())) {
            model.sort(column.getSortDescending(), false);
        }
    }

    /**
     * Pop up confirm remove dialog.
     *
     * @param item
     */
    public void confirmDeleteQualityFormItem(QualityFormItem item) {
        if (qualityFormModel.isTotalPercentage(item)) {

            if (Messagebox.show(
                    _("Deleting this item will disable the report progress option. Are you sure?"), _("Confirm"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION) == Messagebox.OK) {

                Checkbox reportProgress = (Checkbox) editWindow.getFellowIfAny("checkBoxReportProgress");
                disabledCheckboxReportProgress(reportProgress);
            } else {
                return;
            }
        }
        deleteQualityFormItem(item);
    }

    private void deleteQualityFormItem(QualityFormItem item) {
        qualityFormModel.confirmDeleteQualityFormItem(item);
        Util.reloadBindings(gridQualityFormItems);
    }


    public QualityFormType[] getQualityFormTypes() {
        return QualityFormType.values();
    }

    public void onChangeQualityFormItemPercentage() {
        // It must update the order of the items if it is necessary
        getQualityForm().updateAndSortQualityFormItem();
        Util.reloadBindings(gridQualityFormItems);
    }

    public Constraint checkConstraintUniqueQualityFormName() {
        return (comp, value) -> {
            getQualityForm().setName((String) value);
            if ((value == null) || ((String)value).isEmpty()) {
                throw new WrongValueException(comp, _(CANNOT_BE_EMPTY));
            } else if (!qualityFormModel.checkConstraintUniqueQualityFormName()) {
                getQualityForm().setName(null);
                throw new WrongValueException(comp, _("{0} already exists", value));
            }
        };
    }

    public Constraint checkConstraintQualityFormItemName() {
        return (comp, value) -> {
            QualityFormItem item = ((Row) comp.getParent()).getValue();
            item.setName((String)value);
            if ((value == null) || ((String) value).isEmpty()) {
                item.setName(null);
                throw new WrongValueException(comp, _(CANNOT_BE_EMPTY));
            } else if (!qualityFormModel.checkConstraintUniqueQualityFormItemName()) {
                item.setName(null);
                throw new WrongValueException(comp, _("{0} already exists", value));
            }
        };
    }

    public Constraint checkConstraintQualityFormItemPercentage() {
        return (comp, value) -> {

            QualityFormItem item = ((Row) comp.getParent()).getValue();
            BigDecimal newPercentage = (BigDecimal) value;
            item.setPercentage(newPercentage);

            if (newPercentage == null) {
                item.setPercentage(null);
                throw new WrongValueException(comp, _(CANNOT_BE_EMPTY));
            }
            if (qualityFormModel.checkConstraintOutOfRangeQualityFormItemPercentage(item)) {
                item.setPercentage(null);
                throw new WrongValueException(comp, _("percentage should be between 1 and 100"));
            }
            if (!qualityFormModel.checkConstraintUniqueQualityFormItemPercentage()) {
                item.setPercentage(null);
                throw new WrongValueException(comp, _("percentage must be unique"));
            }
        };
    }

    public boolean isByPercentage() {
        return this.getQualityForm() != null && getQualityForm().getQualityFormType().equals(QualityFormType.BY_PERCENTAGE);
    }

    public boolean isByItems() {
        return this.getQualityForm() == null || getQualityForm().getQualityFormType().equals(QualityFormType.BY_ITEMS);
    }

    public void changeQualityFormType() {
        Util.reloadBindings(gridQualityFormItems);
    }

    public void downQualityFormItem(QualityFormItem qualityFormItem) {
        qualityFormModel.downQualityFormItem(qualityFormItem);
        Util.reloadBindings(gridQualityFormItems);
    }

    public void upQualityFormItem(QualityFormItem qualityFormItem) {
        qualityFormModel.upQualityFormItem(qualityFormItem);
        Util.reloadBindings(gridQualityFormItems);
    }

    /**
     * Apply filter to quality forms.
     */
    public void onApplyFilter() {
        // Filter quality forms by name
        predicate = getSelectedName();
        Util.reloadBindings(gridQualityForms);
    }

    private String getSelectedName() {
        return txtFilter.getValue();
    }

    public void validateReportProgress() {
        if ((getQualityForm().isReportAdvance()) && (!hasItemWithTotalPercentage())) {
            Checkbox checkBoxReportProgress = (Checkbox) editWindow.getFellowIfAny("checkBoxReportProgress");

            throw new WrongValueException(
                    checkBoxReportProgress,
                    _("Quality form should include an item with a value of 100% in order to report progress"));
        }
    }

    private boolean hasItemWithTotalPercentage() {
        return this.qualityFormModel.hasItemWithTotalPercentage();
    }

    private void disabledCheckboxReportProgress(Checkbox reportProgress) {
        if (reportProgress != null) {
            getQualityForm().setReportAdvance(false);
            reportProgress.setChecked(false);
            reportProgress.invalidate();
        }
    }

    @Override
    protected String getEntityType() {
        return _("Quality Form");
    }

    @Override
    protected String getPluralEntityType() {
        return _("Quality Forms");
    }

    @Override
    protected void initCreate() {
        qualityFormModel.initCreate();
    }


    @Override
    protected void initEdit(QualityForm qualityForm) {
        qualityFormModel.initEdit(qualityForm);
    }

    @Override
    protected QualityForm getEntityBeingEdited() {
        return qualityFormModel.getQualityForm();
    }

    @Override
    protected void delete(QualityForm qualityForm) throws InstanceNotFoundException {
        qualityFormModel.confirmDelete(qualityForm);
        Grid qualityForms = (Grid) listWindow.getFellowIfAny("qualityFormsList");
        if (qualityForms != null) {
            Util.reloadBindings(qualityForms);
        }
    }

    @Override
    protected boolean beforeDeleting(QualityForm qualityForm){
        return !isReferencedByOtherEntities(qualityForm);
    }

    private boolean isReferencedByOtherEntities(QualityForm qualityForm) {
        try {
            qualityFormModel.checkHasTasks(qualityForm);
            return false;
        } catch (ValidationException e) {
            showCannotDeleteQualityFormDialog(e.getInvalidValue().getMessage());
        }
        return true;
    }

    private void showCannotDeleteQualityFormDialog(String message) {
        Messagebox.show(_(message), _("Warning"), Messagebox.OK, Messagebox.EXCLAMATION);
    }
}
