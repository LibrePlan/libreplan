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

import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.users.entities.User;

import java.util.Date;

/**
 * RiskLog entity, represents parameters to be able to administrate risks that come up in the project.
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
public class RiskLog extends ProjectLog {

    private String projectName;

    private String status;

    private LowMediumHighEnum probability = LowMediumHighEnum.getDefault();

    private LowMediumHighEnum impact = LowMediumHighEnum.getDefault();

    private Date dateCreated;

    private User createdBy;

    private String counterMeasures;

    private String contingency;

    private String responsible;

    private Date actionWhen;

    private String notes;

    private RiskScoreStatesEnum score = RiskScoreStatesEnum.getDefault();

    public static RiskLog create() {
        return create(new RiskLog(new Date()));
    }

    private RiskLog(Date date) {
        this.dateCreated = date;
    }

    /**
     * Constructor for Hibernate. Do not use!
     */
    protected RiskLog() {

    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName (String projectName) {
        this.projectName = projectName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LowMediumHighEnum getProbability() {
        return probability;
    }

    public void setProbability(LowMediumHighEnum probability) {
        this.probability = probability;
    }

    public LowMediumHighEnum getImpact() {
        return impact;
    }

    public void setImpact(LowMediumHighEnum impact) {
        this.impact = impact;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public String getCounterMeasures() {
        return counterMeasures;
    }

    public void setCounterMeasures(String counterMeasures) {
        this.counterMeasures = counterMeasures;
    }

    public String getContingency() {
        return contingency;
    }

    public void setContingency(String contingency) {
        this.contingency = contingency;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getResponsible() {
        return responsible;
    }

    public Date getActionWhen() {
        return actionWhen;
    }

    public void setActionWhen(Date actionWhen) {
        this.actionWhen = actionWhen;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setScoreAfterCM(RiskScoreStatesEnum scoreAfterCM) {
        this.score = scoreAfterCM;
    }

    public RiskScoreStatesEnum getScoreAfterCM() {
        return score;
    }

    public int getRiskScore() {
        return (probability.ordinal() + 1) * (impact.ordinal() + 1);
    }

    @Override
    public String getHumanId() {
        return getCode();
    }

    @Override
    protected IIntegrationEntityDAO<? extends IntegrationEntity> getIntegrationEntityDAO() {
        return Registry.getRiskLogDAO();
    }

}
