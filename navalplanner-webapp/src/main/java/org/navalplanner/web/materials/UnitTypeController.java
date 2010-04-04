package org.navalplanner.web.materials;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.materials.entities.UnitType;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;

/**
 *
 * Controller for the listing and editing unit types
 *
 * @author Javier Moran Rua <jmoran@igalia.com>
 */

public class UnitTypeController extends GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
    .getLog(UnitTypeController.class);

    private Component messagesContainer;
    private IMessagesForUser messagesForUser;
    private OnlyOneVisible visibility;

    private Component listWindow;
    private Component editWindow;

    private IUnitTypeModel unitTypeModel;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        getVisibility().showOnly(listWindow);
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow,editWindow);
        }
        return visibility;
    }

    public List<UnitType> getUnitTypes() {
        return unitTypeModel.getUnitTypes();
    }

    public RowRenderer getUnitTypeRenderer() {

        return new RowRenderer() {
            @Override
            public void render(Row row, Object data) throws Exception {
                UnitType unitType = (UnitType) data;

                appendUnitTypeName(row, unitType);
                appendOperations(row, unitType);
            }

            private void appendUnitTypeName(Row row, UnitType unitType) {
                row.appendChild(new Label(unitType.getMeasure()));
            }

            private void appendOperations(Row row, final UnitType unitType) {
                Hbox hbox = new Hbox();

                hbox.appendChild(Util.createEditButton(new EventListener() {

                    @Override
                    public void onEvent(Event event) throws Exception {
                        goToEditFormInEditionMode(unitType);
                    }
                }));

                hbox.appendChild(Util.createRemoveButton(new EventListener() {

                    @Override
                    public void onEvent(Event event) throws Exception {
                        confirmRemove(unitType);
                    }
                }));

                row.appendChild(hbox);
            }
        };
    }

    private void confirmRemove(UnitType unitType) {
        try {
            int status = Messagebox.show(_("Confirm deleting {0}. Are you sure?", unitType.getMeasure()),
                    "Delete", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                removeUnitType(unitType);
            }
        } catch (InterruptedException e) {
            LOG.error("Error showing confirming message box",e);
            throw new RuntimeException();
        }
    }

    private void removeUnitType(UnitType unitType) {
        if (unitTypeModel.isUnitTypeUsedInAnyMaterial(unitType)) {
            messagesForUser.showMessage(Level.ERROR, _("Unit {0} cannot be " +
                " removed because it is used in materials",
                unitType.getMeasure()));
        } else {
            unitTypeModel.remove(unitType);
            Util.reloadBindings(listWindow);
            messagesForUser.showMessage(Level.INFO, _("Deleted unit type {0}",
                    unitType.getMeasure()));
        }
    }

    public void goToEditFormInCreationMode() {
        unitTypeModel.initCreate();
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    private void goToEditFormInEditionMode(UnitType unitType) {
        unitTypeModel.initEdit(unitType);
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public UnitType getUnitType() {
        return unitTypeModel.getCurrentUnitType();
    }

    public Constraint uniqueMeasureName() {
        return new Constraint() {

            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                String strValue = (String) value;
                if (StringUtils.isBlank(strValue)) {
                    throw new WrongValueException(comp,
                            _("Unit type name cannot be empty")
                            );
                }

                if (unitTypeModel.existsAnotherUnitTypeWithName(strValue)) {
                    throw new WrongValueException(comp,
                            _("The meausure name is not valid. There is " +
                                    "another unit type with the same " +
                                    "measure name"));
                }
            }
        };
    }

    public Constraint uniqueCode() {
        return new Constraint() {

            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                String strValue = (String) value;
                if (StringUtils.isBlank(strValue)) {
                    throw new WrongValueException(comp,
                            _("Unit type code cannot be empty"));
                }

                if (unitTypeModel.existsAnotherUnitTypeWithCode(strValue)) {
                    throw new WrongValueException(comp,
                            _("The code is not valid. There is another " +
                                    "unit type with the same code"));
                }
            }

        };
    }
    public void saveAndExit() {
        if (save()) {
            goToList();
        }
    }

    public void saveAndContinue() {
        if (save()) {
            goToEditFormInEditionMode(getUnitType());
        }
    }

    public void cancel() {
        goToList();
    }

    private boolean save() {
        try {
            validateAll();
            unitTypeModel.confirmSave();
            messagesForUser.showMessage(Level.INFO, _("Unit type saved"));
            return true;
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
            return false;
        }
    }

    private void validateAll() {
        Textbox codeTextBox = (Textbox) editWindow.
            getFellowIfAny("codeTextBox");
        validate((InputElement) codeTextBox,codeTextBox.getValue());

        Textbox measureTextBox = (Textbox) editWindow.
            getFellowIfAny("measureTextBox");
        validate((InputElement) measureTextBox,measureTextBox.getValue());
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

    private void goToList() {
        Util.reloadBindings(listWindow);
        getVisibility().showOnly(listWindow);
    }
}
