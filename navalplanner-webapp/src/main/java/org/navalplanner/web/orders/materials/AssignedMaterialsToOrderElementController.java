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

package org.navalplanner.web.orders.materials;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.orders.IOrderElementModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.api.Textbox;
import org.zkoss.zul.impl.MessageboxDlg;

/**
 * Controller for showing {@link OrderElement} assigned {@link Material}
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class AssignedMaterialsToOrderElementController extends
        GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(AssignedMaterialsToOrderElementController.class);

    private IAssignedMaterialsToOrderElementModel assignedMaterialsToOrderElementModel;

    private Tree categoriesTree;

    private Tree allCategoriesTree;

    private Grid gridMaterials;

    private Listbox lbFoundMaterials;

    private Textbox txtSearchMaterial;

    private Tab tbAssignedMaterials;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("assignedMaterialsController", this, true);
    }

    public void openWindow(IOrderElementModel orderElementModel) {
        assignedMaterialsToOrderElementModel.initEdit(orderElementModel);
        prepareCategoriesTree();
        prepareAllCategoriesTree();
    }

    /**
     * Delay initialization of categories tree till user clicks on Materials tab
     *
     * Initializing model and renderer properties directly in ZUL resulted in calling the renderer
     * more times than actually needed, resulting in noticeable lack of performance
     */
    private void prepareCategoriesTree() {
        if (categoriesTree.getTreeitemRenderer() == null) {
            categoriesTree.setTreeitemRenderer(getMaterialCategoryWithUnitsAndPriceRenderer());
        }
        categoriesTree.setModel(getMaterialCategories());
    }

    public TreeModel getMaterialCategories() {
        return assignedMaterialsToOrderElementModel.getMaterialCategories();
    }

    private void prepareAllCategoriesTree() {
        if (allCategoriesTree.getTreeitemRenderer() == null) {
            allCategoriesTree.setTreeitemRenderer(getMaterialCategoryRenderer());
        }
        allCategoriesTree.setModel(getAllMaterialCategories());
    }

    public TreeModel getAllMaterialCategories() {
        return assignedMaterialsToOrderElementModel.getAllMaterialCategories();
    }

    public double getTotalUnits() {
        double result = 0;

        final OrderElement orderElement = getOrderElement();
        if (orderElement != null) {
            result = orderElement.getTotalMaterialAssigmentUnits();
        }
        return result;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal result = new BigDecimal(0);

        final OrderElement orderElement = getOrderElement();
        if (orderElement != null) {
            result = orderElement.getTotalMaterialAssigmentPrice();
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private OrderElement getOrderElement() {
        return assignedMaterialsToOrderElementModel.getOrderElement();
    }

    /**
     * On selecting category, refresh {@link MaterialAssignment} associated with
     * selected {@link MaterialCategory}
     */
    public void refreshMaterialAssigments() {
        final List<MaterialAssignment> materials = getAssignedMaterials();
        gridMaterials.setModel(new SimpleListModel(materials));
        Util.reloadBindings(gridMaterials);
    }

    public List<MaterialAssignment> getAssignedMaterials() {
        final Treeitem treeitem = categoriesTree.getSelectedItem();
        return getAssignedMaterials(treeitem);
    }

    private List<MaterialAssignment> getAssignedMaterials(Treeitem treeitem) {
        final MaterialCategory materialCategory = (treeitem != null) ? (MaterialCategory) treeitem.getValue() : null;
        return getAssignedMaterials(materialCategory);
    }

    private List<MaterialAssignment> getAssignedMaterials(MaterialCategory materialCategory) {
        return assignedMaterialsToOrderElementModel.getAssignedMaterials(materialCategory);
    }

    /**
     * On changing total price, recalculate unit price and refresh categories tree
     *
     * @param row
     */
    public void updateTotalPrice(Row row) {
        final MaterialAssignment materialAssignment = (MaterialAssignment) row
                .getValue();
        Doublebox totalPrice = (Doublebox) row.getChildren().get(5);
        totalPrice.setValue(materialAssignment.getTotalPrice().doubleValue());
        refreshTotalPriceAndTotalUnits(materialAssignment);
    }

    /**
     * Refresh categoriesTree since it shows totalUnits and totalPrice as well
     */
    private void refreshTotalPriceAndTotalUnits(MaterialAssignment materialAssignment) {
        final Treeitem item = findMaterialCategoryInTree(materialAssignment
                .getMaterial().getCategory(), categoriesTree);
        if (item != null) {
            // Reload categoriesTree
            categoriesTree.setModel(getMaterialCategories());
        }
    }

    /**
     * On changing unit price, recalculate total price and refresh categories tree
     *
     * @param row
     */
    public void updateUnitPrice(Row row) {
        final MaterialAssignment materialAssignment = (MaterialAssignment) row.getValue();
        Doublebox unitPrice = (Doublebox) row.getChildren().get(4);
        unitPrice.setValue(materialAssignment.getUnitPrice().doubleValue());
        refreshTotalPriceAndTotalUnits(materialAssignment);
    }

    /**
     * Finds which {@link Treeitem} in tree is associated with category
     *
     * @param category
     * @param tree
     * @return
     */
    private Treeitem findMaterialCategoryInTree(MaterialCategory category, Tree tree) {
        for (Iterator i = tree.getItems().iterator(); i.hasNext(); ) {
            Treeitem treeitem = (Treeitem) i.next();
            final MaterialCategory materialCategory = (MaterialCategory) treeitem.getValue();
            if (category.equals(materialCategory)) {
                return treeitem;
            }
        }
        return null;
    }

    /**
     * Search materials on pressing search button
     */
    public void searchMaterials() {
        final String text = txtSearchMaterial.getValue();
        final MaterialCategory materialCategory = getSelectedCategory(allCategoriesTree);
        assignedMaterialsToOrderElementModel.searchMaterials(text, materialCategory);
        Util.reloadBindings(lbFoundMaterials);
    }

    /**
     * Returns {@link MaterialCategory} associated with selected {@link Treeitem} in {@link Tree}
     *
     * @param tree
     * @return
     */
    private MaterialCategory getSelectedCategory(Tree tree) {
        final Treeitem treeitem = tree.getSelectedItem();
        return (treeitem != null) ? (MaterialCategory) treeitem.getValue() : null;
    }

    /**
     * Get materials found on latest search
     *
     * @return
     */
    public List<Material> getMatchingMaterials() {
        return assignedMaterialsToOrderElementModel.getMatchingMaterials();
    }

    /**
     * Assigns a list of selected {@link Material} to current {@link OrderElement}
     */
    public void assignSelectedMaterials() {
        Set<Material> materials = getSelectedMaterials();
        if (materials.isEmpty()) {
            return;
        }

        for(Material each: materials) {
            assignedMaterialsToOrderElementModel.addMaterialAssignment(each);
        }

        categoriesTree.clearSelection();
        tbAssignedMaterials.setSelected(true);
        lbFoundMaterials.clearSelection();
        Util.reloadBindings(categoriesTree);
        Util.reloadBindings(gridMaterials);
    }

    private Set<Material> getSelectedMaterials() {
        Set<Material> result = new HashSet<Material>();

        final Set<Listitem> listitems = lbFoundMaterials.getSelectedItems();
        for (Listitem each: listitems) {
            final Material material = (Material) each.getValue();
            result.add(material);
        }
        return result;
    }

    public void clearSelectionCategoriesTree() {
        categoriesTree.clearSelection();
        Util.reloadBindings(gridMaterials);
    }

    public void clearSelectionAllCategoriesTree() {
        allCategoriesTree.clearSelection();
        retrieveAllMaterials();
        Util.reloadBindings(lbFoundMaterials);
    }

    private void retrieveAllMaterials() {
        assignedMaterialsToOrderElementModel.searchMaterials("", null);
    }

    public MaterialCategoryRenderer getMaterialCategoryRenderer() {
        return new MaterialCategoryRenderer();
    }

    private class MaterialCategoryRenderer implements TreeitemRenderer {

        /**
         * Copied verbatim from org.zkoss.zul.Tree;
         */
        @Override
        public void render(Treeitem ti, Object node) throws Exception {
            final MaterialCategory materialCategory = (MaterialCategory) node;

            Label lblName = new Label(materialCategory.getName());

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
            // Add category name
            Treecell cellName = new Treecell();
            cellName.addEventListener("onClick", new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    assignedMaterialsToOrderElementModel.searchMaterials("", materialCategory);
                    Util.reloadBindings(lbFoundMaterials);
                }
            });
            lblName.setParent(cellName);
            cellName.setParent(tr);
        }
    }

    public MaterialCategoryWithUnitsAndPriceRenderer getMaterialCategoryWithUnitsAndPriceRenderer() {
        return new MaterialCategoryWithUnitsAndPriceRenderer();
    }

    private class MaterialCategoryWithUnitsAndPriceRenderer implements TreeitemRenderer {

        /**
         * Copied verbatim from org.zkoss.zul.Tree;
         */
        @Override
        public void render(Treeitem ti, Object node) throws Exception {
            final MaterialCategory materialCategory = (MaterialCategory) node;

            Label lblName = new Label(materialCategory.getName());
            Label lblUnits = new Label(new Double(getUnits(materialCategory)).toString());
            Label lblPrice = new Label(getPrice(materialCategory).toString());

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
            // Add category name
            Treecell cellName = new Treecell();
            lblName.setParent(cellName);
            cellName.setParent(tr);

            // Add total assigned material units in category
            Treecell cellUnits = new Treecell();
            lblUnits.setParent(cellUnits);
            cellUnits.setParent(tr);

            // Add total price for assigned materials in category
            Treecell cellPrice = new Treecell();
            lblPrice.setParent(cellPrice);
            cellPrice.setParent(tr);
        }

        private double getUnits(MaterialCategory materialCategory) {
            return assignedMaterialsToOrderElementModel.getUnits(materialCategory);
        }

        private BigDecimal getPrice(MaterialCategory materialCategory) {
            return assignedMaterialsToOrderElementModel.getPrice(materialCategory);
        }

    }

    /**
     * On clicking remove {@link MaterialAssignment}, shows dialog for
     * confirming removing selected element
     *
     * @param materialAssignment
     */
    public void showRemoveMaterialAssignmentDlg(MaterialAssignment materialAssignment) {
        try {
            int status = Messagebox.show(_("Delete item {0}. Are you sure?", materialAssignment.getMaterial().getCode()),
                    _("Delete"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                removeMaterialAssignment(materialAssignment);
            }
        } catch (InterruptedException e) {
            LOG.error(_("Error on showing delete materialAssignment: ", materialAssignment.getId()), e);
        }
    }

    private void removeMaterialAssignment(MaterialAssignment materialAssignment) {
        assignedMaterialsToOrderElementModel.removeMaterialAssignment(materialAssignment);
        Util.reloadBindings(gridMaterials);
        reloadTree(categoriesTree);
    }

    private void reloadTree(Tree tree) {
        final Treeitem treeitem = tree.getSelectedItem();

        if (treeitem != null) {
            final MaterialCategory materialCategory = (MaterialCategory) treeitem.getValue();
            tree.setModel(getMaterialCategories());
            locateAndSelectMaterialCategory(tree, materialCategory);
        } else {
            tree.setModel(getMaterialCategories());
            Util.reloadBindings(gridMaterials);
        }
    }

    private boolean locateAndSelectMaterialCategory(Tree tree, MaterialCategory materialCategory) {
        Treeitem treeitem = findTreeItemByMaterialCategory(tree.getRoot(), materialCategory);
        if (treeitem != null) {
            treeitem.setSelected(true);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Treeitem findTreeItemByMaterialCategory(Component node, MaterialCategory materialCategory) {
        if (node instanceof Treeitem) {
            final Treeitem treeitem = (Treeitem) node;
            final MaterialCategory _materialCategory = (MaterialCategory) treeitem.getValue();
            if (_materialCategory.getId().equals(materialCategory.getId())) {
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

    /**
     * On clicking Split button, shows dialog for splitting selected
     * {@link MaterialAssignment} into two
     *
     * @param materialAssignment
     */
    @SuppressWarnings("unchecked")
    public void showSplitMaterialAssignmentDlg(MaterialAssignment materialAssignment) {
        MessageboxDlg dialogSplitAssignment;

        final String message = _("Create new material assignment out of material assignment {0}. Are you sure?",
                materialAssignment.getMaterial().getCode());

        Map args = new HashMap();
        args.put("message", message);
        args.put("title", _("Split new assignment"));
        args.put("OK", Messagebox.OK);
        args.put("CANCEL", Messagebox.CANCEL);
        args.put("icon", Messagebox.QUESTION);

        dialogSplitAssignment = (MessageboxDlg) Executions
                .createComponents("/orders/_splitMaterialAssignmentDlg.zul",
                        self, args);
        Doublebox dbUnits = (Doublebox) dialogSplitAssignment.getFellowIfAny("dbUnits");
        dbUnits.setValue(materialAssignment.getUnits());
        try {
            dialogSplitAssignment.doModal();
            int status = dialogSplitAssignment.getResult();
            if (Messagebox.OK == status) {
                splitMaterialAssignment(materialAssignment, dbUnits.getValue());
            }
        } catch (SuspendNotAllowedException e) {
            LOG.error(_("Error on splitting element: ", materialAssignment.getId()), e);
        } catch (InterruptedException e) {
            LOG.error(_("Error on splitting element: ", materialAssignment.getId()), e);
        }
    }

    /**
     * Creates a new {@link MaterialAssignment} out of materialAssignment, but
     * setting its units attribute to units.
     *
     * materialAssignment passed as parameter decreases its units attribute in units
     *
     * @param materialAssignment
     * @param units
     */
    private void splitMaterialAssignment(MaterialAssignment materialAssignment, double units) {
        MaterialAssignment newMaterialAssignment = MaterialAssignment.create(materialAssignment);
        double currentUnits = materialAssignment.getUnits();
        if (units > currentUnits) {
            units = currentUnits;
            currentUnits = 0;
        } else {
            currentUnits -= units;
        }
        newMaterialAssignment.setUnits(units);
        materialAssignment.setUnits(currentUnits);
        assignedMaterialsToOrderElementModel.addMaterialAssignment(newMaterialAssignment);
        Util.reloadBindings(gridMaterials);
    }

}
