/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.workreports.entities;

import org.navalplanner.business.common.BaseEntity;

/**
 * @author Diego Pino García <dpino@igalia.com>
 */

public class WorkReportType extends BaseEntity {

    public static WorkReportType create() {
        WorkReportType workReportType = new WorkReportType();
        workReportType.setNewObject(true);
        return workReportType;
    }

    public static WorkReportType create(String name) {
        WorkReportType workReportType = new WorkReportType(name);
        workReportType.setNewObject(true);
        return workReportType;
    }

    private String name;

    /**
     * Constructor for hibernate. Do not use!
     */
    public WorkReportType() {

    }

    private WorkReportType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
