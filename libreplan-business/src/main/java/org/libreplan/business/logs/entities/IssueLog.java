/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.business.logs.entities;
import  javax.validation.constraints.NotNull;
import java.util.Date;

import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.users.entities.User;

/**
 * IssueLog entity, represents parameters to be able to administrate issues that come up in the project.
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
public class IssueLog extends ProjectLog {

    private IssueTypeEnum type = IssueTypeEnum.getDefault();

    private String status = "LOW";

    private LowMediumHighEnum priority = LowMediumHighEnum.getDefault();

    private LowMediumHighEnum severity = LowMediumHighEnum.getDefault();

    private Date dateRaised;

    private User createdBy;

    private String assignedTo;

    private Date dateResolved;

    private Date deadline;

    private String notes;


    public static IssueLog create() {
        return create(new IssueLog(new Date()));
    }

    /**
     * Constructor for Hibernate. Do not use!
     */
    protected IssueLog() {

    }

    private IssueLog(Date date) {
        this.dateRaised = date;
    }

    public IssueTypeEnum getType() {
        return type;
    }

    public void setType(IssueTypeEnum type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LowMediumHighEnum getPriority() {
        return priority;
    }

    public void setPriority(LowMediumHighEnum priority) {
        this.priority = priority;
    }

    public LowMediumHighEnum getSeverity() {
        return severity;
    }

    public void setSeverity(LowMediumHighEnum severity) {
        this.severity = severity;
    }

    @NotNull(message = "date raised is not specified")
    public Date getDateRaised() {
        return dateRaised;
    }

    public void setDateRaised(Date dateEntered) {
        this.dateRaised = dateEntered;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User user) {
        this.createdBy = user;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public  Date getDateResolved() {
        return dateResolved;
    }

    public void setDateResolved(Date dateResolved) {
        this.dateResolved = dateResolved;
    }


    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date decisionDate) {
        this.deadline = decisionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


    @Override
    public String getHumanId() {
        return getCode();
    }

    @Override
    protected IIntegrationEntityDAO<? extends IntegrationEntity> getIntegrationEntityDAO() {
        return Registry.getIssueLogDAO();
    }
}
