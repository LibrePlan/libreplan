package org.navalplanner.web.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;

public class AdvanceMeasurementDTO{

    private AdvanceMeasurement advanceMeasurement;

    private AdvanceAssigmentDTO advanceAssigmentDTO;

    private Date date;

    private BigDecimal value;

    private boolean isNewObject = true;

    private boolean isNewDTO = true;

    private boolean selectedRemove = false;

    private String percentage;

    public AdvanceMeasurementDTO() {
        this.date = new Date();
        this.percentage = new String("0 %");
        this.isNewDTO = true;
        this.isNewObject = false;
    }

    public AdvanceMeasurementDTO(AdvanceMeasurement advanceMeasurement){

        this.advanceMeasurement = advanceMeasurement;
        this.date = advanceMeasurement.getDate();
        this.value = advanceMeasurement.getValue();

        this.percentage = new String("0 %");
        this.isNewDTO = false;
        if(advanceMeasurement.getVersion()==null){
            this.isNewObject = true;
        }else{
            this.isNewObject = false;
        }
    }

    public boolean getIsNewObject(){
        return this.isNewObject;
    }

    public boolean getIsNewDTO() {
        return this.isNewDTO;
    }

    public AdvanceAssigmentDTO getAdvanceAssigmentDTO() {
        return this.advanceAssigmentDTO;
    }

    public void setAdvanceAssigmentDTO(AdvanceAssigmentDTO advanceAssigmentDTO) {
        this.advanceAssigmentDTO = advanceAssigmentDTO;
    }

    public AdvanceMeasurement getAdvanceMeasurement() {
        return this.advanceMeasurement;
    }

    public void setAdvanceMeasurement(AdvanceMeasurement advanceMeasurement) {
        this.advanceMeasurement = advanceMeasurement;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public String getPercentage() {
        if(advanceAssigmentDTO == null) return "0 %";
        if(value == null) return "0 %";
        if(advanceAssigmentDTO.getMaxValue() != null){
            BigDecimal maxValue = advanceAssigmentDTO.getMaxValue();
            BigDecimal percentage = new BigDecimal(0);
            BigDecimal division = (value.divide(maxValue,4,RoundingMode.HALF_UP));
            division.setScale(2, RoundingMode.HALF_UP);
            percentage = division.multiply(new BigDecimal(100));
            percentage = percentage.setScale(2, RoundingMode.HALF_UP);
            this.percentage = percentage.toString()+" %";
            return this.percentage;
        }
        return "0 %";
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return this.date;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public boolean isSelectedForRemove(){
        return this.selectedRemove;
    }

    public void setSelectedForRemove(boolean selectedRemove){
        this.selectedRemove = selectedRemove;
    }
}