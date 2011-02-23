/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.resources.criterion;

import static org.navalplanner.web.I18nHelper._;

import java.util.ConcurrentModificationException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Window;

/**
 * Controller for Criterions <br />
 */
public class CriterionAdminController extends GenericForwardComposer {

    private static final Log LOG = LogFactory
            .getLog(CriterionAdminController.class);

    @Autowired
    private ICriterionsModel criterionsModel;

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    private Window listing;

    private Window editWindow;

    private Checkbox cbHierarchy;

    private OnlyOneVisible onlyOneVisible;

    private CriterionTreeController editionTree;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        onlyOneVisible = new OnlyOneVisible(listing, editWindow, editWindow);
        onlyOneVisible.showOnly(listing);
        comp.setVariable("controller", this, false);
        messagesForUser = new MessagesForUser(messagesContainer);
        cbHierarchy = (Checkbox) editWindow.getFellow("cbHierarchy");
        setupResourceCombobox((Combobox) editWindow.getFellowIfAny("resourceCombobox"));
    }

    public void goToCreateForm() {
        try {
            criterionsModel.prepareForCreate();
            setupCriterionTreeController(editWindow);
            showEditWindow(_("Create Criterion Type"));
        } catch (Exception e) {
            messagesForUser.showMessage(Level.ERROR,
                    _("Error setting up creation form."));
            LOG.error(_("Error setting up creation form for Criterion Type"), e);
        }
    }

    private void showEditWindow(String title) {
        editWindow.setTitle(title);
        onlyOneVisible.showOnly(editWindow);
        setResourceComboboxValue((Combobox) editWindow.getFellowIfAny("resourceCombobox"));
        Util.reloadBindings(editWindow);
    }

    public void goToEditForm(CriterionType criterionType) {
        try {
            criterionsModel.prepareForEdit(criterionType);
            setupCriterionTreeController(editWindow);
            showEditWindow(_("Edit Criterion Type"));
        } catch (Exception e) {
            messagesForUser.showMessage(Level.ERROR,
                    _("Error setting up edition form."));
            LOG.error(
                    _("Error setting up edition form for Criterion Type with id: {0}",
                            criterionType.getId()), e);
        }
    }

    public void confirmRemove(CriterionType criterionType) {
        if (!criterionsModel.canRemove(criterionType)) {
            messagesForUser
                    .showMessage(
                            Level.WARNING,
                            _("This criterion type cannot be deleted because it has assignments to projects or resources"));
            return;
        }
        showConfirmRemoveWindow(criterionType);
    }

    private void showConfirmRemoveWindow(CriterionType criterionType) {
        try {
            int status = Messagebox.show(
                    _("Confirm deleting {0}. Are you sure?",
                            criterionType.getName()), _("Delete"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                remove(criterionType);
            }

        } catch (InterruptedException e) {
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
        }
    }

    public void remove(CriterionType criterionType) {
        criterionsModel.confirmRemove(criterionType);
        Util.reloadBindings(listing);
        messagesForUser.showMessage(
            Level.INFO, _("Removed {0}", criterionType.getName()));
    }

    public void confirmDisabledHierarchy() {
        if (!cbHierarchy.isChecked()){
            showConfirmingHierarchyWindow();
        }
    }

    public boolean allowRemove(CriterionType criterionType){
        if(criterionType.getCriterions().size() > 0) {
            return false;
        }
        return true;
    }

    public boolean notAllowRemove(CriterionType criterionType){
        return !allowRemove(criterionType);
    }

    public boolean isActivo(){
        return true;
    }

    private void showConfirmingHierarchyWindow() {
        try {
            int status = Messagebox
                    .show(_("Disable hierarchy will cause criteria tree to be flattened. Are you sure?"),
                            _("Question"), Messagebox.OK | Messagebox.CANCEL,
                            Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                disableHierarchy();
                editionTree.reloadTree();
            } else {
                cbHierarchy.setChecked(true);
            }
        } catch (InterruptedException e) {
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
        }
    }

    public void disableHierarchy() {
        editionTree.disabledHierarchy();
        messagesForUser.showMessage(
                Level.INFO,
                _("Tree {0} sucessfully flattened", criterionsModel
                        .getCriterionType().getName()));
        Util.reloadBindings(listing);
    }

    public void changeEnabled(Checkbox checkbox) {
        editionTree.updateEnabledCriterions(checkbox.isChecked());
    }

    public CriterionTreeController getEdition() {
        return editionTree;
    }

    public void saveAndClose(){
        try {
            clearUserMessages();
            save();
            close();
        } catch (ValidationException e) {

        }
    }

    public void close() {
        onlyOneVisible.showOnly(listing);
        Util.reloadBindings(listing);
    }

    private void save() throws ValidationException {
        try {
            criterionsModel.saveCriterionType();
            messagesForUser.showMessage(Level.INFO,
                    _("CriterionType and its criteria saved"));
        } catch (ValidationException e) {
            for (InvalidValue invalidValue : e.getInvalidValues()) {
                String message = invalidValue.getPropertyName() + " : "
                        + _(invalidValue.getMessage());
                messagesForUser.showMessage(Level.ERROR, message);
            }
            throw e;
        }
    }

    private void clearUserMessages() {
        messagesForUser.clearMessages();
    }

    public void saveAndContinue() {
        try{
            save();
            reloadCriterionType();
        } catch (ValidationException e) {

        }
    }

    private void reloadCriterionType() {
        Tree tree = (Tree) editWindow.getFellowIfAny("tree");
        criterionsModel.reloadCriterionType();
        Util.reloadBindings(tree);
    }

    public List<CriterionType> getCriterionTypes() {
        List<CriterionType> types = criterionsModel.getTypes();
        return types;
    }

    public ICriterionType<?> getCriterionType() {
        return criterionsModel.getCriterionType();
    }

    public ICriterionTreeModel getCriterionTreeModel() {
        return criterionsModel.getCriterionTreeModel();
    }

    public Criterion getCriterion() {
        return criterionsModel.getCriterion();
    }

    private void setupResourceCombobox(Combobox combo) {
        for (ResourceEnum resource : ResourceEnum.values()) {
            Comboitem item = combo.appendItem(_(resource.getDisplayName()));
            item.setValue(resource);
        }
    }

    private void setResourceComboboxValue(Combobox combo) {
        CriterionType criterionType = (CriterionType) getCriterionType();
        for (Object object : combo.getItems()) {
            Comboitem item = (Comboitem) object;
            if(criterionType != null &&
                    item.getValue().equals(criterionType.getResource())) {
                combo.setSelectedItem(item);
            }
        }
    }

    public void setResource(Comboitem item) {
        if (item != null) {
            ((CriterionType)getCriterionType()).setResource((ResourceEnum) item.getValue());
        }
    }

    private void setupCriterionTreeController(Component comp) throws Exception {
        editionTree = new CriterionTreeController(criterionsModel);
        editionTree
                .setCriterionCodeEditionDisabled(((CriterionType) criterionsModel
                        .getCriterionType()).isCodeAutogenerated());
        editionTree.doAfterCompose(comp.getFellow("criterionsTree"));
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            try {
                // we have to auto-generate the code for new objects
                criterionsModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
            Util.reloadBindings(editWindow);
        }
        //disable code field in criterion tree controller
        editionTree.setCriterionCodeEditionDisabled(ce.isChecked());
        editionTree.reloadTree();
    }

}
