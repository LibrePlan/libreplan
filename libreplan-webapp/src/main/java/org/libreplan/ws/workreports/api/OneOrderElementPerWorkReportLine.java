/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 - ComtecSF S.L.
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
package org.libreplan.ws.workreports.api;

import java.util.ArrayList;
import java.util.List;

import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.workreports.entities.IWorkReportsElements;
import org.libreplan.business.workreports.entities.WorkReportLine;

/**
 * Singleton implementation for {@link IBindingOrderElementStrategy}, with this
 * implementation there could be only one {@link OrderElement} for each
 * {@link WorkReportLine}
 *
 * @author Ignacio Díaz Teijido <ignacio.diaz@comtecsf.es>
 *
 */
public class OneOrderElementPerWorkReportLine implements
        IBindingOrderElementStrategy {

    private static OneOrderElementPerWorkReportLine instance = null;

    // This avoids external instantiation
    private OneOrderElementPerWorkReportLine() {

    }

    // Singleton instantiator
    public static OneOrderElementPerWorkReportLine getInstance() {
        if (instance == null)
            instance = new OneOrderElementPerWorkReportLine();
        return instance;
    }

    @Override
    public List<OrderElement> getOrderElementsBound(
            IWorkReportDTOsElements workReportDTO) throws ValidationException {
        List<OrderElement> result = new ArrayList<OrderElement>();
        if (workReportDTO.getOrderElement() != null) {
            OrderElement orderElement;
            try {
                orderElement = Registry.getOrderElementDAO().findUniqueByCode(
                        workReportDTO.getOrderElement());
            } catch (InstanceNotFoundException e) {
                throw new UnsupportedOperationException("Element not found");
            }
            result.add(orderElement);
        }
        return result;
    }

    @Override
    public String getOrderElementCodesBound(
            IWorkReportsElements workReportEntity) {
        if (workReportEntity.getOrderElement() != null)
            return workReportEntity.getOrderElement().getCode();
        else
            return "";
    }

    @Override
    public void assignOrderElementsToWorkReportLine(
            IWorkReportsElements workReportEntity, List<OrderElement> list)
            throws ValidationException {
        if (list.size() == 1)
            workReportEntity.setOrderElement(list.get(0));
        else {
            if (workReportEntity instanceof WorkReportLine)
                throw new ValidationException(
                    "List must have exactly one element");
        }
    }
}
