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
package org.libreplan.web.orders.labels;

import static org.libreplan.web.I18nHelper._;

import java.util.List;

import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.Autocomplete;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.orders.IOrderElementModel;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Textbox;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class AssignedLabelsController<T, M> extends
        GenericForwardComposer {

    private Autocomplete cbLabelType;

    private Grid directLabels;

    private Textbox txtLabelName;

    private BandboxSearch bdLabels;

    public void openWindow(M model) {
        setOuterModel(model);
        openElement(getElement());
    }

    protected abstract IAssignedLabelsModel<T> getModel();

    private void openElement(T element) {
        getModel().init(element);

        // Clear components
        bdLabels.clear();
        txtLabelName.setValue("");

        Util.reloadBindings(self);
        Util.reloadBindings(directLabels);
    }

    IOrderElementModel orderElementModel;

    protected abstract void setOuterModel(M orderElementModel);

    protected abstract T getElement();

    /**
     * Executed on pressing Assign button Adds selected label to direct labels
     * list
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
     * Executed on pressing createAndAssign button Creates a new label for a
     * type, in case it does not exist, and added it to the list of direct
     * labels
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
        Label label = getModel().findLabelByNameAndType(
                labelName, labelType);
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
        return getModel().createLabel(labelName,
                labelType);
    }

    private void clear(Textbox textbox) {
        textbox.setValue("");
    }

    public void clear() {

    }

    private void assignLabel(Label label) {
        getModel().assignLabel(label);
        Util.reloadBindings(directLabels);
    }

    private boolean isAssigned(Label label) {
        return getModel().isAssigned(label);
    }

    public void deleteLabel(Label label) {
        getModel().deleteLabel(label);
        Util.reloadBindings(directLabels);
    }

    public List<Label> getLabels() {
        return getModel().getLabels();
    }

    public List<Label> getInheritedLabels() {
        return getModel().getInheritedLabels();
    }

    public List<Label> getAllLabels() {
        return getModel().getAllLabels();
    }

    public void close() {

    }

}
