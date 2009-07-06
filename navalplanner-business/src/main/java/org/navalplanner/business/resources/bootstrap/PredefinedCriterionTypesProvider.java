package org.navalplanner.business.resources.bootstrap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class provides the CriterionTypes with their criterions that are known a
 * priori<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Component
@Scope("singleton")
public class PredefinedCriterionTypesProvider implements ICriterionTypeProvider {

    public PredefinedCriterionTypesProvider() {
    }

    @Override
    public Map<CriterionType, List<String>> getRequiredCriterions() {
        Map<CriterionType, List<String>> result = new HashMap<CriterionType, List<String>>();
        for (PredefinedCriterionTypes type : PredefinedCriterionTypes.values()) {
            result.put(CriterionType.asCriterionType(type), type.getPredefined());
        }
        return result;
    }
}
