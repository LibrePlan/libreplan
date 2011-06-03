/*
 * This file is part of NavalPlan
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.web.resources.worker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.INewObject;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.Interval;

/**
 * DTO represents the handled data in the form of assigning satisfaction criterions.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CriterionSatisfactionDTO implements INewObject {

    public static List<CriterionSatisfactionDTO> keepHavingCriterion(
            Set<CriterionSatisfactionDTO> listDTOs) {
        List<CriterionSatisfactionDTO> result = new ArrayList<CriterionSatisfactionDTO>();
        for (CriterionSatisfactionDTO each : listDTOs) {
            if (each != null && each.getCriterionWithItsType() != null) {
                result.add(each);
            }
        }
        return result;
    }

    public static final String START_DATE = "startDate";

    public static final String CRITERION_WITH_ITS_TYPE = "criterionWithItsType";

    private String state;

    private String criterionAndType;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull
    private CriterionWithItsType criterionWithItsType;

    private Boolean isDeleted = false;

    private Boolean newObject = false;

    private CriterionSatisfaction criterionSatisfaction;

    public CriterionSatisfactionDTO(){
        this.setNewObject(true);
        this.state = "";
        this.criterionAndType = "";
        this.startDate = new LocalDate();
        this.endDate = null;
    }

    public CriterionSatisfactionDTO(CriterionSatisfaction criterionSatisfaction) {
        this.startDate = criterionSatisfaction.getStartDate();
        this.endDate = criterionSatisfaction.getEndDate();
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

    public void setNewObject(Boolean isNewObject) {
        this.newObject = isNewObject;
    }


    public Boolean isOldObject(){
        return !isNewObject();
    }

    public CriterionWithItsType getCriterionWithItsType() {
        return criterionWithItsType;
    }

    public void setCriterionWithItsType(CriterionWithItsType criterionWithItsType) {
        this.criterionWithItsType = criterionWithItsType;
    }

    public Date getStartDate() {
        return asDate(startDate);
    }

    public LocalDate getStart() {
        return startDate;
    }

    public LocalDate getEnd() {
        return endDate;
    }

    public Date getEndDate() {
        return asDate(endDate);
    }

    private Date asDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.toDateTimeAtStartOfDay().toDate();
    }

    public CriterionSatisfaction getCriterionSatisfaction() {
        return criterionSatisfaction;
    }

    public void setCriterionSatisfaction(CriterionSatisfaction criterionSatisfaction) {
        this.criterionSatisfaction = criterionSatisfaction;
    }

    public boolean isCurrent() {
        return ((!isFinished()) && (!isIncoming()));
    }

     public Interval getInterval() {
        return Interval.range(startDate, endDate);
    }

    private boolean isFinished() {
        return (endDate != null) ? getEndDate().compareTo(new Date()) < 0
                : false;
    }

    private boolean isIncoming() {
        return (startDate != null) ? getStartDate().compareTo(new Date()) > 0
                : false;
    }

    public void setStartDate(Date startDate) {
        this.startDate = asLocalDate(startDate);
    }

    public void setEndDate(Date endDate) {
        this.endDate = asLocalDate(endDate);
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
        if (getEndDate() == null || startDate == null) {
            return true;
        }
        if (startDate.compareTo(getEndDate()) < 0) {
            return true;
        }
        return false;
    }

    public boolean isPreviousStartDate(LocalDate startDate) {
        if (newObject) {
            return true;
        }
        if (getStartDate() == null || startDate == null) {
            return true;
        }
        if (startDate.compareTo(getCriterionSatisfaction().getStartDate()) <= 0) {
            return true;
        }
        return false;
    }

    public boolean isPreviousStartDate(Date value) {
        return isPreviousStartDate(asLocalDate(value));
    }

    public boolean isGreaterStartDate(Date endDate) {
        if (getStartDate() == null || endDate == null) {
            return true;
        }
        if (endDate.compareTo(getStartDate()) >= 0) {
            return true;
        }
        return false;
    }

    public boolean isPostEndDate(LocalDate endDate) {
        if (newObject) {
            return true;
        }
        if (getEndDate() == null || endDate == null) {
            return true;
        }
        if (getCriterionSatisfaction().getEndDate() == null) {
            return true;
        }
        if (endDate.compareTo(getCriterionSatisfaction().getEndDate()) >= 0) {
            return true;
        }
        return false;
    }

    public boolean isPostEndDate(Date value) {
        return isPostEndDate(asLocalDate(value));
    }

    private LocalDate asLocalDate(Date value) {
        if (value == null) {
            return null;
        }
        return LocalDate.fromDateFields(value);
    }

    public String getCriterionAndType() {
        if (criterionWithItsType == null) {
            return criterionAndType;
        }
        return criterionWithItsType.getNameAndType();

    }

    @Override
    public boolean isNewObject() {
        return newObject == null ? false : newObject;
    }

}
