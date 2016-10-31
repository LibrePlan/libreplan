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
package org.libreplan.business.hibernate.notification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.calendars.entities.CalendarAvailability;
import org.libreplan.business.calendars.entities.CalendarData;
import org.libreplan.business.calendars.entities.CalendarException;
import org.libreplan.business.common.AdHocTransactionService;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.costcategories.daos.ICostCategoryDAO;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.business.externalcompanies.daos.IExternalCompanyDAO;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.labels.daos.ILabelDAO;
import org.libreplan.business.labels.daos.ILabelTypeDAO;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.chart.ResourceLoadChartData;
import org.libreplan.business.planner.daos.IDayAssignmentDAO;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.GenericResourceAllocation;
import org.libreplan.business.planner.entities.ICostCalculator;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.daos.ICriterionTypeDAO;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.Machine;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.VirtualWorker;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Óscar González Fernández
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class PredefinedDatabaseSnapshots {

    private static final Log LOG = LogFactory.getLog(PredefinedDatabaseSnapshots.class);

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private ISnapshotRefresherService snapshotRefresherService;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Autowired
    private ILabelDAO labelDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private ICostCategoryDAO costCategoryDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private ICostCalculator hoursCostCalculator;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    private IAutoUpdatedSnapshot<SortedMap<CriterionType, List<Criterion>>> criterionsMap;

    private IAutoUpdatedSnapshot<Map<LabelType, List<Label>>> labelsMap;

    private IAutoUpdatedSnapshot<List<Worker>> listWorkers;

    private IAutoUpdatedSnapshot<List<CostCategory>> listCostCategories;

    private IAutoUpdatedSnapshot<List<Criterion>> listCriterion;

    private IAutoUpdatedSnapshot<Map<Class<?>, List<Resource>>> mapResources;

    private IAutoUpdatedSnapshot<List<ExternalCompany>> externalCompanies;

    private IAutoUpdatedSnapshot<List<String>> customerReferences;

    private IAutoUpdatedSnapshot<List<String>> ordersCodes;

    private IAutoUpdatedSnapshot<ResourceLoadChartData> resourceLoadChartData;

    private IAutoUpdatedSnapshot<List<WorkReportLine>> workReportLines;

    private IAutoUpdatedSnapshot<Map<TaskElement,SortedMap<LocalDate, BigDecimal>>> estimatedCostPerTask;

    private IAutoUpdatedSnapshot<Map<TaskElement,SortedMap<LocalDate, BigDecimal>>> advanceCostPerTask;

    private boolean snapshotsRegistered = false;

    public SortedMap<CriterionType, List<Criterion>> snapshotCriterionsMap() {
        return criterionsMap.getValue();
    }

    public Map<LabelType, List<Label>> snapshotLabelsMap() {
        return labelsMap.getValue();
    }

    public List<Worker> snapshotListWorkers() {
        return listWorkers.getValue();
    }

    public List<CostCategory> snapshotListCostCategories() {
        return listCostCategories.getValue();
    }

    public List<Criterion> snapshotListCriterion() {
        return listCriterion.getValue();
    }

    public Map<Class<?>, List<Resource>> snapshotMapResources() {
        return mapResources.getValue();
    }

    public List<ExternalCompany> snapshotExternalCompanies() {
        return externalCompanies.getValue();
    }

    public List<String> snapshotCustomerReferences() {
        return customerReferences.getValue();
    }

    public List<String> snapshotOrdersCodes() {
        return ordersCodes.getValue();
    }

    public ResourceLoadChartData snapshotResourceLoadChartData() {
        return resourceLoadChartData.getValue();
    }

    public List<WorkReportLine> snapshotWorkReportLines() {
        return workReportLines.getValue();
    }

    public Map<TaskElement,SortedMap<LocalDate, BigDecimal>> snapshotEstimatedCostPerTask() {
        return estimatedCostPerTask.getValue();
    }

    public Map<TaskElement,SortedMap<LocalDate, BigDecimal>> snapshotAdvanceCostPerTask() {
        return advanceCostPerTask.getValue();
    }

    public void registerSnapshots() {
        if ( snapshotsRegistered ) {
            LOG.warn("snapshots have already been registered");
            return;
        }

        snapshotsRegistered = true;
        criterionsMap = snapshot("criterions map", calculateCriterionsMap(), CriterionType.class, Criterion.class);
        labelsMap = snapshot("labels map", calculateLabelsMap(), LabelType.class, Label.class);
        listWorkers = snapshot("workers", calculateWorkers(), Worker.class);
        listCostCategories = snapshot("list cost categories", calculateListCostCategories(), CostCategory.class);
        listCriterion = snapshot("list criterions", calculateListCriterion(), Criterion.class);

        mapResources = snapshot(
                "map resources",
                calculateMapResources(),
                Resource.class,
                Worker.class,
                Machine.class,
                VirtualWorker.class);

        externalCompanies = snapshot("external companies", calculateExternalCompanies(), ExternalCompany.class);
        customerReferences = snapshot("customer references", calculateCustomerReferences(), Order.class);
        ordersCodes = snapshot("order codes", calculateOrdersCodes(), Order.class);

        resourceLoadChartData = snapshot(
                "resource load grouped by date",
                calculateResourceLoadChartData(),
                CalendarAvailability.class,
                CalendarException.class,
                CalendarData.class,
                TaskElement.class,
                SpecificResourceAllocation.class,
                GenericResourceAllocation.class,
                ResourceAllocation.class);

        workReportLines = snapshot("work report lines", calculateWorkReportLines(), WorkReportLine.class);

        estimatedCostPerTask = snapshot(
                "estimated cost per task",
                calculateEstimatedCostPerTask(),
                TaskElement.class,
                Task.class,
                TaskGroup.class,
                DayAssignment.class);

        advanceCostPerTask = snapshot(
                "advance cost per task",
                calculateAdvanceCostPerTask(),
                TaskElement.class,
                Task.class,
                TaskGroup.class,
                DirectAdvanceAssignment.class);
    }

    private <T> IAutoUpdatedSnapshot<T> snapshot(String name, Callable<T> callable, Class<?>... reloadOnChangesOf) {
        return snapshotRefresherService
                .takeSnapshot(name, callableOnReadOnlyTransaction(callable), ReloadOn.onChangeOf(reloadOnChangesOf));
    }

    @SuppressWarnings("unchecked")
    private <T> Callable<T> callableOnReadOnlyTransaction(Callable<T> callable) {
        return AdHocTransactionService.readOnlyProxy(transactionService, Callable.class, callable);
    }

    private Callable<SortedMap<CriterionType, List<Criterion>>> calculateCriterionsMap() {
        return () -> {
            SortedMap<CriterionType, List<Criterion>> result = new TreeMap<>(getComparatorByName());
            for (CriterionType criterionType : criterionTypeDAO.getSortedCriterionTypes()) {
                if ( criterionType.isEnabled() ) {
                    List<Criterion> criterions = criterionType.getSortCriterions();
                    result.put(criterionType, criterions);
                }
            }
            return result;
        };
    }

    private Comparator<CriterionType> getComparatorByName(){
        return (arg0, arg1) -> arg0.getName().compareTo(arg1.getName());
    }

    private Callable<Map<LabelType, List<Label>>> calculateLabelsMap() {
        return () -> {
            Map<LabelType, List<Label>> result = new HashMap<>();
            for (LabelType labelType : labelTypeDAO.getAll()) {
                List<Label> labels = new ArrayList<>(labelDAO.findByType(labelType));
                result.put(labelType, labels);
            }
            return result;
        };
    }



    private Callable<List<Worker>> calculateWorkers() {
        return () -> workerDAO.getAll();
    }



    private Callable<List<CostCategory>> calculateListCostCategories() {
        return () -> costCategoryDAO.findActive();
    }

    private Callable<List<Criterion>> calculateListCriterion() {
        return criterionDAO::getAll;
    }

    private Callable<Map<Class<?>, List<Resource>>> calculateMapResources() {
        return () -> {
            Map<Class<?>, List<Resource>> result = new HashMap<>();
            result.put(Worker.class, Resource.sortByName(new ArrayList<>(resourceDAO.getRealWorkers())));
            result.put(Machine.class, Resource.sortByName(new ArrayList<>(resourceDAO.getMachines())));
            result.put(VirtualWorker.class, Resource.sortByName(new ArrayList<>(resourceDAO.getVirtualWorkers())));

            return result;
        };
    }

    private Callable<List<ExternalCompany>> calculateExternalCompanies() {
        return () -> externalCompanyDAO.getExternalCompaniesAreClient();
    }

    private Callable<List<String>> calculateCustomerReferences() {
        return () -> {
            List<String> result = new ArrayList<>();
            for (Order order : orderDAO.getOrdersWithNotEmptyCustomersReferences()) {
                result.add(order.getCustomerReference());
            }
            return result;
        };
    }

    private Callable<List<String>> calculateOrdersCodes() {
        return () -> {
            List<String> result = new ArrayList<>();
            for (Order order : orderDAO.getOrders()) {
                result.add(order.getCode());
            }
            return result;
        };
    }

    private Callable<ResourceLoadChartData> calculateResourceLoadChartData() {
        return () -> {
            List<DayAssignment> dayAssignments = dayAssignmentDAO.getAllFor(scenarioManager.getCurrent(), null, null);
            List<Resource> resources = resourceDAO.list(Resource.class);
            return new ResourceLoadChartData(dayAssignments, resources);

        };
    }

    private Callable<List<WorkReportLine>> calculateWorkReportLines() {
        return () -> workReportLineDAO.list(WorkReportLine.class);
    }



    private Callable<Map<TaskElement, SortedMap<LocalDate, BigDecimal>>> calculateEstimatedCostPerTask() {
        return () -> {
            Map<TaskElement, SortedMap<LocalDate, BigDecimal>> map = new HashMap<>();

            taskElementDAO.
                    list(TaskElement.class)
                    .stream()
                    .filter(task -> task instanceof Task)
                    .forEach(task -> map.put(task, hoursCostCalculator.getEstimatedCost((Task) task)));

            return map;
        };
    }

    private Callable<Map<TaskElement, SortedMap<LocalDate, BigDecimal>>> calculateAdvanceCostPerTask() {
        return () -> {
            Map<TaskElement, SortedMap<LocalDate, BigDecimal>> map = new HashMap<>();
            for (TaskElement task : taskElementDAO.list(TaskElement.class)) {
                if ( task instanceof Task ) {
                    map.put(task, hoursCostCalculator.getAdvanceCost((Task)task));
                }
            }
            return map;
        };
    }

}
