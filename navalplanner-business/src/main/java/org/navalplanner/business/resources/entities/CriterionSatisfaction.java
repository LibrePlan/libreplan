package org.navalplanner.business.resources.entities;

import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang.Validate;

/**
 * Declares a interval of time in which the criterion is satisfied <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionSatisfaction {

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

    private Long id;

    @SuppressWarnings("unused")
    private long version;

    public CriterionSatisfaction() {

    }

    public CriterionSatisfaction(Date startDate, Criterion criterion,
            Resource resource) {
        Validate.notNull(startDate, "startDate must be not null");
        Validate.notNull(criterion, "criterion must be not null");
        Validate.notNull(resource, "resource must be not null");
        this.startDate = startDate;
        this.criterion = criterion;
        this.resource = resource;
    }

    public CriterionSatisfaction(Criterion criterion, Resource resource,
            Interval interval) {
        this(interval.getStart(), criterion, resource);
        if (interval.getEnd() != null) {
            this.finish(interval.getEnd());
        }
    }

    public Long getId() {
        return id;
    }

    private Date startDate;

    private Date finishDate;

    private Criterion criterion;

    private Resource resource;

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
        Validate.notNull(finish);
        Validate.isTrue(startDate.equals(finish) || startDate.before(finish));
        this.finishDate = finish;
    }

    public boolean isFinished() {
        return finishDate != null;
    }

    public void setEndDate(Date date) {
        if (date == null) {
            finishDate = null;
        }
        if ((startDate.equals(date) || startDate.before(date)))
            finishDate = date;
    }

    public void setStartDate(Date date) {
        if ((finishDate == null || finishDate.after(date)))
            startDate = date;
    }

    public boolean overlapsWith(Interval interval) {
        return getInterval().overlapsWith(interval);
    }

}
