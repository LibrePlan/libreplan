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

package org.libreplan.web.workreports;

import java.util.Date;

import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.zkoss.ganttz.IPredicate;

/**
 * Checks if {@link WorkReportType}, the start date and finish date from
 * {@link WorkReport} matches attributes
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkReportPredicate implements IPredicate {

    private WorkReportType type;

    private Date startDate;

    private Date finishDate;

    public WorkReportPredicate(WorkReportType type, Date startDate,
            Date finishDate) {
        this.type = type;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    @Override
    public boolean accepts(Object object) {
        final WorkReportDTO workReportDTO = (WorkReportDTO) object;
        return accepts(workReportDTO);
    }

    private boolean accepts(WorkReportDTO workReportDTO) {
        if (workReportDTO == null) {
            return false;
        }
        if (equalsType(workReportDTO) && acceptFiltersDates(workReportDTO)) {
            return true;
        }
        return false;
    }

    private boolean equalsType(WorkReportDTO workReportDTO) {
        if (type == null) {
            return true;
        }
        if (workReportDTO.getWorkReport() != null
                && workReportDTO.getWorkReport().getWorkReportType() != null
                && workReportDTO.getWorkReport().getWorkReportType().getId()
                        .equals(type.getId())) {
            return true;
        }
        return false;
    }

    private boolean acceptFiltersDates(WorkReportDTO workReportDTO) {
        // Check if exist work report items into interval between the start date
        // and finish date.
        if ((isInTheRangeFilterDates(workReportDTO.getDateStart()) || isInTheRangeFilterDates(workReportDTO
                .getDateFinish()))
                || ((isInTheRangeWorkReportDates(startDate, workReportDTO)) || (isInTheRangeWorkReportDates(
                        finishDate, workReportDTO)))) {
            return true;
        }
        return false;
    }

    private boolean isInTheRangeFilterDates(Date date) {
        // Check if date is into interval between the startdate and finish date
        return (isGreaterToStartDate(date, startDate) && isLowerToFinishDate(
                date, finishDate));
    }

    private boolean isInTheRangeWorkReportDates(Date date,
            WorkReportDTO workReportDTO) {
        // Check if date is into interval between the startdate and finish date
        return (isGreaterToStartDate(date, workReportDTO.getDateStart()) && isLowerToFinishDate(
                date, workReportDTO.getDateFinish()));
    }

    private boolean isGreaterToStartDate(Date date, Date startDate) {
        if (startDate == null) {
            return true;
        }

        if (date != null && (date.compareTo(startDate) >= 0)) {
            return true;
        }
        return false;
    }

    private boolean isLowerToFinishDate(Date date, Date finishDate) {
        if (finishDate == null) {
            return true;
        }
        if (date != null && (date.compareTo(finishDate) <= 0)) {
            return true;
        }
        return false;
    }
}
