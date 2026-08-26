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

package org.libreplan.web.resources.criterion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.ICriterionType;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Tree;
import org.zkoss.zkplus.spring.SpringUtil;

import java.util.ConcurrentModificationException;
import java.util.List;

import static org.libreplan.web.I18nHelper._t;

/**
 * Controller for Criterions.
 * <br />
 */
public class CriterionAdminController extends BaseCRUDController<CriterionType> {

    private static final Log LOG = LogFactory.getLog(CriterionAdminController.class);

    private ICriterionsModel criterionsModel;

    private Checkbox cbHierarchy;

    private CriterionTreeController editionTree;

    public CriterionAdminController() {
        // The list's "each" template needs criterionsModel to be ready at compose time (it
        // evaluates the model's collection synchronously while the page's components are being
        // built, before doAfterCompose ever runs) - fetching it lazily in doAfterCompose is too
        // late, so it's fetched here instead, matching AdvanceTypeCRUDController/
        // LabelTypeCRUDController's constructors.
        criterionsModel = (ICriterionsModel) SpringUtil.getBean("criterionsModel");
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        cbHierarchy = (Checkbox) editWindow.getFellow("cbHierarchy");
        setupResourceCombobox((Combobox) editWindow.getFellowIfAny("resourceCombobox"));

        // BaseCRUDController.doAfterCompose() already calls showListWindow() (which reloads
        // listWindow's bindings), but that happens before AnnotateBinderInit's own later
        // page-level pass has created any binder at all, so it's a no-op. Redo it here, now that
        // the binder exists. Only listWindow is reloaded - editWindow's bindings reference
        // controller.criterionType, which is still null at this point (only set up by
        // initCreate()/initEdit(), called later); showEditWindow() reloads editWindow's own
        // bindings once that has happened.
        Util.createBindingsFor(comp);
        Util.reloadBindings(listWindow);
    }

    public void confirmDisabledHierarchy() {
        if (!cbHierarchy.isChecked()){
            showConfirmingHierarchyWindow();
        }
    }

    public boolean allowRemove(CriterionType criterionType){
        return criterionType.getCriterions().size() <= 0;
    }

    public boolean notAllowRemove(CriterionType criterionType){
        return !allowRemove(criterionType);
    }

    public boolean isActivo(){
        return true;
    }

    private void showConfirmingHierarchyWindow() {
        int status = Messagebox.show(
                _t("Disable hierarchy will cause criteria tree to be flattened. Are you sure?"),
                _t("Question"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);

        if (Messagebox.OK == status) {
            disableHierarchy();
            editionTree.reloadTree();
        } else {
            cbHierarchy.setChecked(true);
        }
    }

    public void disableHierarchy() {
        editionTree.disabledHierarchy();

        messagesForUser.showMessage(
                Level.INFO,
                _t("Tree {0} sucessfully flattened", criterionsModel.getCriterionType().getName()));

        Util.reloadBindings(listWindow);
    }

    public void changeEnabled(Checkbox checkbox) {
        editionTree.updateEnabledCriterions(checkbox.isChecked());
    }

    public CriterionTreeController getEdition() {
        return editionTree;
    }

    private void reloadCriterionType() {
        Tree tree = (Tree) editWindow.getFellowIfAny("tree");
        criterionsModel.reloadCriterionType();
        Util.reloadBindings(tree);
    }

    public List<CriterionType> getCriterionTypes() {
        return new ListModelList<>(criterionsModel.getTypes());
    }

    /**
     * The list's rows used to be declared with a ZUML "each" template (self="@{each=...}") - under
     * this app's AnnotateBinder/ZK 10 stack that only ever clones the row's FIRST child for each
     * iteration (confirmed by dumping the live server-side component tree: every cloned row had
     * exactly 1 child, no exception thrown). Building rows programmatically via RowRenderer, like
     * AdvanceTypeCRUDController already does, sidesteps that "each" bug entirely.
     */
    public RowRenderer getCriterionTypeRenderer() {
        return new RowRenderer() {

            @Override
            public void render(Row row, Object data, int index) {
                final CriterionType criterionType = (CriterionType) data;

                Label nameLabel = new Label(criterionType.getName());
                nameLabel.setTooltiptext(criterionType.getDescription());
                row.appendChild(nameLabel);

                row.appendChild(new Label(criterionType.getCode()));
                row.appendChild(new Label(criterionType.getResource().toString()));

                Checkbox checkbox = new Checkbox();
                checkbox.setChecked(criterionType.isEnabled());
                checkbox.setDisabled(true);
                row.appendChild(checkbox);

                Hbox hbox = new Hbox();
                hbox.appendChild(Util.createEditButton(event -> goToEditForm(criterionType)));

                Button removeButton = Util.createRemoveButton(event -> confirmDelete(criterionType));
                removeButton.setDisabled(criterionType.isImmutable());
                hbox.appendChild(removeButton);

                row.appendChild(hbox);

                row.addEventListener(Events.ON_CLICK, event -> goToEditForm(criterionType));
            }

        };
    }

    public ICriterionType<?> getCriterionType() {
        return criterionsModel.getCriterionType();
    }

    /**
     * IntegrationEntity.isCodeAutogenerated() returns a boxed Boolean - ZK's BeanELResolver only
     * recognizes the "is" prefix for primitive boolean getters, so a thin wrapper (paired with a
     * setter for @save) exposes a primitive boolean instead.
     */
    public boolean isCriterionTypeCodeAutogenerated() {
        return ((CriterionType) getCriterionType()).isCodeAutogenerated();
    }

    public void setCriterionTypeCodeAutogenerated(boolean codeAutogenerated) {
        ((CriterionType) getCriterionType()).setCodeAutogenerated(codeAutogenerated);
    }

    /**
     * CriterionType.allowHierarchy() has no "is"/"get" accessor prefix at all (by design, part
     * of the ICriterionType interface, used elsewhere in Java code) - BeanELResolver requires
     * one for EL property resolution, so wrap it here rather than rename the widely-used
     * interface method.
     */
    public boolean isCriterionTypeAllowHierarchy() {
        return getCriterionType().allowHierarchy();
    }

    public void setCriterionTypeAllowHierarchy(boolean allowHierarchy) {
        ((CriterionType) getCriterionType()).setAllowHierarchy(allowHierarchy);
    }

    public ICriterionTreeModel getCriterionTreeModel() {
        return criterionsModel.getCriterionTreeModel();
    }

    public Criterion getCriterion() {
        return criterionsModel.getCriterion();
    }

    private void setupResourceCombobox(Combobox combo) {
        for (ResourceEnum resource : ResourceEnum.values()) {
            Comboitem item = combo.appendItem(_t(resource.getDisplayName()));
            item.setValue(resource);
        }
    }

    private void setResourceComboboxValue(Combobox combo) {
        CriterionType criterionType = (CriterionType) getCriterionType();

        for (Object object : combo.getItems()) {

            Comboitem item = (Comboitem) object;

            if (criterionType != null && item.getValue().equals(criterionType.getResource())) {
                combo.setSelectedItem(item);
            }
        }
    }

    public void setResource(Comboitem item) {
        if (item != null) {
            ((CriterionType)getCriterionType()).setResource(item.getValue());
        }
    }

    private void setupCriterionTreeController(Component comp) throws Exception {
        editionTree = new CriterionTreeController(criterionsModel);

        editionTree.setCriterionCodeEditionDisabled(
                ((CriterionType) criterionsModel.getCriterionType()).isCodeAutogenerated());

        editionTree.doAfterCompose(comp.getFellow("criterionsTree"));
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            try {
                // We have to auto-generate the code for new objects
                criterionsModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
            Util.reloadBindings(editWindow);
        }
        // Disable code field in criterion tree controller
        editionTree.setCriterionCodeEditionDisabled(ce.isChecked());
        editionTree.reloadTree();
    }

    @Override
    protected String getEntityType() {
        return _t("Criterion Type");
    }

    @Override
    protected String getPluralEntityType() {
        return _t("Criterion Types");
    }

    @Override
    protected void initCreate() {
        criterionsModel.prepareForCreate();
        try {
            setupCriterionTreeController(editWindow);
        } catch (Exception e) {
            LOG.error("Error setting up creating form for Criterion Type", e);
        }
        setResourceComboboxValue((Combobox) editWindow.getFellowIfAny("resourceCombobox"));
    }

    @Override
    protected void initEdit(CriterionType criterionType) {
        criterionsModel.prepareForEdit(criterionType);
        try {
            setupCriterionTreeController(editWindow);
        } catch (Exception e) {
            LOG.error("Error setting up edition form for Criterion Type with id: " + criterionType.getId(), e);
        }
        setResourceComboboxValue((Combobox) editWindow.getFellowIfAny("resourceCombobox"));
    }

    @Override
    protected void save() throws ValidationException {
        criterionsModel.saveCriterionType();
        reloadCriterionType();
    }

    @Override
    protected CriterionType getEntityBeingEdited() {
        return (CriterionType) criterionsModel.getCriterionType();
    }

    @Override
    protected void delete(CriterionType criterionType) throws InstanceNotFoundException {
        criterionsModel.confirmRemove(criterionType);
    }

    @Override
    protected boolean beforeDeleting(CriterionType criterionType) {
        if (!criterionsModel.canRemove(criterionType)) {
            messagesForUser.showMessage(
                    Level.WARNING,
                    _t("This criterion type cannot be deleted because it is assigned to projects or resources"));

            return false;
        }

        return true;
    }

}
