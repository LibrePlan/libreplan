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

package org.navalplanner.web.orders.labels;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.Autocomplete;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.navalplanner.web.orders.IOrderElementModel;
import org.navalplanner.web.orders.IOrderModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Textbox;

/**
 * Controller for showing OrderElement assigned labels
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class AssignedLabelsToOrderElementController extends
        GenericForwardComposer {

    private IAssignedLabelsToOrderElementModel assignedLabelsToOrderElementModel;

    private Autocomplete cbLabelType;

    private Grid directLabels;

    private Textbox txtLabelName;

    private BandboxSearch bdLabels;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
    }

    public void openWindow(IOrderElementModel orderElementModel) {
        setOrderElementModel(orderElementModel);
        openWindow(getOrderElement());
    }

    private void openWindow(OrderElement orderElement) {
        assignedLabelsToOrderElementModel.init(orderElement);

        // Clear components
        bdLabels.clear();
        txtLabelName.setValue("");

        Util.reloadBindings(self);
        Util.reloadBindings(directLabels);
    }

    IOrderElementModel orderElementModel;

    private void setOrderElementModel(IOrderElementModel orderElementModel) {
        this.orderElementModel = orderElementModel;
        setOrderModel(orderElementModel.getOrderModel());
    }

    private void setOrderModel(IOrderModel orderModel) {
        if (assignedLabelsToOrderElementModel != null) {
            assignedLabelsToOrderElementModel.setOrderModel(orderModel);
        }
    }

    public OrderElement getOrderElement() {
        return orderElementModel.getOrderElement();
    }

    /**
     * Executed on pressing Assign button
     *
     * Adds selected label to direct labels list
     *
     */
    public void onAssignLabel() {
        Label label = (Label) bdLabels.getSelectedElement();
        if (label == null) {
            throw new WrongValueException(bdLabels, _("please, select a label"));
        }
        if (isAssigned(label)) {
            throw new WrongValueException(bdLabels, _("already assigned"));
        }
        try {
            assignLabel(label);
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(bdLabels, e.getMessage());
        }
        bdLabels.clear();
    }

    /**
     * Executed on pressing createAndAssign button
     *
     * Creates a new label for a type, in case it does not exist, and added it
     * to the list of direct labels
     *
     */
    public void onCreateAndAssign() {
        // Check LabelType is not null
        final Comboitem comboitem = cbLabelType.getSelectedItem();
        if (comboitem == null || comboitem.getValue() == null) {
            throw new WrongValueException(cbLabelType,
                    _("please, select an item"));
        }

        // Check Label is not null or empty
        final String labelName = txtLabelName.getValue();
        if (labelName == null || labelName.isEmpty()) {
            throw new WrongValueException(txtLabelName,
                    _("cannot be null or empty"));
        }

        // Label does not exist, create
        final LabelType labelType = (LabelType) comboitem.getValue();
        Label label = assignedLabelsToOrderElementModel
                .findLabelByNameAndType(labelName, labelType);
        if (label == null) {
            label = addLabel(labelName, labelType);
        } else {
            // Label is already assigned?
            if (isAssigned(label)) {
                throw new WrongValueException(txtLabelName,
                        _("already assigned"));
            }
        }
        try {
            assignLabel(label);
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(txtLabelName, e.getMessage());
        }
        clear(txtLabelName);
    }

    private Label addLabel(String labelName, LabelType labelType) {
        Label label = createLabel(labelName, labelType);
        bdLabels.addElement(label);
        return label;
    }

    private Label createLabel(String labelName, LabelType labelType) {
        return assignedLabelsToOrderElementModel.createLabel(labelName,
                labelType);
    }

    private void clear(Textbox textbox) {
        textbox.setValue("");
    }

    public void clear() {

    }

    private void assignLabel(Label label) {
        assignedLabelsToOrderElementModel.assignLabel(label);
        Util.reloadBindings(directLabels);
    }

    private boolean isAssigned(Label label) {
        return assignedLabelsToOrderElementModel.isAssigned(label);
    }

    public void deleteLabel(Label label) {
        assignedLabelsToOrderElementModel.deleteLabel(label);
        Util.reloadBindings(directLabels);
    }

    public List<Label> getLabels() {
        return assignedLabelsToOrderElementModel.getLabels();
    }

    public List<Label> getInheritedLabels() {
        return assignedLabelsToOrderElementModel.getInheritedLabels();
    }

    public List<Label> getAllLabels() {
        return assignedLabelsToOrderElementModel.getAllLabels();
    }

    public void close() {

    }
}
