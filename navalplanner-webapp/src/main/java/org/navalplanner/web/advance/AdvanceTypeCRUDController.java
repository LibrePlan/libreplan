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

package org.navalplanner.web.advance;

import static org.navalplanner.web.I18nHelper._;

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

    private boolean isEditing = false;

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
        isEditing = true;
    }

    public Constraint lessThanDefaultMaxValue() {
        Constraint newConstraint = new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if (((BigDecimal) value) == null)
                    throw new WrongValueException(comp,
                            _("Value is not valid, the default max value must not be null"));

                if (!(advanceTypeModel.isPrecisionValid((BigDecimal) value))) {
                    throw new WrongValueException(
                            comp,
                            _("Value is not valid, the Precision value must be less than the defalt max value and not null"));
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
                            _("Value is not valid, the Precision value must not be null "));
                if (!(advanceTypeModel
                        .isDefaultMaxValueValid((BigDecimal) value))) {
                    throw new WrongValueException(
                            comp,
                            _("Value is not valid, the Precision value must be less than the defalt max value "));
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
                            _("The name is not valid, the name must not be null "));
                if (!(advanceTypeModel.distinctNames((String) value))) {
                    throw new WrongValueException(comp,
                            _("The name is not valid, Exist other advance type with a similar name. "));
                }
            }
        };
        return newConstraint;
    }

    public void save() {
        try {
            advanceTypeModel.save();
            messagesForUser.showMessage(Level.INFO, _("Advance type saved"));
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
        messagesForUser.showMessage(
            Level.INFO, _("Removed {0}", advanceType.getUnitName()));
    }

    public void goToCreateForm() {
        advanceTypeModel.prepareForCreate();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
        isEditing = false;
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, createWindow,
                    editWindow);
        }
        return visibility;
    }

    public void setDefaultMaxValue(BigDecimal defaultMaxValue) {
        try {
            advanceTypeModel.setDefaultMaxValue(defaultMaxValue);
        } catch (IllegalArgumentException e) {
            Component component = getCurrentWindow().getFellow(
                    "defaultMaxValue");
            throw new WrongValueException(component, e.getMessage());
        }
    }

    public BigDecimal getDefaultMaxValue() {
        return advanceTypeModel.getDefaultMaxValue();
    }

    private Component getCurrentWindow() {
        if (!isEditing) {
            return createWindow;
        }
        return editWindow;
    }

    public void setPercentage(Boolean percentage) {
        try {
            advanceTypeModel.setPercentage(percentage);
        } catch (IllegalArgumentException e) {
            Component component = getCurrentWindow().getFellow("percentage");
            throw new WrongValueException(component
                    .getFellow("defaultMaxValue"), e.getMessage());
        }
    }

    public Boolean getPercentage() {
        return advanceTypeModel.getPercentage();
    }

}
