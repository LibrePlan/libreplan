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

import static org.libreplan.web.I18nHelper._t;

import java.util.List;

import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.Autocomplete;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.security.SecurityUtils;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class AssignedLabelsController<T, M> extends GenericForwardComposer {

    private Autocomplete cbLabelType;

    private Grid directLabels;

    private Textbox txtLabelName;

    private BandboxSearch bdLabels;

    private Button buttonCreateAndAssign;

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

        Util.createBindingsFor(self);
        Util.reloadBindings(self);
        // directLabels' own model="@load(...)" binding already got a (correctly empty, since
        // getModel().init(element) above hadn't run yet) value pushed during this same page's
        // very first, eager tab-panel composition - see refreshDirectLabels()'s comment for why
        // that leaves it never receiving a further push from the binder. Do the same explicit,
        // direct push here so the initial view reflects whatever's really assigned instead of
        // that stale first (always empty) snapshot.
        refreshDirectLabels();
    }

    protected abstract void setOuterModel(M orderElementModel);

    protected abstract T getElement();

    /**
     * Executed on pressing Assign button Adds selected label to direct labels list.
     */
    public void onAssignLabel() {
        Label label = (Label) bdLabels.getSelectedElement();
        if (label == null) {
            throw new WrongValueException(bdLabels, _t("please, select a label"));
        }
        if (isAssigned(label)) {
            throw new WrongValueException(bdLabels, _t("already assigned"));
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
     * type, in case it does not exist, and added it to the list of direct labels.
     */
    public void onCreateAndAssign() {

        // Check if user has permissions to create labels
        if (!SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_LABELS)) {
            throw new WrongValueException(buttonCreateAndAssign,
                    _t("you do not have permissions to create new labels"));
        }

        // Check LabelType is not null
        final Comboitem comboitem = cbLabelType.getSelectedItem();
        if (comboitem == null || comboitem.getValue() == null) {
            throw new WrongValueException(cbLabelType, _t("please, select an item"));
        }

        // Check Label is not null or empty
        final String labelName = txtLabelName.getValue();
        if (labelName == null || labelName.isEmpty()) {
            throw new WrongValueException(txtLabelName, _t("cannot be empty"));
        }

        // Label does not exist, create
        final LabelType labelType = comboitem.getValue();
        Label label = getModel().findLabelByNameAndType(labelName, labelType);
        if (label == null) {
            label = addLabel(labelName, labelType);
        } else {
            if (isAssigned(label)) {
                throw new WrongValueException(txtLabelName, _t("already assigned"));
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
        return getModel().createLabel(labelName, labelType);
    }

    private void clear(Textbox textbox) {
        textbox.setValue("");
    }

    private void assignLabel(Label label) {
        getModel().assignLabel(label);
        refreshDirectLabels();
    }

    private boolean isAssigned(Label label) {
        return getModel().isAssigned(label);
    }

    public void deleteLabel(Label label) {
        getModel().deleteLabel(label);
        refreshDirectLabels();
    }

    /**
     * directLabels' own model="@load(...)" binding gets its first value pushed very early -
     * before openWindow()/init() ever runs, while getLabels() still (correctly, per its own
     * null-element guard) returns empty - and that premature push leaves the binder no longer
     * treating this component as needing a reload later (Util.reloadBindings(directLabels), even
     * called with the force strategy, stops producing any client-side update for it from that
     * point on - confirmed by tracing NewDataSortableGrid.setModel(), which is never invoked
     * again after this happens, even though the underlying data is correct by then). Bypassing
     * the binder for this one incremental update - the same wrapped ListModel getLabelsModel()
     * already builds for the binding - sidesteps whatever state the binder got stuck in.
     *
     * rowRenderer="@load(...)" on the same tag needs the identical bypass for the identical
     * reason - setting only the model without also re-asserting the renderer leaves rows
     * falling back to their raw toString() in composition contexts where that binding never
     * fired (see ManageOrderElementAdvancesController's editAdvances fix for the confirmed
     * live symptom of this exact gap).
     */
    private void refreshDirectLabels() {
        directLabels.setRowRenderer(getDirectLabelsRenderer());
        directLabels.setModel(getLabelsModel());
    }

    public List<Label> getLabels() {
        return getModel().getLabels();
    }

    /**
     * NewDataSortableGrid.setModel(ListModel) is a strict, non-generic override - unlike plain
     * Grid, ZK Bind's List-&gt;ListModel auto-wrap does not apply, so directLabels' "model=" needs
     * an already-wrapped ListModel explicitly.
     */
    public ListModel<Label> getLabelsModel() {
        return new SimpleListModel<>(getLabels());
    }

    public List<Label> getInheritedLabels() {
        return getModel().getInheritedLabels();
    }

    // DEAD CODE START - getAllLabels() has no remaining caller since
    // _listOrderElementLabels.zul's bandboxSearch stopped binding model= to it (see the comment
    // there). Left in place pending Jeroen's decision on whether to remove it.
    public List<Label> getAllLabels() {
        return getModel().getAllLabels();
    }
    // DEAD CODE END

    public RowRenderer getInheritedLabelsRenderer() {
        return (row, data, i) -> {
            final Label label = (Label) data;
            row.setValue(label);

            row.appendChild(new org.zkoss.zul.Label(label.getType().getName()));
            row.appendChild(new org.zkoss.zul.Label(label.getName()));
        };
    }

    public RowRenderer getDirectLabelsRenderer() {
        return (row, data, i) -> {
            final Label label = (Label) data;
            row.setValue(label);

            row.appendChild(new org.zkoss.zul.Label(label.getType().getName()));
            row.appendChild(new org.zkoss.zul.Label(label.getName()));
            row.appendChild(Util.createRemoveButton(event -> deleteLabel(label)));
        };
    }

}
