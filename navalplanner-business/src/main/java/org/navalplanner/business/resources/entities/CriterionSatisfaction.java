package org.navalplanner.business.resources.entities;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ICriterionSatisfactionDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Declares a interval of time in which the criterion is satisfied <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionSatisfaction {

    private Long id;

    @SuppressWarnings("unused")
    private long version;

    @Autowired
    private ICriterionSatisfactionDAO criterionSatisfactionDAO;


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
    }

    public Long getId() {
        return id;
    }

    private Date startDate;

    private Date finishDate;

    private Criterion criterion;

    private Resource resource;

    public Date getStartDate() {
        return startDate==null ? null: new Date(startDate.getTime());
    }

    public Date getEndDate() {
        if (isFinished() ) {
            return new Date(finishDate.getTime());
        } else {
            return null;
        }
    }

    public Criterion getCriterion() {
        return criterion;
    }

    public Resource getResource() {
        return resource;
    }

    public boolean isActiveNow() {
        Date now = new Date();
        return isActiveAt(now);
    }

    public boolean isActiveAt(Date date) {
        return (startDate.before(date) || startDate.equals(date))
                && (finishDate == null || date.before(finishDate));
    }

    public boolean isActiveIn(Date start, Date end) {
        return (startDate.equals(start) || startDate.before(start))
                && (finishDate == null || end.before(finishDate));
    }

    public void finish(Date finish) {
        Validate.notNull(finish);
        Validate.isTrue(startDate.equals(finish) || startDate.before(finish));
        finishDate = finish;
    }

    public boolean isFinished() {
        return finishDate != null;
    }

    public void setEndDate(Date date) {
        if ( (startDate.equals(date) || startDate.before(date)) )
            finishDate = date;
    }

    public void setStartDate(Date date) {
        if ( (finishDate == null || finishDate.after(date)) )
            startDate = date;
    }

}
