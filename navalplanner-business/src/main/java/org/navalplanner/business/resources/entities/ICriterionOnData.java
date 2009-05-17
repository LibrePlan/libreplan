package org.navalplanner.business.resources.entities;

import java.util.Collection;
import java.util.Date;

/**
 * Expands the capabilities of {@link ICriterion}. It also lets retrieve the
 * resources satisfying the criterion <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionOnData extends ICriterion {

    Collection<Resource> getResourcesSatisfying();

    Collection<Resource> getResourcesSatisfying(Date start, Date end)
            throws IllegalArgumentException;

}
