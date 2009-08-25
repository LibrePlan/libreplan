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
    public List<AdvanceMeasurementDTO> getAdvanceMeasurements();
    public void init(OrderElement orderElement);
    public void addNewLine();
<<<<<<< HEAD:navalplanner-webapp/src/main/java/org/navalplanner/web/orders/IManageOrderElementAdvancesModel.java
    public void removeLine(IAdvanceMeasurementDTO advanceDTO);
    public List<AdvanceType> getActivesAdvanceTypes();
    public boolean isPrecisionValid(IAdvanceMeasurementDTO advanceDTO, BigDecimal value);
    public boolean greatThanMaxValue(IAdvanceMeasurementDTO advanceDTO, BigDecimal value);
    public boolean isGreatValidDate(IAdvanceMeasurementDTO advanceDTO, Date value);
=======
    public void removeLine(AdvanceMeasurementDTO advanceDTO);
    public List<AdvanceType> getActivesAdvanceTypes();
    public boolean isPrecisionValid(AdvanceMeasurementDTO advanceDTO, BigDecimal value);
    public boolean greatThanMaxValue(AdvanceMeasurementDTO advanceDTO, BigDecimal value);
    public boolean isGreatValidDate(AdvanceMeasurementDTO advanceDTO, Date value);
>>>>>>> cb4190f... ItEr23S07CUIntroducionAvanceUnidadeTraballoItEr22S07 : Remove the interfaz IAdvanceMeasurementDTO:navalplanner-webapp/src/main/java/org/navalplanner/web/orders/IManageOrderElementAdvancesModel.java
    public void accept()throws InstanceNotFoundException,
            DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException;
}
