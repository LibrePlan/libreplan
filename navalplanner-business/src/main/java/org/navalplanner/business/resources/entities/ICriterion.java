package org.navalplanner.business.resources.entities;

import java.util.Date;

/**
 * It's a predicate that can be applied on resources <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterion {

    boolean isSatisfiedBy(Resource resource);

    boolean isSatisfiedBy(Resource resource, Date start, Date end);

}
