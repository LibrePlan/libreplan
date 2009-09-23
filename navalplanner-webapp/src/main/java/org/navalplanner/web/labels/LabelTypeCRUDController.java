package org.navalplanner.web.labels;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelExt;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * CRUD Controller for {@link LabelType}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class LabelTypeCRUDController extends GenericForwardComposer {

    @Autowired
    ILabelTypeModel labelTypeModel;

    private Window listWindow;

    private Window editWindow;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private IMessagesForUser messagesEditWindow;

    private Component messagesContainer;

    private Grid gridLabelTypes;

    private Grid gridLabels;

    public LabelTypeCRUDController() {

    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        messagesEditWindow = new MessagesForUser(editWindow
                .getFellowIfAny("messagesContainer"));
        gridLabels = (Grid) editWindow.getFellowIfAny("gridLabels");
        gridLabelTypes = (Grid) listWindow.getFellowIfAny("gridLabelTypes");
        showListWindow();
    }

    private void showListWindow() {
        getVisibility().showOnly(listWindow);
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, editWindow);
        }
        return visibility;
    }

    /**
     * Return all {@link LabelType}
     *
     * @return
     */
    public List<LabelType> getLabelTypes() {
        return labelTypeModel.getLabelTypes();
    }

    /**
     * Return current {@link LabelType}
     *
     * @return
     */
    public LabelType getLabelType() {
        return labelTypeModel.getLabelType();
    }

    public List<Label> getLabels() {
        return labelTypeModel.getLabels();
    }

    /**
     * Prepare form for Create
     */
    public void goToCreateForm() {
        labelTypeModel.initCreate();
        editWindow.setTitle(_("Create label type"));
        showEditWindow();
        Util.reloadBindings(editWindow);
    }

    private void showEditWindow() {
        getVisibility().showOnly(editWindow);
    }

    /**
     * Prepare form for Edit
     *
     * @param labelType
     */
    public void goToEditForm(LabelType labelType) {
        labelTypeModel.initEdit(labelType);
        editWindow.setTitle(_("Edit label type"));
        showEditWindow();
        Util.reloadBindings(editWindow);
    }

    /**
     * Save current {@link LabelType} and return
     */
    public void save() {
        try {
            labelTypeModel.confirmSave();
            goToList();
            messagesForUser.showMessage(Level.INFO, _("Label type saved"));
        } catch (ValidationException e) {
            showInvalidValues(e);
        }
    }

    /**
     * Save current {@link LabelType} and continue
     */
    public void saveAndContinue() {
        try {
            labelTypeModel.confirmSave();
            messagesEditWindow.showMessage(Level.INFO, _("Label saved"));
        } catch (ValidationException e) {
            showInvalidValues(e);
        }
    }

    /**
     * Show all {@link LabelType}
     */
    private void goToList() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
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
        Component component = editWindow.getFellowIfAny(invalidValue
                .getPropertyName());
        if (component != null) {
            throw new WrongValueException(component, invalidValue.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void validateLabel(InvalidValue invalidValue) {
        Row listitem = findLabel(gridLabels.getRows(), (Label) invalidValue
                .getBean());
        if (listitem != null) {
            throw new WrongValueException(listitem, invalidValue.getMessage());
        }
    }

    private Row findLabel(Rows rows, Label label) {
        for (Object row : rows.getChildren()) {
            Textbox textbox = (Textbox) ((Row) row).getFirstChild();
            if (label.equals(textbox.getValue())) {
                return (Row) row;
            }
        }
        return null;
    }

    /**
     * Cancel edition
     */
    public void cancel() {
        goToList();
    }

    public void createLabel() {
        labelTypeModel.addLabel();
        Util.reloadBindings(gridLabels);
        // After adding a new row, model might be disordered, so we force it to
        // sort again respecting previous settings
        forceSortGridLabels();
    }

    /**
     * Sorts {@link Grid} model by first column, respecting sort order
     * 
     * FIXME: This is a temporary solution, there should be a better/smarter way
     * of preserving order in the Grid every time a new element is added to its
     * model
     */
    private void forceSortGridLabels() {
        Column column = (Column) gridLabels.getColumns().getFirstChild();
        ListModelExt model = (ListModelExt) gridLabels.getModel();
        if ("ascending".equals(column.getSortDirection())) {
            model.sort(column.getSortAscending(), true);
            return;
        }
        if ("descending".equals(column.getSortDirection())) {
            model.sort(column.getSortDescending(), false);
            return;
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
     *
     * @param labelType
     */
    public void confirmDeleteLabel(Label label) {
        labelTypeModel.confirmDeleteLabel(label);
        Util.reloadBindings(gridLabels);
    }

    /**
     * Pop up confirm remove dialog
     *
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

}
