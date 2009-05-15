package org.navalplanner.business.resources.entities;

import java.util.Date;

import org.apache.commons.lang.Validate;

/**
 * Declares a interval of time in which the criterion is satisfied <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionSatisfaction {

    private Long id;

    @SuppressWarnings("unused")
    private long version;

    /**
     * Required by hibernate. Do not use directly
     */
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
        this.resource.add(this);
    }

    public Long getId() {
        return id;
    }

    private Date startDate;

    private Date finishDate;

    private Criterion criterion;

    private Resource resource;

    public Date getStartDate() {
        return new Date(startDate.getTime());
    }

    public Date getEndDate() {
        return new Date(finishDate.getTime());
    }

    public Criterion getCriterion() {
        return criterion;
    }

    public Resource getResource() {
        return resource;
    }

    public boolean isActiveNow() {
        return startDate.before(new Date()) && finishDate == null;
    }

    public boolean isActiveIn(Date start, Date end) {
        return startDate.before(start)
                && (finishDate == null || end.before(finishDate));
    }

    public void finish(Date finish) {
        Validate.notNull(finish);
        Validate.isTrue(startDate.before(finish));
        finishDate = finish;
    }

    public boolean isFinished() {
        return finishDate != null;
    }
}
