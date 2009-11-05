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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.navalplanner.business.common.BaseEntity;

/**
 * Machine Workers Configuration Unit<br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class MachineWorkersConfigurationUnit extends BaseEntity {

    private Machine machine;

    private BigDecimal alpha;

    private String name;

    private Set<MachineWorkerAssignment> workerAssignments = new HashSet<MachineWorkerAssignment>();

    private Set<Criterion> requiredCriterions = new HashSet<Criterion>();

    public static MachineWorkersConfigurationUnit create(Machine machine,
            String name,
            BigDecimal alpha) {
        return (MachineWorkersConfigurationUnit) create(new MachineWorkersConfigurationUnit(
                machine,
                name, alpha));
    }

    protected MachineWorkersConfigurationUnit(Machine machine, String name,
            BigDecimal alpha) {
        this.machine = machine;
        this.name = name;
        this.alpha = alpha;
    }

    public MachineWorkersConfigurationUnit() {
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setAlpha(BigDecimal alpha) {
        this.alpha = alpha;
    }

    public BigDecimal getAlpha() {
        return alpha;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<MachineWorkerAssignment> getWorkerAssignments() {
        return Collections.unmodifiableSet(workerAssignments);
    }

    public void addWorkerAssignment(MachineWorkerAssignment assignment) {
        workerAssignments.add(assignment);
    }

    public void addNewWorkerAssignment(Worker worker) {
        MachineWorkerAssignment assigment = MachineWorkerAssignment.create(
                this, worker);
        workerAssignments.add(assigment);
    }

    public void removeMachineWorkersConfigurationUnit(
            MachineWorkerAssignment assignment) {
        workerAssignments.remove(assignment);
    }

    public void setRequiredCriterions(Set<Criterion> requiredCriterions) {
        this.requiredCriterions = requiredCriterions;
    }

    public Set<Criterion> getRequiredCriterions() {
        return Collections.unmodifiableSet(requiredCriterions);
    }

    public void addRequiredCriterion(Criterion criterion) {
        requiredCriterions.add(criterion);
    }

    public void removeRequiredCriterion(Criterion criterion) {
        requiredCriterions.remove(criterion);
    }

}
