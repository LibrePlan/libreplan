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

package org.navalplanner.web.resources.criterion;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import static org.navalplanner.web.I18nHelper._;

/**
 * Controller for Criterions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionAdminController_V2 extends GenericForwardComposer {

    private static final Log log = LogFactory
            .getLog(CriterionAdminController.class);

    private ICriterionsModel_V2 criterionsModel_V2;

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    private Window listing;

    private Window confirmRemove;

    private boolean confirmingRemove = false;

    private Window confirmDisabledHierarchy;

    private boolean confirmingDisabledHierarchy = false;

    private Component editComponent;

    private Component createComponent;

    private OnlyOneVisible onlyOneVisible;

    private Component workersComponent;

    private CriterionTreeController editionTree;

    private CriterionWorkersController workers;

    public CriterionAdminController_V2() {

    }

    public void goToCreateForm() {
        onlyOneVisible.showOnly(createComponent);
        criterionsModel_V2.prepareForCreate();
        Util.reloadBindings(createComponent);
    }

    public void goToEditForm(CriterionType criterionType) {
        onlyOneVisible.showOnly(editComponent);
        criterionsModel_V2.prepareForEdit(criterionType);
        Util.reloadBindings(editComponent);
    }

    public void confirmRemove(CriterionType criterionType) {
        criterionsModel_V2.prepareForRemove(criterionType);
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

    public void remove(CriterionType criterionType) {
        criterionsModel_V2.remove(criterionType);
        hideConfirmingWindow();
        Util.reloadBindings(listing);
        messagesForUser.showMessage(
            Level.INFO, _("Removed {0}", criterionType.getName()));
    }

    public void confirmDisabledHierarchy(Checkbox checkbox) {
        if(!checkbox.isChecked()){
            showConfirmingHierarchyWindow();
        }
    }

    public boolean allowRemove(CriterionType criterionType){
        if(criterionType.getCriterions().size() > 0)
            return false;
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
        criterionsModel_V2.disableHierarchy();
        editionTree.reloadTree();
        hideConfirmingHierarchtWindow();
        Util.reloadBindings(listing);
        messagesForUser.showMessage(
            Level.INFO, _("Fattened Tree {0}", criterionsModel_V2.getCriterionType().getName()));
    }

    public void changeEnabled(Checkbox checkbox) {
        criterionsModel_V2.updateEnabledCriterions(checkbox.isChecked());
        editionTree.reloadTree();
    }

    public CriterionTreeController getEdition() {
        return editionTree;
    }

    public void saveAndClose(){
        save();
        close();
    }

    public void saveAndContinue(){
        save();
    }

    public void close(){
        onlyOneVisible.showOnly(listing);
        Util.reloadBindings(listing);
    }

    private void save() {
        try {
            criterionsModel_V2.saveCriterionType();
            messagesForUser.showMessage(Level.INFO, _("CriterionType and it`s criterions saved"));
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
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

    public CriterionWorkersController getWorkers() {
        return workers;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        onlyOneVisible = new OnlyOneVisible(listing, editComponent,
                createComponent, workersComponent);
        onlyOneVisible.showOnly(listing);
        comp.setVariable("controller", this, false);
        messagesForUser = new MessagesForUser(messagesContainer);

        setupCriterionTreeController(comp, "editComponent");
        setupCriterionTreeController(comp, "createComponent");
    }

    private void setupCriterionTreeController(Component comp, String window
            )throws Exception {
        editionTree = new CriterionTreeController(criterionsModel_V2);
        editionTree.doAfterCompose(comp.getFellow(window).getFellow(
                "criterionsTree"));
    }


}
