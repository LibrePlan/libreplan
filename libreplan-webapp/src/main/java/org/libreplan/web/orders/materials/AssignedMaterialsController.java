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
package org.libreplan.web.orders.materials;

import org.libreplan.business.materials.entities.Material;
import org.libreplan.business.materials.entities.MaterialAssignment;
import org.libreplan.business.materials.entities.MaterialCategory;
import org.libreplan.business.materials.entities.UnitType;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
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
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.impl.MessageboxDlg;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.libreplan.web.I18nHelper._;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class AssignedMaterialsController<T, A> extends GenericForwardComposer<Component> {

    private Tree categoriesTree;

    private Tree allCategoriesTree;

    private Grid gridMaterials;

    private Listbox lbFoundMaterials;

    private Textbox txtSearchMaterial;

    private Tab tbAssignedMaterials;

    private Vbox assignmentsBox;

    private UnitTypeListRenderer unitTypeListRenderer = new UnitTypeListRenderer();

    protected abstract IAssignedMaterialsModel<T, A> getModel();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        getModel().loadUnitTypes();
        createAssignmentsBoxComponent(assignmentsBox);
    }

    protected abstract void createAssignmentsBoxComponent(Component parent);

    public void openWindow(T element) {
        initializeEdition(element);
        prepareCategoriesTree();
        prepareAllCategoriesTree();

        Util.createBindingsFor(self);
        Util.reloadBindings(self);
    }

    protected abstract void initializeEdition(T orderElement);

    /**
     * Delay initialization of categories tree till user clicks on Materials tab.
     *
     * Initializing model and renderer properties directly in ZUL resulted in calling the renderer
     * more times than actually needed, resulting in noticeable lack of performance.
     */
    private void prepareCategoriesTree() {
        if ( categoriesTree.getItemRenderer() == null ) {
            categoriesTree.setItemRenderer(getMaterialCategoryWithUnitsAndPriceRenderer());
        }
        categoriesTree.setModel(getMaterialCategories());
    }

    public abstract TreeModel getMaterialCategories();

    private void prepareAllCategoriesTree() {
        if ( allCategoriesTree.getItemRenderer() == null ) {
            allCategoriesTree.setItemRenderer(getMaterialCategoryRenderer());
        }
        allCategoriesTree.setModel(getAllMaterialCategories());
    }

    public abstract TreeModel getAllMaterialCategories();

    public abstract BigDecimal getTotalUnits();

    public abstract BigDecimal getTotalPrice();

    /**
     * On selecting category, refresh {@link MaterialAssignment} associated with selected {@link MaterialCategory}.
     */
    public void refreshMaterialAssignments() {
        final List<A> materials = getAssignedMaterials();
        gridMaterials.setModel(new SimpleListModel<>(materials));
        reloadGridMaterials();
    }

    public List<A> getAssignedMaterials() {
        final Treeitem treeitem = categoriesTree.getSelectedItem();
        return getAssignedMaterials(treeitem);
    }

    private List<A> getAssignedMaterials(Treeitem treeitem) {
        final MaterialCategory materialCategory = (treeitem != null) ? (MaterialCategory) treeitem.getValue() : null;

        return getAssignedMaterials(materialCategory);
    }

    private List<A> getAssignedMaterials(MaterialCategory materialCategory) {
        return getModel().getAssignedMaterials(materialCategory);
    }

    /**
     * On changing total price, recalculate unit price and refresh categories tree.
     *
     * @param row
     */
    public void updateTotalPrice(Row row) {
        final A materialAssignment =  row.getValue();
        reloadGridMaterials();
        refreshTotalPriceAndTotalUnits(materialAssignment);
    }

    protected abstract Double getTotalPrice(A materialAssignment);

    /**
     * Refresh categoriesTree since it shows totalUnits and totalPrice as well.
     */
    private void refreshTotalPriceAndTotalUnits(A materialAssignment) {
        final Treeitem item = findMaterialCategoryInTree(getCategory(materialAssignment), categoriesTree);
        if ( item != null ) {
            // Reload categoriesTree
            categoriesTree.setModel(getMaterialCategories());
        }
    }

    private Treeitem findMaterialCategoryInTree(MaterialCategory category, Tree tree) {
        for (Treeitem treeitem : tree.getItems()) {
            final MaterialCategory materialCategory = treeitem.getValue();

            if ( category.equals(materialCategory) ) {
                return treeitem;
            }
        }

        return null;
    }

    private MaterialCategory getCategory(A assignment) {
        return getMaterial(assignment).getCategory();
    }

    /**
     * Search materials on pressing search button.
     */
    public void searchMaterials() {
        final String text = txtSearchMaterial.getValue();
        final MaterialCategory materialCategory = getSelectedCategory(allCategoriesTree);
        getModel().searchMaterials(text, materialCategory);
        Util.reloadBindings(lbFoundMaterials);
    }

    /**
     * Returns {@link MaterialCategory} associated with selected {@link Treeitem} in {@link Tree}.
     *
     * @param tree
     * @return {@link MaterialCategory}
     */
    private MaterialCategory getSelectedCategory(Tree tree) {
        final Treeitem treeitem = tree.getSelectedItem();
        return (treeitem != null) ? (MaterialCategory) treeitem.getValue() : null;
    }

    /**
     * Get materials found on latest search.
     *
     * @return {@link List<Material>}
     */
    public List<Material> getMatchingMaterials() {
        return getModel().getMatchingMaterials();
    }

    /**
     * Assigns a list of selected {@link Material} to current {@link OrderElement}.
     */
    public void assignSelectedMaterials() {
        Set<Material> materials = getSelectedMaterials();
        if ( materials.isEmpty() ) {
            return;
        }

        for(Material each: materials) {
            getModel().addMaterialAssignment(each);
        }

        categoriesTree.clearSelection();
        tbAssignedMaterials.setSelected(true);
        lbFoundMaterials.clearSelection();
        Util.reloadBindings(categoriesTree);
        reloadGridMaterials();
    }

    private Set<Material> getSelectedMaterials() {
        Set<Material> result = new HashSet<>();

        final Set<Listitem> listitems = lbFoundMaterials.getSelectedItems();

        for (Listitem each: listitems) {
            final Material material = each.getValue();
            result.add(material);
        }

        return result;
    }

    public void clearSelectionCategoriesTree() {
        categoriesTree.clearSelection();
        reloadGridMaterials();
    }

    private void reloadGridMaterials() {
        if ( gridMaterials != null ) {
            Util.reloadBindings(gridMaterials);
        }
    }

    public void clearSelectionAllCategoriesTree() {
        allCategoriesTree.clearSelection();
        retrieveAllMaterials();
        Util.reloadBindings(lbFoundMaterials);
    }

    private void retrieveAllMaterials() {
        getModel().searchMaterials("", null);
    }

    /** Should be public! */
    public MaterialCategoryRenderer getMaterialCategoryRenderer() {
        return new MaterialCategoryRenderer();
    }

    private class MaterialCategoryRenderer implements TreeitemRenderer {

        /**
         * Copied verbatim from org.zkoss.zul.Tree;
         */
        @Override
        public void render(Treeitem treeitem, Object o, int i) throws Exception {
            final MaterialCategory materialCategory = (MaterialCategory) o;

            Label lblName = new Label(materialCategory.getName());

            Treerow tr;
            treeitem.setValue(o);
            if ( treeitem.getTreerow() == null ) {
                tr = new Treerow();
                tr.setParent(treeitem);
                treeitem.setOpen(true); // Expand node
            } else {
                tr = treeitem.getTreerow();
                tr.getChildren().clear();
            }

            // Add category name
            Treecell cellName = new Treecell();
            cellName.addEventListener("onClick", event ->  {
                getModel().searchMaterials("", materialCategory);
                Util.reloadBindings(lbFoundMaterials);
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
        public void render(Treeitem treeitem, Object o, int i) throws Exception {
            final MaterialCategory materialCategory = (MaterialCategory) o;

            Label lblName = new Label(materialCategory.getName());
            Label lblUnits = new Label(getUnits(materialCategory).toString());
            Label lblPrice = new Label(getPrice(materialCategory).toString() + getCurrencySymbol());

            Treerow tr;
            treeitem.setValue(o);
            if ( treeitem.getTreerow() == null ) {
                tr = new Treerow();
                tr.setParent(treeitem);
                treeitem.setOpen(true); // Expand node
            } else {
                tr = treeitem.getTreerow();
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

        private BigDecimal getUnits(MaterialCategory materialCategory) {
            return getModel().getUnits(materialCategory);
        }

        private BigDecimal getPrice(MaterialCategory materialCategory) {
            return getModel().getPrice(materialCategory);
        }
    }

    /**
     * On clicking remove {@link MaterialAssignment}, shows dialog for confirming removing selected element.
     *
     * @param materialAssignment
     */
    public void showRemoveMaterialAssignmentDlg(A materialAssignment) {
        int status = Messagebox.show(
                _("Delete item {0}. Are you sure?", getMaterial(materialAssignment).getCode()),
                _("Delete"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);

        if ( Messagebox.OK == status ) {
            removeMaterialAssignment(materialAssignment);
        }
    }

    protected abstract Material getMaterial(A materialAssignment);

    private void removeMaterialAssignment(A materialAssignment) {
        getModel().removeMaterialAssignment(materialAssignment);
        reloadGridMaterials();
        reloadTree(categoriesTree);
    }

    private void reloadTree(Tree tree) {
        final Treeitem treeitem = tree.getSelectedItem();

        if ( treeitem != null ) {
            final MaterialCategory materialCategory = treeitem.getValue();
            tree.setModel(getMaterialCategories());
            locateAndSelectMaterialCategory(tree, materialCategory);
        } else {
            tree.setModel(getMaterialCategories());
            reloadGridMaterials();
        }
    }

    private boolean locateAndSelectMaterialCategory(Tree tree, MaterialCategory materialCategory) {
        Treeitem treeitem = findTreeItemByMaterialCategory(tree.getRoot(), materialCategory);
        if ( treeitem != null ) {
            treeitem.setSelected(true);
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private Treeitem findTreeItemByMaterialCategory(Component node, MaterialCategory materialCategory) {
        if ( node instanceof Treeitem ) {
            final Treeitem treeitem = (Treeitem) node;
            final MaterialCategory _materialCategory =  treeitem.getValue();
            if ( _materialCategory.getId().equals(materialCategory.getId()) ) {
                return treeitem;
            }
        }
        for (Component obj : node.getChildren()) {
            if (obj != null) {
                Treeitem treeitem = findTreeItemByMaterialCategory(obj, materialCategory);
                if ( treeitem != null ) {
                    return treeitem;
                }
            }
        }

        return null;
    }

    /**
     * On clicking Split button, shows dialog for splitting selected {@link MaterialAssignment} into two.
     *
     * @param materialAssignment
     */
    @SuppressWarnings("unchecked")
    public void showSplitMaterialAssignmentDlg(A materialAssignment) {
        MessageboxDlg dialogSplitAssignment;

        final String message =
                _("Do you want to split the material assignment {0}?", getMaterial(materialAssignment).getCode());

        Map<String, java.io.Serializable> args = new HashMap<>();
        args.put("message", message);
        args.put("title", _("Split new assignment"));
        args.put("OK", Messagebox.OK);
        args.put("CANCEL", Messagebox.CANCEL);
        args.put("icon", Messagebox.QUESTION);

        dialogSplitAssignment = (MessageboxDlg) Executions.createComponents("/orders/_splitMaterialAssignmentDlg.zul",
                self, args);
        Decimalbox dbUnits = (Decimalbox) dialogSplitAssignment.getFellowIfAny("dbUnits");
        dbUnits.setValue(getUnits(materialAssignment));
        try {
            dialogSplitAssignment.doModal();

            Messagebox.Button status = dialogSplitAssignment.getResult();

            if ( Messagebox.Button.OK == status ) {
                splitMaterialAssignment(materialAssignment, dbUnits.getValue());
            }
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new {@link MaterialAssignment} out of materialAssignment, but setting its units attribute to units.
     *
     * @param materialAssignment
     * @param units
     */
    private void splitMaterialAssignment(A materialAssignment, BigDecimal units) {
        A newAssignment = copyFrom(materialAssignment);
        BigDecimal currentUnits = getUnits(materialAssignment);
        BigDecimal newUnits = units;

        if ( units.compareTo(currentUnits) > 0 ) {
            newUnits = currentUnits;
            currentUnits = BigDecimal.ZERO;
        } else {
            currentUnits = currentUnits.subtract(newUnits);
        }
        setUnits(newAssignment, newUnits);
        setUnits(materialAssignment, currentUnits);
        getModel().addMaterialAssignment(newAssignment);
        reloadGridMaterials();
    }

    protected abstract void setUnits(A assignment, BigDecimal units);

    protected abstract A copyFrom(A assignment);

    protected abstract BigDecimal getUnits(A assignment);

    public List<UnitType> getUnitTypes() {
        return getModel().getUnitTypes();
    }

    public void selectUnitType(Component self) {
        Listitem selectedItem = ((Listbox) self).getSelectedItem();
        UnitType unitType = selectedItem.getValue();
        Material material = ((Row) self.getParent()).getValue();
        material.setUnitType(unitType);
    }

    public UnitTypeListRenderer getRenderer() {
        return unitTypeListRenderer;
    }

    public class UnitTypeListRenderer implements ListitemRenderer {
        /**
         * RowRenderer for a @{UnitType} element.
         *
         * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
         */
        @Override
        public void render(Listitem listitem, Object o, int i) throws Exception {
            final UnitType unitType = (UnitType) o;
            listitem.setValue(unitType);

            Listcell listCell = new Listcell(unitType.getMeasure());
            listitem.appendChild(listCell);

            Listbox listbox = listitem.getListbox();
            Component parent = listbox.getParent();

            if ( parent instanceof Row ) {
                Object assignment = ((Row) parent).getValue();
                if ( getModel().isCurrentUnitType(assignment, unitType) ) {
                    listitem.getListbox().setSelectedItem(listitem);
                }

                return;
            }

            if ( parent instanceof Listcell ) {
                Material material = ((Listitem) (parent.getParent())).getValue();
                if ( isCurrentUnitType(material, unitType) ) {
                    listitem.getListbox().setSelectedItem(listitem);
                }
            }
        }

        private boolean isCurrentUnitType(Material material, UnitType unitType) {
            return material != null &&
                    material.getUnitType() != null &&
                    unitType.getId().equals(material.getUnitType().getId());
        }
    }

    public String getCurrencySymbol() {
        return Util.getCurrencySymbol();
    }

    public String getMoneyFormat() {
        return Util.getMoneyFormat();
    }

}
