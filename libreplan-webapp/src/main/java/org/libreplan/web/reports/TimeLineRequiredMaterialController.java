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

package org.libreplan.web.reports;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;

import org.libreplan.business.materials.entities.Material;
import org.libreplan.business.materials.entities.MaterialCategory;
import org.libreplan.business.materials.entities.MaterialStatusEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

import com.igalia.java.zk.components.JasperreportComponent;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class TimeLineRequiredMaterialController extends
        LibrePlanReportController {

    private static final String REPORT_NAME = "timeLineRequiredMaterial";

    private ITimeLineRequiredMaterialModel timeLineRequiredMaterialModel;

    private Tree allCategoriesTree;

    private Datebox startingDate;

    private Datebox endingDate;

    private Date filterStartingDate = getDefaultStartingDate();

    private Date filterEndingDate = getDefaultEndingDate();

    private String selectedStatus = getDefaultStatus();

    private Listbox lbOrders;

    private BandboxSearch bdOrders;

    List<MaterialCategory> filterCategories = new ArrayList<MaterialCategory>();

    List<Material> filterMaterials = new ArrayList<Material>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        timeLineRequiredMaterialModel.init();
        prepareAllCategoriesTree();
    }

    public List<Order> getAllOrders() {
        return timeLineRequiredMaterialModel.getOrders();
    }

    public List<Order> getSelectedOrders() {
        return Collections.unmodifiableList(timeLineRequiredMaterialModel
                .getSelectedOrders());
    }

    public void onSelectOrder() {
        Order order = (Order) bdOrders.getSelectedElement();
        if (order == null) {
            throw new WrongValueException(bdOrders, _("please, select a project"));
        }
        boolean result = timeLineRequiredMaterialModel
                .addSelectedOrder(order);
        if (!result) {
            throw new WrongValueException(bdOrders,
                    _("This project has already been added."));
        } else {
            Util.reloadBindings(lbOrders);
        }
        bdOrders.clear();
    }

    public void onRemoveOrder(Order order) {
        timeLineRequiredMaterialModel.removeSelectedOrder(order);
        Util.reloadBindings(lbOrders);
    }

    @Override
    protected String getReportName() {
        return REPORT_NAME;
    }

    @Override
    protected JRDataSource getDataSource() {
        return timeLineRequiredMaterialModel.getTimeLineRequiredMaterial(
                getStartingDate(), getEndingDate(),
                getCorrespondentStatus(selectedStatus),
                getSelectedOrders(), getSelectedCategories(),
                getSelectedMaterials());
    }

    public Date getStartingDate() {
        return this.filterStartingDate;
    }

    public void setStartingDate(Date date) {
        if (date == null) {
            this.filterStartingDate = getDefaultStartingDate();
            this.startingDate.setValue(filterStartingDate);
        } else {
            this.filterStartingDate = date;
        }
    }

    private Date getDefaultStartingDate() {
        return new Date();
    }

    public Date getEndingDate() {
        return this.filterEndingDate;
    }

    public void setEndingDate(Date date) {
        if (date == null) {
            this.filterEndingDate = getDefaultEndingDate();
            this.endingDate.setValue(filterEndingDate);
        } else {
            this.filterEndingDate = date;
        }
    }

    public Date getDefaultEndingDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getStartingDate());
        calendar.add(calendar.MONTH, 1);

        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        calendar.set(year, month, date);
        return calendar.getTime();
    }

    @Override
    protected Map<String, Object> getParameters() {
        Map<String, Object> result = super.getParameters();

        result.put("startingDate", getStartingDate());
        result.put("endingDate", getEndingDate());
        result.put("status", getSelectedStatusName());
        return result;
    }

    public void showReport(JasperreportComponent jasperreport){
        super.showReport(jasperreport);
    }

    public List<String> getMaterialStatus() {
        List<String> status = new ArrayList<String>();
        status.add(getDefaultStatus());
        for (MaterialStatusEnum matStatus : MaterialStatusEnum.values()) {
            status.add(matStatus.name());
        }
        return status;
    }

    public String getDefaultStatus() {
        return _("All");
    }

    public String getSelectedStatus(){
        return selectedStatus;
    }

    public void setSelectedStatus(String status) {
        selectedStatus = status;
    }

    private MaterialStatusEnum getCorrespondentStatus(String status) {
        for (MaterialStatusEnum matStatus : MaterialStatusEnum.values()) {
            if (status.equals(matStatus.name())) {
                return matStatus;
            }
        }
        return null;
    }

    private String getSelectedStatusName() {
        if (getSelectedStatus().equals(getDefaultStatus())) {
            return null;
        }
        return selectedStatus;
    }

    /**
     * Operations to filter by category and/or material
     */

    private void prepareAllCategoriesTree() {
        if (allCategoriesTree.getTreeitemRenderer() == null) {
            allCategoriesTree
                    .setTreeitemRenderer(getMaterialCategoryRenderer());
        }
        allCategoriesTree.setModel(getAllMaterialCategories());
    }

    public TreeModel getAllMaterialCategories() {
        return getModel().getAllMaterialCategories();
    }

    public void clearSelectionAllCategoriesTree() {
        allCategoriesTree.clearSelection();
    }

    public MaterialCategoryRenderer getMaterialCategoryRenderer() {
        return new MaterialCategoryRenderer();
    }

    private static class MaterialCategoryRenderer implements TreeitemRenderer {

        /**
         * Copied verbatim from org.zkoss.zul.Tree;
         */
        @Override
        public void render(Treeitem ti, Object node) {
            Label lblName = null;
            if (node instanceof MaterialCategory) {
                final MaterialCategory materialCategory = (MaterialCategory) node;
                lblName = new Label(materialCategory.getName());
            } else if (node instanceof Material) {
                final Material material = (Material) node;
                lblName = new Label(material.getDescription());
            }

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
        }
    }

    private ITimeLineRequiredMaterialModel getModel() {
        return this.timeLineRequiredMaterialModel;
    }

    private List<MaterialCategory> getSelectedCategories() {
        filterCategories.clear();
        Set<Treeitem> setItems = (Set<Treeitem>) allCategoriesTree
                .getSelectedItems();

        for (Treeitem ti : setItems) {
            if ((ti.getValue() != null)
                    && (ti.getValue() instanceof MaterialCategory)) {
                filterCategories.add((MaterialCategory) ti.getValue());
                addSubCategories((MaterialCategory) ti.getValue());
            }
        }
        return filterCategories;
    }

    private void addSubCategories(MaterialCategory category) {
        for (MaterialCategory subCategory : category.getSubcategories()) {
            filterCategories.add(subCategory);
            addSubCategories(subCategory);
        }
    }

    private List<Material> getSelectedMaterials() {
        List<Material> materials = new ArrayList<Material>();
        Set<Treeitem> setItems = (Set<Treeitem>) allCategoriesTree
                .getSelectedItems();
        for (Treeitem ti : setItems) {
            if ((ti.getValue() != null) && (ti.getValue() instanceof Material)
                    && (!isContainedInCategories((Material) ti.getValue()))) {
                materials.add((Material) ti.getValue());
            }
        }
        return materials;
    }

    private boolean isContainedInCategories(Material material) {
        if (material != null) {
            return getSelectedCategories().contains(material.getCategory());
        }
        return false;
    }
}
