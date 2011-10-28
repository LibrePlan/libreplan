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
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class PredefinedDatabaseSnapshots {

    private static final Log LOG = LogFactory
            .getLog(PredefinedDatabaseSnapshots.class);

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private ISnapshotRefresherService snapshotRefresherService;

    private IAutoUpdatedSnapshot<SortedMap<CriterionType, List<Criterion>>> criterionsMap;

    public SortedMap<CriterionType, List<Criterion>> snapshotCriterionsMap() {
        return criterionsMap.getValue();
    }

    private IAutoUpdatedSnapshot<Map<LabelType, List<Label>>> labelsMap;

    public Map<LabelType, List<Label>> snapshotLabelsMap() {
        return labelsMap.getValue();
    }

    private IAutoUpdatedSnapshot<List<Worker>> listWorkers;

    public List<Worker> snapshotListWorkers() {
        return listWorkers.getValue();
    }

    private IAutoUpdatedSnapshot<List<CostCategory>> listCostCategories;

    public List<CostCategory> snapshotListCostCategories() {
        return listCostCategories.getValue();
    }

    private IAutoUpdatedSnapshot<List<Criterion>> listCriterion;

    public List<Criterion> snapshotListCriterion() {
        return listCriterion.getValue();
    }

    private IAutoUpdatedSnapshot<Map<Class<?>, List<Resource>>> mapResources;

    public Map<Class<?>, List<Resource>> snapshotMapResources() {
        return mapResources.getValue();
    }

    private IAutoUpdatedSnapshot<List<ExternalCompany>> externalCompanies;

    public List<ExternalCompany> snapshotExternalCompanies() {
        return externalCompanies.getValue();
    }

    private IAutoUpdatedSnapshot<List<String>> customerReferences;

    public List<String> snapshotCustomerReferences() {
        return customerReferences.getValue();
    }

    private IAutoUpdatedSnapshot<List<String>> ordersCodes;

    public List<String> snapshotOrdersCodes() {
        return ordersCodes.getValue();
    }

    private IAutoUpdatedSnapshot<ResourceLoadChartData>
        resourceLoadChartData;

    public ResourceLoadChartData snapshotResourceLoadChartData() {
        return resourceLoadChartData.getValue();
    }

    private IAutoUpdatedSnapshot<List<WorkReportLine>> workReportLines;

    public List<WorkReportLine> snapshotWorkReportLines() {
        return workReportLines.getValue();
    }

    private IAutoUpdatedSnapshot<Map<TaskElement,SortedMap<LocalDate, BigDecimal>>>
            estimatedCostPerTask;

    public Map<TaskElement,SortedMap<LocalDate, BigDecimal>> snapshotEstimatedCostPerTask() {
        return estimatedCostPerTask.getValue();
    }

    private IAutoUpdatedSnapshot<Map<TaskElement,SortedMap<LocalDate, BigDecimal>>>
            advanceCostPerTask;

    public Map<TaskElement,SortedMap<LocalDate, BigDecimal>> snapshotAdvanceCostPerTask() {
        return advanceCostPerTask.getValue();
    }

    private boolean snapshotsRegistered = false;

    public void registerSnapshots() {
        if (snapshotsRegistered) {
            LOG.warn("snapshots have already been registered");
            return;
        }
        snapshotsRegistered = true;
        criterionsMap = snapshot("criterions map", calculateCriterionsMap(),
                CriterionType.class, Criterion.class);
        labelsMap = snapshot("labels map", calculateLabelsMap(),
                LabelType.class, Label.class);
        listWorkers = snapshot("workers", calculateWorkers(), Worker.class);
        listCostCategories = snapshot("list cost categories",
                calculateListCostCategories(),
                CostCategory.class);
        listCriterion = snapshot("list criterions", calculateListCriterion(),
                Criterion.class);
        mapResources = snapshot("map resources", calculateMapResources(),
                Resource.class, Worker.class, Machine.class,
                VirtualWorker.class);
        externalCompanies = snapshot("external companies",
                calculateExternalCompanies(),
                ExternalCompany.class);
        customerReferences = snapshot("customer references",
                calculateCustomerReferences(), Order.class);
        ordersCodes = snapshot("order codes", calculateOrdersCodes(),
                Order.class);
        resourceLoadChartData = snapshot("resource load grouped by date",
                calculateResourceLoadChartData(),
                CalendarAvailability.class, CalendarException.class,
                CalendarData.class, TaskElement.class, SpecificResourceAllocation.class,
                GenericResourceAllocation.class, ResourceAllocation.class);
        workReportLines = snapshot("work report lines", calculateWorkReportLines(),
                WorkReportLine.class);
        estimatedCostPerTask = snapshot("estimated cost per task",
                calculateEstimatedCostPerTask(),
                TaskElement.class, Task.class, TaskGroup.class, DayAssignment.class);
        advanceCostPerTask = snapshot("advance cost per task",
                calculateAdvanceCostPerTask(),
                TaskElement.class, Task.class, TaskGroup.class,
                DirectAdvanceAssignment.class);
    }

    private <T> IAutoUpdatedSnapshot<T> snapshot(String name,
            Callable<T> callable,
            Class<?>... reloadOnChangesOf) {
        return snapshotRefresherService.takeSnapshot(name,
                callableOnReadOnlyTransaction(callable),
                ReloadOn.onChangeOf(reloadOnChangesOf));
    }

    @SuppressWarnings("unchecked")
    private <T> Callable<T> callableOnReadOnlyTransaction(Callable<T> callable) {
        return AdHocTransactionService.readOnlyProxy(transactionService,
                Callable.class, callable);
    }

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    private Callable<SortedMap<CriterionType, List<Criterion>>> calculateCriterionsMap() {
        return new Callable<SortedMap<CriterionType, List<Criterion>>>() {
            @Override
            public SortedMap<CriterionType, List<Criterion>> call() {
                SortedMap<CriterionType, List<Criterion>> result = new TreeMap<CriterionType, List<Criterion>>(
                        getComparatorByName());
                for (CriterionType criterionType : criterionTypeDAO
                        .getSortedCriterionTypes()) {
                    if (criterionType.isEnabled()) {
                        List<Criterion> criterions = criterionType
                                .getSortCriterions();
                        result.put(criterionType, criterions);
                    }
                }
                return result;
            }
        };
    }

    private Comparator<CriterionType> getComparatorByName(){
        return new Comparator<CriterionType>() {
            @Override
            public int compare(CriterionType arg0, CriterionType arg1) {
                return (arg0.getName().compareTo(arg1.getName()));
            }
        };
    }

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Autowired
    private ILabelDAO labelDAO;

    private Callable<Map<LabelType, List<Label>>> calculateLabelsMap() {
        return new Callable<Map<LabelType,List<Label>>>() {
            @Override
            public Map<LabelType, List<Label>> call() {
                Map<LabelType, List<Label>> result = new HashMap<LabelType, List<Label>>();
                for (LabelType labelType : labelTypeDAO.getAll()) {
                    List<Label> labels = new ArrayList<Label>(
                            labelDAO.findByType(labelType));
                    result.put(labelType, labels);
                }
                return result;
            }
        };
    }

    @Autowired
    private IWorkerDAO workerDAO;

    private Callable<List<Worker>> calculateWorkers() {
        return new Callable<List<Worker>>() {

            @Override
            public List<Worker> call() {
                return workerDAO.getAll();
            }
        };
    }

    @Autowired
    private ICostCategoryDAO costCategoryDAO;

    private Callable<List<CostCategory>> calculateListCostCategories() {
        return new Callable<List<CostCategory>>() {
            @Override
            public List<CostCategory> call() {
                return costCategoryDAO.findActive();
            }
        };
    }

    private Callable<List<Criterion>> calculateListCriterion() {
        return new Callable<List<Criterion>>() {
            @Override
            public List<Criterion> call() {
                return criterionDAO.getAll();
            }
        };
    }

    @Autowired
    private IResourceDAO resourceDAO;

    private Callable<Map<Class<?>, List<Resource>>> calculateMapResources() {
        return new Callable<Map<Class<?>, List<Resource>>>() {

            @Override
            public Map<Class<?>, List<Resource>> call() {
                Map<Class<?>, List<Resource>> result = new HashMap<Class<?>, List<Resource>>();
                result.put(Worker.class, Resource
                        .sortByName(new ArrayList<Resource>(resourceDAO
                                .getRealWorkers())));
                result.put(Machine.class, Resource
                        .sortByName(new ArrayList<Resource>(resourceDAO
                                .getMachines())));
                result.put(VirtualWorker.class, Resource
                        .sortByName(new ArrayList<Resource>(resourceDAO
                                .getVirtualWorkers())));
                return result;
            }
        };
    }


    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    private Callable<List<ExternalCompany>> calculateExternalCompanies() {
        return new Callable<List<ExternalCompany>>() {
            @Override
            public List<ExternalCompany> call() {
                return externalCompanyDAO.getExternalCompaniesAreClient();
            }
        };
    }

    @Autowired
    private IOrderDAO orderDAO;

    private Callable<List<String>> calculateCustomerReferences() {
        return new Callable<List<String>>() {
            @Override
            public List<String> call() {
                // FIXME replace by a HQL query, for god's sake!
                List<String> result = new ArrayList<String>();
                for (Order order : orderDAO.getOrders()) {
                    if ((order.getCustomerReference() != null)
                            && (!order.getCustomerReference().isEmpty())) {
                        result.add(order.getCustomerReference());
                    }
                }
                return result;
            }
        };
    }

    private Callable<List<String>> calculateOrdersCodes() {
        return new Callable<List<String>>() {
            @Override
            public List<String> call() {
                List<String> result = new ArrayList<String>();
                for (Order order : orderDAO.getOrders()) {
                    result.add(order.getCode());
                }
                return result;
            }
        };
    }

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    private Callable<ResourceLoadChartData> calculateResourceLoadChartData() {
        return new Callable<ResourceLoadChartData>() {
            @Override
            public ResourceLoadChartData call() {

                List<DayAssignment> dayAssignments = dayAssignmentDAO.getAllFor(
                        scenarioManager.getCurrent(), null, null);
                List<Resource> resources = resourceDAO.list(Resource.class);
                return new ResourceLoadChartData(dayAssignments, resources);

            }
        };
    }

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    private Callable<List<WorkReportLine>> calculateWorkReportLines() {
        return new Callable<List<WorkReportLine>>() {
            @Override
            public List<WorkReportLine> call() {
                return workReportLineDAO.list(WorkReportLine.class);
            }
        };
    }

    @Autowired
    private ICostCalculator hoursCostCalculator;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    private Callable<Map<TaskElement, SortedMap<LocalDate, BigDecimal>>> calculateEstimatedCostPerTask() {
        return new Callable<Map<TaskElement, SortedMap<LocalDate, BigDecimal>>>() {
            @Override
            public Map<TaskElement, SortedMap<LocalDate, BigDecimal>> call() {
                Map<TaskElement, SortedMap<LocalDate, BigDecimal>> map =
                    new HashMap<TaskElement, SortedMap<LocalDate,BigDecimal>>();
                for(TaskElement task : taskElementDAO.list(TaskElement.class)) {
                    if(task instanceof Task) {
                        map.put(task, hoursCostCalculator.getEstimatedCost((Task)task));
                    }
                }
                return map;
            }
        };
    }

    private Callable<Map<TaskElement, SortedMap<LocalDate, BigDecimal>>> calculateAdvanceCostPerTask() {
        return new Callable<Map<TaskElement, SortedMap<LocalDate, BigDecimal>>>() {
            @Override
            public Map<TaskElement, SortedMap<LocalDate, BigDecimal>> call() {
                Map<TaskElement, SortedMap<LocalDate, BigDecimal>> map =
                    new HashMap<TaskElement, SortedMap<LocalDate,BigDecimal>>();
                for(TaskElement task : taskElementDAO.list(TaskElement.class)) {
                    if(task instanceof Task) {
                        map.put(task, hoursCostCalculator.getAdvanceCost((Task)task));
                    }
                }
                return map;
            }
        };
    }

}
