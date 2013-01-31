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

package org.libreplan.business.common.entities;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;

public class JiraConfiguration extends BaseEntity {

    /**
     * Code prefix for different entities integrated with JIRA.
     */
    public static final String CODE_PREFIX = "JIRA-";

    public static JiraConfiguration create() {
        return create(new JiraConfiguration());
    }

    private boolean jiraActivated;

    private String jiraUrl;

    /**
     * Stores one of the next 2 options:
     * <ul>
     * <li>A comma-separated list of labels</li>
     * <li>A URL that will return a comma-separated list of labels</li>
     * </ul>
     */
    private String jiraLabels;

    private String jiraUserId;

    private String jiraPassword;

    private TypeOfWorkHours jiraConnectorTypeOfWorkHours;

    /**
     * Constructor for Hibernate. Do not use!
     */
    protected JiraConfiguration() {
    }

    public boolean isJiraActivated() {
        return jiraActivated;
    }

    public void setJiraActivated(boolean jiraActivated) {
        this.jiraActivated = jiraActivated;
    }

    public String getJiraUrl() {
        return jiraUrl;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }

    public String getJiraLabels() {
        return jiraLabels;
    }

    public void setJiraLabels(String jiraLabels) {
        this.jiraLabels = jiraLabels;
    }

    public String getJiraUserId() {
        return jiraUserId;
    }

    public void setJiraUserId(String jiraUserId) {
        this.jiraUserId = jiraUserId;
    }

    public String getJiraPassword() {
        return jiraPassword;
    }

    public void setJiraPassword(String jiraPassword) {
        this.jiraPassword = jiraPassword;
    }

    public TypeOfWorkHours getJiraConnectorTypeOfWorkHours() {
        return jiraConnectorTypeOfWorkHours;
    }

    public void setJiraConnectorTypeOfWorkHours(TypeOfWorkHours typeOfWorkHours) {
        jiraConnectorTypeOfWorkHours = typeOfWorkHours;
    }

}
