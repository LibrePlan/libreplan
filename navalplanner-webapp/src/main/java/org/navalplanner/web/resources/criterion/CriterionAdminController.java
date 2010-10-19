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
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Window;

/**
 * Controller for Criterions <br />
 */
public class CriterionAdminController extends GenericForwardComposer {

    private static final Log log = LogFactory
            .getLog(CriterionAdminController.class);

    private ICriterionsModel criterionsModel_V2;

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    private Window listing;

    private Checkbox checkboxHierarchy;

    private Window confirmRemove;

    private boolean confirmingRemove = false;

    private Window confirmDisabledHierarchy;

    private boolean confirmingDisabledHierarchy = false;

    private Component editComponent;

    private Component createComponent;

    private OnlyOneVisible onlyOneVisible;

    private Component workersComponent;

    private CriterionTreeController editionTree;

    public CriterionAdminController() {

    }

    public void goToCreateForm() {
        try{
            criterionsModel_V2.prepareForCreate();
            setupCriterionTreeController(createComponent);
            onlyOneVisible.showOnly(createComponent);
            setResourceComboboxValue((Combobox) createComponent.getFellowIfAny("resourceCombobox"));
            Util.reloadBindings(createComponent);
        }catch(Exception e){
            messagesForUser.showMessage(
                    Level.ERROR, _("Error setting up creation form."));
            log.error(_("Error setting up creation form for Criterion Type"), e );
        }
    }

    public void goToEditForm(CriterionType criterionType) {
        try{
            criterionsModel_V2.prepareForEdit(criterionType);
            setupCriterionTreeController(editComponent);
            onlyOneVisible.showOnly(editComponent);
            setResourceComboboxValue((Combobox) editComponent.getFellowIfAny("resourceCombobox"));
            Util.reloadBindings(editComponent);
        }catch(Exception e){
            messagesForUser.showMessage(
                    Level.ERROR, _("Error setting up edition form."));
            log.error(_("Error setting up edition form for Criterion Type with id: {0}",
                    criterionType.getId()), e );
        }
    }

    public void confirmRemove(CriterionType criterionType) {
        criterionsModel_V2.prepareForRemove(criterionType);
        if (criterionsModel_V2.isDeletable(criterionType)) {
            showConfirmingWindow();
        } else {
            messagesForUser
                    .showMessage(
                            Level.WARNING,
                            _("This criterion type cannot be deleted because it has assignments to projects or resources"));
        }
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

    public void remove(CriterionType criterionType) {
        criterionsModel_V2.remove(criterionType);
        hideConfirmingWindow();
        Util.reloadBindings(listing);
        messagesForUser.showMessage(
            Level.INFO, _("Removed {0}", criterionType.getName()));
    }

    public void confirmDisabledHierarchy(Checkbox checkbox) {
        checkboxHierarchy = checkbox;
        if(!checkboxHierarchy.isChecked()){
            showConfirmingHierarchyWindow();
        }
        editionTree.reloadTree();
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

    public void cancelDisEnabledHierarchy() {
        confirmingDisabledHierarchy = false;
        confirmDisabledHierarchy.setVisible(false);
        checkboxHierarchy.setChecked(true);
        Util.reloadBindings(confirmDisabledHierarchy);
    }

    public boolean isConfirmingDisEnabledHierarchy() {
        return confirmingDisabledHierarchy;
    }

    private void hideConfirmingHierarchtWindow() {
        confirmingDisabledHierarchy = false;
        Util.reloadBindings(confirmDisabledHierarchy);
    }

    private void showConfirmingHierarchyWindow() {
        this.confirmingDisabledHierarchy = true;
        try {
            Util.reloadBindings(this.confirmDisabledHierarchy);
            confirmDisabledHierarchy.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void okDisEnabledHierarchy() {
        editionTree.disabledHierarchy();
        hideConfirmingHierarchtWindow();
        Util.reloadBindings(listing);
        messagesForUser.showMessage(Level.INFO, _("Fattened Tree {0}",
                criterionsModel_V2.getCriterionType().getName()));
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
            criterionsModel_V2.saveCriterionType();
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
        Tree tree = (Tree) getCurrentWindow().getFellowIfAny("tree");
        criterionsModel_V2.reloadCriterionType();
        Util.reloadBindings(tree);
    }

    private Component getCurrentWindow() {
        return (editComponent.isVisible()) ? editComponent : createComponent;
    }

    public List<CriterionType> getCriterionTypes() {
        List<CriterionType> types = criterionsModel_V2.getTypes();
        return types;
    }

    public ICriterionType<?> getCriterionType() {
        return criterionsModel_V2.getCriterionType();
    }

    public ICriterionTreeModel getCriterionTreeModel() {
        return criterionsModel_V2.getCriterionTreeModel();
    }

    public Criterion getCriterion() {
        return criterionsModel_V2.getCriterion();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        onlyOneVisible = new OnlyOneVisible(listing, editComponent,
                createComponent, workersComponent);
        onlyOneVisible.showOnly(listing);
        comp.setVariable("controller", this, false);
        messagesForUser = new MessagesForUser(messagesContainer);
        setupResourceCombobox((Combobox) createComponent.getFellowIfAny("resourceCombobox"));
        setupResourceCombobox((Combobox) editComponent.getFellowIfAny("resourceCombobox"));
    }

    private void setupResourceCombobox(Combobox combo) {
        for(ResourceEnum resource : ResourceEnum.values()) {
            Comboitem item = combo.appendItem(_(resource.getDisplayName()));
            item.setValue(resource);
        }
    }

    private void setResourceComboboxValue(Combobox combo) {
        CriterionType criterionType = (CriterionType) getCriterionType();
        for(Object object : combo.getItems()) {
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

    private void setupCriterionTreeController(Component comp)throws Exception {
        editionTree = new CriterionTreeController(criterionsModel_V2);
        editionTree.setCriterionCodeEditionDisabled(
((CriterionType) criterionsModel_V2
                        .getCriterionType()).isCodeAutogenerated());
        editionTree.doAfterCompose(comp.getFellow(
                "criterionsTree"));
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            try {
                // we have to auto-generate the code for new objects
                criterionsModel_V2.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
            Util.reloadBindings(createComponent);
        }
        //disable code field in criterion tree controller
        editionTree.setCriterionCodeEditionDisabled(ce.isChecked());
        editionTree.reloadTree();
    }
}
