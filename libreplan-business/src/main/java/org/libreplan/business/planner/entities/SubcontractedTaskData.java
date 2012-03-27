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

package org.libreplan.business.planner.entities;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.externalcompanies.entities.DeliverDateComparator;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.util.deepcopy.OnCopy;
import org.libreplan.business.util.deepcopy.Strategy;

/**
 * Gathers all the information related with a subcontracted {@link Task}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class SubcontractedTaskData extends BaseEntity {

    public static SubcontractedTaskData create(Task task) {
        SubcontractedTaskData subcontractedTaskData = new SubcontractedTaskData(task);
        subcontractedTaskData.subcontratationDate = new Date();
        return create(subcontractedTaskData);
    }

    public static SubcontractedTaskData createFrom(
            SubcontractedTaskData subcontractedTaskData) {
        if (subcontractedTaskData == null) {
            return null;
        }

        SubcontractedTaskData result = new SubcontractedTaskData();
        result.task = subcontractedTaskData.getTask();
        result.externalCompany = subcontractedTaskData.externalCompany;
        result.subcontratationDate = subcontractedTaskData.subcontratationDate;
        result.subcontractCommunicationDate = subcontractedTaskData.subcontractCommunicationDate;
        result.workDescription = subcontractedTaskData.workDescription;
        result.subcontractPrice = subcontractedTaskData.subcontractPrice;
        result.subcontractedCode = subcontractedTaskData.subcontractedCode;
        result.nodeWithoutChildrenExported = subcontractedTaskData.nodeWithoutChildrenExported;
        result.labelsExported = subcontractedTaskData.labelsExported;
        result.materialAssignmentsExported = subcontractedTaskData.materialAssignmentsExported;
        result.hoursGroupsExported = subcontractedTaskData.hoursGroupsExported;
        result.setState(subcontractedTaskData.getState());
        result.setRequiredDeliveringDates(subcontractedTaskData.getRequiredDeliveringDates());

        return create(result);
    }

    private Task task;

    @OnCopy(Strategy.SHARE)
    private ExternalCompany externalCompany;

    private Date subcontratationDate;

    private Date subcontractCommunicationDate;

    private String workDescription;

    private BigDecimal subcontractPrice;

    private String subcontractedCode;

    private Boolean nodeWithoutChildrenExported;
    private Boolean labelsExported;
    private Boolean materialAssignmentsExported;
    private Boolean hoursGroupsExported;

    private SubcontractState state = SubcontractState.PENDING_INITIAL_SEND;

    private final SortedSet<SubcontractorDeliverDate> requiredDeliveringDates = new TreeSet<SubcontractorDeliverDate>(
            new DeliverDateComparator());

    private Set<SubcontractorCommunication> subcontractorCommunications = new HashSet<SubcontractorCommunication>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public SubcontractedTaskData() {
    }

    private SubcontractedTaskData(Task task) {
        this.task = task;
    }

    @NotNull(message = "task not specified")
    public Task getTask() {
        return task;
    }

    @NotNull(message = "external company not specified")
    public ExternalCompany getExternalCompany() {
        return externalCompany;
    }

    public void setExternalCompany(ExternalCompany externalCompany) {
        this.externalCompany = externalCompany;
    }

    public Date getSubcontractCommunicationDate() {
        return subcontractCommunicationDate;
    }

    public void setSubcontractCommunicationDate(
            Date subcontractCommunicationDate) {
        this.subcontractCommunicationDate = subcontractCommunicationDate;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public BigDecimal getSubcontractPrice() {
        return subcontractPrice;
    }

    public void setSubcontractPrice(BigDecimal subcontractPrice) {
        this.subcontractPrice = subcontractPrice;
    }

    public String getSubcontractedCode() {
        return subcontractedCode;
    }

    public void setSubcontractedCode(String subcontractedCode) {
        this.subcontractedCode = subcontractedCode;
    }

    public boolean isNodeWithoutChildrenExported() {
        if (nodeWithoutChildrenExported == null) {
            return false;
        }
        return nodeWithoutChildrenExported;
    }

    public void setNodeWithoutChildrenExported(
            Boolean nodeWithoutChildrenExported) {
        if (nodeWithoutChildrenExported == null) {
            nodeWithoutChildrenExported = false;
        }
        this.nodeWithoutChildrenExported = nodeWithoutChildrenExported;
    }

    public boolean isLabelsExported() {
        if (labelsExported == null) {
            return false;
        }
        return labelsExported;
    }

    public void setLabelsExported(Boolean labelsExported) {
        if (labelsExported == null) {
            labelsExported = false;
        }
        this.labelsExported = labelsExported;
    }

    public Boolean isMaterialAssignmentsExported() {
        if (materialAssignmentsExported == null) {
            return false;
        }
        return materialAssignmentsExported;
    }

    public void setMaterialAssignmentsExported(
            Boolean materialAssignmentsExported) {
        if (materialAssignmentsExported == null) {
            materialAssignmentsExported = false;
        }
        this.materialAssignmentsExported = materialAssignmentsExported;
    }

    public Boolean isHoursGroupsExported() {
        if (hoursGroupsExported == null) {
            return false;
        }
        return hoursGroupsExported;
    }

    public void setHoursGroupsExported(Boolean hoursGroupsExported) {
        if (hoursGroupsExported == null) {
            hoursGroupsExported = false;
        }
        this.hoursGroupsExported = hoursGroupsExported;
    }

    @NotNull(message = "subcontratation date not specified")
    public Date getSubcontratationDate() {
        return subcontratationDate;
    }

    public void applyChanges(SubcontractedTaskData subcontratedTask) {
        this.externalCompany = subcontratedTask.externalCompany;
        this.subcontratationDate = subcontratedTask.subcontratationDate;
        this.subcontractCommunicationDate = subcontratedTask.subcontractCommunicationDate;
        this.workDescription = subcontratedTask.workDescription;
        this.subcontractPrice = subcontratedTask.subcontractPrice;
        this.subcontractedCode = subcontratedTask.subcontractedCode;
        this.nodeWithoutChildrenExported = subcontratedTask.nodeWithoutChildrenExported;
        this.labelsExported = subcontratedTask.labelsExported;
        this.materialAssignmentsExported = subcontratedTask.materialAssignmentsExported;
        this.hoursGroupsExported = subcontratedTask.hoursGroupsExported;
        this.state = subcontratedTask.getState();
        this.setRequiredDeliveringDates(subcontratedTask.getRequiredDeliveringDates());
    }

    @AssertTrue(message = "external company should be subcontractor")
    public boolean checkConstraintExternalCompanyIsSubcontractor() {
        if (!firstLevelValidationsPassed()) {
            return true;
        }

        return externalCompany.isSubcontractor();
    }

    private boolean firstLevelValidationsPassed() {
        return (externalCompany != null) && (subcontratationDate != null);
    }

    public void setState(SubcontractState state) {
        this.state = state;
    }

    @NotNull(message = "state not specified")
    public SubcontractState getState() {
        return state;
    }

    public boolean isSendable() {
        return state.isSendable()
                && externalCompany.getInteractsWithApplications();
    }

    public void setRequiredDeliveringDates(
            SortedSet<SubcontractorDeliverDate> requiredDeliveringDates) {
        this.requiredDeliveringDates.clear();
        this.requiredDeliveringDates.addAll(requiredDeliveringDates);
    }

    public SortedSet<SubcontractorDeliverDate> getRequiredDeliveringDates() {
        return Collections.unmodifiableSortedSet(this.requiredDeliveringDates);
    }

    public void addRequiredDeliveringDates(
            SubcontractorDeliverDate subDeliverDate) {
        this.requiredDeliveringDates.add(subDeliverDate);
    }

    public void removeRequiredDeliveringDates(
            SubcontractorDeliverDate subcontractorDeliverDate) {
        this.requiredDeliveringDates.remove(subcontractorDeliverDate);
    }

    public void updateFirstRequiredDeliverDate(Date subcontractCommunicationDate){
        if(this.requiredDeliveringDates != null && !this.requiredDeliveringDates.isEmpty()){
            this.requiredDeliveringDates.first().setCommunicationDate(subcontractCommunicationDate);
        }
    }
}
