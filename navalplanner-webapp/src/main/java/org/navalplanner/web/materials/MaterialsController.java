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

package org.navalplanner.web.materials;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
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

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;


    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("materialsController", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);

        // Renders grid and enables delete button is material is new
        gridMaterials.addEventListener("onInitRender", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                gridMaterials.renderAll();

                final Rows rows = gridMaterials.getRows();
                for (Iterator i = rows.getChildren().iterator(); i.hasNext(); ) {
                    final Row row = (Row) i.next();
                    final Material material = (Material) row.getValue();
                    Button btnDelete = (Button) row.getChildren().get(5);
                    btnDelete.setDisabled(!material.isNewObject());
                }
            }
        });
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
        public void render(Treeitem ti, Object node) throws Exception {
            final MaterialCategory materialCategory = (MaterialCategory) node;

            Textbox tb = new Textbox(materialCategory.getName());
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
            public void onEvent(Event event) throws Exception {
                confirmRemove(materialCategory);
            }
        });
        btnDelete.setDisabled(hasSubcategoriesOrMaterials(materialCategory));
        Treecell tc = new Treecell();
        tc.setParent(ti.getTreerow());
        btnDelete.setParent(tc);
    }

    private boolean hasSubcategoriesOrMaterials(MaterialCategory materialCategory) {
        return materialCategory.getSubcategories() == null || !materialCategory.getSubcategories().isEmpty();
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
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
            LOG.error(_("Error on showing removing element: ", materialCategory.getId()), e);
        }
    }

    private void removeMaterialCategory(MaterialCategory materialCategory) {
        materialsModel.removeMaterialCategory(materialCategory);
    }

    public void addMaterialCategory() {
        String categoryName = txtCategory.getValue();
        if (categoryName == null || categoryName.isEmpty()) {
            throw new WrongValueException(txtCategory, _("cannot be null or empty"));
        }

        final MaterialCategory category = MaterialCategory.create(_(categoryName));
        MaterialCategory parent = null;
        final Treeitem treeitem = categoriesTree.getSelectedItem();
        if (treeitem != null) {
            parent = (MaterialCategory) treeitem.getValue();
        }
        try {
            materialsModel.addMaterialCategory(parent, category);
            txtCategory.setValue("");
        } catch (ValidationException e) {
            for (InvalidValue invalidValue : e.getInvalidValues()) {
                 Object value = invalidValue.getBean();
                 if (value instanceof MaterialCategory) {
                     MaterialCategory materialCategory = (MaterialCategory) value;
                     Component comp = findInMaterialCategoryTree(materialCategory);
                     if (comp != null) {
                         throw new WrongValueException(comp, invalidValue.getMessage());
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
            return;
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
            Util.reloadBindings(gridMaterials);
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
            if (each.getBean() instanceof Material) {
                final Material material = (Material) each.getBean();
                showConstraintErrorsFor(material.getCategory());
            }
        }
    }

    private void showConstraintErrorsFor(MaterialCategory materialCategory) {
        Treeitem treeitem = findTreeItemByMaterialCategory(categoriesTree.getRoot(), materialCategory);
        if (treeitem != null) {
            treeitem.setSelected(true);

            // Load materials for category
            final List<Material> materials = getMaterials(materialCategory);
            gridMaterials.setModel(new SimpleListModel(materials));
            gridMaterials.renderAll();

            // Show errors
            ConstraintChecker.isValid(gridMaterials);
        }
    }

    private Treeitem findTreeItemByMaterialCategory(Component node, MaterialCategory materialCategory) {
        if (node instanceof Treeitem) {
            final Treeitem treeitem = (Treeitem) node;
            final MaterialCategory _materialCategory = (MaterialCategory) treeitem.getValue();
            if (_materialCategory.equals(materialCategory)) {
                return treeitem;
            }
        }
        for (Iterator i = node.getChildren().iterator(); i.hasNext(); ) {
            Object obj = i.next();
            if (obj instanceof Component) {
                Treeitem treeitem =  findTreeItemByMaterialCategory((Component) obj, materialCategory);
                if (treeitem != null) {
                    return treeitem;
                }
            }
        }
        return null;
    }

    public void refreshMaterials() {
        final List<Material> materials = getMaterials();
        gridMaterials.setModel(new SimpleListModel(materials));
        Util.reloadBindings(gridMaterials);
    }

    public List<Material> getMaterials() {
        return getMaterials(categoriesTree.getSelectedItem());
    }

    private List<Material> getMaterials(Treeitem treeitem) {
        final List<Material> result = new ArrayList<Material>();
        if (treeitem != null) {
            result.addAll(getMaterials((MaterialCategory) treeitem.getValue()));
        }
        return result;
    }

    private List<Material> getMaterials(MaterialCategory materialCategory) {
        return materialsModel.getMaterials(materialCategory);
    }

    public void remove(Material material) {
        materialsModel.removeMaterial(material);
        Util.reloadBindings(gridMaterials);
    }

}
