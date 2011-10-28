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

package org.libreplan.web.resources.machine;

import java.util.List;

import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Machine;
import org.libreplan.business.resources.entities.MachineWorkersConfigurationUnit;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.web.common.IIntegrationEntityModel;
import org.libreplan.web.resources.search.ResourcePredicate;

/*
 * This interface contains the operations to create/edit a machine.
 *
 * Conversation state: the Machine instance and associated entities.
 * The MachineWorkersConfigurationUnit set of the machine,
 * the MachineWorkerAssigments of each MachineWorkersConfigurationUnit instance,
 * the Criterion set required by each MachineWorkersConfigurationUnit instance,
 * the calendar associated with the Machine instance
 *
 * <strong>Conversation protocol:</strong>
 *
 * <strong>Initial steps:</strong>
 *   <code>initCreate</code>
 *   <code>initEdit</code>
 *
 * <strong>Intermediate conversational steps:</strong>
 *   <code>getConfigurationUnitsOfMachine</code>
 *   <code>setCalendarOfMachine</code>
 *   <code>setCalendarOfMachine</code>
 *
 * <strong>Final conversational step:</strong>
 *   <code>confirmSave()</code>
 *
 * <strong>Not conversational steps:</strong>
 *   <code>getMachines()</code>
 *   <code>getBaseCalendars()</code>
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
public interface IMachineModel extends IIntegrationEntityModel {
    // Initial conversational steps
    void initCreate();
    void initEdit(Machine machine);

    // Intermediate conversation steps
    Machine getMachine();
    ResourceCalendar getCalendarOfMachine();
    List<MachineWorkersConfigurationUnit> getConfigurationUnitsOfMachine();
    void setCalendarOfMachine(ResourceCalendar resourceCalendar);
    void addWorkerAssigmentToConfigurationUnit(MachineWorkersConfigurationUnit
 machineWorkersConfigurationUnit,
            Worker worker);
    void addCriterionRequirementToConfigurationUnit(
            MachineWorkersConfigurationUnit unit, Criterion criterion);

    MachineWorkersConfigurationUnit getConfigurationUnitById(Long id)
            throws InstanceNotFoundException;

    // Final conversational step
    void confirmSave() throws ValidationException;

    // Non conversational methods
    List<Machine> getMachines();
    List<BaseCalendar> getBaseCalendars();

    void removeConfigurationUnit(MachineWorkersConfigurationUnit unit);

    void setCalendar(ResourceCalendar resourceCalendar);

    ResourceCalendar getCalendar();

    BaseCalendar getDefaultCalendar();

    List<Machine> getFilteredMachines(ResourcePredicate predicate);

    public List<Machine> getAllMachines();

    boolean canRemove(Machine machine);

    void confirmRemove(Machine machine) throws InstanceNotFoundException;

    void removeCalendar();

}
