/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.resources.entities;

import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
/**
 * Declares a interval of time in which the criterion is satisfied <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionSatisfaction extends BaseEntity {

    public static final Comparator<CriterionSatisfaction> BY_START_COMPARATOR;

    static {
        BY_START_COMPARATOR = new Comparator<CriterionSatisfaction>() {

            @Override
            public int compare(CriterionSatisfaction o1,
                    CriterionSatisfaction o2) {
                return o1.getStartDate().compareTo(o2.getStartDate());
            }
        };
    }

    public static CriterionSatisfaction create() {
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction();
        criterionSatisfaction.setNewObject(true);
        return criterionSatisfaction;
    }

    public static CriterionSatisfaction create(Date startDate,
            Criterion criterion, Resource resource) {
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                startDate, criterion, resource);
        criterionSatisfaction.setNewObject(true);
        return criterionSatisfaction;
    }

    public static CriterionSatisfaction create(Criterion criterion,
            Resource resource, Interval interval) {
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(criterion, resource, interval);
        criterionSatisfaction.setNewObject(true);
        return criterionSatisfaction;
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public CriterionSatisfaction() {

    }

    private CriterionSatisfaction(Date startDate, Criterion criterion,
            Resource resource) {
        Validate.notNull(startDate, "startDate must be not null");
        Validate.notNull(criterion, "criterion must be not null");
        Validate.notNull(resource, "resource must be not null");
        this.startDate = startDate;
        this.criterion = criterion;
        this.resource = resource;
    }

    private CriterionSatisfaction(Criterion criterion, Resource resource,
            Interval interval) {
        this(interval.getStart(), criterion, resource);
        if (interval.getEnd() != null) {
            this.finish(interval.getEnd());
        }
    }

    @NotNull
    private Date startDate;

    private Date finishDate;

    private Criterion criterion;

    private Resource resource;

    private Boolean isDeleted = false;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public CriterionSatisfaction copy() {
        CriterionSatisfaction result = create();
        result.startDate = startDate;
        result.finishDate = finishDate;
        result.criterion = criterion;
        result.resource = resource;
        return result;
    }

    public Date getStartDate() {
        return startDate != null ? new Date(startDate.getTime()) : null;
    }

    public Date getEndDate() {
        if (isFinished()) {
            return new Date(finishDate.getTime());
        } else {
            return null;
        }
    }

    public Interval getInterval() {
        return Interval.range(startDate, finishDate);
    }

    public Criterion getCriterion() {
        return criterion;
    }

    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public boolean isCurrent() {
        Date now = new Date();
        return isEnforcedAt(now);
    }

    public boolean isEnforcedAt(Date date) {
        return getInterval().contains(date);
    }

    public boolean isAlwaysEnforcedIn(Interval interval) {
        return getInterval().includes(interval);
    }

    public void finish(Date finish) {
        Validate.isTrue(finish == null || getStartDate() == null
                || getStartDate().equals(finish) || getStartDate().before(finish));
        Validate.isTrue(finishDate == null || isNewObject() ||
                getEndDate().equals(finish) || getEndDate().before(finish));
        if(finish !=null) finish = new Date(finish.getTime());
        this.finishDate = finish;
    }

    public boolean isFinished() {
        return finishDate != null;
    }

    public void setEndDate(Date date) {
        if(date != null) finish(date);
        this.finishDate = date;
    }

    public void setStartDate(Date date) {
        if(date != null){
            Validate.isTrue(startDate == null || isNewObject() ||
                getStartDate().equals(date) || getStartDate().after(date));
        }
        startDate = date;
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

    public boolean goesBeforeWithoutOverlapping(CriterionSatisfaction other) {
        int compare = BY_START_COMPARATOR.compare(this, other);
        if (compare > 0) {
            return false;
        } else {
            Interval thisInterval = getInterval();
            return !thisInterval.overlapsWith(other.getInterval());
        }
    }

    public void validate(){
            Validate.notNull(resource);
            Validate.notNull(startDate);
            Validate.notNull(criterion);
            Validate.isTrue(finishDate == null || finishDate.after(startDate));
            Validate.isTrue(finishDate == null || startDate.equals(finishDate)
                    || startDate.before(finishDate));
    }
}
