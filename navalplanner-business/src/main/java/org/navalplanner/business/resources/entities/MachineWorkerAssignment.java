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

package org.navalplanner.business.resources.entities;

import java.util.Date;

import org.navalplanner.business.common.BaseEntity;


/**
 * Machine Worker Assignment<br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class MachineWorkerAssignment extends BaseEntity {

    private Date startDate;

    private Date finishDate;

    private Worker worker;

    private MachineWorkersConfigurationUnit machineWorkersConfigurationUnit;

    public MachineWorkersConfigurationUnit getMachineWorkersConfigurationUnit() {
        return machineWorkersConfigurationUnit;
    }

    public static MachineWorkerAssignment create(
            MachineWorkersConfigurationUnit configurationUnit, Worker worker) {
        return create(new MachineWorkerAssignment(
                configurationUnit, worker));
    }

    protected MachineWorkerAssignment(
            MachineWorkersConfigurationUnit configurationUnit, Worker worker) {
        this.machineWorkersConfigurationUnit = configurationUnit;
        this.worker = worker;
    }

    public MachineWorkerAssignment() {
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Worker getWorker() {
        return worker;
    }

}
