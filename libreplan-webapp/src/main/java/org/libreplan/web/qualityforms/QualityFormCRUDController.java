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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.qualityforms.entities.QualityFormItem;
import org.libreplan.business.qualityforms.entities.QualityFormType;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.Util;
import org.libreplan.web.users.ProfileCRUDController;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelExt;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;

/**
 * CRUD Controller for {@link QualityForm}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class QualityFormCRUDController extends BaseCRUDController<QualityForm> {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(QualityFormCRUDController.class);

    @Autowired
    private IQualityFormModel qualityFormModel;

    private Grid gridQualityForms;

    private Grid gridQualityFormItems;

    private String predicate;

    private Combobox cbFilterQualityFormName;

    private Textbox txtFilter;

    public QualityFormCRUDController() {

    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        txtFilter = (Textbox) listWindow.getFellowIfAny("txtFilter");
        cbFilterQualityFormName = (Combobox) listWindow
                .getFellowIfAny("cbFilterQualityFormName");
        gridQualityFormItems = (Grid) editWindow
                .getFellowIfAny("gridQualityFormItems");
        gridQualityForms = (Grid) listWindow.getFellowIfAny("qualityFormsList");
    }

    /**
     * Return all {@link QualityForm}
     * @return
     */
    public List<QualityForm> getQualityForms() {
        return qualityFormModel.getQualityForms(predicate);
    }

    /**
     * Return current {@link QualityForm}
     * @return
     */
    public QualityForm getQualityForm() {
        return qualityFormModel.getQualityForm();
    }

    /**
     * Return all {@link QualityFormItem} assigned to the current
     * {@link QualityForm}
     * @return
     */
    public List<QualityFormItem> getQualityFormItems() {
        return qualityFormModel.getQualityFormItems();
    }

    @Override
    protected void beforeSaving() throws ValidationException {
        validate();
    }

    @Override
    protected void save() throws ValidationException {
        qualityFormModel.confirmSave();
    }

    /**
     * Validates all {@link Textbox} in the form
     */
    private void validate() {
        Textbox boxName = (Textbox) editWindow
                .getFellowIfAny("qualityFormName");
        validate(boxName, boxName.getValue());

        validateReportProgress();

        List<Row> rows = gridQualityFormItems.getRows().getChildren();
        for (Row row : rows) {
            validate(row);
        }
    }

    @SuppressWarnings("unchecked")
    private void validate(Row row) {
        for (Iterator i = row.getChildren().iterator(); i.hasNext();) {
            final Component comp = (Component) i.next();
            if (comp instanceof Textbox) {
                validate((Textbox) comp, ((Textbox) comp).getValue());
            }
            if (comp instanceof Decimalbox) {
                validate((Decimalbox) comp, ((Decimalbox) comp).getValue());
            }
        }
    }

    /**
     * Validates {@link Textbox} checking {@link Constraint}
     * @param comp
     */
    private void validate(InputElement comp, Object value) {
        if (comp != null && comp.getConstraint() != null) {
            final Constraint constraint = comp.getConstraint();
            constraint.validate(comp, value);
        }
    }


    private void showInvalidValues(ValidationException e) {
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            Object value = invalidValue.getBean();
            if (value instanceof QualityForm) {
                validateQualityForm(invalidValue);
            }
            if (value instanceof QualityFormItem) {
                validateQualityFormItem(invalidValue);
            }
        }
    }

    private void validateQualityForm(InvalidValue invalidValue) {
        Component component = editWindow.getFellowIfAny("qualityFormName");
        if (component != null) {
            throw new WrongValueException(component, invalidValue.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void validateQualityFormItem(InvalidValue invalidValue) {
        Row rowItem = findQualityFormItem(gridQualityFormItems.getRows()
                .getChildren(), (QualityFormItem) invalidValue.getBean());
        if (rowItem != null) {
            InputElement inputElement = getInputText(rowItem, invalidValue
                    .getPropertyName());
            if (inputElement != null) {
                throw new WrongValueException(rowItem, invalidValue
                        .getMessage());
            }
        }
    }

    private Row findQualityFormItem(List<Row> rows, QualityFormItem item) {
        for (Row row : rows) {
            if (item.equals(row.getValue())) {
                return row;
            }
        }
        return null;
    }

    private InputElement getInputText(Row row, String property) {
        if (property != null) {
            if (property.equals(QualityFormItem.propertyName)) {
                return (InputElement) row.getFirstChild();
            }
            if (property.equals(QualityFormItem.propertyPercentage)) {
                return (InputElement) row.getChildren().get(2);
            }
        }
        return null;
    }

    public void createQualityFormItem() {
        qualityFormModel.addQualityFormItem();
        Util.reloadBindings(gridQualityFormItems);

        // After adding a new row, model might be disordered, so we force it to
        // sort again respecting previous settings
        forceSortGridQualityFormItems();
    }

    /**
     * Sorts {@link Grid} model by first column, respecting sort order
     *
     * FIXME: This is a temporary solution, there should be a better/smarter way
     * of preserving order in the Grid every time a new element is added to its
     * model
     */
    private void forceSortGridQualityFormItems() {
        Column column = (Column) gridQualityFormItems.getColumns()
                .getChildren().get(2);
        ListModelExt model = (ListModelExt) gridQualityFormItems.getModel();
        if ("ascending".equals(column.getSortDirection())) {
            model.sort(column.getSortAscending(), true);
        }
        if ("descending".equals(column.getSortDirection())) {
            model.sort(column.getSortDescending(), false);
        }
    }

    /**
     * Pop up confirm remove dialog
     * @param QualityFormItem
     */
    public void confirmDeleteQualityFormItem(QualityFormItem item) {
        if (qualityFormModel.isTotalPercentage(item)) {
            try {
                if (Messagebox
                        .show(
                                _("Deleting this item, it will disable the report progress option. Are you sure?"),
                                _("Confirm"),
                                Messagebox.OK | Messagebox.CANCEL,
                                Messagebox.QUESTION) == Messagebox.OK) {
                    Checkbox reportProgress = (Checkbox) editWindow
                            .getFellowIfAny("checkBoxReportProgress");
                    disabledCheckbocReportProgress(reportProgress);
                } else {
                    return;
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
        // it must update the order of the items if it is necessary.
        getQualityForm().updateAndSortQualityFormItem();
        Util.reloadBindings(gridQualityFormItems);
    }

    public Constraint checkConstraintUniqueQualityFormName() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                getQualityForm().setName((String) value);
                if(((String)value == null) || (((String)value)).isEmpty()){
                    throw new WrongValueException(comp, _("cannot be empty"));
                } else if (!qualityFormModel
                        .checkConstraintUniqueQualityFormName()) {
                    getQualityForm().setName(null);
                    throw new WrongValueException(comp, _("{0} already exists",
                            (String) value));
                }
            }
        };
    }

    public Constraint checkConstraintQualityFormItemName() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                QualityFormItem item = (QualityFormItem) ((Row) comp
                        .getParent()).getValue();
                item.setName((String)value);
                if (((String) value == null) || (((String) value)).isEmpty()) {
                    item.setName(null);
                    throw new WrongValueException(comp, _("cannot be empty"));
                } else if (!qualityFormModel
                        .checkConstraintUniqueQualityFormItemName()) {
                    item.setName(null);
                    throw new WrongValueException(comp, _("{0} already exists",
                            (String) value));
                }
            }
        };
    }

    public Constraint checkConstraintQualityFormItemPercentage() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {

                QualityFormItem item = (QualityFormItem) ((Row) comp
                        .getParent()).getValue();
                BigDecimal newPercentage = (BigDecimal) value;
                item.setPercentage(newPercentage);

                if (newPercentage == null) {
                    item.setPercentage(null);
                    throw new WrongValueException(comp, _("cannot be empty"));
                }
                if (qualityFormModel
                        .checkConstraintOutOfRangeQualityFormItemPercentage(item)) {
                    item.setPercentage(null);
                    throw new WrongValueException(comp,
                            _("percentage must be in range (0,100]"));
                }
                if (!qualityFormModel
                        .checkConstraintUniqueQualityFormItemPercentage()) {
                    item.setPercentage(null);
                    throw new WrongValueException(comp,
                            _("percentage cannot be duplicated"));
                }
            }
        };
    }

    public boolean isByPercentage() {
        if (this.getQualityForm() != null) {
            return getQualityForm().getQualityFormType().equals(
                    QualityFormType.BY_PERCENTAGE);
        }
        return false;
    }

    public boolean isByItems() {
        if (this.getQualityForm() != null) {
            return getQualityForm().getQualityFormType().equals(
                    QualityFormType.BY_ITEMS);
        }
        return true;
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
     * Apply filter to quality forms
     * @param event
     */
    public void onApplyFilter(Event event) {
        // Filter quality forms by name
        predicate = getSelectedName();
        Util.reloadBindings(gridQualityForms);
    }

    private String getSelectedName() {
        return txtFilter.getValue();
    }

    private void clearFilter() {
        txtFilter.setValue("");
        predicate = getSelectedName();
    }

    public void validateReportProgress() {
        if ((getQualityForm().getReportAdvance())
                && (!hasItemWithTotalPercentage())) {
            Checkbox checkBoxReportProgress = (Checkbox) editWindow
                    .getFellowIfAny("checkBoxReportProgress");
            throw new WrongValueException(
                    checkBoxReportProgress,
                    _("The quality form must have an item with 100% value to report progress"));
        }
    }

    private boolean hasItemWithTotalPercentage() {
        return this.qualityFormModel.hasItemWithTotalPercentage();
    }

    private void disabledCheckbocReportProgress(Checkbox reportProgress) {
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
        Grid qualityForms = (Grid) listWindow
                .getFellowIfAny("qualityFormsList");
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
            showCannotDeleteQualityFormDialog(e.getInvalidValue().getMessage(),
                    qualityForm);
        }
        return true;
    }

    private void showCannotDeleteQualityFormDialog(String message, QualityForm qualityForm) {
        try {
            Messagebox.show(_(message), _("Warning"), Messagebox.OK,
                    Messagebox.EXCLAMATION);
        } catch (InterruptedException e) {
            LOG.error(
                    _("Error on showing warning message removing qualityForm: ",
                            qualityForm.getId()), e);
        }
    }
}
