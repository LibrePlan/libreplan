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

import java.util.List;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.workreports.entities.IWorkReportsElements;
import org.libreplan.business.workreports.entities.WorkReportLine;

/**
 * This interface should be implemented for each strategy that allows to look
 * for the set of {@link OrderElement} associated to
 * {@link IWorkReportsElements}, i.e.: an strategy could be only one
 * {@link OrderElement} per {@link WorkReportLine}.
 *
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 *
 */
public interface IBindingOrderElementStrategy {

    /**
     * This method allows to get the list of {@link OrderElement} associated to
     * a given {@link IWorkReportDTOsElements}, depending on implementation, the
     * result could have one or more elements.
     *
     * @param workReportDTO
     * @return list of {@link OrderElement} associated to the
     *         {@link IWorkReportDTOsElements} param
     * @throws ValidationException
     */
    List<OrderElement> getOrderElementsBound(
            IWorkReportDTOsElements workReportDTO) throws ValidationException;

    /**
     * This method returns an string representing the code of the
     * {@link OrderElement} associated to the {@link IWorkReportsElements} given
     * as parameter
     *
     * @param workReportEntity
     * @return the code
     */
    String getOrderElementCodesBound(IWorkReportsElements workReportEntity);

    /**
     * This method allows to assign a list of {@link OrderElement} to the
     * {@link IWorkReportsElements} given as parameter. It should throw a
     * {@link ValidationException} when the operation could not be done,
     * depending on the strategy implemented
     *
     * @param workReportEntity
     * @param list
     * @throws ValidationException
     */
    void assignOrderElementsToWorkReportLine(
            IWorkReportsElements workReportEntity, List<OrderElement> list)
            throws ValidationException;

}
