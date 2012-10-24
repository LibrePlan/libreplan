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

package org.libreplan.business.common.entities;

import org.libreplan.business.common.BaseEntity;

public class JiraConfiguration extends BaseEntity {

    public static JiraConfiguration create() {
        return create(new JiraConfiguration());
    }

    private boolean jiraActivated;

    private String jiraUrl;

    private String jiraLabelUrl;

    private String jiraUserId;

    private String jiraPassword;


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

    public String getJiraLabelUrl() {
        return jiraLabelUrl;
    }

    public void setJiraLabelUrl(String jiraLabelUrl) {
        this.jiraLabelUrl = jiraLabelUrl;
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


}
