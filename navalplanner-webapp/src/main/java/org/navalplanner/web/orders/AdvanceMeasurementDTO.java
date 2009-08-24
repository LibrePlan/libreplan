package org.navalplanner.web.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceType;

public class AdvanceMeasurementDTO implements IAdvanceMeasurementDTO {

    private AdvanceType advanceType;

    private AdvanceMeasurement advanceMeasurement;

    private AdvanceAssigment advanceAssigment;

    private Date date;

    private BigDecimal maxValue;

    private BigDecimal value;

    private boolean reportGlobalAdvance;

    private boolean isNewObject = true;

    private boolean isNewDTO = true;

    private boolean selectedRemove = false;

    private String percentage;

    public AdvanceMeasurementDTO() {
        this.reportGlobalAdvance = false;
        this.date = new Date();
        this.percentage = new String("");
        this.isNewDTO = true;
        this.isNewObject = false;
    }

    public AdvanceMeasurementDTO(AdvanceType advanceType,
            AdvanceAssigment advanceAssigment,
            AdvanceMeasurement advanceMeasurement) {
        this.advanceType = advanceType;

        this.advanceMeasurement = advanceMeasurement;
        this.date = advanceMeasurement.getDate();
        this.maxValue = advanceMeasurement.getMaxValue();
        this.value = advanceMeasurement.getValue();

        this.advanceAssigment = advanceAssigment;
        this.reportGlobalAdvance = advanceAssigment.getReportGlobalAdvance();

        this.percentage = new String("");
        this.isNewDTO = false;
        if(advanceAssigment.getVersion()==null){
            this.isNewObject = true;
        }else{
            this.isNewObject = false;
        }
    }

    @Override
    public boolean getIsNewObject() {
        return this.isNewObject;
    }

    @Override
    public boolean getIsNewDTO() {
        return this.isNewDTO;
    }

    @Override
    public void setAdvanceType(AdvanceType advanceType){
        this.advanceType = advanceType;
    }
    @Override
    public AdvanceType getAdvanceType() {
        return this.advanceType;
    }

    @Override
    public AdvanceAssigment getAdvanceAssigment() {
        return this.advanceAssigment;
    }

    @Override
    public AdvanceMeasurement getAdvanceMeasurement() {
        return this.advanceMeasurement;
    }

    @Override
    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    @Override
    public String getPercentage() {
        if((value != null)&&(maxValue != null)){
            BigDecimal percentage = new BigDecimal(0);
            BigDecimal division = (value.divide(maxValue,4,RoundingMode.HALF_UP));
            division.setScale(2, RoundingMode.HALF_UP);
            percentage = division.multiply(new BigDecimal(100));
            percentage = percentage.setScale(2, RoundingMode.HALF_UP);
            this.percentage = percentage.toString();
            return this.percentage;
        }
        return "";
    }

    @Override
    public void setReportGlobalAdvance(boolean reportGlobalAdvance) {
        this.reportGlobalAdvance = reportGlobalAdvance;
    }

    @Override
    public boolean getReportGlobalAdvance() {
        return this.reportGlobalAdvance;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public Date getDate() {
        return this.date;
    }

    @Override
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public BigDecimal getValue() {
        return this.value;
    }

    @Override
    public BigDecimal getMaxValue() {
        return this.maxValue;
    }

    @Override
    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean isSelectedForRemove(){
        return this.selectedRemove;
    }

    @Override
    public void setSelectedForRemove(boolean selectedRemove){
        this.selectedRemove = selectedRemove;
    }
}