package org.navalplanner.web.orders;

import java.math.BigDecimal;
import java.util.Date;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceType;

public interface IAdvanceMeasurementDTO {

    public boolean getIsNewObject();
    public boolean getIsNewDTO();

    public AdvanceAssigment getAdvanceAssigment();
    public AdvanceMeasurement getAdvanceMeasurement();

    public void setPercentage(String percentage);
    public String getPercentage();

    public void setReportGlobalAdvance(boolean reportGlobalAdvance);
    public boolean getReportGlobalAdvance();

    public void setDate(Date date);
    public Date getDate();

    public void setValue(BigDecimal value);
    public BigDecimal getValue();

    public BigDecimal getMaxValue();
    public void setMaxValue(BigDecimal maxValue);

    public boolean isSelectedForRemove();
    public void setSelectedForRemove(boolean selectedRemove);

    public AdvanceType getAdvanceType();
    public void setAdvanceType(AdvanceType advanceType);
}
