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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

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
                if (((BigDecimal) value) == null) {
                    throw new WrongValueException(comp,
                            _("Value is not valid, the precision value must not be empty"));
                }

                if (!(advanceTypeModel.isPrecisionValid((BigDecimal) value))) {
                    throw new WrongValueException(
                            comp,
                            _("Value is not valid, the Precision value must be less than the defalt max value."));
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
                if (((BigDecimal) value) == null) {
                    throw new WrongValueException(comp,
                            _("Value is not valid, the default max value must not be empty "));
                }
                if (!(advanceTypeModel
                        .isDefaultMaxValueValid((BigDecimal) value))) {
                    throw new WrongValueException(
                            comp,
                            _("Value is not valid, the default max value must be greater than the precision value "));
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
                if (((String) value).isEmpty()) {
                    throw new WrongValueException(comp,
                            _("The name is not valid, the name must not be null "));
                }
                if (!advanceTypeModel.distinctNames((String) value)) {
                    throw new WrongValueException(comp,
                            _("The name is not valid, there is another progress type with the same name. "));
                }
            }
        };
        return newConstraint;
    }

    private boolean save() {
        try {
            advanceTypeModel.save();
            messagesForUser.showMessage(Level.INFO, _("Progress type saved"));
            return true;
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
            return false;
        }
    }

    public void confirmRemove(AdvanceType advanceType) {
        try {
            int status = Messagebox.show(_(
                    "Confirm deleting {0}. Are you sure?", advanceType
                            .getUnitName()), "Remove", Messagebox.OK
                    | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                advanceTypeModel.prepareForRemove(advanceType);
                advanceTypeModel.remove(advanceType);
            }
            Util.reloadBindings(listWindow);
            messagesForUser.showMessage(Level.INFO, _("Removed {0}",
                    advanceType.getUnitName()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
        advanceTypeModel.setPercentage(percentage);
        Util.reloadBindings(getCurrentWindow().getFellow(
                "defaultMaxValue"));
    }

    public Boolean getPercentage() {
        return advanceTypeModel.getPercentage();
    }

    public void saveAndExit() {
        if (save()) {
            goToList();
        }
    }

    public void saveAndContinue() {
        if (save()) {
            goToEditForm(getAdvanceType());
        }
    }

    public boolean isImmutable() {
        return advanceTypeModel.isImmutable();
    }

    public boolean isImmutableOrAlreadyInUse(AdvanceType advanceType) {
        return advanceTypeModel.isImmutableOrAlreadyInUse(advanceType);
    }

    public RowRenderer getAdvanceTypeRenderer() {
        return new RowRenderer() {

            @Override
            public void render(Row row, Object data) throws Exception {
                AdvanceType advanceType = (AdvanceType) data;

                appendLabelName(row, advanceType);
                appendCheckboxEnabled(row, advanceType);
                appendCheckboxPredefined(row, advanceType);
                appendOperations(row, advanceType);
            }

            private void appendLabelName(Row row, AdvanceType advanceType) {
                row.appendChild(new Label(advanceType.getUnitName()));
            }

            private void appendCheckboxEnabled(Row row, AdvanceType advanceType) {
                Checkbox checkbox = new Checkbox();
                checkbox.setChecked(advanceType.getActive());
                checkbox.setDisabled(true);
                row.appendChild(checkbox);
            }

            private void appendCheckboxPredefined(Row row,
                    AdvanceType advanceType) {
                Checkbox checkbox = new Checkbox();
                checkbox.setChecked(advanceType.isImmutable());
                checkbox.setDisabled(true);
                row.appendChild(checkbox);
            }

            private void appendOperations(Row row, final AdvanceType advanceType) {
                Hbox hbox = new Hbox();

                hbox.appendChild(Util.createEditButton(new EventListener() {

                    @Override
                    public void onEvent(Event event) throws Exception {
                        goToEditForm(advanceType);
                    }
                }));

                Button removeButton = Util
                        .createRemoveButton(new EventListener() {

                    @Override
                    public void onEvent(Event event) throws Exception {
                        confirmRemove(advanceType);
                    }
                });
                removeButton.setDisabled(advanceTypeModel
                        .isImmutableOrAlreadyInUse(advanceType));
                hbox.appendChild(removeButton);

                row.appendChild(hbox);
            }

        };
    }

}
