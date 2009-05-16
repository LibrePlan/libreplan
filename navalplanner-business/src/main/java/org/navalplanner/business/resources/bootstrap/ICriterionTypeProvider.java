package org.navalplanner.business.resources.bootstrap;

import java.util.List;
import java.util.Map;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;

/**
 * Defines a class that can provide some known {@link ICriterionType} along with
 * their associated Criterion <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionTypeProvider {

    public Map<ICriterionType<?>, List<Criterion>> getRequiredCriterions();

}
