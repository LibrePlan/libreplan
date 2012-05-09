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
package org.libreplan.web.subcontract;

import java.util.List;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.web.subcontract.exceptions.ConnectionProblemsException;
import org.libreplan.web.subcontract.exceptions.UnrecoverableErrorServiceException;

/**
 * Contract for {@link SubcontractedTasksModel}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface ISubcontractedTasksModel {

    List<SubcontractedTaskData> getSubcontractedTasks();

    void sendToSubcontractor(SubcontractedTaskData subcontractedTaskData)
            throws ValidationException, ConnectionProblemsException,
            UnrecoverableErrorServiceException;

    String exportXML(SubcontractedTaskData subcontractedTaskData);

    Order getOrder(SubcontractedTaskData subcontractedTaskData);
}