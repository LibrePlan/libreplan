/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.web.planner.allocation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.planner.entities.CalculatedValue;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.web.common.EffortDurationBox;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.components.NewAllocationSelectorCombo;
import org.libreplan.web.common.components.ResourceAllocationBehaviour;
import org.libreplan.web.planner.taskedition.TaskPropertiesController;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tab;

public class FakeFormBinder extends FormBinder {

    public FakeFormBinder(AllocationRowsHandler allocationRowsHandler) {
        super(allocationRowsHandler);
    }

    public void setAssignedEffortComponent(
            EffortDurationBox assignedEffortComponent) {
    }

    public AllocationResult getLastAllocation() {
        return null;
    }

    public void setCalculatedValue(CalculatedValue newCalculatedValue) {
    }

    public CalculatedValue getCalculatedValue() {
        return CalculatedValue.RESOURCES_PER_DAY;
    }

    public void setWorkableDays(Intbox duration,
            final TaskPropertiesController taskPropertiesController,
            final Label labelTaskStart, final Label labelTaskEnd) {
    }

    public LocalDate getAllocationEnd() {
        return getTask().getEndAsLocalDate();
    }

    public LocalDate getAllocationStart() {
        return getTask().getStartAsLocalDate();
    }

    public void setAllResourcesPerDay(Decimalbox allResourcesPerDay) {
    }

    public List<AllocationRow> getCurrentRows() {
        return Collections.<AllocationRow> emptyList();
    }

    public void doApply() {
    }

    public boolean accept() {
        return true;
    }

    public void setApplyButton(Button applyButton) {
    }

    public EffortDuration getAssignedEffort() {
        // this method used to return the effort written in the allocation
        // screen
        // we simulate it returning all the hours of the task
        return EffortDuration.hours(getTask().getWorkHours());
    }

    public Integer getWorkableDays() {
        return getTask().getWorkableDays();
    }

    public void setDeleteButtonFor(AllocationRow row, Button deleteButton) {
    }

    public void newAllocationAdded() {
    }

    public void markAssignedHoursMustBePositive() {
    }

    public void markRepeatedResources(List<Resource> resources) {
    }

    private List<String> getResourcesDescriptions(List<Resource> resources) {
        return Collections.<String> emptyList();
    }

    public void markNoResourcesMatchedByCriterions(ResourceEnum resourceType,
            Collection<? extends Criterion> criterions) {
    }

    public void markThereisAlreadyAssignmentWith(ResourceEnum resourceType,
            Collection<? extends Criterion> criterions) {
    }

    public void markNoEmptyResourcesPerDay(List<AllocationRow> rows) {
    }

    public void setAllocationsGrid(Grid allocationsGrid) {
    }

    public void setMessagesForUser(IMessagesForUser messages) {
    }

    public void detach() {
    }

    public void setRecommendedAllocation(Button recommendedAllocation) {
    }

    public EventListener getRecommendedAllocationListener() {
        return new EventListener() {
            @Override
            public void onEvent(Event event) {
            }
        };
    }

    public void rowRemoved() {
    }

    public void setWorkerSearchTab(Tab workerSearchTab) {
    }

    public void setAdvancedSearchButton(Button advancedSearchButton) {
    }

    public void setNewAllocationSelectorCombo(
            NewAllocationSelectorCombo newAllocationSelectorCombo) {
    }

    public void setAllOriginalEffort(Label allOriginalEffort) {
    }

    public Label getAllOriginalEffort() {
        return null;
    }

    public void setAllTotalEffort(Label allTotalHours) {
    }

    public Label getAllTotalEffort() {
        return null;
    }

    public void setAllConsolidatedEffort(Label allConsolidatedEffort) {
    }

    public Label getAllConsolidatedEffort() {
        return null;
    }

    public void setAllTotalResourcesPerDay(Label allTotalResourcesPerDay) {
    }

    public Label getAllTotalResourcesPerDay() {
        return null;
    }

    public void setAllConsolidatedResourcesPerDay(
            Label allConsolidatedResourcesPerDay) {
    }

    public Label getAllConsolidatedResourcesPerDay() {
        return null;
    }

    public void loadAggregatedCalculations() {
    }

    public boolean allowMultipleSelection() {
        return ResourceAllocationBehaviour.NON_LIMITING
                .allowMultipleSelection();
    }

    public void cannotAllocateMoreThanOneResource(List<Resource> resources) {
    }

    public void setBehaviour(ResourceAllocationBehaviour behaviour) {
    }

    public boolean isAnyNotFlat() {
        return false;
    }

    public boolean isAnyManual() {
        return false;
    }

}
