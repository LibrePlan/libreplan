package org.navalplanner.web.advance;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link AdvanceType}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AdvanceTypeCRUDController extends GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(AdvanceTypeCRUDController.class);

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Component editWindow;

    private Component createWindow;

    private Component listWindow;

    private Window confirmRemove;

    private boolean confirmingRemove = false;

    private IAdvanceTypeModel advanceTypeModel;

    private OnlyOneVisible visibility;

    public List<AdvanceType> getAdvanceTypes() {
        return advanceTypeModel.getAdvanceTypes();
    }

    public AdvanceType getAdvanceType() {
        return advanceTypeModel.getAdvanceType();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        getVisibility().showOnly(listWindow);
    }

    public void cancel() {
        goToList();
    }

    public void goToList() {
        Util.reloadBindings(listWindow);
        getVisibility().showOnly(listWindow);
    }

    public void goToEditForm(AdvanceType advanceType) {
        advanceTypeModel.prepareForEdit(advanceType);
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public Constraint lessThanDefaultMaxValue() {
        Constraint newConstraint = new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if (((BigDecimal) value) == null)
                    throw new WrongValueException(comp,
                            "Value is not valid, the default max value must be not null");

                if (!(advanceTypeModel.isPrecisionValid((BigDecimal) value))) {
                    throw new WrongValueException(
                            comp,
                            "Value is not valid, the Precision value must be less than the defalt max value and not null ");
                }
            }
        };
        return newConstraint;
    }

    public Constraint greaterThanPrecision() {
        Constraint newConstraint = new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if (((BigDecimal) value) == null)
                    throw new WrongValueException(comp,
                            "Value is not valid, the Precision value must be not null");
                if (!(advanceTypeModel
                        .isDefaultMaxValueValid((BigDecimal) value))) {
                    throw new WrongValueException(
                            comp,
                            "Value is not valid, the Precision value must be less than the defalt max value");
                }
            }
        };
        return newConstraint;
    }

    public Constraint distinctNames() {
        Constraint newConstraint = new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if (((String) value).isEmpty())
                    throw new WrongValueException(comp,
                            "The name is not valid, the name must be not null ");
                if (!(advanceTypeModel.distinctNames((String) value))) {
                    throw new WrongValueException(comp,
                            "The name is not valid, Exist other advance type with a similar name. ");
                }
            }
        };
        return newConstraint;
    }

    public void save() {
        try {
            advanceTypeModel.save();
            messagesForUser.showMessage(Level.INFO, "advance type saved");
            goToList();
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    public void confirmRemove(AdvanceType advanceType) {
        advanceTypeModel.prepareForRemove(advanceType);
        showConfirmingWindow();
    }

    public void cancelRemove() {
        confirmingRemove = false;
        confirmRemove.setVisible(false);
        Util.reloadBindings(confirmRemove);
    }

    public boolean isConfirmingRemove() {
        return confirmingRemove;
    }

    private void hideConfirmingWindow() {
        confirmingRemove = false;
        Util.reloadBindings(confirmRemove);
    }

    private void showConfirmingWindow() {
        confirmingRemove = true;
        try {
            Util.reloadBindings(confirmRemove);
            confirmRemove.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void remove(AdvanceType advanceType) {
        advanceTypeModel.remove(advanceType);
        hideConfirmingWindow();
        Util.reloadBindings(listWindow);
        messagesForUser.showMessage(Level.INFO, "removed "
                + advanceType.getUnitName());
    }

    public void goToCreateForm() {
        advanceTypeModel.prepareForCreate();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, createWindow,
                    editWindow);
        }
        return visibility;
    }

}
