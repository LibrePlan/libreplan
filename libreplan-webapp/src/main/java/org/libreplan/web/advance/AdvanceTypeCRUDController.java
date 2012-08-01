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

package org.libreplan.web.advance;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

/**
 * Controller for CRUD actions over a {@link AdvanceType}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 */
public class AdvanceTypeCRUDController extends BaseCRUDController<AdvanceType> {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(AdvanceTypeCRUDController.class);

    private IAdvanceTypeModel advanceTypeModel;

    public List<AdvanceType> getAdvanceTypes() {
        return advanceTypeModel.getAdvanceTypes();
    }

    public AdvanceType getAdvanceType() {
        return advanceTypeModel.getAdvanceType();
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
                            _("Invalid value. Precission value must be lower than the Default Max value."));
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
                            _("Invalid value. Default Max Value cannot be empty"));
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

    @Override
    protected void save() throws ValidationException {
        advanceTypeModel.save();
    }

    public void setDefaultMaxValue(BigDecimal defaultMaxValue) {
        try {
            advanceTypeModel.setDefaultMaxValue(defaultMaxValue);
        } catch (IllegalArgumentException e) {
            Component component = editWindow.getFellow(
                    "defaultMaxValue");

            throw new WrongValueException(component, e.getMessage());
        }
    }

    public BigDecimal getDefaultMaxValue() {
        return advanceTypeModel.getDefaultMaxValue();
    }

    public void setPercentage(Boolean percentage) {
        advanceTypeModel.setPercentage(percentage);
    }

    public Boolean getPercentage() {
        return advanceTypeModel.getPercentage();
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
            public void render(Row row, Object data) {
                final AdvanceType advanceType = (AdvanceType) data;
                appendLabelName(row, advanceType);
                appendCheckboxEnabled(row, advanceType);
                appendCheckboxPredefined(row, advanceType);
                appendOperations(row, advanceType);
                row.addEventListener(Events.ON_CLICK, new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        goToEditForm(advanceType);
                    }
                });
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
                    public void onEvent(Event event) {
                        goToEditForm(advanceType);
                    }
                }));

                Button removeButton = Util
                        .createRemoveButton(new EventListener() {

                    @Override
                            public void onEvent(Event event) throws InstanceNotFoundException {
                        confirmDelete(advanceType);
                    }
                });
                removeButton.setDisabled(advanceTypeModel
                        .isImmutableOrAlreadyInUse(advanceType));
                hbox.appendChild(removeButton);

                row.appendChild(hbox);
            }

        };
    }

    @Override
    protected String getEntityType() {
        return _("Progress Type");
    }

    @Override
    protected String getPluralEntityType() {
        return _("Progress Types");
    }

    @Override
    protected void initCreate() {
        advanceTypeModel.prepareForCreate();
    }

    @Override
    protected void initEdit(AdvanceType advanceType) {
        advanceTypeModel.prepareForEdit(advanceType);
    }

    @Override
    protected AdvanceType getEntityBeingEdited() {
        return advanceTypeModel.getAdvanceType();
    }

    @Override
    protected void delete(AdvanceType advanceType) throws InstanceNotFoundException{
        advanceTypeModel.prepareForRemove(advanceType);
        advanceTypeModel.remove(advanceType);
    }
}
