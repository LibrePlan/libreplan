/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.joda.time.LocalDate;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.labels.daos.ILabelDAO;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.reports.dtos.CostExpenseSheetDTO;
import org.libreplan.business.reports.dtos.OrderCostMasterDTO;
import org.libreplan.business.reports.dtos.OrderCostsPerResourceDTO;
import org.libreplan.business.reports.dtos.ReportPerOrderElementDTO;
import org.libreplan.business.resources.daos.ICriterionTypeDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderCostsPerResourceModel implements IOrderCostsPerResourceModel {

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    ITaskElementDAO taskDAO;

    @Autowired
    private ILabelDAO labelDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    private List<Order> selectedOrders = new ArrayList<Order>();

    private List<Label> selectedLabels = new ArrayList<Label>();

    private List<Criterion> selectedCriterions = new ArrayList<Criterion>();

    private List<Order> allOrders = new ArrayList<Order>();

    private List<Criterion> allCriterions = new ArrayList<Criterion>();

    private List<Label> allLabels = new ArrayList<Label>();

    private String selectedCriteria;

    private String selectedLabel;

    private boolean hasChangeCriteria = false;

    private boolean hasChangeLabels = false;

    private static List<ResourceEnum> applicableResources = new ArrayList<ResourceEnum>();

    static {
        applicableResources.add(ResourceEnum.WORKER);
    }

    @Override
    @Transactional(readOnly = true)
    public JRDataSource getOrderReport(final List<Order> orders, Date startingDate,
            Date endingDate, List<Label> labels, List<Criterion> criterions) {

        reattachLabels();

        // list to the master report
        List<OrderCostMasterDTO> listOrderCostMasterDTO = new ArrayList<OrderCostMasterDTO>();

        // list to the WorkReportLine subreport
        List<OrderCostsPerResourceDTO> workingHoursPerWorkerList = orderDAO
                .getOrderCostsPerResource(orders, startingDate, endingDate, criterions);
        filteredOrderElementsByLabels(workingHoursPerWorkerList, labels);
        Collections.sort(workingHoursPerWorkerList);
        Map<OrderElement, List<OrderCostsPerResourceDTO>> mapWRL = groupWorkReportLinesByOrderElment(workingHoursPerWorkerList);

        // list to the ExpenseSheet subreport
        List<CostExpenseSheetDTO> costExpenseSheetList = orderDAO.getCostExpenseSheet(orders,
                startingDate, endingDate, criterions);
        filteredOrderElementsByLabels(costExpenseSheetList, labels);
        Map<OrderElement, List<CostExpenseSheetDTO>> mapES = groupExpensesByOrderElment(costExpenseSheetList);

        Set<OrderElement> listOrderElement = new HashSet<OrderElement>(mapWRL.keySet());
        listOrderElement.addAll(mapES.keySet());

        if (listOrderElement.isEmpty()) {
            listOrderCostMasterDTO.add(createEmptyOrderCostMasterDTO());
        } else {
            for (OrderElement orderElement : listOrderElement) {
                List<OrderCostsPerResourceDTO> listWorkReportLineDTO = mapWRL.get(orderElement);
                if (listWorkReportLineDTO == null || listWorkReportLineDTO.isEmpty()) {
                    Order order = Order.create();
                    order.setName(_("All projects"));
                    listWorkReportLineDTO = createEmptyWorkReportLineList(order);
                }

                JRDataSource dsWRL = new JRBeanCollectionDataSource(listWorkReportLineDTO);

                List<CostExpenseSheetDTO> listExpenseSheetDTO = mapES.get(orderElement);
                JRDataSource dsES = null;
                if (listExpenseSheetDTO != null && !listExpenseSheetDTO.isEmpty()) {
                    dsES = new JRBeanCollectionDataSource(listExpenseSheetDTO);
                }

                OrderCostMasterDTO orderCostMasterDTO = new OrderCostMasterDTO(orderElement, dsWRL,
                        dsES);
                initOrderInOrderCostMasterDTO(orderCostMasterDTO, listWorkReportLineDTO,
                        listExpenseSheetDTO);
                listOrderCostMasterDTO.add(orderCostMasterDTO);
            }
        }
        if (listOrderCostMasterDTO != null && !listOrderCostMasterDTO.isEmpty()) {
            Collections.sort(listOrderCostMasterDTO);
            return new JRBeanCollectionDataSource(listOrderCostMasterDTO);
        } else {
            return new JREmptyDataSource();
        }
    }

    private void initOrderInOrderCostMasterDTO(OrderCostMasterDTO orderCostMasterDTO,
            List<OrderCostsPerResourceDTO> listWorkReportLineDTO,
            List<CostExpenseSheetDTO> listExpenseSheetDTO) {
        if (listExpenseSheetDTO != null && !listExpenseSheetDTO.isEmpty()) {
            Order order = listExpenseSheetDTO.get(0).getOrder();
            orderCostMasterDTO.setOrderCode(order.getCode());
            orderCostMasterDTO.setOrderName(order.getName());
        } else if (listWorkReportLineDTO != null && !listWorkReportLineDTO.isEmpty()) {
            orderCostMasterDTO.setOrderCode(listWorkReportLineDTO.get(0).getOrderCode());
            orderCostMasterDTO.setOrderName(listWorkReportLineDTO.get(0).getOrderName());
        }
    }

    private Map<OrderElement, List<OrderCostsPerResourceDTO>> groupWorkReportLinesByOrderElment(
            List<OrderCostsPerResourceDTO> workingHoursPerWorkerList) {
        Map<OrderElement, List<OrderCostsPerResourceDTO>> mapWRL = new HashMap<OrderElement, List<OrderCostsPerResourceDTO>>();
        for (OrderCostsPerResourceDTO dto : workingHoursPerWorkerList) {
            OrderElement orderElement = dto.getOrderElement();
            if (mapWRL.get(orderElement) == null) {
                mapWRL.put(orderElement, new ArrayList<OrderCostsPerResourceDTO>());
            }
            mapWRL.get(orderElement).add(dto);
        }
        return mapWRL;
    }

    private Map<OrderElement, List<CostExpenseSheetDTO>> groupExpensesByOrderElment(
            List<CostExpenseSheetDTO> costExpenseSheetList) {
        Map<OrderElement, List<CostExpenseSheetDTO>> mapES = new HashMap<OrderElement, List<CostExpenseSheetDTO>>();
        for (CostExpenseSheetDTO dto : costExpenseSheetList) {
            OrderElement orderElement = dto.getOrderElement();
            if (mapES.get(orderElement) == null) {
                mapES.put(orderElement, new ArrayList<CostExpenseSheetDTO>());
            }
            mapES.get(orderElement).add(dto);
        }
        return mapES;
    }

    private List<OrderCostsPerResourceDTO> createEmptyWorkReportLineList(Order order) {
        List<OrderCostsPerResourceDTO> listWorkReportLineDTO = new ArrayList<OrderCostsPerResourceDTO>();
        Worker emptyWorker = createFictitiousWorker();
        WorkReportLine wrl = createEmptyWorkReportLine(emptyWorker);
        listWorkReportLineDTO.add(createEmptyOrderCostsPerResourceDTO(order, emptyWorker, wrl));
        return listWorkReportLineDTO;
    }

    private OrderCostsPerResourceDTO createEmptyOrderCostsPerResourceDTO(Order order,
            Worker emptyWorker, WorkReportLine wrl) {
        OrderCostsPerResourceDTO emptyDTO = new OrderCostsPerResourceDTO(emptyWorker, wrl);
        emptyDTO.setOrderName(order.getName());
        emptyDTO.setOrderCode(order.getCode());
        emptyDTO.setCost(new BigDecimal(0));
        return emptyDTO;
    }

    private List<CostExpenseSheetDTO> createEmptyExpenseSheetLineList(Order order) {
        List<CostExpenseSheetDTO> listES = new ArrayList<CostExpenseSheetDTO>();
        listES.add(createEmptyExpenseSheetDTO(order));
        return listES;
    }

    private CostExpenseSheetDTO createEmptyExpenseSheetDTO(OrderElement orderElement) {
        ExpenseSheetLine emptyBean = ExpenseSheetLine.create(BigDecimal.ZERO, "Expense concept",
                new LocalDate(), orderElement);
        CostExpenseSheetDTO emptyDTO = new CostExpenseSheetDTO(emptyBean);
        return emptyDTO;
    }

    private OrderCostMasterDTO createEmptyOrderCostMasterDTO() {
        // create empty order
        Order order = Order.create();
        order.setName(_("All projects"));

        // create empty subreport to expense sheets
        JRDataSource emptyES = new JRBeanCollectionDataSource(
                createEmptyExpenseSheetLineList(order));

        // create empty subreport to work report lines
        JRDataSource emptyWRL = new JRBeanCollectionDataSource(createEmptyWorkReportLineList(order));

        OrderCostMasterDTO emptyDTO = new OrderCostMasterDTO(order, emptyWRL, emptyES);
        return emptyDTO;
    }

    private void loadAllOrders() {
        this.allOrders = orderDAO.getOrdersByReadAuthorizationByScenario(
                SecurityUtils.getSessionUserLoginName(),
                scenarioManager.getCurrent());

        Collections.sort(this.allOrders);
    }

    @Override
    public List<Order> getOrders() {
        return allOrders;
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

    private WorkReportLine createEmptyWorkReportLine(Worker worker) {
        OrderLine leaf = OrderLine.create();
        leaf.setCode(_("All project tasks"));

        TypeOfWorkHours w = TypeOfWorkHours.create();
        w.setDefaultPrice(new BigDecimal(0));

        WorkReportLine wrl = new WorkReportLine();
        wrl.setEffort(EffortDuration.zero());
        wrl.setTypeOfWorkHours(w);
        wrl.setResource(worker);
        wrl.setOrderElement(leaf);
        return wrl;
    }

    private Worker createFictitiousWorker() {
        Worker unnasigned = new Worker();
        unnasigned.setFirstName(_("Total dedication"));
        unnasigned.setSurname(" ");
        return unnasigned;
    }

    @Override
    @Transactional(readOnly = true)
    public void init() {
        selectedOrders.clear();
        selectedCriterions.clear();
        selectedLabels.clear();

        allOrders.clear();
        allLabels.clear();
        allCriterions.clear();

        loadAllOrders();
        loadAllLabels();
        loadAllCriterions();
    }

    @Transactional(readOnly = true)
    private void filteredOrderElementsByLabels(
            List<? extends ReportPerOrderElementDTO> listExpenses, List<Label> labels) {
        List<ReportPerOrderElementDTO> listExpensesCopy = new ArrayList<ReportPerOrderElementDTO>(
                listExpenses);

        if (labels != null && !labels.isEmpty()) {
            for (ReportPerOrderElementDTO dto : listExpensesCopy) {
                List<Label> inheritedLabels = getInheritedLabels(dto.getOrderElement());

                if (!containsAny(labels, inheritedLabels)) {
                    listExpenses.remove(dto);
                }
            }
        }
    }

    private boolean containsAny(List<Label> labelsA, List<Label> labelsB) {
        for (Label label : labelsB) {
            if (labelsA.contains(label)) {
                return true;
            }
        }
        return false;
    }

    public List<Label> getInheritedLabels(OrderElement orderElement) {
        List<Label> result = new ArrayList<Label>();
        if (orderElement != null) {
            result.addAll(orderElement.getLabels());
            OrderElement parent = orderElement.getParent();
            while (parent != null) {
                result.addAll(parent.getLabels());
                parent = parent.getParent();
            }
        }
        return result;
    }

    private void reattachLabels() {
        for (Label label : getAllLabels()) {
            labelDAO.reattach(label);
        }
    }

    @Override
    public List<Label> getAllLabels() {
        return allLabels;
    }

    @Transactional(readOnly = true)
    private void loadAllLabels() {
        allLabels = labelDAO.getAll();
        // initialize the labels
        for (Label label : allLabels) {
            label.getType().getName();
        }
    }

    @Override
    public void removeSelectedLabel(Label label) {
        this.selectedLabels.remove(label);
        this.hasChangeLabels = true;
    }

    @Override
    public boolean addSelectedLabel(Label label) {
        if (this.selectedLabels.contains(label)) {
            return false;
        }
        this.selectedLabels.add(label);
        this.hasChangeLabels = true;
        return true;
    }

    @Override
    public List<Label> getSelectedLabels() {
        return selectedLabels;
    }

    @Override
    public List<Criterion> getCriterions() {
        return this.allCriterions;
    }

    private void loadAllCriterions() {
        List<CriterionType> listTypes = getCriterionTypes();
        for (CriterionType criterionType : listTypes) {
            if (criterionType.isEnabled()) {
                Set<Criterion> listCriterion = getDirectCriterions(criterionType);
                addCriterionWithItsType(listCriterion);
            }
        }
    }

    private static Set<Criterion> getDirectCriterions(CriterionType criterionType) {
        Set<Criterion> criterions = new HashSet<Criterion>();
        for (Criterion criterion : criterionType.getCriterions()) {
            if (criterion.getParent() == null) {
                criterions.add(criterion);
            }
        }
        return criterions;
    }

    private void addCriterionWithItsType(Set<Criterion> children) {
        for (Criterion criterion : children) {
            if (criterion.isActive()) {
                // Add to the list
                allCriterions.add(criterion);
                addCriterionWithItsType(criterion.getChildren());
            }
        }
    }

    private List<CriterionType> getCriterionTypes() {
        return criterionTypeDAO.getCriterionTypesByResources(applicableResources);
    }

    @Override
    public void removeSelectedCriterion(Criterion criterion) {
        this.selectedCriterions.remove(criterion);
        this.hasChangeCriteria = true;
    }

    @Override
    public boolean addSelectedCriterion(Criterion criterion) {
        if (this.selectedCriterions.contains(criterion)) {
            return false;
        }
        this.selectedCriterions.add(criterion);
        this.hasChangeCriteria = true;
        return true;
    }

    @Override
    public List<Criterion> getSelectedCriterions() {
        return selectedCriterions;
    }

    public void setSelectedLabel(String selectedLabel) {
        this.selectedLabel = selectedLabel;
    }

    public String getSelectedLabel() {
        if (hasChangeLabels) {
            this.selectedLabel = null;
            Iterator<Label> iterator = this.selectedLabels.iterator();
            if (iterator.hasNext()) {
                this.selectedLabel = new String();
                this.selectedLabel = this.selectedLabel.concat(iterator.next().getName());
            }
            while (iterator.hasNext()) {
                this.selectedLabel = this.selectedLabel.concat(", " + iterator.next().getName());
            }
            hasChangeLabels = false;
        }
        return selectedLabel;
    }

    public void setSelectedCriteria(String selectedCriteria) {
        this.selectedCriteria = selectedCriteria;
    }

    public String getSelectedCriteria() {
        if (hasChangeCriteria) {
            this.selectedCriteria = null;
            Iterator<Criterion> iterator = this.selectedCriterions.iterator();
            if (iterator.hasNext()) {
                this.selectedCriteria = new String();
                this.selectedCriteria = this.selectedCriteria.concat(iterator.next().getName());
            }
            while (iterator.hasNext()) {
                this.selectedCriteria = this.selectedCriteria.concat(", "
                        + iterator.next().getName());
            }
            hasChangeCriteria = false;
        }
        return selectedCriteria;
    }
}
