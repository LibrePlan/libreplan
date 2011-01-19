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

package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.TaskQualityForm;
import org.navalplanner.business.qualityforms.entities.TaskQualityFormItem;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;

/**
 * Controller for showing OrderElement assigned task quality forms
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AssignedTaskQualityFormsToOrderElementController extends
        GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(AssignedTaskQualityFormsToOrderElementController.class);

    private IMessagesForUser messagesForUser;

    private static final String ITEM = "item";

    private IAssignedTaskQualityFormsToOrderElementModel assignedTaskQualityFormsToOrderElementModel;

    private Grid assignedTaskQualityForms;

    private BandboxSearch bdQualityForms;

    private TaskQualityFormItemsRowRenderer taskQualityFormItemsRowRenderer = new TaskQualityFormItemsRowRenderer();

    private TaskQualityFormsRowRenderer taskQualityFormsRowRenderer = new TaskQualityFormsRowRenderer();

    private IMessagesForUser messages;

    private Component messagesContainerTaskQualityForms;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("assignedTaskQualityFormsController", this, true);
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

    IOrderElementModel orderElementModel;

    public void setOrderElementModel(IOrderElementModel orderElementModel) {
        this.orderElementModel = orderElementModel;
        setOrderElement(orderElementModel.getOrderElement());
        setOrderModel(orderElementModel.getOrderModel());
    }

    private void setOrderModel(IOrderModel orderModel) {
        if (assignedTaskQualityFormsToOrderElementModel != null) {
            assignedTaskQualityFormsToOrderElementModel
                    .setOrderModel(orderModel);
        }
    }

    public OrderElement getOrderElement() {
        return orderElementModel.getOrderElement();
    }

    public void setOrderElement(OrderElement orderElement) {
        if (assignedTaskQualityFormsToOrderElementModel != null) {
            assignedTaskQualityFormsToOrderElementModel
                    .setOrderElement(orderElement);
        }
    }

    /**
     * Executed on pressing Assign button Adds selected quality form to task
     * quality form list
     */
    public void onAssignTaskQualityForm() {
        BandboxSearch qualityFormFinder = bdQualityForms;
        QualityForm qualityForm = retrieveQualityFormFrom(qualityFormFinder,
                new ICheckQualityFormAssigned() {

                    @Override
                    public boolean isAssigned(QualityForm qualityForm) {
                        return AssignedTaskQualityFormsToOrderElementController.this
                                .isAssigned(qualityForm);
                    }
                });
        assignQualityForm(qualityForm);
    }

    public interface ICheckQualityFormAssigned {
        public boolean isAssigned(QualityForm qualityForm);
    }

    public static QualityForm retrieveQualityFormFrom(
            BandboxSearch qualityFormFinder,
            ICheckQualityFormAssigned checkQualityFormAssigned) {
        QualityForm qualityForm = (QualityForm) qualityFormFinder
                .getSelectedElement();
        if (qualityForm == null) {
            throw new WrongValueException(qualityFormFinder,
                    _("please, select a quality form"));
        }
        if (checkQualityFormAssigned.isAssigned(qualityForm)) {
            throw new WrongValueException(qualityFormFinder,
                    _("already assigned"));
        }
        qualityFormFinder.clear();
        return qualityForm;
    }

    public void clear() {

    }

    private void assignQualityForm(QualityForm qualityForm) {
        assignedTaskQualityFormsToOrderElementModel
                .assignTaskQualityForm(qualityForm);
        reloadTaskQualityForms();
    }

    private boolean isAssigned(QualityForm qualityForm) {
        return assignedTaskQualityFormsToOrderElementModel
                .isAssigned(qualityForm);
    }

    public void confirmRemove(TaskQualityForm taskQualityForm) {
        try {
            int status = Messagebox.show(_(
                    "Confirm deleting {0}. Are you sure?",
                    getTaskQualityFormName(taskQualityForm)), _("Delete"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                deleteTaskQualityForm(taskQualityForm);
            }
        } catch (InterruptedException e) {
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
            LOG.error(_("Error on showing removing element: ", taskQualityForm
                    .getId()), e);
        }
    }

    private String getTaskQualityFormName(TaskQualityForm taskQualityForm) {
        if (taskQualityForm == null || taskQualityForm.getQualityForm() == null) {
            return ITEM;
        }
        return taskQualityForm.getQualityForm().getName();
    }

    public void deleteTaskQualityForm(TaskQualityForm taskQualityForm) {
        assignedTaskQualityFormsToOrderElementModel
                .deleteTaskQualityForm(taskQualityForm);
        reloadTaskQualityForms();
    }

    public List<TaskQualityForm> getTaskQualityForms() {
        return assignedTaskQualityFormsToOrderElementModel
                .getTaskQualityForms();
    }

    public List<QualityForm> getNotAssignedQualityForms() {
        return assignedTaskQualityFormsToOrderElementModel
                .getNotAssignedQualityForms();
    }

    private void reloadTaskQualityForms() {
        Util.reloadBindings(bdQualityForms);
        Util.reloadBindings(assignedTaskQualityForms);
        assignedTaskQualityForms.invalidate();
    }

    public void sortTaskQualityForms() {
        Column columnName = (Column) assignedTaskQualityForms.getColumns()
                .getChildren().get(1);
        if (columnName != null) {
            if (columnName.getSortDirection().equals("ascending")) {
                columnName.sort(false, false);
                columnName.setSortDirection("ascending");
            } else if (columnName.getSortDirection().equals("descending")) {
                columnName.sort(true, false);
                columnName.setSortDirection("descending");
            }
        }
    }

    public void close() {
    }

    public TaskQualityFormsRowRenderer getTaskQualityFormsRowRenderer() {
        return taskQualityFormsRowRenderer;
    }

    public class TaskQualityFormsRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            TaskQualityForm taskQualityForm = (TaskQualityForm) data;
            row.setValue(taskQualityForm);

            appendDetails(row, taskQualityForm);
            appendNewLabel(row, taskQualityForm.getQualityForm().getName());
            appendNewLabel(row, taskQualityForm.getQualityForm()
                    .getQualityFormType().toString());
            appendCheckboxReportAdvance(row, taskQualityForm);
            appendOperations(row);
        }

        private void appendCheckboxReportAdvance(Row row,
                final TaskQualityForm taskQualityForm) {
            final Checkbox tmpCheckbox = new Checkbox();
            Checkbox checkbox = Util.bind(tmpCheckbox,
                    new Util.Getter<Boolean>() {
                        @Override
                        public Boolean get() {
                            return taskQualityForm.isReportAdvance();
                        }
                    }, new Util.Setter<Boolean>() {
                        @Override
                        public void set(Boolean value) {
                            try {
                                if (value) {
                                    assignedTaskQualityFormsToOrderElementModel
                                            .addAdvanceAssignmentIfNeeded(taskQualityForm);
                                } else {
                                    assignedTaskQualityFormsToOrderElementModel
                                            .removeAdvanceAssignmentIfNeeded(taskQualityForm);
                                }

                                taskQualityForm.setReportAdvance(value);
                            } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
                                throw new RuntimeException(e);
                            } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
                                messages
                                        .showMessage(
                                                Level.ERROR,
                                                _("Another task in the same branch is already reporting progress for this quality form"));
                                tmpCheckbox.setChecked(false);
                            }
                        }
                    });

            if (!taskQualityForm.getQualityForm().isReportAdvance()) {
                checkbox.setDisabled(true);
            }

            row.appendChild(checkbox);
        }
    }

    private void appendDetails(Row row, TaskQualityForm taskQualityForm) {
        Detail details = new Detail();
        details.setParent(row);
        details.appendChild(appendGridItems(row, taskQualityForm));
        details.setOpen(false);
    }

    private Grid appendGridItems(Row row, TaskQualityForm taskQualityForm) {
        Grid gridItems = new Grid();

        gridItems.setMold("paging");
        gridItems.setPageSize(5);
        gridItems.setFixedLayout(true);

        renderColumns(gridItems);

        gridItems.setRowRenderer(getTaskQualityFormItemsRowRenderer());
        gridItems.setModel(new SimpleListModel(taskQualityForm
                .getTaskQualityFormItems().toArray()));

        return gridItems;
    }

    private void renderColumns(Grid gridItems) {

        Columns columns = gridItems.getColumns();
        // Create listhead first time is rendered
        if (columns == null) {
            columns = new Columns();
        }
        // Delete all headers
        columns.getChildren().clear();
        columns.setSizable(true);

        // Add static headers
        Column columnName = new Column();
        columnName.setLabel(_("Name"));
        columnName.setSort("auto=(name)");
        columnName.setSortDirection("ascending");
        columns.appendChild(columnName);

        Column columnPosition = new Column();
        columnPosition.setLabel(_("Position"));
        columns.appendChild(columnPosition);

        Column columnPercentage = new Column();
        columnPercentage.setLabel(_("Percentage"));
        columns.appendChild(columnPercentage);

        Column columnPassed = new Column();
        columnPassed.setLabel(_("Passed"));
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
        buttonRemove.setTooltiptext(_("Delete"));

        buttonRemove.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                confirmRemove((TaskQualityForm) row.getValue());
            }
        });
    }

    public TaskQualityFormItemsRowRenderer getTaskQualityFormItemsRowRenderer() {
        return taskQualityFormItemsRowRenderer;
    }

    public class TaskQualityFormItemsRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            TaskQualityFormItem item = (TaskQualityFormItem) data;
            row.setValue(item);

            appendNewLabel(row, item.getName());
            appendNewLabel(row, item.getPosition().toString());
            appendNewLabel(row, item.getPercentage().toString());
            appendCheckPassed(row);
            appendDate(row);
        }
    }

    private void appendNewLabel(Row row, String label) {
        org.zkoss.zul.Label labelName = new org.zkoss.zul.Label();
        labelName.setParent(row);
        labelName.setValue(label);
    }

    private void appendDate(final Row row) {
        Datebox date = new Datebox();
        date.setParent(row);

        final TaskQualityForm taskQualityForm = getTaskQualityFormByRow(row);
        final TaskQualityFormItem item = (TaskQualityFormItem) row.getValue();

        Util.bind(date, new Util.Getter<Date>() {
            @Override
            public Date get() {
                return item.getDate();
            }
        }, new Util.Setter<Date>() {

            @Override
            public void set(Date value) {
                item.setDate(value);
            }
        });

        date.setDisabled(assignedTaskQualityFormsToOrderElementModel
                .isDisabledDateItem(taskQualityForm, item));
        date.setConstraint(checkConsecutiveDate(row));
    }

    private void appendCheckPassed(final Row row) {
        Checkbox checkbox = new Checkbox();
        checkbox.setParent(row);

        final TaskQualityForm taskQualityForm = getTaskQualityFormByRow(row);
        final TaskQualityFormItem item = (TaskQualityFormItem) row.getValue();

        Util.bind(checkbox, new Util.Getter<Boolean>() {
            @Override
            public Boolean get() {
                return item.getPassed();
            }
        }, new Util.Setter<Boolean>() {

            @Override
            public void set(Boolean value) {
                item.setPassed(value);
            }
        });

        checkbox.setDisabled(assignedTaskQualityFormsToOrderElementModel
                .isDisabledPassedItem(taskQualityForm, item));

        if (!taskQualityForm.isByItems()) {
            checkbox.addEventListener(Events.ON_CHECK, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    assignedTaskQualityFormsToOrderElementModel
                            .updatePassedTaskQualityFormItems(taskQualityForm);
                    Grid gridItems = row.getGrid();
                    gridItems.setModel(new SimpleListModel(taskQualityForm
                            .getTaskQualityFormItems().toArray()));
                    gridItems.invalidate();
                }
            });
        }
    }

    private Constraint checkConsecutiveDate(final Row row) {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {

                final TaskQualityFormItem item = (TaskQualityFormItem) row
                        .getValue();
                final TaskQualityForm taskQualityForm = getTaskQualityFormByRow(row);

                if (taskQualityForm != null) {
                    item.setDate((Date) value);

                    if (((Date) value == null)
                            && (!item.checkConstraintIfDateCanBeNull())) {
                        item.setDate(null);
                        throw new WrongValueException(comp,
                                _("date not specified."));
                    }
                    if (!assignedTaskQualityFormsToOrderElementModel
                            .isCorrectConsecutiveDate(taskQualityForm, item)) {
                        item.setDate(null);
                        throw new WrongValueException(
                                comp,
                                _("must be greater than the previous date."));
                    }
                }
            }
        };
    }

    private TaskQualityForm getTaskQualityFormByRow(final Row row) {
        try {
            return (TaskQualityForm) ((Row) row.getGrid().getParent()
                    .getParent()).getValue();
        } catch (Exception e) {
            return null;
        }
    }

    // Operations to confirm and validate

    public boolean confirm() {
        assignedTaskQualityFormsToOrderElementModel.updateAdvancesIfNeeded();
        boolean result = validate();
        validateConstraints();
        return result;
    }

    public void validateConstraints() {
        ConstraintChecker.isValid(self);
    }

    /**
     * Shows invalid values for {@link CriterionSatisfaction} entities
     * @param e
     */
    private boolean validate() throws ValidationException {
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
     * Shows invalid values for {@link TaskQualityForm} entities
     * @param e
     */
    private void showInvalidValues(ValidationException e) {
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            Object value = invalidValue.getBean();
            if (value instanceof TaskQualityForm) {
                showInvalidValue(invalidValue, (TaskQualityForm) value);
            }
        }
    }

    private void showInvalidValue(InvalidValue invalidValue,
            TaskQualityForm taskQualityForm) {
        if (assignedTaskQualityForms != null) {
            // Find which row contains TaskQualityForm inside grid
            Row row = findRowOfTaskQualityForm(assignedTaskQualityForms
                    .getRows().getChildren(), taskQualityForm);

            if (row != null) {
                String itemName = (String) invalidValue.getValue();
                String propertyName = invalidValue.getPropertyName();
                Row rowItem = findRowOfTaskQualityFormItem(row, itemName);

                if (rowItem != null) {
                    if (TaskQualityFormItem.propertyDate.equals(propertyName)) {
                        openDetails(rowItem);
                        Datebox datebox = getDatebox(rowItem);
                        throw new WrongValueException(datebox, invalidValue
                            .getMessage());
                    }
                    if (TaskQualityFormItem.propertyPassed.equals(propertyName)) {
                        openDetails(rowItem);
                        Checkbox checkbox = getCheckbox(rowItem);
                        throw new WrongValueException(checkbox, invalidValue
                                .getMessage());
                    }
                }
            }
        }
    }

    private Row findRowOfTaskQualityForm(List<Row> rows,
            TaskQualityForm taskQualityForm) {
        for (Row row : rows) {
            if (taskQualityForm.equals(row.getValue())) {
                return row;
            }
        }
        return null;
    }

    private Row findRowOfTaskQualityFormItem(Row rowTaskQualityForm,
            String itemName) {
        Grid gridItems = (Grid) rowTaskQualityForm.getFirstChild()
                .getFirstChild();
        List<Row> rows = (List<Row>) gridItems.getRows().getChildren();
        for (Row row : rows) {
            TaskQualityFormItem item = (TaskQualityFormItem) row.getValue();
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
        if (row.getValue() instanceof TaskQualityForm) {
            return ((Detail) row.getFirstChild());
        } else {
            return ((Detail) row.getGrid().getParent());
        }
    }

    private Datebox getDatebox(Row row) {
        return (Datebox) row.getChildren().get(4);
    }

    private Checkbox getCheckbox(Row row) {
        return (Checkbox) row.getChildren().get(3);
    }

}
