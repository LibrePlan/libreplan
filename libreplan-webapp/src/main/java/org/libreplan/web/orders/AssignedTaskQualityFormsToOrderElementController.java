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

import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.common.exceptions.ValidationException.InvalidValue;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.qualityforms.entities.TaskQualityForm;
import org.libreplan.business.qualityforms.entities.TaskQualityFormItem;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.ConstraintChecker;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Row;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Constraint;

import java.util.Date;
import java.util.List;

import com.libreplan.java.zk.components.customdetailrowcomponent.Detail;

import static org.libreplan.web.I18nHelper._;

/**
 * Controller for showing OrderElement assigned task quality forms.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AssignedTaskQualityFormsToOrderElementController extends GenericForwardComposer {

    private static final String ITEM = "item";

    private static final String ASCENDING = "ascending";

    private static final String DELETE_ACTION = "Delete";

    private IAssignedTaskQualityFormsToOrderElementModel assignedTaskQualityFormsToOrderElementModel;

    private Grid assignedTaskQualityForms;

    private BandboxSearch bdQualityForms;

    private TaskQualityFormItemsRowRenderer taskQualityFormItemsRowRenderer = new TaskQualityFormItemsRowRenderer();

    private TaskQualityFormsRowRenderer taskQualityFormsRowRenderer = new TaskQualityFormsRowRenderer();

    private IMessagesForUser messages;

    private Component messagesContainerTaskQualityForms;

    private IOrderElementModel orderElementModel;

    public AssignedTaskQualityFormsToOrderElementController() {
        if ( assignedTaskQualityFormsToOrderElementModel == null ) {
            assignedTaskQualityFormsToOrderElementModel = (IAssignedTaskQualityFormsToOrderElementModel)
                    SpringUtil.getBean("assignedTaskQualityFormsToOrderElementModel");
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("assignedTaskQualityFormsController", this, true);
        messages = new MessagesForUser(messagesContainerTaskQualityForms);
        this.reloadTaskQualityForms();
    }

    public void openWindow(IOrderElementModel orderElementModel) {
        setOrderElementModel(orderElementModel);
        openWindow(getOrderElement());
    }

    private void openWindow(OrderElement orderElement) {
        assignedTaskQualityFormsToOrderElementModel.init(orderElement);

        // Clear components
        bdQualityForms.clear();
        Util.createBindingsFor(self);
        Util.reloadBindings(self);
        reloadTaskQualityForms();
    }

    public void setOrderElementModel(IOrderElementModel orderElementModel) {
        this.orderElementModel = orderElementModel;
        setOrderElement(orderElementModel.getOrderElement());
        setOrderModel(orderElementModel.getOrderModel());
    }

    private void setOrderModel(IOrderModel orderModel) {
        if ( assignedTaskQualityFormsToOrderElementModel != null ) {
            assignedTaskQualityFormsToOrderElementModel.setOrderModel(orderModel);
        }
    }

    public OrderElement getOrderElement() {
        return orderElementModel.getOrderElement();
    }

    public void setOrderElement(OrderElement orderElement) {
        if (assignedTaskQualityFormsToOrderElementModel != null) {
            assignedTaskQualityFormsToOrderElementModel.setOrderElement(orderElement);
        }
    }

    /**
     * Executed on pressing Assign button Adds selected quality form to task quality form list.
     */
    public void onAssignTaskQualityForm() {
        BandboxSearch qualityFormFinder = bdQualityForms;
        QualityForm qualityForm = retrieveQualityFormFrom(qualityFormFinder, new ICheckQualityFormAssigned() {
            @Override
            public boolean isAssigned(QualityForm qualityForm) {
                return AssignedTaskQualityFormsToOrderElementController.this.isAssigned(qualityForm);
            }
        });

        assignQualityForm(qualityForm);
    }

    @FunctionalInterface
    public interface ICheckQualityFormAssigned {
        boolean isAssigned(QualityForm qualityForm);
    }

    public static QualityForm retrieveQualityFormFrom(BandboxSearch qualityFormFinder,
                                                      ICheckQualityFormAssigned checkQualityFormAssigned) {

        QualityForm qualityForm = (QualityForm) qualityFormFinder.getSelectedElement();

        if (qualityForm == null) {
            throw new WrongValueException(qualityFormFinder, _("please, select a quality form"));
        }

        if (checkQualityFormAssigned.isAssigned(qualityForm)) {
            throw new WrongValueException(qualityFormFinder, _("already assigned"));
        }
        qualityFormFinder.clear();

        return qualityForm;
    }

    private void assignQualityForm(QualityForm qualityForm) {
        assignedTaskQualityFormsToOrderElementModel.assignTaskQualityForm(qualityForm);
        reloadTaskQualityForms();
    }

    private boolean isAssigned(QualityForm qualityForm) {
        return assignedTaskQualityFormsToOrderElementModel.isAssigned(qualityForm);
    }

    public void confirmRemove(TaskQualityForm taskQualityForm) {
        int status = Messagebox.show(
                _("Confirm deleting {0}. Are you sure?", getTaskQualityFormName(taskQualityForm)),
                _(DELETE_ACTION), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);

        if (Messagebox.OK == status) {
            deleteTaskQualityForm(taskQualityForm);
        }
    }

    private String getTaskQualityFormName(TaskQualityForm taskQualityForm) {
        return taskQualityForm == null || taskQualityForm.getQualityForm() == null
                ? ITEM
                : taskQualityForm.getQualityForm().getName();
    }

    private void deleteTaskQualityForm(TaskQualityForm taskQualityForm) {
        try {
            assignedTaskQualityFormsToOrderElementModel.removeAdvanceAssignmentIfNeeded(taskQualityForm);
        } catch (ValidationException e) {
            showInformativeMessage(e.getMessage());

            return;
        }
        assignedTaskQualityFormsToOrderElementModel.deleteTaskQualityForm(taskQualityForm);
        reloadTaskQualityForms();
    }

    /**
     * It should be public!
     */
    public List<TaskQualityForm> getTaskQualityForms() {
        return assignedTaskQualityFormsToOrderElementModel.getTaskQualityForms();
    }

    /**
     * It should be public!
     */
    public List<QualityForm> getNotAssignedQualityForms() {
        return assignedTaskQualityFormsToOrderElementModel.getNotAssignedQualityForms();
    }

    private void reloadTaskQualityForms() {
        Util.reloadBindings(bdQualityForms);
        Util.reloadBindings(assignedTaskQualityForms);
        assignedTaskQualityForms.invalidate();
    }

    /**
     * It should be public!
     */
    public void sortTaskQualityForms() {
        Column columnName = (Column) assignedTaskQualityForms.getColumns().getChildren().get(1);
        if (columnName != null) {
            if (columnName.getSortDirection().equals(ASCENDING)) {
                columnName.sort(false, false);
                columnName.setSortDirection(ASCENDING);
            } else if ("descending".equals(columnName.getSortDirection())) {
                columnName.sort(true, false);
                columnName.setSortDirection("descending");
            }
        }
    }

    /**
     * It should be public!
     */
    public TaskQualityFormsRowRenderer getTaskQualityFormsRowRenderer() {
        return taskQualityFormsRowRenderer;
    }

    public class TaskQualityFormsRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data, int i) {
            TaskQualityForm taskQualityForm = (TaskQualityForm) data;
            row.setValue(taskQualityForm);

            appendDetails(row, taskQualityForm);
            appendNewLabel(row, taskQualityForm.getQualityForm().getName());
            appendNewLabel(row, _(taskQualityForm.getQualityForm().getQualityFormType().toString()));
            appendCheckboxReportAdvance(row, taskQualityForm);
            appendOperations(row);
        }

        private void appendCheckboxReportAdvance(Row row, final TaskQualityForm taskQualityForm) {
            final Checkbox tmpCheckbox = new Checkbox();
            Checkbox checkbox = Util.bind(
                    tmpCheckbox,
                    taskQualityForm::isReportAdvance,
                    value -> {
                        try {
                            if (value) {
                                assignedTaskQualityFormsToOrderElementModel
                                        .addAdvanceAssignmentIfNeeded(taskQualityForm);
                            } else {
                                try {
                                    assignedTaskQualityFormsToOrderElementModel
                                            .removeAdvanceAssignmentIfNeeded(taskQualityForm);

                                } catch (ValidationException e) {
                                    showInformativeMessage(e.getMessage());
                                    return;
                                }
                            }
                            taskQualityForm.setReportAdvance(value);
                        } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
                            throw new RuntimeException(e);
                        } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
                            messages.showMessage(
                                    Level.ERROR,
                                    _("Another task in the same branch is already reporting progress" +
                                            " for this quality form"));

                            tmpCheckbox.setChecked(false);
                        }
                    });

            if (!taskQualityForm.getQualityForm().isReportAdvance()) {
                checkbox.setDisabled(true);
            }

            row.appendChild(checkbox);
        }

        private void appendDetails(Row row, TaskQualityForm taskQualityForm) {
            Detail details = new Detail();
            details.setParent(row);
            details.appendChild(appendGridItems(taskQualityForm));
            details.setOpen(false);
        }

        private Grid appendGridItems(TaskQualityForm taskQualityForm) {
            Grid gridItems = new Grid();

            gridItems.setMold("paging");
            gridItems.setPageSize(5);
            gridItems.setSizedByContent(false);

            renderColumns(gridItems);

            gridItems.setRowRenderer(getTaskQualityFormItemsRowRenderer());
            gridItems.setModel(new SimpleListModel<>(taskQualityForm.getTaskQualityFormItems().toArray()));

            return gridItems;
        }

        private void renderColumns(Grid gridItems) {

            Columns columns = gridItems.getColumns();

            // Create ListHead first time is rendered
            if (columns == null) {
                columns = new Columns();
            }

            // Delete all headers
            columns.getChildren().clear();
            columns.setSizable(true);

            // Add static headers
            Column columnName = new Column();
            columnName.setLabel(_("Name"));
            Util.setSort(columnName, "auto=(name)");
            columnName.setSortDirection(ASCENDING);
            columns.appendChild(columnName);

            Column columnPosition = new Column();
            columnPosition.setLabel(_("Position"));
            columns.appendChild(columnPosition);

            Column columnPercentage = new Column();
            columnPercentage.setLabel(_("Percentage"));
            columns.appendChild(columnPercentage);

            Column columnPassed = new Column();
            columnPassed.setLabel(_("Checked"));
            columns.appendChild(columnPassed);

            Column columnDate = new Column();
            columnDate.setLabel(_("Date"));
            columns.appendChild(columnDate);

            columns.setParent(gridItems);
        }

        private void appendOperations(final Row row) {
            Button buttonRemove = new Button();
            buttonRemove.setParent(row);
            buttonRemove.setClass("icono");
            buttonRemove.setImage("/common/img/ico_borrar1.png");
            buttonRemove.setHoverImage("/common/img/ico_borrar.png");
            buttonRemove.setTooltiptext(_(DELETE_ACTION));

            buttonRemove.addEventListener(Events.ON_CLICK, event -> confirmRemove(row.getValue()));
        }

        private TaskQualityFormItemsRowRenderer getTaskQualityFormItemsRowRenderer() {
            return taskQualityFormItemsRowRenderer;
        }
    }

    private void showInformativeMessage(String message) {
        Messagebox.show(_(message), _(DELETE_ACTION), Messagebox.OK, Messagebox.ERROR);
    }

    private class TaskQualityFormItemsRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data, int i) {
            TaskQualityFormItem item = (TaskQualityFormItem) data;
            row.setValue(item);

            appendNewLabel(row, item.getName());
            appendNewLabel(row, item.getStringPosition());
            appendNewLabel(row, item.getPercentage().toString());
            appendCheckPassed(row);
            appendDate(row);
        }
        private void appendDate(final Row row) {
            Datebox date = new Datebox();
            date.setParent(row);

            final TaskQualityForm taskQualityForm = getTaskQualityFormByRow(row);
            final TaskQualityFormItem item = row.getValue();

            Util.bind(
                    date,
                    item::getDate,
                    value -> {
                        item.setDate(value);
                        updateAdvancesIfNeeded();
                    });

            date.setDisabled(assignedTaskQualityFormsToOrderElementModel.isDisabledDateItem(taskQualityForm, item));
            date.setConstraint(checkConsecutiveDate(row));
        }

        private void appendCheckPassed(final Row row) {
            Checkbox checkbox = new Checkbox();
            checkbox.setParent(row);

            final TaskQualityForm taskQualityForm = getTaskQualityFormByRow(row);
            final TaskQualityFormItem item = row.getValue();

            Util.bind(
                    checkbox,
                    item::getPassed,
                    value -> {
                        item.setPassed(value);
                        updateAdvancesIfNeeded();
                    });

            checkbox.setDisabled(
                    assignedTaskQualityFormsToOrderElementModel.isDisabledPassedItem(taskQualityForm, item));

            if (!taskQualityForm.isByItems()) {
                checkbox.addEventListener(Events.ON_CHECK, event -> {
                    assignedTaskQualityFormsToOrderElementModel.updatePassedTaskQualityFormItems(taskQualityForm);
                    Grid gridItems = row.getGrid();
                    gridItems.setModel(new SimpleListModel(taskQualityForm.getTaskQualityFormItems().toArray()));
                    gridItems.invalidate();
                });
            }
        }

        private Constraint checkConsecutiveDate(final Row row) {
            return (comp, value) -> {

                final TaskQualityFormItem item = row.getValue();
                final TaskQualityForm taskQualityForm = getTaskQualityFormByRow(row);

                if (taskQualityForm != null) {
                    item.setDate((Date) value);

                    if ((value == null) && (!item.isIfDateCanBeNullConstraint())) {
                        item.setDate(null);
                        throw new WrongValueException(comp, _("date not specified"));
                    }
                    if (!assignedTaskQualityFormsToOrderElementModel.isCorrectConsecutiveDate(taskQualityForm, item)) {
                        item.setDate(null);
                        throw new WrongValueException(comp, _("must be after the previous date"));
                    }
                }
            };
        }

        private TaskQualityForm getTaskQualityFormByRow(final Row row) {
            try {
                return (TaskQualityForm) ((Row) row.getGrid().getParent().getParent()).getValue();
            } catch (Exception e) {
                return null;
            }
        }
    }


    private void appendNewLabel(Row row, String label) {
        Label labelName = new Label();
        labelName.setParent(row);
        labelName.setValue(label);
    }

    /**
     * Operations to confirm and validate.
     */

    public boolean confirm() {
        updateAdvancesIfNeeded();
        boolean result = validate();
        validateConstraints();
        return result;
    }

    private void updateAdvancesIfNeeded() {
        assignedTaskQualityFormsToOrderElementModel.updateAdvancesIfNeeded();
    }

    private void validateConstraints() {
        ConstraintChecker.isValid(self);
    }

    /**
     * Shows invalid values for {@link CriterionSatisfaction} entities.
     */
    private boolean validate() {
        try {
            assignedTaskQualityFormsToOrderElementModel.validate();
        } catch (ValidationException e) {
            showInvalidValues(e);

            return false;
        } catch (IllegalStateException e) {
            messages.showMessage(Level.ERROR, e.getMessage());

            return false;
        } catch (IllegalArgumentException e) {
            messages.showMessage(Level.ERROR, e.getMessage());

            return false;
        }

        return true;
    }

    /**
     * Shows invalid values for {@link TaskQualityForm} entities.
     *
     * @param e
     */
    private void showInvalidValues(ValidationException e) {
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            Object value = invalidValue.getRootBean();
            if (value instanceof TaskQualityForm) {
                showInvalidValue(invalidValue, (TaskQualityForm) value);
            }
        }
    }

    private void showInvalidValue(InvalidValue invalidValue, TaskQualityForm taskQualityForm) {
        if (assignedTaskQualityForms != null) {

            // Find which row contains TaskQualityForm inside grid
            Row row = findRowOfTaskQualityForm(assignedTaskQualityForms.getRows().<Row>getChildren(), taskQualityForm);

            if (row != null && invalidValue.getInvalidValue() instanceof String) {
                String itemName = (String) invalidValue.getInvalidValue();
                String propertyName = invalidValue.getPropertyPath();
                Row rowItem = findRowOfTaskQualityFormItem(row, itemName);

                if (rowItem != null) {
                    if (TaskQualityFormItem.propertyDate.equals(propertyName)) {
                        openDetails(rowItem);
                        Datebox datebox = getDatebox(rowItem);
                        throw new WrongValueException(datebox, _(invalidValue.getMessage()));
                    }

                    if (TaskQualityFormItem.propertyPassed.equals(propertyName)) {
                        openDetails(rowItem);
                        Checkbox checkbox = getCheckbox(rowItem);
                        throw new WrongValueException(checkbox, _(invalidValue.getMessage()));
                    }
                }
            }
        }
    }

    private Row findRowOfTaskQualityForm(List<Row> rows, TaskQualityForm taskQualityForm) {
        for (Row row : rows) {
            if (taskQualityForm.equals(row.getValue())) {
                return row;
            }
        }

        return null;
    }

    private Row findRowOfTaskQualityFormItem(Row rowTaskQualityForm, String itemName) {
        Grid gridItems = (Grid) rowTaskQualityForm.getFirstChild().getFirstChild();
        List<Row> rows = gridItems.getRows().getChildren();
        for (Row row : rows) {
            TaskQualityFormItem item = row.getValue();
            if ((item != null) && (itemName.equals(item.getName()))) {
                return row;
            }
        }

        return null;
    }

    private void openDetails(Row row) {
        Detail details = getDetails(row);
        if (details != null) {
            details.setOpen(true);
            details.invalidate();
            assignedTaskQualityForms.invalidate();
        }
    }

    private Detail getDetails(Row row) {
        return row.getValue() instanceof TaskQualityForm
                ? (Detail) row.getFirstChild()
                : (Detail) row.getGrid().getParent();
    }

    private Datebox getDatebox(Row row) {
        return (Datebox) row.getChildren().get(4);
    }

    private Checkbox getCheckbox(Row row) {
        return (Checkbox) row.getChildren().get(3);
    }

}
