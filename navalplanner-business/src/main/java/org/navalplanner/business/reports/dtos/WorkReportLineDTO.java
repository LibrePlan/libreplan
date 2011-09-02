/*
 * This file is part of NavalPlan
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 * Desenvolvemento Tecnolóxico de Galicia
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.reports.dtos;

import java.util.Date;

import org.joda.time.LocalDate;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workreports.entities.WorkReportLine;

/**
 * DTO for {@link WorkReportLine} entity.
 *
 * @author Susana Montes Pedreira <smonts@wirelessgalicia.com>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
public class WorkReportLineDTO {

    private EffortDuration sumEffort;

    private Date date;

    private Resource resource;

    private TypeOfWorkHours typeOfWorkHours;

    public WorkReportLineDTO() {
    }

    public WorkReportLineDTO(Resource resource,
            TypeOfWorkHours typeOfWorkHours, Date date, Long effortDB) {
        this.setDate(date);
        this.setResource(resource);
        this.setTypeOfWorkHours(typeOfWorkHours);
        this.setSumEffort(EffortDuration.seconds(effortDB.intValue()));
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public void setTypeOfWorkHours(TypeOfWorkHours typeOfWorkHours) {
        this.typeOfWorkHours = typeOfWorkHours;
    }

    public TypeOfWorkHours getTypeOfWorkHours() {
        return typeOfWorkHours;
    }

    public LocalDate getLocalDate() {
        return LocalDate.fromDateFields(getDate());
    }

    public void setSumEffort(EffortDuration effort) {
        this.sumEffort = effort;
    }

    public EffortDuration getSumEffort() {
        return sumEffort;
    }

}
