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

package org.navalplanner.business.resources.entities;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ICriterionSatisfactionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
/**
 * Declares a interval of time in which the criterion is satisfied <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class CriterionSatisfaction extends IntegrationEntity {

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
        return create(new CriterionSatisfaction());
    }

    public static CriterionSatisfaction create(LocalDate startDate,
            Criterion criterion, Resource resource) {
        return create(
            new CriterionSatisfaction(startDate, criterion, resource));
    }

    public static CriterionSatisfaction create(Criterion criterion,
            Resource resource, Interval interval) {

        return create(new CriterionSatisfaction(criterion, resource, interval));

    }

    /**
     * @throws InstanceNotFoundException if criterion type or criterion does
     *         not exist
     */
    public static CriterionSatisfaction createUnvalidated(String code,
            String criterionTypeName, String criterionName, Resource resource,
            LocalDate startDate, LocalDate finishDate)
            throws InstanceNotFoundException {

        ICriterionTypeDAO criterionTypeDAO =
            Registry.getCriterionTypeDAO();

        /* Get CriterionType. */
        CriterionType criterionType = criterionTypeDAO.findUniqueByName(
            criterionTypeName);

        /* Get Criterion. */
        Criterion criterion = criterionType.getCriterion(
            criterionName);

        /* Create instance of CriterionSatisfaction. */
        CriterionSatisfaction criterionSatisfaction =
            create(new CriterionSatisfaction(), code);

        criterionSatisfaction.criterion = criterion;
        criterionSatisfaction.resource = resource;
        criterionSatisfaction.startDate = startDate;
        criterionSatisfaction.finishDate = finishDate;

        return criterionSatisfaction;

    }

    /**
     * @throws InstanceNotFoundException if criterion type or criterion does
     *         not exist
     */
    public void updateUnvalidated(String criterionTypeName,
            String criterionName, LocalDate startDate, LocalDate finishDate)
        throws InstanceNotFoundException {

        CriterionType criterionType = null;

        if (StringUtils.isBlank(criterionTypeName)) {
            criterionType = criterion.getType();
        } else {
            criterionType = Registry.getCriterionTypeDAO().findUniqueByName(
                criterionTypeName);
        }

        String newCriterionName = null;

        if (StringUtils.isBlank(criterionName)) {
            newCriterionName = StringUtils.trim(criterion.getName());
        } else {
            newCriterionName = criterionName;
        }

        this.criterion = criterionType.getCriterion(newCriterionName);

        if (startDate != null) {
            this.startDate = startDate;
        }

        if (finishDate != null) {
            this.finishDate = finishDate;
        }

    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public CriterionSatisfaction() {

    }

    private CriterionSatisfaction(LocalDate startDate, Criterion criterion,
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

    private LocalDate startDate;

    private LocalDate finishDate;

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

    @NotNull(message="criterion satisfaction's start date not specified")
    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return finishDate;
    }

    public Interval getInterval() {
        return Interval.range(startDate, finishDate);
    }

    @NotNull(message="criterion satisfaction's criterion not specified")
    public Criterion getCriterion() {
        return criterion;
    }

    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }

    @NotNull(message="criterion satisfaction's resource not specified")
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public boolean isCurrent() {
        LocalDate today = new LocalDate();
        return isEnforcedAt(today);
    }

    public boolean isEnforcedAt(LocalDate date) {
        return getInterval().contains(date);
    }

    public boolean isAlwaysEnforcedIn(Interval interval) {
        return getInterval().includes(interval);
    }

    public void finish(LocalDate finish) {
        Validate.notNull(finish);
        Validate.isTrue(getStartDate() == null
                || getStartDate().compareTo(finish) <= 0);
        Validate.isTrue(finishDate == null || isNewObject()
                || getEndDate().equals(finish) || getEndDate().isBefore(finish));
        this.finishDate = finish;
    }

    public void noFinish() {
        this.finishDate = null;
    }

    public boolean isFinished() {
        return finishDate != null;
    }

    public void setEndDate(LocalDate date) {
        if (date != null) {
            finish(date);
        }
        this.finishDate = date;
    }

    public void setStartDate(LocalDate date) {
        if(date != null){
            Validate.isTrue(startDate == null || isNewObject()
                    || getStartDate().equals(date)
                    || getStartDate().isAfter(date));
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

    public ResourceEnum getResourceType() {
        return criterion.getType().getResource();
    }

    @AssertTrue(message="criterion satisfaction with end date less than start " +
        "date")
    public boolean checkConstraintPositiveTimeInterval() {

        /* Check if it makes sense to check the constraint .*/
        if (!isStartDateSpecified()) {
            return true;
        }

        /* Check the constraint. */
        if (finishDate == null) {
            return true;
        }

        return (finishDate.isAfter(startDate) || startDate.equals(finishDate));

    }

    public boolean isStartDateSpecified() {
        return startDate != null;
    }

    @Override
    protected ICriterionSatisfactionDAO getIntegrationEntityDAO() {
        return Registry.getCriterionSatisfactionDAO();
    }

}
