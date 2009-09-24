package org.navalplanner.web.orders;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.zkoss.zul.XYModel;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IManageOrderElementAdvancesModel {

    public void prepareEditAdvanceMeasurements(AdvanceAssignment advanceAssignment);

    public List<AdvanceMeasurement> getAdvanceMeasurements();

    public List<AdvanceAssignment> getAdvanceAssignments();

    public void init(OrderElement orderElement);

    public void addNewLineAdvaceAssignment();

    public void addNewLineAdvaceMeasurement();

    public void removeLineAdvanceAssignment(AdvanceAssignment advance);

    public void removeLineAdvanceMeasurement(AdvanceMeasurement advance);

    public List<AdvanceType> getPossibleAdvanceTypes(
            DirectAdvanceAssignment directAdvanceAssignment);

    public boolean isReadOnlyAdvanceMeasurements();

    public void cleanAdvance();

    public boolean isPrecisionValid(BigDecimal value);

    public boolean greatThanMaxValue(BigDecimal value);

    public boolean isDistinctValidDate(Date value,
            AdvanceMeasurement newAdvanceMeasurement);

    public BigDecimal getUnitPrecision();

    public AdvanceMeasurement getLastAdvanceMeasurement(
            DirectAdvanceAssignment advanceAssignment);

    public void sortListAdvanceMeasurement();

    public String getInfoAdvanceAssignment();

    public void accept()throws InstanceNotFoundException,
            DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException;

    public BigDecimal getPercentageAdvanceMeasurement(
            AdvanceMeasurement advanceMeasurement);

    public DirectAdvanceAssignment calculateFakeDirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment);

    public BigDecimal getAdvancePercentageChildren();

    public XYModel getChartData();

}
