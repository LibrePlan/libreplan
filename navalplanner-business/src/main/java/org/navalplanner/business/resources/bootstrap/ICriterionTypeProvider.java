package org.navalplanner.business.resources.bootstrap;

import java.util.List;
import java.util.Map;

import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ICriterionType;

/**
 * Defines a class that can provide some known {@link ICriterionType} along with
 * their associated Criterion <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface ICriterionTypeProvider {

    public Map<CriterionType, List<String>> getRequiredCriterions();

}
