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

package org.navalplanner.web.resources.machine;

import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.MachineWorkersConfigurationUnit;

/*
 * This interface contains the operations to create/edit a machine.
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IMachineModel {

    void confirmSave() throws ValidationException;

    List<BaseCalendar> getBaseCalendars();

    ResourceCalendar getCalendar();

    Machine getMachine();

    List<Machine> getMachines();

    List<MachineWorkersConfigurationUnit> getConfigurationUnits();

    void initCreate();

    void initEdit(Machine machine);

    void setCalendar(ResourceCalendar resourceCalendar);
}
