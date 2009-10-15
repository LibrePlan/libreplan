/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.web.resources.worker;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.Interval;

/**
 * DTO represents the handled data in the form of assigning satisfaction criterions.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CriterionSatisfactionDTO {

    public static final String START_DATE = "startDate";

    public static final String CRITERION_WITH_ITS_TYPE = "criterionWithItsType";

    private String state;

    private String criterionAndType;

    @NotNull
    private Date startDate;

    private Date endDate;

    @NotNull
    private CriterionWithItsType criterionWithItsType;

    private Boolean isDeleted = false;

    private Boolean isNewObject = false;

    private CriterionSatisfaction criterionSatisfaction;

    public CriterionSatisfactionDTO(){
        this.setIsNewObject(true);
        this.state = "";
        this.criterionAndType = "";
        this.startDate =  new Date();
        this.endDate = null;
    }

    public CriterionSatisfactionDTO(CriterionSatisfaction criterionSatisfaction) {
        this.setStartDate(criterionSatisfaction.getStartDate());
        this.setEndDate(criterionSatisfaction.getEndDate());
        this.state = "";
        this.criterionAndType = "";
        this.setCriterionSatisfaction(criterionSatisfaction);

        Criterion criterion = criterionSatisfaction.getCriterion();
        CriterionType type = criterion.getType();
        this.setCriterionWithItsType(new CriterionWithItsType(type,criterion));
    }

    public void setCriterionAndType(String criterionAndType) {
        this.criterionAndType = criterionAndType;
    }

    public String getState() {
        if(startDate == null) return "";
        if( !isFinished() || isCurrent() ) return "Current";
        return "Expired";
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setIsNewObject(Boolean isNewObject) {
        this.isNewObject = isNewObject;
    }

    public Boolean isIsNewObject() {
        return isNewObject == null ? false : isNewObject;
    }

    public Boolean isIsOldObject(){
        return !isIsNewObject();
    }

    public CriterionWithItsType getCriterionWithItsType() {
        return criterionWithItsType;
    }

    public void setCriterionWithItsType(CriterionWithItsType criterionWithItsType) {
        this.criterionWithItsType = criterionWithItsType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public CriterionSatisfaction getCriterionSatisfaction() {
        return criterionSatisfaction;
    }

    public void setCriterionSatisfaction(CriterionSatisfaction criterionSatisfaction) {
        this.criterionSatisfaction = criterionSatisfaction;
    }

    public boolean isCurrent() {
        Date now = new Date();
        if(!isFinished()) return true;
        return (now.compareTo(getEndDate()) <= 0);
    }

     public Interval getInterval() {
        return Interval.range(startDate, endDate);
    }

    public boolean isFinished() {
        return endDate != null;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isIsDeleted() {
        return isDeleted == null ? false : isDeleted;
    }


    public boolean overlapsWith(Interval interval) {
        return getInterval().overlapsWith(interval);
    }

    public boolean isLessToEndDate(Date startDate){
        if((getEndDate() == null) ||
                (startDate == null)) return true;
        if(startDate.compareTo(getEndDate()) < 0) return true;
        return false;
    }

    public boolean isPreviousStartDate(Date startDate){
        if(isNewObject) return true;
        if((getStartDate() == null) ||
                (startDate == null)) return true;
        if(startDate.compareTo(getCriterionSatisfaction().getStartDate()) <= 0)
            return true;
        return false;
    }

    public boolean isGreaterStartDate(Date endDate){
        if((getStartDate() == null) ||
                (endDate == null)) return true;
        if(endDate.compareTo(getStartDate()) >= 0) return true;
        return false;
    }

    public boolean isPostEndDate(Date endDate){
        if(isNewObject) return true;
        if((getEndDate() == null) ||
                (endDate == null)) return true;
        if(getCriterionSatisfaction().getEndDate() == null)
            return true;
        if(endDate.compareTo(getCriterionSatisfaction().getEndDate()) >= 0)
            return true;
        return false;
    }

    public String getCriterionAndType() {
        if(criterionWithItsType == null) return criterionAndType;
        return criterionWithItsType.getNameAndType();

    }
}
