package org.navalplanner.business.resources.entities;

import java.util.Date;

/**
 * A criterion stored in the database <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Criterion implements ICriterion {

    private Long id;

    @SuppressWarnings("unused")
    private long version;

    private String type;

    private boolean active;

    public Long getId() {
        return id;
    }

    @Override
    public boolean isSatisfiedBy(Resource resource) {
        return !resource.getActiveSatisfactionsFor(this).isEmpty();
    }

    public boolean isSatisfiedBy(Resource resource, Date start, Date end) {
        return !resource.getActiveSatisfactionsForIn(this, start, end)
                .isEmpty();
    }

}
