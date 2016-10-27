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

package org.libreplan.web.orders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.advance.entities.AdvanceAssignment;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.entities.IndirectAdvanceAssignment;
import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.OrderElement;
import org.zkoss.zul.XYModel;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IManageOrderElementAdvancesModel {

    void prepareEditAdvanceMeasurements(AdvanceAssignment advanceAssignment);

    List<AdvanceMeasurement> getAdvanceMeasurements();

    List<AdvanceAssignment> getAdvanceAssignments();

    void initEdit(OrderElement orderElement);

    boolean addNewLineAdvanceAssignment();

    AdvanceMeasurement addNewLineAdvanceMeasurement();

    void removeLineAdvanceAssignment(AdvanceAssignment advance);

    void removeLineAdvanceMeasurement(AdvanceMeasurement advance);

    List<AdvanceType> getPossibleAdvanceTypes(DirectAdvanceAssignment directAdvanceAssignment);

    boolean isReadOnlyAdvanceMeasurements();

    void cleanAdvance(DirectAdvanceAssignment advance);

    boolean isPrecisionValid(AdvanceMeasurement advanceMeasurement);

    boolean greatThanMaxValue(AdvanceMeasurement advanceMeasurement);

    boolean isDistinctValidDate(LocalDate value, AdvanceMeasurement newAdvanceMeasurement);

    BigDecimal getUnitPrecision();

    AdvanceMeasurement getLastAdvanceMeasurement(DirectAdvanceAssignment advanceAssignment);

    void sortListAdvanceMeasurement();

    String getInfoAdvanceAssignment();

    void confirmSave()
            throws InstanceNotFoundException,
            DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException;

    BigDecimal getPercentageAdvanceMeasurement(AdvanceMeasurement advanceMeasurement);

    DirectAdvanceAssignment calculateFakeDirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment);

    BigDecimal getAdvancePercentageChildren();

    XYModel getChartData(Set<AdvanceAssignment> selectedAdvances);

    void refreshChangesFromOrderElement();

    boolean isQualityForm(AdvanceAssignment advance);

    boolean isReadOnly(AdvanceAssignment advance);

    boolean lessThanPreviousMeasurements();

    boolean hasConsolidatedAdvances(AdvanceAssignment advance);

    boolean hasConsolidatedAdvances(AdvanceMeasurement advanceMeasurement);

    boolean canRemoveOrChange(AdvanceMeasurement advanceMeasurement);

    boolean findIndirectConsolidation(AdvanceMeasurement advanceMeasurement);

    void resetAdvanceAssignment();

    BigDecimal getMaxValue(AdvanceType advanceType);

    AdvanceAssignment getSpreadAdvance();

    LocalDate getLastConsolidatedMeasurementDate(AdvanceAssignment advanceAssignment);

    boolean hasAnyConsolidatedAdvanceCurrentOrderElement();

    boolean hasAnySubcontractedTaskOnChildren();

    boolean isSubcontractedAdvanceType(AdvanceAssignment advance);

    boolean isSubcontractedAdvanceTypeAndSubcontractedTask(AdvanceAssignment advance);

    Boolean isAlreadyReportedProgress(AdvanceMeasurement measure);

    boolean hasReportedProgress(AdvanceAssignment advance);

    Boolean isAlreadyReportedProgressWith(LocalDate date);


}
