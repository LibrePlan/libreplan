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

package org.libreplan.web.planner.order;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.entities.EndDateCommunication;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.SubcontractorDeliverDate;
import org.libreplan.business.planner.entities.Task;

/**
 * Contract for {@link SubcontractModel}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface ISubcontractModel {

    /*
     * Non conversational steps
     */
    List<ExternalCompany> getSubcontractorExternalCompanies();

    /*
     * Initial conversation steps
     */
    void init(Task task, org.zkoss.ganttz.data.Task ganttTask);

    /*
     * Intermediate conversation steps
     */
    SubcontractedTaskData getSubcontractedTaskData();

    void setExternalCompany(ExternalCompany externalCompany);

    boolean hasResourceAllocations();

    void setEndDate(Date endDate);
    void removeSubcontractedTaskData();

    /*
     * Final conversation steps
     */
    void confirm() throws ValidationException;
    void cancel();

    SortedSet<SubcontractorDeliverDate> getDeliverDates();

    void addDeliverDate(Date subDeliverDate);

    void removeRequiredDeliverDate(
            SubcontractorDeliverDate subcontractorDeliverDate);

    boolean alreadyExistsRepeatedDeliverDate(Date newDeliverDate);

    SortedSet<EndDateCommunication> getAskedEndDates();

}