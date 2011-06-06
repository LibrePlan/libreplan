/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.navalplanner.business.materials.daos.IMaterialCategoryDAO;
import org.navalplanner.business.materials.daos.IMaterialDAO;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.MaterialStatusEnum;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.reports.dtos.TimeLineRequiredMaterialDTO;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zul.TreeModel;

/**
 * Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TimeLineRequiredMaterialModel implements
        ITimeLineRequiredMaterialModel {

    @Autowired
    IOrderElementDAO orderElementDAO;

    @Autowired
    IOrderDAO orderDAO;

    @Autowired
    ITaskSourceDAO taskSourceDAO;

    @Autowired
    ITaskElementDAO taskElementDAO;

    @Autowired
    IMaterialDAO materialDAO;

    @Autowired
    private IMaterialCategoryDAO categoryDAO;

    private Date startingDate;

    private Date endingDate;

    private List<MaterialAssignment> listMaterialAssignment = new ArrayList<MaterialAssignment>();

    private MutableTreeModel<Object> allMaterialCategories = MutableTreeModel
            .create(Object.class);

    @Autowired
    private IScenarioManager scenarioManager;

    private List<Order> selectedOrders = new ArrayList<Order>();

    private List<Order> allOrders = new ArrayList<Order>();

    @Override
    @Transactional(readOnly = true)
    public void init() {
        selectedOrders.clear();
        allOrders.clear();
        loadAllOrders();
    }

    private void loadAllOrders() {
        allOrders = orderDAO.getOrdersByScenario(scenarioManager.getCurrent());
    }

    @Override
    public List<Order> getOrders() {
        return allOrders;
    }

    private void initializeOrderElements(List<OrderElement> orderElements) {
        for (OrderElement each: orderElements) {
            initializeOrderElement(each);
        }
    }

    private void initializeOrderElement(OrderElement orderElement) {
        orderElement.getName();
    }

    @Override
    public void removeSelectedOrder(Order order) {
        this.selectedOrders.remove(order);
    }

    @Override
    public boolean addSelectedOrder(Order order) {
        if (this.selectedOrders.contains(order)) {
            return false;
        }
        this.selectedOrders.add(order);
        return true;
    }

    @Override
    public List<Order> getSelectedOrders() {
        return selectedOrders;
    }

    private void reattachmentOrder(Order order) {
        orderDAO.reattachUnmodifiedEntity(order);
        initializeOrderElements(order.getAllOrderElements());
    }

    @Override
    @Transactional(readOnly = true)
    public JRDataSource getTimeLineRequiredMaterial(Date startingDate,
            Date endingDate, MaterialStatusEnum status, List<Order> listOrders,
            List<MaterialCategory> categories, List<Material> materials) {

        List<Order> orderToInitialized = new ArrayList<Order>(listOrders);
        if (listOrders.isEmpty()) {
            orderToInitialized.addAll(getOrders());
        }

        for (Order each : orderToInitialized) {
            reattachmentOrder(each);
        }

        Scenario currentScenario = scenarioManager.getCurrent();
        for (Order each : orderToInitialized) {
            each.useSchedulingDataFor(currentScenario);
        }

        List<TimeLineRequiredMaterialDTO> result = filterConsult(startingDate,
                endingDate, status, listOrders, categories, materials);


         if (result != null && !result.isEmpty()) {
            return new JRBeanCollectionDataSource(result);
        } else {
            return new JREmptyDataSource();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeLineRequiredMaterialDTO> filterConsult(Date startingDate,
            Date endingDate, MaterialStatusEnum status, List<Order> listOrders,
            List<MaterialCategory> categories, List<Material> materials) {

        this.startingDate = startingDate;
        this.endingDate = endingDate;

        listMaterialAssignment = materialDAO.getFilterMaterial(status,
                listOrders, categories, materials);
        loadDataMaterial();

        List<TimeLineRequiredMaterialDTO> result = new ArrayList<TimeLineRequiredMaterialDTO>();
        result = sort(filterAndCreateMaterialDTOs());
        return result;
    }

    private List<TimeLineRequiredMaterialDTO> filterAndCreateMaterialDTOs() {
        List<TimeLineRequiredMaterialDTO> result = new ArrayList<TimeLineRequiredMaterialDTO>();
        for (MaterialAssignment material : listMaterialAssignment) {
            OrderElement order = orderDAO.loadOrderAvoidingProxyFor(material
                    .getOrderElement());

            TaskElement task = findTaskBy(material);
            reloadTask(task);
            Date startDate;
            Date endDate;

            if(task != null){
                startDate = task.getStartDate();
                endDate = task.getEndDate();
            }else{
                startDate = order.getInitDate();
                endDate = order.getInitDate();
            }
            // check if the dates match
            if(acceptDates(startDate,endDate)){
                result.add(new TimeLineRequiredMaterialDTO(material, task,
                        startDate, order));
            }
        }
        return result;
    }

    private void reloadTask(TaskElement task) {
        if (task != null) {
            task.getOrderElement().getName();
        }
    }

    private void loadDataMaterial() {
        for (MaterialAssignment material : listMaterialAssignment) {
            material.getOrderElement().getName();
            material.getMaterial().getCode();
        }
    }

    private TaskElement findTaskBy(MaterialAssignment material) {
        // look up into the order elements tree
        TaskElement task = lookToUpAssignedTask(material);
        if (task != null) {
            return task;
        }

        // look down into the order elements tree
        task = lookToDownAssignedTask(material);
        if (task != null) {
            return task;
        }

        // not exist assigned task
        return null;
    }

    private TaskElement lookToUpAssignedTask(MaterialAssignment material) {
        OrderElement current = material.getOrderElement();
        while (current != null) {
            if (current.isSchedulingPoint()) {
                return current.getAssociatedTaskElement();
            }
            current = current.getParent();
        }
        return null;
    }

    private TaskElement lookToDownAssignedTask(MaterialAssignment material) {
        OrderElement current = material.getOrderElement();
        Date lessDate = null;
        TaskElement resultTask = null;
        for (OrderElement child : current.getAllChildren()) {
            if (child.isSchedulingPoint()) {
                TaskElement task = child.getAssociatedTaskElement();
                if ((task != null)
                        && ((lessDate == null) || (lessDate.after(task
                                .getStartDate())))) {
                    lessDate = task.getStartDate();
                    resultTask = task;
                }
            }
        }
        return resultTask;
    }

    private boolean acceptDates(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return ((startDate.compareTo(startingDate) >= 0) && (endDate
                .compareTo(endingDate) <= 0));
    }

    public List<TimeLineRequiredMaterialDTO> sort(
            List<TimeLineRequiredMaterialDTO> list) {
        List<TimeLineRequiredMaterialDTO> result = new ArrayList<TimeLineRequiredMaterialDTO>();
        if ((list != null) && (!list.isEmpty())) {
            result.add(list.get(0));
            for (int i = 1; i < list.size(); i++) {
                int j = 0;
                while ((j < result.size())
                        && isGreaterDate(result.get(j), list.get(i))) {
                    j++;
                }
                if (j >= result.size()) {
                    result.add(list.get(i));
                } else {
                    result.add(j, list.get(i));
                }
            }
        }
        return result;
    }

    private boolean isGreaterDate(TimeLineRequiredMaterialDTO dto,
            TimeLineRequiredMaterialDTO dtoToOrder) {
        if (dto != null) {
            return dto.getInitDate().after(dtoToOrder.getInitDate());
        }
        return true;
    }

    /**
     * Operation to filter by categories and materials
     */

    @Transactional(readOnly = true)
    public TreeModel getAllMaterialCategories() {
        if (allMaterialCategories.isEmpty()) {
            feedTree(allMaterialCategories, categoryDAO.getAll());
        }
        return allMaterialCategories;
    }

    private void feedTree(MutableTreeModel<Object> tree,
            List<MaterialCategory> materialCategories) {
        for (MaterialCategory each : materialCategories) {
            initializeMaterialCategory(each);
            addCategory(tree, each);
        }
    }

    private void initializeMaterials(Collection<Material> materials) {
        for (Material each : materials) {
            initializeMaterial(each);
        }
    }

    protected void initializeMaterialCategories(
            Collection<MaterialCategory> materialCategories) {
        for (MaterialCategory each : materialCategories) {
            initializeMaterialCategory(each);
        }
    }

    protected void initializeMaterialCategory(MaterialCategory materialCategory) {
        materialCategory.getName();
        initializeMaterials(materialCategory.getMaterials());
        initializeMaterialCategories(materialCategory.getSubcategories());
    }

    private void initializeMaterial(Material material) {
        material.getDescription();
        material.getCategory().getName();
    }

    /**
     * Adds category to treeModel If category.parent is not in treeModel add it
     * to treeModel recursively.
     */
    private void addCategory(MutableTreeModel<Object> materialCategories,
            MaterialCategory materialCategory) {

        categoryDAO.reattach(materialCategory);
        final MaterialCategory parent = materialCategory.getParent();
        if (!materialCategories.contains(parent, materialCategory)) {
            if (parent == null) {
                materialCategories.addToRoot(materialCategory);
            } else {
                addCategory(materialCategories, parent);
                materialCategories.add(parent, materialCategory);
            }
            addMaterials(materialCategories, materialCategory);
        }
    }

    private void addMaterials(MutableTreeModel<Object> materialCategories,
            MaterialCategory materialCategory) {
        for (Material material : materialCategory.getMaterials()) {
            materialCategories.add(materialCategory, material);
        }
    }
}
