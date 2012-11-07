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

package org.libreplan.ws.boundusers.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.ws.boundusers.api.TaskDTO;
import org.libreplan.ws.boundusers.api.TaskListDTO;
import org.libreplan.ws.common.impl.DateConverter;

/**
 * Converter from/to {@link Task} related entities to/from DTOs.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public final class TaskConverter {

    private TaskConverter() {
    }

    public final static TaskDTO toDTO(Task task) {
        OrderElement orderElement = task.getOrderElement();

        AdvanceMeasurement lastAdvanceMeasurement = orderElement
                .getLastAdvanceMeasurement();
        BigDecimal progressValue = null;
        LocalDate progressDate = null;
        if (lastAdvanceMeasurement != null) {
            progressValue = lastAdvanceMeasurement.getValue();
            progressDate = lastAdvanceMeasurement.getDate();
        }

        return new TaskDTO(task.getName(), orderElement.getCode(), orderElement
                .getOrder().getName(),
                DateConverter.toXMLGregorianCalendar(task.getStartDate()),
                DateConverter.toXMLGregorianCalendar(task.getEndDate()),
                progressValue,
                DateConverter.toXMLGregorianCalendar(progressDate),
                orderElement.getEffortAsString());
    }

    public final static TaskListDTO toDTO(Collection<Task> tasks) {
        List<TaskDTO> dtos = new ArrayList<TaskDTO>();
        for (Task each : tasks) {
            dtos.add(toDTO(each));
        }
        return new TaskListDTO(dtos);
    }

}
