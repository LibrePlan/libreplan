package org.navalplanner.web.orders;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssigmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IManageOrderElementAdvancesModel {

    public void prepareEditAdvanceMeasurements(AdvanceAssigmentDTO advanceAssigmentDTO);

    public List<AdvanceMeasurementDTO> getAdvanceMeasurementDTOs();

    public List<AdvanceAssigmentDTO> getAdvanceAssigmentDTOs();

    public void init(OrderElement orderElement);

    public void addNewLineAdvaceAssigment();

    public void addNewLineAdvaceMeasurement();

    public void removeLineAdvanceAssigment(AdvanceAssigmentDTO advanceDTO);

    public void removeLineAdvanceMeasurement(AdvanceMeasurementDTO advanceDTO);

    public List<AdvanceType> getActivesAdvanceTypes();

    public boolean isReadOnlyAdvanceMeasurementDTOs();

    public void cleanAdvance();

    public boolean isPrecisionValid(BigDecimal value);

    public boolean greatThanMaxValue(BigDecimal value);

    public boolean isDistinctValidDate(Date value,AdvanceMeasurementDTO newAdvanceMeasurementDTO);

    public BigDecimal getUnitPrecision();

    public AdvanceMeasurementDTO getFirstAdvanceMeasurement(AdvanceAssigmentDTO advanceAssigmentDTO);

    public void modifyListAdvanceMeasurement(AdvanceMeasurementDTO advanceMeasurementDTO);

    public String getInfoAdvanceAssigment();

    public void accept()throws InstanceNotFoundException,
            DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException;
}
