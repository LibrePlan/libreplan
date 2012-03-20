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

package org.libreplan.web.planner.consolidations;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.entities.IndirectAdvanceAssignment;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation.AllocationsSpecified;
import org.libreplan.business.planner.entities.ResourceAllocation.DetachDayAssignmentOnRemoval;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.consolidations.CalculatedConsolidatedValue;
import org.libreplan.business.planner.entities.consolidations.CalculatedConsolidation;
import org.libreplan.business.planner.entities.consolidations.ConsolidatedValue;
import org.libreplan.business.planner.entities.consolidations.Consolidation;
import org.libreplan.business.planner.entities.consolidations.NonCalculatedConsolidatedValue;
import org.libreplan.business.planner.entities.consolidations.NonCalculatedConsolidation;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * Model for UI operations related to {@link Task}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AdvanceConsolidationModel implements IAdvanceConsolidationModel {

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    private Task task;

    private IContextWithPlannerTask<TaskElement> context;

    private Consolidation consolidation;

    private DirectAdvanceAssignment spreadAdvance;

    private boolean isUnitType = false;

    private OrderElement orderElement;

    private List<AdvanceConsolidationDTO> consolidationDTOs = new ArrayList<AdvanceConsolidationDTO>();

    private void initConsolidatedDates() {
        consolidationDTOs = AdvanceConsolidationDTO
                .sortByDate(getConsolidationDTOs());
        initLastConsolidatedDate();
        initLastConsolidatedAndSavedDate();
    }

    private boolean containsAdvance(AdvanceMeasurement advanceMeasurement) {
        for (AdvanceConsolidationDTO dto : consolidationDTOs) {
            if (dto.getDate().compareTo(
                    advanceMeasurement.getDate().toDateTimeAtStartOfDay()
                            .toDate()) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initLastConsolidatedDate() {
        // init the lastConsolidatedDate
        LocalDate consolidatedUntil = (task.getConsolidation() == null) ? null
                : task.getConsolidation().getConsolidatedUntil();
        AdvanceConsolidationDTO.lastConsolidatedDate = (consolidatedUntil == null) ? null
                : consolidatedUntil.toDateTimeAtStartOfDay().toDate();
    }

    private void initLastConsolidatedAndSavedDate() {
        // init the lastConsolidatedAndSaveDate
        int i = 0;
        AdvanceConsolidationDTO.lastConsolidatedAndSavedDate = null;
        while ((i < consolidationDTOs.size())
                && (!consolidationDTOs.get(i).isSavedConsolidatedValue())) {
            i++;
        }
        if(i < consolidationDTOs.size()){
            AdvanceConsolidationDTO.lastConsolidatedAndSavedDate = consolidationDTOs.get(i).getDate();
        }
    }

    @Override
    public void cancel() {

    }

    @Override
    @Transactional(readOnly = true)
    public void accept() {
        if (context != null && orderElement != null && isVisibleAdvances()) {
            org.zkoss.ganttz.data.Task ganttTask = context.getTask();

            createConsolidationIfNeeded();

            for (AdvanceConsolidationDTO dto : consolidationDTOs) {
                if (dto.isConsolidated()) {
                    addConsolidationIfIsNeeded(dto);
                } else {
                    deleteConsolidationIfIsNeeded(dto);
                }
            }

            updateConsolidationInAdvanceIfIsNeeded();

            ganttTask.enforceDependenciesDueToPositionPotentiallyModified();
            ganttTask.reloadResourcesText();
            context.reloadCharts();
        }
    }

    private void createConsolidationIfNeeded() {
        if (consolidation == null && task != null) {
            if (advanceIsCalculated()) {
                IndirectAdvanceAssignment indirectAdvanceAssignment = getIndirecAdvanceAssignment();
                consolidation = CalculatedConsolidation.create(task,
                        indirectAdvanceAssignment);
            } else {
                consolidation = NonCalculatedConsolidation.create(task,
                        spreadAdvance);
            }
            task.setConsolidation(consolidation);
        }
    }

    private IndirectAdvanceAssignment getIndirecAdvanceAssignment() {
        if (orderElement != null) {
            Set<IndirectAdvanceAssignment> indirects = orderElement
                    .getIndirectAdvanceAssignments();
            for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirects) {
                if (indirectAdvanceAssignment.getReportGlobalAdvance()) {
                    return indirectAdvanceAssignment;
                }
            }
        }
        return null;
    }

    private void addConsolidationIfIsNeeded(AdvanceConsolidationDTO dto) {
        if (dto.getConsolidatedValue() == null) {
            ConsolidatedValue consolidatedValue = createNewConsolidatedValue(dto);
            dto.setConsolidatedValue(consolidatedValue);
        }
        addConsolidatedValue(dto.getConsolidatedValue());
    }

    private void addConsolidatedValue(ConsolidatedValue value) {
        if (consolidation == null || task == null
                || consolidation.getConsolidatedValues().contains(value)) {
            return;
        }
        if (!consolidation.isCalculated()) {
            ((NonCalculatedConsolidation) consolidation)
                    .addConsolidatedValue((NonCalculatedConsolidatedValue) value);
        } else {
            ((CalculatedConsolidation) consolidation)
                    .addConsolidatedValue((CalculatedConsolidatedValue) value);
        }

        task.updateAssignmentsConsolidatedValues();

        Set<ResourceAllocation<?>> allResourceAllocations = task
                .getAllResourceAllocations();
        withDetachOnDayAssignmentRemoval(allResourceAllocations);

        IntraDayDate end = task.getIntraDayEndDate();
        if (value.getDate().compareTo(end.getDate().minusDays(1)) >= 0) {
            reassignExpandingTask(allResourceAllocations);
        } else {
            reassignAll(task.getIntraDayStartDate(), end,
                    allResourceAllocations);
        }

        resetIntendedResourcesPerDayWithNonConsolidated(allResourceAllocations);
    }

    private void resetIntendedResourcesPerDayWithNonConsolidated(
            Set<ResourceAllocation<?>> allResourceAllocations) {
        for (ResourceAllocation<?> resourceAllocation : allResourceAllocations) {
            resourceAllocation.resetIntendedIntendedResourcesPerDayWithNonConsolidated();
        }
    }

    private void withDetachOnDayAssignmentRemoval(
            Collection<? extends ResourceAllocation<?>> allocations) {
        for (ResourceAllocation<?> each : allocations) {
            each.setOnDayAssignmentRemoval(new DetachDayAssignmentOnRemoval());
        }
    }

    private void reassignAll(IntraDayDate start, IntraDayDate end,
            Collection<? extends ResourceAllocation<?>> allocations) {
        for (ResourceAllocation<?> each : allocations) {
            EffortDuration pendingEffort = consolidation
                    .getNotConsolidated(each.getIntendedTotalAssigment());
            reassign(each, start, end, pendingEffort);
        }
    }

    private void reassign(ResourceAllocation<?> resourceAllocation,
            IntraDayDate start, IntraDayDate end, EffortDuration pendingEffort) {
        resourceAllocation.withPreviousAssociatedResources()
                .onInterval(start, end).allocate(pendingEffort);
    }

    private void reassignExpandingTask(
            Collection<? extends ResourceAllocation<?>> allResourceAllocations) {

        List<IntraDayDate> ends = new ArrayList<IntraDayDate>();
        for (ResourceAllocation<?> resourceAllocation : allResourceAllocations) {
            if (!AllocationsSpecified.isZero(resourceAllocation
                    .asResourcesPerDayModification().getGoal().getAmount())) {
                EffortDuration pendingEffort = consolidation
                        .getNotConsolidated(resourceAllocation
                                .getIntendedTotalAssigment());
                IntraDayDate date = ResourceAllocation.allocating(
                        Arrays.asList(resourceAllocation
                                .asResourcesPerDayModification()))
                        .untilAllocating(pendingEffort);
                ends.add(date);
            }
        }
        if (!ends.isEmpty()) {
            task.setIntraDayEndDate(Collections.max(ends));
        }
    }

    private ConsolidatedValue createNewConsolidatedValue(
            AdvanceConsolidationDTO dto) {
        if (consolidation != null && task != null) {

            if (consolidation.isCalculated()) {
                return CalculatedConsolidatedValue.create(
                        LocalDate.fromDateFields(dto.getDate()),
                        dto.getPercentage(), task.getIntraDayEndDate());
            } else {
                AdvanceMeasurement measure = dto.getAdvanceMeasurement();
                NonCalculatedConsolidatedValue consolidatedValue = NonCalculatedConsolidatedValue
                        .create(LocalDate.fromDateFields(dto.getDate()),
                                dto.getPercentage(), measure,
                                task.getIntraDayEndDate());
                measure.getNonCalculatedConsolidatedValues().add(
                        consolidatedValue);
                return consolidatedValue;
            }
        }
        return null;
    }

    private void deleteConsolidationIfIsNeeded(AdvanceConsolidationDTO dto) {
        if (dto.getConsolidatedValue() == null || consolidation == null
                || task == null) {
            return;
        }

        if (!consolidation.getConsolidatedValues().isEmpty()) {
            IntraDayDate endExclusive = consolidation.getConsolidatedValues()
                    .last().getTaskEndDate();
            task.setIntraDayEndDate(endExclusive);
        }
        if (!consolidation.isCalculated()) {
            ((NonCalculatedConsolidation) consolidation)
                    .getNonCalculatedConsolidatedValues().remove(
                            dto.getConsolidatedValue());
            dto.getAdvanceMeasurement().getNonCalculatedConsolidatedValues()
                    .remove(dto.getConsolidatedValue());
        } else {
            ((CalculatedConsolidation) consolidation)
                    .getCalculatedConsolidatedValues().remove(
                            dto.getConsolidatedValue());
        }

        task.updateAssignmentsConsolidatedValues();

        Set<ResourceAllocation<?>> allResourceAllocations = task
                .getAllResourceAllocations();
        withDetachOnDayAssignmentRemoval(allResourceAllocations);

        reassignAll(task.getIntraDayStartDate(), task.getIntraDayEndDate(),
                allResourceAllocations);

        resetIntendedResourcesPerDayWithNonConsolidated(allResourceAllocations);
    }

    private void updateConsolidationInAdvanceIfIsNeeded() {
        if (consolidation != null) {
            if (consolidation.isEmpty()) {
                removeConsolidationInAdvance();
            } else {
                addConsolidationInAdvance();
            }
        }
    }

    private void removeConsolidationInAdvance() {
        if (advanceIsCalculated()) {
            IndirectAdvanceAssignment indirectAdvanceAssignment = getIndirecAdvanceAssignment();
            indirectAdvanceAssignment.getCalculatedConsolidation().remove(
                    (CalculatedConsolidation) consolidation);
            ((CalculatedConsolidation) consolidation)
                    .setIndirectAdvanceAssignment(null);
        } else {
            spreadAdvance.getNonCalculatedConsolidation().remove(
                    (NonCalculatedConsolidation) consolidation);
            ((NonCalculatedConsolidation) consolidation)
                    .setDirectAdvanceAssignment(null);
        }
    }

    private void addConsolidationInAdvance() {
        if (advanceIsCalculated()) {
            IndirectAdvanceAssignment indirectAdvanceAssignment = getIndirecAdvanceAssignment();
            if (!indirectAdvanceAssignment.getCalculatedConsolidation()
                    .contains(consolidation)) {
                indirectAdvanceAssignment.getCalculatedConsolidation().add(
                    (CalculatedConsolidation) consolidation);
                ((CalculatedConsolidation) consolidation)
                        .setIndirectAdvanceAssignment(indirectAdvanceAssignment);
            }
        } else {
            if (!spreadAdvance.getNonCalculatedConsolidation().contains(
                    consolidation)) {
                spreadAdvance.getNonCalculatedConsolidation().add(
                    (NonCalculatedConsolidation) consolidation);
                ((NonCalculatedConsolidation) consolidation)
                        .setDirectAdvanceAssignment(spreadAdvance);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void initAdvancesFor(Task task,
            IContextWithPlannerTask<TaskElement> context,
            PlanningState planningState) {
        this.context = context;
        initTask(task);
        initOrderElement();
        initConsolidation();
        initAdvanceConsolidationsDTOs(task);
    }

    private void initTask(Task task) {
        this.task = task;
        taskElementDAO.reattach(this.task);

        orderElement = task.getOrderElement();
        orderElementDAO.reattach(orderElement);
    }

    private void initOrderElement() {
        spreadAdvance = orderElement.getReportGlobalAdvanceAssignment();
        initSpreadAdvance();
    }

    private void initConsolidation() {
        consolidation = task.getConsolidation();
        if (consolidation != null) {
            consolidation.getConsolidatedValues().size();
        }
    }

    private void initAdvanceConsolidationsDTOs(Task task) {
        if (spreadAdvance != null) {
            isUnitType = (!spreadAdvance.getAdvanceType().getPercentage());
            createAdvanceConsolidationDTOs();
            initConsolidatedDates();
            addNonConsolidatedAdvances();
            setReadOnlyConsolidations();
        }
    }

    private void initSpreadAdvance() {
        if (spreadAdvance != null) {
            if (advanceIsCalculated()) {
                IndirectAdvanceAssignment indirectAdvanceAssignment = getIndirecAdvanceAssignment();
                indirectAdvanceAssignment.getCalculatedConsolidation().size();
            } else {
                spreadAdvance.getNonCalculatedConsolidation().size();
                initAdvanceMeasurements(spreadAdvance.getAdvanceMeasurements());
            }
        }
    }

    private void initAdvanceMeasurements(Set<AdvanceMeasurement> measures) {
        for (AdvanceMeasurement measure : measures) {
            measure.getNonCalculatedConsolidatedValues().size();
        }
    }

    private void createAdvanceConsolidationDTOs() {
        consolidationDTOs = new ArrayList<AdvanceConsolidationDTO>();
        if (consolidation != null) {
            if (!consolidation.isCalculated()) {
                for (NonCalculatedConsolidatedValue consolidatedValue : ((NonCalculatedConsolidation) consolidation)
                        .getNonCalculatedConsolidatedValues()) {
                    consolidationDTOs.add(new AdvanceConsolidationDTO(
                            consolidatedValue.getAdvanceMeasurement(),
                            consolidatedValue));
                }
            }else{
                for (CalculatedConsolidatedValue consolidatedValue : ((CalculatedConsolidation) consolidation)
                        .getCalculatedConsolidatedValues()) {
                    consolidationDTOs.add(new AdvanceConsolidationDTO(null,
                            consolidatedValue));
                }
            }
        }
    }

    private void addNonConsolidatedAdvances() {
        for (AdvanceMeasurement advance : getAdvances()) {
            if (canBeConsolidateAndShow(advance)) {
                consolidationDTOs.add(new AdvanceConsolidationDTO(advance));
            }
        }
        consolidationDTOs = AdvanceConsolidationDTO
                .sortByDate(consolidationDTOs);
    }

    private boolean canBeConsolidateAndShow(
            AdvanceMeasurement advanceMeasurement) {
        Date date = advanceMeasurement.getDate().toDateTimeAtStartOfDay().toDate();
        return ((AdvanceConsolidationDTO.canBeConsolidateAndShow(date)) && (!containsAdvance(advanceMeasurement)));
    }

    @Override
    public String getInfoAdvanceAssignment() {
        if (this.spreadAdvance == null || this.orderElement == null) {
            return "";
        }
        return getInfoAdvanceAssignment(this.spreadAdvance);
    }

    private String getInfoAdvanceAssignment(DirectAdvanceAssignment assignment) {
        if (assignment == null) {
            return "";
        }
        if (assignment.getMaxValue() == null) {
            return "";
        }
        return _("( max: {0} )", assignment.getMaxValue());
    }

    private List<AdvanceMeasurement> getAdvances() {
        if (spreadAdvance != null) {
            return new ArrayList<AdvanceMeasurement>(spreadAdvance
                    .getAdvanceMeasurements());
        }
        return new ArrayList<AdvanceMeasurement>();
    }

    @Override
    public boolean isVisibleAdvances() {
        return (!isVisibleMessages());
    }

    @Override
    public boolean isVisibleMessages() {
        return ((getAdvances().size() == 0) || (isSubcontrated()) || (!hasResourceAllocation()));
    }

    private boolean advanceIsCalculated(){
        return ((spreadAdvance != null) && (spreadAdvance.isFake()));
    }

    public String infoMessages() {
        if (getAdvances().size() > 0) {
            return _("It is not allowed to consolidate progress.");
        }
        return _("There are not any assigned progress to current task");
    }

    public void setConsolidationDTOs(
            List<AdvanceConsolidationDTO> consolidationDTOs) {
        this.consolidationDTOs = consolidationDTOs;
    }

    public List<AdvanceConsolidationDTO> getConsolidationDTOs() {
        if (spreadAdvance != null && orderElement != null) {
            return consolidationDTOs;
        }
        return new ArrayList<AdvanceConsolidationDTO>();
    }

    private boolean hasResourceAllocation() {
        return ((task != null) && (task.hasResourceAllocations()));
    }

    private boolean isSubcontrated() {
        return ((task != null) && (task.isSubcontracted()));
    }

    public boolean hasLimitingResourceAllocation() {
        return ((task != null) && (task.hasLimitedResourceAllocation()));
    }

    @Override
    public void setReadOnlyConsolidations() {
        // set all advance consolidations as read only
        AdvanceConsolidationDTO.setAllReadOnly(hasLimitingResourceAllocation());
    }

    public boolean isUnitType() {
        return this.isUnitType;
    }

}
