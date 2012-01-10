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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.joda.time.LocalDate;
import org.libreplan.business.labels.daos.ILabelDAO;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.reports.dtos.WorkingProgressPerTaskDTO;
import org.libreplan.business.resources.daos.ICriterionTypeDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.scenarios.IScenarioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *@author Diego Pino Garcia <dpino@igalia.com>
 *@author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WorkingProgressPerTaskModel implements IWorkingProgressPerTaskModel {

    @Autowired
    IOrderDAO orderDAO;

    @Autowired
    ITaskElementDAO taskDAO;

    @Autowired
    private ILabelDAO labelDAO;

    @Autowired
    private ICommonQueries commonQueries;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    private List<Label> selectedLabels = new ArrayList<Label>();

    private List<Criterion> selectedCriterions = new ArrayList<Criterion>();

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
    public List<Order> getOrders() {
        List<Order> result = orderDAO.getOrdersByScenario(scenarioManager
                .getCurrent());
        Collections.sort(result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public JRDataSource getWorkingProgressPerTaskReport(Order order,
            Date referenceDate, List<Label> labels, List<Criterion> criterions) {

        orderDAO.reattachUnmodifiedEntity(order);
        order.useSchedulingDataFor(scenarioManager.getCurrent());
        LocalDate referenceLocalDate = new LocalDate(referenceDate);

        final List<WorkingProgressPerTaskDTO> workingHoursPerWorkerList =
            new ArrayList<WorkingProgressPerTaskDTO>();

        reattachLabels();
        final List<Task> tasks = commonQueries.filteredTaskElements(order,
                labels, criterions);
        final List<Task> sortTasks = sortTasks(order, tasks);
        for (Task task : sortTasks) {
            workingHoursPerWorkerList.add(new WorkingProgressPerTaskDTO(task,
                    referenceLocalDate));
        }
        if (!workingHoursPerWorkerList.isEmpty()) {
            return new JRBeanCollectionDataSource(workingHoursPerWorkerList);
        } else {
            return new JREmptyDataSource();
        }
    }

    private List<Task> sortTasks(Order order, List<Task> tasks) {
        List<Task> sortTasks = new ArrayList<Task>();
        final List<OrderElement> orderElements = order.getAllChildren();
        for (OrderElement orderElement : orderElements) {
            Task task = findOrderElementInTasks(orderElement, tasks);
            if (task != null) {
                sortTasks.add(task);
            }
        }
        return sortTasks;
    }

    private Task findOrderElementInTasks(OrderElement orderElement,
            List<Task> tasks) {
        for (Task task : tasks) {
            if (task != null
                    && task.getOrderElement().getId().equals(
                            orderElement.getId())) {
                return task;
            }
        }
        return null;
    }
    @Override
    @Transactional(readOnly = true)
    public void init() {
        selectedCriterions.clear();
        selectedLabels.clear();

        allLabels.clear();
        allCriterions.clear();
        loadAllLabels();
        loadAllCriterions();
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

    private static Set<Criterion> getDirectCriterions(
            CriterionType criterionType) {
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
        return criterionTypeDAO
                .getCriterionTypesByResources(applicableResources);
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
            Iterator<Label> iterator = this.selectedLabels.iterator();
            this.selectedLabel = null;
            if (iterator.hasNext()) {
                this.selectedLabel = new String();
                this.selectedLabel = this.selectedLabel.concat(iterator
                        .next().getName());
            }
            while (iterator.hasNext()) {
                this.selectedLabel = this.selectedLabel.concat(", "
                        + iterator.next().getName());
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
                this.selectedCriteria = this.selectedCriteria.concat(iterator
                        .next().getName());
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
