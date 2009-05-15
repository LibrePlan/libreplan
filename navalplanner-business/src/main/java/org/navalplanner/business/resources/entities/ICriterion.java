package org.navalplanner.business.resources.entities;

import java.util.Date;

/**
 * Responsible of searching the resources satisfiying some condition or set of
 * conditions. <br />
 * Created at May 12, 2009
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterion {

    boolean isSatisfiedBy(Resource resource);

    boolean isSatisfiedBy(Resource resource, Date start, Date end);

}
