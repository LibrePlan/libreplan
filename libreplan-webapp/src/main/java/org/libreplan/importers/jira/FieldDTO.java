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

package org.libreplan.importers.jira;


/**
 * DTO representing a jira-issue Field
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class FieldDTO {

    private String summary;
    private StatusDTO status;
    private TimeTrackingDTO timetracking;
    private WorkLogDTO worklog;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public TimeTrackingDTO getTimetracking() {
        return timetracking;
    }

    public void setTimetracking(TimeTrackingDTO timetracking) {
        this.timetracking = timetracking;
    }

    public WorkLogDTO getWorklog() {
        return worklog;
    }

    public void setWorklog(WorkLogDTO worklog) {
        this.worklog = worklog;
    }

    public StatusDTO getStatus() {
        return status;
    }

    public void setStatus(StatusDTO status) {
        this.status = status;
    }

}
