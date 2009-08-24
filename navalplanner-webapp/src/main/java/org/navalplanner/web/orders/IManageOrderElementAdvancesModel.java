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
    public List<IAdvanceMeasurementDTO> getAdvanceMeasurements();
    public void init(OrderElement orderElement);
    public void prepareForCreate();
    public void prepareForRemove(IAdvanceMeasurementDTO advanceDTO);
    public List<AdvanceType> getActivesAdvanceTypes();
    public boolean isPrecisionValid(IAdvanceMeasurementDTO advanceDTO, BigDecimal value);
    public boolean greatThanMaxValue(IAdvanceMeasurementDTO advanceDTO, BigDecimal value);
    public boolean isGreatValidDate(IAdvanceMeasurementDTO advanceDTO, Date value);
    public void confirm()throws InstanceNotFoundException,
            DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException;
}
