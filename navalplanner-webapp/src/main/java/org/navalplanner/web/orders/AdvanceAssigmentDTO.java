/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.web.orders;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.SortedSet;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceMeasurementComparator;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.util.ListSorter;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AdvanceAssigmentDTO {

    private AdvanceType advanceType;

    private SortedSet<AdvanceMeasurement> advanceMeasurements =
            new TreeSet<AdvanceMeasurement>(new AdvanceMeasurementComparator());

    private ListSorter<AdvanceMeasurementDTO> advanceMeasurementDTOs =
            ListSorter.create(new ArrayList<AdvanceMeasurementDTO>(), new AdvanceMeasurementDTOComparator());

    private AdvanceAssigment advanceAssigment;

    private BigDecimal maxValue;

    private boolean reportGlobalAdvance;

    private AdvanceAssigment.Type type;

    private boolean isNewObject = true;

    private boolean isNewDTO = true;

    private boolean selectedRemove = false;

    public AdvanceAssigmentDTO() {
        this.reportGlobalAdvance = false;
        this.isNewDTO = true;
        this.isNewObject = false;
        this.type = AdvanceAssigment.Type.DIRECT;
    }

    public AdvanceAssigmentDTO(AdvanceType advanceType,
            AdvanceAssigment advanceAssigment,
            SortedSet<AdvanceMeasurement> advanceMeasurements) {
        this.advanceType = advanceType;
        this.advanceMeasurements = advanceMeasurements;
        this.advanceAssigment = advanceAssigment;

        this.maxValue = advanceAssigment.getMaxValue();
        this.reportGlobalAdvance = advanceAssigment.getReportGlobalAdvance();
        this.type = advanceAssigment.getType();

        this.isNewDTO = false;
        if(advanceAssigment.getVersion()==null){
            this.isNewObject = true;
        }else{
            this.isNewObject = false;
        }
    }

    public boolean getIsNewObject() {
        return this.isNewObject;
    }

    public boolean getIsNewDTO() {
        return this.isNewDTO;
    }

    public void setAdvanceType(AdvanceType advanceType){
        this.advanceType = advanceType;
    }

    public AdvanceType getAdvanceType() {
        return this.advanceType;
    }

    public AdvanceAssigment getAdvanceAssigment() {
        return this.advanceAssigment;
    }

    public void setAdvanceAssigment(AdvanceAssigment advanceAssigment) {
        this.advanceAssigment = advanceAssigment;
    }

    public SortedSet<AdvanceMeasurement> getAdvanceMeasurements() {
        return this.advanceMeasurements;
    }

    public void setAdvanceMeasurements(SortedSet<AdvanceMeasurement> advanceMeasurements) {
        this.advanceMeasurements = advanceMeasurements;
    }

    public ListSorter<AdvanceMeasurementDTO> getAdvanceMeasurementDTOs() {
        return this.advanceMeasurementDTOs;
    }

    public void setAdvanceMeasurementDTOs(ListSorter<AdvanceMeasurementDTO> advanceMeasurementDTOs) {
        this.advanceMeasurementDTOs = advanceMeasurementDTOs;
    }

    public void setReportGlobalAdvance(boolean reportGlobalAdvance) {
        this.reportGlobalAdvance = reportGlobalAdvance;
    }

    public boolean getReportGlobalAdvance() {
        return this.reportGlobalAdvance;
    }

    public BigDecimal getMaxValue(){
        return this.maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public AdvanceAssigment.Type getType(){
        return this.type;
    }

    public void setType(AdvanceAssigment.Type type) {
        this.type = type;
    }

    public boolean isSelectedForRemove(){
        return this.selectedRemove;
    }

    public void setSelectedForRemove(boolean selectedRemove){
        this.selectedRemove = selectedRemove;
    }
}
