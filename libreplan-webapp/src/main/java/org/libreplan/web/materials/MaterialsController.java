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

package org.libreplan.web.materials;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.materials.entities.Material;
import org.libreplan.business.materials.entities.MaterialCategory;
import org.libreplan.business.materials.entities.UnitType;
import org.libreplan.web.common.ConstraintChecker;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

/**
 * Controller for {@link Material} materials
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class MaterialsController extends
        GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(MaterialsController.class);

    @Autowired
    private IMaterialsModel materialsModel;

    private Tree categoriesTree;

    private Grid gridMaterials;

    private Textbox txtCategory;

    private Button btnAddMaterial;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Caption materialsCaption;

    private UnitTypeListRenderer unitTypeListRenderer = new UnitTypeListRenderer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("materialsController", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);

        // load the unit types
        loadUnitTypes();

        // Renders grid and enables delete button is material is new
        gridMaterials.addEventListener("onInitRender", new EventListener() {

            @Override
            public void onEvent(Event event) {
                gridMaterials.renderAll();

                final Rows rows = gridMaterials.getRows();
                for (Iterator i = rows.getChildren().iterator(); i.hasNext(); ) {
                    final Row row = (Row) i.next();
                    final Material material = (Material) row.getValue();
                    Button btnDelete = (Button) row.getChildren().get(6);
                    if (!materialsModel.canRemoveMaterial(material)) {
                        btnDelete.setDisabled(true);
                        btnDelete.setImage("/common/img/ico_borrar_out.png");
                        btnDelete.setHoverImage("/common/img/ico_borrar_out.png");
                    }
                }
            }
        });
    }

    private void loadUnitTypes() {
        materialsModel.loadUnitTypes();
    }

    public List<UnitType> getUnitTypes() {
        return materialsModel.getUnitTypes();
    }

    public void selectUnitType(Component self) {
        Listitem selectedItem = ((Listbox) self).getSelectedItem();
            UnitType unitType = (UnitType) selectedItem.getValue();
            Material material = (Material) ((Row) self.getParent()).getValue();
            material.setUnitType(unitType);
    }

    public TreeModel getMaterialCategories() {
        return materialsModel.getMaterialCategories();
    }

    public MaterialCategoryRenderer getMaterialCategoryRenderer() {
        return new MaterialCategoryRenderer();
    }

    /**
     * Render for criterionsTree
     *
     * I had to implement a renderer for the Tree, for setting open to tree for
     * each treeitem while being rendered.
     *
     * I tried to do this by iterating through the list of items after setting
     * model in doAfterCompose, but I got a ConcurrentModificationException. It
     * seems that at that point some other component was using the list of item,
     * so it was not possible to modify it. There's not other point where to
     * initialize components but doAfterCompose.
     *
     * Finally, I tried this solution and it works.
     *
     * @author Diego Pino Garcia <dpino@igalia.com>
     *
     */
    private class MaterialCategoryRenderer implements TreeitemRenderer {

        /**
         * Copied verbatim from org.zkoss.zul.Tree;
         */
        @Override
        public void render(Treeitem ti, Object node) {
            final MaterialCategory materialCategory = (MaterialCategory) node;

            final Textbox tb = new Textbox(materialCategory.getName());
            tb.setWidth("90%");
            tb.addEventListener("onChange", new EventListener() {
                @Override
                public void onEvent(Event event) {
                    final InputEvent ie = (InputEvent) event;
                    materialCategory.setName(ie.getValue());
                }
            });
            tb.addEventListener("onFocus", new EventListener() {
                @Override
                public void onEvent(Event event) {
                    ((Treeitem)tb.getParent().getParent().getParent()).setSelected(true);
                    refreshMaterials();
                }
            });
            Treecell tc = new Treecell();

            Treerow tr = null;
            ti.setValue(node);
            if (ti.getTreerow() == null) {
                tr = new Treerow();
                tr.setParent(ti);
                ti.setOpen(true); // Expand node
            } else {
                tr = ti.getTreerow();
                tr.getChildren().clear();
            }
            tb.setParent(tc);
            tc.setParent(tr);

            final Textbox codeTb = new Textbox(materialCategory.getCode());
            codeTb.setWidth("95%");
            codeTb.setDisabled(materialCategory.isCodeAutogenerated());
            codeTb.addEventListener("onChange", new EventListener() {
                @Override
                public void onEvent(Event event) {
                    final InputEvent ie = (InputEvent) event;
                    materialCategory.setCode(ie.getValue());
                }
            });
            codeTb.addEventListener("onFocus", new EventListener() {
                @Override
                public void onEvent(Event event) {
                    ((Treeitem)codeTb.getParent().getParent().getParent()).setSelected(true);
                    refreshMaterials();
                }
            });
            Treecell codeTc = new Treecell();
            codeTb.setParent(codeTc);
            codeTc.setParent(tr);

            final Checkbox cb = new Checkbox();
            cb.setChecked(materialCategory.isCodeAutogenerated());
            cb.addEventListener("onCheck", new EventListener() {
                @Override
                public void onEvent(Event event) {
                    final CheckEvent ce = (CheckEvent) event;
                    materialCategory.setCodeAutogenerated(ce.isChecked());
                    if (ce.isChecked()) {
                        try {
                            materialsModel.setCodeAutogenerated(ce.isChecked(),
                                    materialCategory);
                        } catch (ConcurrentModificationException err) {
                            messagesForUser.showMessage(Level.ERROR, err
                                    .getMessage());
                        }
                    }
                    codeTb.setValue(materialCategory.getCode());
                    codeTb.setDisabled(ce.isChecked());
                    Util.reloadBindings(codeTb);
                    Util.reloadBindings(gridMaterials);
                }
            });
            Treecell generateCodeTc = new Treecell();
            cb.setParent(generateCodeTc);
            generateCodeTc.setParent(tr);

            appendDeleteButton(ti);
        }
    }

    private void appendDeleteButton(final Treeitem ti) {
        final MaterialCategory materialCategory = (MaterialCategory) ti.getValue();

        Button btnDelete = new Button("", "/common/img/ico_borrar1.png");
        btnDelete.setHoverImage("/common/img/ico_borrar.png");
        btnDelete.setSclass("icono");
        btnDelete.setTooltiptext(_("Delete"));
        btnDelete.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) {
                confirmRemove(materialCategory);
            }
        });
        btnDelete.setDisabled(hasSubcategoriesOrMaterials(materialCategory));
        Treecell tc = new Treecell();
        tc.setParent(ti.getTreerow());
        btnDelete.setParent(tc);
    }

    private boolean hasSubcategoriesOrMaterials(MaterialCategory materialCategory) {
        return !materialCategory.getSubcategories().isEmpty() || !materialCategory.getMaterials().isEmpty();
    }

    public void confirmRemove(MaterialCategory materialCategory) {

        try {
            int status = Messagebox.show(_("Confirm deleting {0}. Are you sure?",
                    materialCategory.getName()), _("Delete"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                removeMaterialCategory(materialCategory);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeMaterialCategory(MaterialCategory materialCategory) {
        materialsModel.confirmRemoveMaterialCategory(materialCategory);
        reloadCategoriesTree(categoriesTree.getSelectedItem());
    }

    public void addMaterialCategory() {
        String categoryName = txtCategory.getValue();
        if (categoryName == null || categoryName.isEmpty()) {
            throw new WrongValueException(txtCategory, _("cannot be empty"));
        }

        MaterialCategory parent = null;
        final Treeitem treeitem = categoriesTree.getSelectedItem();
        if (treeitem != null) {
            parent = (MaterialCategory) treeitem.getValue();
        }
        try {
            materialsModel.addMaterialCategory(parent, categoryName);
            txtCategory.setValue("");
            reloadCategoriesTree(treeitem);
        } catch (ValidationException e) {
            for (InvalidValue invalidValue : e.getInvalidValues()) {
                 Object value = invalidValue.getBean();
                 if (value instanceof MaterialCategory) {
                     MaterialCategory materialCategory = (MaterialCategory) value;
                     Component comp = findInMaterialCategoryTree(materialCategory);
                     if (comp != null) {
                        throw new WrongValueException(comp,
                                _(invalidValue.getMessage()));
                     }
                 }
            }
        }
    }

    /**
     * Finds which element in categoryTree has the same name as {@link MaterialCategory},
     * and returns name {@link Textbox} component
     *
     * @param materialCategory
     * @return
     */
    private Component findInMaterialCategoryTree(MaterialCategory materialCategory) {
        final Treechildren children = categoriesTree.getTreechildren();
        for(Treeitem each: (Collection<Treeitem>) children.getItems()) {
            final MaterialCategory _materialCategory = (MaterialCategory) each.getValue();
            final Textbox textbox = getMaterialCategoryTextbox(each);
            // Clear previous errors
            textbox.clearErrorMessage();
            if (_materialCategory.equals(materialCategory)) {
                return textbox;
            }
        }
        return null;
    }

    private Textbox getMaterialCategoryTextbox(Treeitem treeitem) {
        final Treerow tr = treeitem.getTreerow();
        final Treecell tc = (Treecell) tr.getChildren().get(0);
        return (Textbox) tc.getChildren().get(0);
    }

    public void addMaterialToMaterialCategory(Treeitem treeitem) {
        if (treeitem == null) {
            throw new WrongValueException(btnAddMaterial, _("Cannot insert material in general view. Please, select a category"));
        }
        final MaterialCategory materialCategory = (MaterialCategory) treeitem.getValue();
        materialsModel.addMaterialToMaterialCategory(materialCategory);
        Util.reloadBindings(gridMaterials);
    }

    public void saveAndContinue() {
        if (save()) {
            messagesForUser.showMessage(Level.INFO, _("Materials saved"));
            // Reload materials and categories, keep track of category currently being selected
            final Treeitem treeitem = categoriesTree.getSelectedItem();
            materialsModel.reloadMaterialCategories();
            categoriesTree.setSelectedItem(treeitem);
            reloadCategoriesTree(categoriesTree.getSelectedItem());
            Util.reloadBindings(gridMaterials);
        }
    }

    private void reloadCategoriesTree(Treeitem treeitem) {
        Util.reloadBindings(categoriesTree);
        if (treeitem != null) {
            final MaterialCategory materialCategory = (MaterialCategory) treeitem.getValue();
            categoriesTree.invalidate();
            locateAndSelectMaterialCategory(materialCategory);
        } else {
            categoriesTree.invalidate();
        }
    }

    private boolean save() {
        try {
            materialsModel.confirmSave();
            return true;
        } catch (ValidationException e) {
            showInvalidValues(e);
        }
        return false;
    }

    private void showInvalidValues(ValidationException validationException) {
        final InvalidValue[] invalidValues = validationException.getInvalidValues();
        for (InvalidValue each: invalidValues) {
            final Object bean = each.getBean();
            // Errors related with contraints in Material (not null, etc)
            if (bean instanceof Material) {
                final Material material = (Material) bean;
                showConstraintErrorsFor(material.getCategory());
            }
            // Unique material in materialCategory
            if (bean instanceof MaterialCategory) {
                final MaterialCategory materialCategory = (MaterialCategory) bean;
                final Treeitem treeitem = findTreeItemByMaterialCategory(categoriesTree, materialCategory);
                if (treeitem != null) {
                    if(each.getPropertyName().equals("name")) {
                        throw new WrongValueException(
                                getCategoryTextbox(treeitem),
                                _(each.getMessage()));
                    }
                    if(each.getPropertyName().equals("code")) {
                        throw new WrongValueException(
                                getCategoryCodeTextbox(treeitem),
                                _(each.getMessage()));
                    }
                }
            }
        }
        messagesForUser.showInvalidValues(validationException);
    }

    private Textbox getCategoryTextbox(Treeitem treeitem) {
        final Treerow treerow = (Treerow) treeitem.getChildren().get(0);
        final Treecell treecell = (Treecell) treerow.getChildren().get(0);
        return (Textbox) treecell.getChildren().get(0);
    }

    private Textbox getCategoryCodeTextbox(Treeitem treeitem) {
        final Treerow treerow = (Treerow) treeitem.getChildren().get(0);
        final Treecell treecell = (Treecell) treerow.getChildren().get(1);
        return (Textbox) treecell.getChildren().get(0);
    }

    private boolean locateAndSelectMaterialCategory(MaterialCategory materialCategory) {
        Treeitem treeitem = findTreeItemByMaterialCategory(categoriesTree, materialCategory);
        if (treeitem != null) {
            treeitem.setSelected(true);
            return true;
        }
        return false;
    }

    private Treeitem findTreeItemByMaterialCategory(Tree tree, MaterialCategory materialCategory) {
        for (Iterator i = tree.getItems().iterator(); i.hasNext();)  {
            final Treeitem treeitem = (Treeitem) i.next();
            final MaterialCategory _materialCategory = (MaterialCategory) treeitem.getValue();
            if (_materialCategory.getId() != null && _materialCategory.getId().equals(materialCategory.getId())) {
                return treeitem;
            }
        }
        return null;
    }

    private void showConstraintErrorsFor(MaterialCategory materialCategory) {
        if (locateAndSelectMaterialCategory(materialCategory)) {

            // Load materials for category
            final List<Material> materials = getMaterials(materialCategory);
            gridMaterials.setModel(new SimpleListModel(materials));
            gridMaterials.renderAll();

            // Show errors
            ConstraintChecker.isValid(gridMaterials);
        }
    }

    public void refreshMaterials() {
        final List<Material> materials = getMaterials();
        gridMaterials.setModel(new SimpleListModel(materials));
        refreshMaterialsListTitle();
        Util.reloadBindings(gridMaterials);
    }

    private void refreshMaterialsListTitle() {
        Treeitem treeitem = categoriesTree.getSelectedItem();
        if (treeitem != null) {
            materialsCaption.setLabel(_("List of materials for category: {0}",
                    ((MaterialCategory) treeitem.getValue()).getName()));
        }
        else {
            materialsCaption
                    .setLabel
                (_("List of materials for all categories (select one to filter)"));
        }
    }

    public List<Material> getMaterials() {
        return getMaterials(categoriesTree.getSelectedItem());
    }

    private List<Material> getMaterials(Treeitem treeitem) {
        final List<Material> result = new ArrayList<Material>();
        if (treeitem != null) {
            result.addAll(getMaterials((MaterialCategory) treeitem.getValue()));
        } else {
            result.addAll(materialsModel.getMaterials());
        }
        return result;
    }

    private List<Material> getMaterials(MaterialCategory materialCategory) {
        return materialsModel.getMaterials(materialCategory);
    }

    public void remove(Material material) {
        if(materialsModel.canRemoveMaterial(material)) {
            materialsModel.removeMaterial(material);
            Util.reloadBindings(gridMaterials);
        }
        else {
            messagesForUser.showMessage(Level.ERROR,
                    _("Cannot delete that material because it is assigned to a project."));
        }
    }

    public void clearSelectionCategoriesTree() {
        categoriesTree.clearSelection();
        this.refreshMaterialsListTitle();
        Util.reloadBindings(gridMaterials);
    }

    public UnitTypeListRenderer getRenderer() {
        return unitTypeListRenderer;
    }

    /**
     * RowRenderer for a @{UnitType} element
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    public static class UnitTypeListRenderer implements ListitemRenderer {
        @Override
        public void render(Listitem listItem, Object data) {
            final UnitType unitType = (UnitType) data;
            listItem.setValue(unitType);

            Listcell listCell = new Listcell(unitType.getMeasure());
            listItem.appendChild(listCell);

            Material material = (Material) ((Row) listItem.getListbox()
                    .getParent()).getValue();
            if ((material.getUnitType() != null)
                    && (unitType.getId().equals(material.getUnitType().getId()))) {
                listItem.getListbox().setSelectedItem(listItem);
            }
        }
    }

    public String getMoneyFormat() {
        return Util.getMoneyFormat();
    }

}
