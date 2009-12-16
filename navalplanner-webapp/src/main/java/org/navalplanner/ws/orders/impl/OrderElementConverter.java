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

package org.navalplanner.ws.orders.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.ws.common.api.ResourceEnumDTO;
import org.navalplanner.ws.common.impl.ResourceEnumConverter;
import org.navalplanner.ws.orders.api.HoursGroupDTO;
import org.navalplanner.ws.orders.api.OrderDTO;
import org.navalplanner.ws.orders.api.OrderElementDTO;
import org.navalplanner.ws.orders.api.OrderLineDTO;
import org.navalplanner.ws.orders.api.OrderLineGroupDTO;

/**
 * Converter from/to {@link OrderElement} entities to/from DTOs.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public final class OrderElementConverter {

    private OrderElementConverter() {
    }

    public final static OrderElementDTO toDTO(OrderElement orderElement) {
        String name = orderElement.getName();
        String code = orderElement.getCode();
        Date initDate = orderElement.getInitDate();
        Date deadline = orderElement.getDeadline();
        String description = orderElement.getDescription();

        if (orderElement instanceof OrderLine) {
            Set<HoursGroupDTO> hoursGroups = new HashSet<HoursGroupDTO>();
            for (HoursGroup hoursGroup : ((OrderLine) orderElement)
                    .getHoursGroups()) {
                hoursGroups.add(toDTO(hoursGroup));
            }

            return new OrderLineDTO(name, code, initDate, deadline,
                    description, hoursGroups);
        } else { // orderElement instanceof OrderLineGroup
            List<OrderElementDTO> children = new ArrayList<OrderElementDTO>();
            for (OrderElement element : orderElement.getChildren()) {
                children.add(toDTO(element));
            }

            if (orderElement instanceof Order) {
                Boolean dependenciesConstraintsHavePriority = ((Order) orderElement)
                        .getDependenciesConstraintsHavePriority();
                BaseCalendar calendar = ((Order) orderElement).getCalendar();
                String calendarName = null;
                if (calendar != null) {
                    calendarName = calendar.getName();
                }

                return new OrderDTO(name, code, initDate, deadline,
                        description, children,
                        dependenciesConstraintsHavePriority, calendarName);
            } else { // orderElement instanceof OrderLineGroup
                return new OrderLineGroupDTO(name, code, initDate, deadline,
                        description, children);
            }
        }
    }

    public final static HoursGroupDTO toDTO(HoursGroup hoursGroup) {
        ResourceEnumDTO resourceType = ResourceEnumConverter.toDTO(hoursGroup
                .getResourceType());
        return new HoursGroupDTO(hoursGroup.getName(), resourceType, hoursGroup
                .getWorkingHours());
    }

    public final static OrderElement toEntity(OrderElementDTO orderElementDTO) {
        OrderElement orderElement;

        if (orderElementDTO instanceof OrderLineDTO) {
            orderElement = OrderLine.create();

            for (HoursGroupDTO hoursGroupDTO : ((OrderLineDTO) orderElementDTO).hoursGroups) {
                HoursGroup hoursGroup = toEntity(hoursGroupDTO);
                ((OrderLine) orderElement).addHoursGroup(hoursGroup);
            }
        } else { // orderElementDTO instanceof OrderLineGroupDTO
            List<OrderElement> children = new ArrayList<OrderElement>();
            for (OrderElementDTO element : ((OrderLineGroupDTO) orderElementDTO).children) {
                children.add(toEntity(element));
            }

            if (orderElementDTO instanceof OrderDTO) {
                orderElement = Order.create();

                ((Order) orderElement)
                        .setDependenciesConstraintsHavePriority(((OrderDTO) orderElementDTO).dependenciesConstraintsHavePriority);
                List<BaseCalendar> calendars = Registry.getBaseCalendarDAO()
                        .findByName(((OrderDTO) orderElementDTO).calendarName);
                if ((calendars != null) && (calendars.size() == 1)) {
                    ((Order) orderElement).setCalendar(calendars.get(0));
                }
            } else { // orderElementDTO instanceof OrderLineGroupDTO
                orderElement = OrderLineGroup.create();
            }

            for (OrderElement child : children) {
                ((OrderLineGroup) orderElement).add(child);
            }
        }

        orderElement.setName(orderElementDTO.name);
        orderElement.setCode(orderElementDTO.code);
        orderElement.setInitDate(orderElementDTO.initDate);
        orderElement.setDeadline(orderElementDTO.deadline);
        orderElement.setDescription(orderElementDTO.description);

        return orderElement;
    }

    private static HoursGroup toEntity(HoursGroupDTO hoursGroupDTO) {
        ResourceEnum resourceType = ResourceEnumConverter
                .fromDTO(hoursGroupDTO.resourceType);
        HoursGroup hoursGroup = HoursGroup.createUnvalidated(
                hoursGroupDTO.name, resourceType, hoursGroupDTO.workingHours);
        return hoursGroup;
    }

}
