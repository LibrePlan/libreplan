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

package org.navalplanner.web.labels;

import static org.navalplanner.web.I18nHelper._;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.web.common.BaseCRUDController;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.ListModelExt;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Rows;

/**
 * CRUD Controller for {@link LabelType}
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class LabelTypeCRUDController extends BaseCRUDController<LabelType> {

    @Autowired
    private ILabelTypeModel labelTypeModel;

    private IMessagesForUser messagesEditWindow;

    private Grid gridLabelTypes;

    private Grid gridLabels;

    private Textbox newLabelTextbox;

    public LabelTypeCRUDController() {

    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesEditWindow = new MessagesForUser(editWindow
                .getFellowIfAny("messagesContainer"));
        initializeLabelsGrid();
        initializeLabelTypesGrid();
        newLabelTextbox = (Textbox) editWindow
                .getFellowIfAny("newLabelTextbox");
    }

    private void initializeLabelsGrid() {
        gridLabels = (Grid) editWindow.getFellowIfAny("gridLabels");
        // Renders grid and enables delete button if label is new
        gridLabels.addEventListener("onInitRender", new EventListener() {

            @Override
            public void onEvent(Event event) {
                gridLabels.renderAll();

                final Rows rows = gridLabels.getRows();
                for (Iterator i = rows.getChildren().iterator(); i.hasNext();) {
                    final Row row = (Row) i.next();
                    final Label label = (Label) row.getValue();
                    Button btnDelete = (Button) row.getChildren().get(2);
                    if (!canRemoveLabel(label)) {
                        btnDelete.setDisabled(true);
                        btnDelete.setImage("/common/img/ico_borrar_out.png");
                        btnDelete
                                .setHoverImage("/common/img/ico_borrar_out.png");
                        btnDelete.setTooltiptext("");
                    }
                }
            }

            private boolean canRemoveLabel(Label label) {
                if (label.isNewObject()) {
                    return true;
                }
                return label.getOrderElements().isEmpty();
            }

        });
    }

    private void initializeLabelTypesGrid() {
        gridLabelTypes = (Grid) listWindow.getFellowIfAny("labelTypes");

        gridLabelTypes.addEventListener("onInitRender", new EventListener() {

            @Override
            public void onEvent(Event event) {
                gridLabelTypes.renderAll();

                final Rows rows = gridLabelTypes.getRows();
                for (Iterator i = rows.getChildren().iterator(); i.hasNext();) {
                    final Row row = (Row) i.next();
                    final LabelType labelType = (LabelType) row.getValue();
                    Hbox hbox = (Hbox) row.getChildren().get(2);
                    Button btnDelete = (Button) hbox.getChildren().get(1);
                    if (!canRemoveLabelType(labelType)) {
                        btnDelete.setDisabled(true);
                        btnDelete.setImage("/common/img/ico_borrar_out.png");
                        btnDelete
                                .setHoverImage("/common/img/ico_borrar_out.png");
                        btnDelete.setTooltiptext("");
                    }
                }
            }

            private boolean canRemoveLabelType(LabelType labelType) {
                boolean canRemove = true;
                if (labelType.isNewObject()) {
                    return canRemove;
                }
                // If at least one of its labels is being used by and
                // orderelement, cannot remove labelType
                for (Label each: labelType.getLabels()) {
                    if (!each.getOrderElements().isEmpty()) {
                        canRemove = false;
                        break;
                    }
                }
                return canRemove;
            }

        });
    }

    /**
     * Return all {@link LabelType}
     * @return
     */
    public List<LabelType> getLabelTypes() {
        return labelTypeModel.getLabelTypes();
    }

    /**
     * Return current {@link LabelType}
     * @return
     */
    public LabelType getLabelType() {
        return labelTypeModel.getLabelType();
    }

    public List<Label> getLabels() {
        return labelTypeModel.getLabels();
    }

    /**
     * Save current {@link LabelType} and return
     */
    public void save() {
        validate();
        try {
            labelTypeModel.confirmSave();
            goToList();
            messagesForUser.showMessage(Level.INFO, _("Label type saved"));
        } catch (ValidationException e) {
            showInvalidValues(e);
        }
    }

    /**
     * Validates all {@link Textbox} in the form
     */
    private void validate() {
        validate((Textbox) editWindow.getFellowIfAny("label_type_name"));
        for (Row row : getRows()) {
            validate(row);
        }
    }

    @SuppressWarnings("unchecked")
    private void validate(Row row) {
        for (Iterator i = row.getChildren().iterator(); i.hasNext();) {
            final Component comp = (Component) i.next();
            if (comp instanceof Textbox) {
                validate((Textbox) comp);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Row> getRows() {
        return gridLabels.getRows().getChildren();
    }

    /**
     * Validates {@link Textbox} checking {@link Constraint}
     * @param comp
     */
    private void validate(Textbox comp) {
        if (comp != null && comp.getConstraint() != null && !comp.isDisabled()) {
            final Constraint constraint = comp.getConstraint();
            constraint.validate(comp, comp.getValue());
        }
    }

    /**
     * Save current {@link LabelType} and continue
     */
    public void saveAndContinue() {
        validate();
        try {
            labelTypeModel.confirmSave();
            goToEditForm(labelTypeModel.getLabelType());
            messagesEditWindow.showMessage(Level.INFO, _("Label saved"));
        } catch (ValidationException e) {
            showInvalidValues(e);
        }
    }

    private void showInvalidValues(ValidationException e) {
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            Object value = invalidValue.getBean();
            if (value instanceof LabelType) {
                validateLabelType(invalidValue);
            }
            if (value instanceof Label) {
                validateLabel(invalidValue);
            }
        }
    }

    private void validateLabelType(InvalidValue invalidValue) {
        Component component = editWindow.getFellowIfAny("label_type_"
                + invalidValue.getPropertyName());
        if (component != null) {
            throw new WrongValueException(component, invalidValue.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void validateLabel(InvalidValue invalidValue) {
        Row listitem = findLabel(gridLabels.getRows().getChildren(),
                (Label) invalidValue.getBean());
        if (listitem != null) {
            throw new WrongValueException(listitem, invalidValue.getMessage());
        }
    }

    private Row findLabel(List<Row> rows, Label label) {
        for (Row row : rows) {
            if (label.equals(row.getValue())) {
                return row;
            }
        }
        return null;
    }

    /**
     * Cancel edition
     */
    public void close() {
        goToList();
    }

    public void createLabel() {
        try{
            validateNewLabel();
            validate();
            labelTypeModel.addLabel(newLabelTextbox.getValue());
            Util.reloadBindings(gridLabels);
            // After adding a new row, model might be disordered, so we force it
            // to
            // sort again respecting previous settings
            forceSortGridLabels();
            newLabelTextbox.setValue("");
        } catch (ValidationException e) {
            for (InvalidValue invalidValue : e.getInvalidValues()) {
                messagesForUser.showMessage(Level.ERROR, invalidValue
                        .getMessage());
            }
        }
    }

    private String validateNewLabel() throws ValidationException {
        String name = newLabelTextbox.getValue();
        labelTypeModel.validateNameNotEmpty(name);
        labelTypeModel.thereIsOtherWithSameNameAndType(name);
        return name;
    }

    /**
     * Sorts {@link Grid} model by first column, respecting sort order
     * FIXME: This is a temporary solution, there should be a better/smarter way
     * of preserving order in the Grid every time a new element is added to its
     * model
     */
    private void forceSortGridLabels() {
        Column column = (Column) gridLabels.getColumns().getFirstChild();
        ListModelExt model = (ListModelExt) gridLabels.getModel();
        if ("ascending".equals(column.getSortDirection())) {
            model.sort(column.getSortAscending(), true);
        }
        if ("descending".equals(column.getSortDirection())) {
            model.sort(column.getSortDescending(), false);
        }
    }

    public void onChangeLabelName(Event e) {
        InputEvent ie = (InputEvent) e;
        if (!labelTypeModel.labelNameIsUnique(ie.getValue())) {
            throw new WrongValueException(e.getTarget(), _(
                    "{0} already exists", ie.getValue()));
        }
    }

    /**
     * Pop up confirm remove dialog
     * @param labelType
     */
    public void confirmDeleteLabel(Label label) {
        labelTypeModel.confirmDeleteLabel(label);
        Util.reloadBindings(gridLabels);
    }

    /**
     * Pop up confirm remove dialog
     * @param labelType
     */
    public void confirmDelete(LabelType labelType) {
        try {
            if (Messagebox.show(_("Delete item. Are you sure?"), _("Confirm"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION) == Messagebox.OK) {
                labelTypeModel.confirmDelete(labelType);
                Grid labelTypes = (Grid) listWindow
                        .getFellowIfAny("labelTypes");
                if (labelTypes != null) {
                    Util.reloadBindings(labelTypes);
                }
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            try {
                labelTypeModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
        }
        Util.reloadBindings(editWindow);
    }

    @Override
    protected String getEntityType() {
        return _("Label Type");
    }

    @Override
    protected String getPluralEntityType() {
        return _("Label Types");
    }

    @Override
    protected void initCreate() {
        labelTypeModel.initCreate();
    }

    @Override
    protected void initEdit(LabelType labelType) {
        labelTypeModel.initEdit(labelType);
    }

}
