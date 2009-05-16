package org.navalplanner.business.resources.bootstrap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class provides the CriterionTypes with their criterions that are known a
 * priori<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope("singleton")
public class PredefinedCriterionTypesProvider implements ICriterionTypeProvider {

    public PredefinedCriterionTypesProvider() {
    }

    @Override
    public Map<ICriterionType<?>, List<Criterion>> getRequiredCriterions() {
        Map<ICriterionType<?>, List<Criterion>> result = new HashMap<ICriterionType<?>, List<Criterion>>();
        for (PredefinedCriterionTypes type : PredefinedCriterionTypes.values()) {
            result.put(type, type.getPredefined());
        }
        return result;
    }

}
