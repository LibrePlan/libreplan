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

    public void prepareEditAdvanceMeasurements(AdvanceAssignment advanceAssignment);

    public List<AdvanceMeasurement> getAdvanceMeasurements();

    public List<AdvanceAssignment> getAdvanceAssignments();

    public void initEdit(OrderElement orderElement);

    public boolean addNewLineAdvanceAssignment();

    public AdvanceMeasurement addNewLineAdvaceMeasurement();

    public void removeLineAdvanceAssignment(AdvanceAssignment advance);

    public void removeLineAdvanceMeasurement(AdvanceMeasurement advance);

    public List<AdvanceType> getPossibleAdvanceTypes(
            DirectAdvanceAssignment directAdvanceAssignment);

    public boolean isReadOnlyAdvanceMeasurements();

    public void cleanAdvance(DirectAdvanceAssignment advance);

    public boolean isPrecisionValid(AdvanceMeasurement advanceMeasurement);

    public boolean greatThanMaxValue(AdvanceMeasurement advanceMeasurement);

    public boolean isDistinctValidDate(LocalDate value,
            AdvanceMeasurement newAdvanceMeasurement);

    public BigDecimal getUnitPrecision();

    public AdvanceMeasurement getLastAdvanceMeasurement(
            DirectAdvanceAssignment advanceAssignment);

    public void sortListAdvanceMeasurement();

    public String getInfoAdvanceAssignment();

    public void confirmSave()throws InstanceNotFoundException,
            DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException;

    public BigDecimal getPercentageAdvanceMeasurement(
            AdvanceMeasurement advanceMeasurement);

    public DirectAdvanceAssignment calculateFakeDirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment);

    public BigDecimal getAdvancePercentageChildren();

    public XYModel getChartData(Set<AdvanceAssignment> selectedAdvances);

    public void refreshChangesFromOrderElement();

    public boolean isQualityForm(AdvanceAssignment advance);

    public boolean lessThanPreviousMeasurements();

    public boolean hasConsolidatedAdvances(AdvanceAssignment advance);

    public boolean hasConsolidatedAdvances(AdvanceMeasurement advanceMeasurement);

    public boolean canRemoveOrChange(AdvanceMeasurement advanceMeasurement);

    public boolean findIndirectConsolidation(
            AdvanceMeasurement advanceMeasurement);

    public void resetAdvanceAssignment();

    BigDecimal getMaxValue(AdvanceType advanceType);

    AdvanceAssignment getSpreadAdvance();

    LocalDate getLastConsolidatedMeasurementDate(
            AdvanceAssignment advanceAssignment);

    boolean hasAnyConsolidatedAdvanceCurrentOrderElement();

}
