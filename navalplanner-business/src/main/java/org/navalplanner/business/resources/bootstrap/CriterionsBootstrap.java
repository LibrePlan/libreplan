package org.navalplanner.business.resources.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.services.CriterionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads all {@link ICriterionTypeProvider} and if there is any criterion that
 * doesn't exist, creates them.<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope("singleton")
public class CriterionsBootstrap implements ICriterionsBootstrap {

    @Autowired
    private CriterionService criterionService;

    @Autowired
    private List<ICriterionTypeProvider> providers;

    public CriterionsBootstrap() {
    }

    public List<ICriterionType<?>> getTypes() {
        ArrayList<ICriterionType<?>> result = new ArrayList<ICriterionType<?>>();
        for (ICriterionTypeProvider provider : providers) {
            result.addAll(provider.getRequiredCriterions().keySet());
        }
        return result;
    }

    @Override
    @Transactional
    public void loadRequiredData() {
        Map<ICriterionType<?>, List<Criterion>> typesWithCriterions = getTypesWithCriterions();
        for (Entry<ICriterionType<?>, List<Criterion>> entry : typesWithCriterions
                .entrySet()) {
            for (Criterion criterion : entry.getValue()) {
                criterionService.createIfNotExists(criterion);
            }
        }
    }

    private Map<ICriterionType<?>, List<Criterion>> getTypesWithCriterions() {
        HashMap<ICriterionType<?>, List<Criterion>> result = new HashMap<ICriterionType<?>, List<Criterion>>();
        for (ICriterionTypeProvider provider : providers) {
            for (Entry<ICriterionType<?>, List<Criterion>> entry : provider
                    .getRequiredCriterions().entrySet()) {
                if (!result.containsKey(entry.getKey())) {
                    result.put(entry.getKey(), new ArrayList<Criterion>());
                }
                result.get(entry.getKey()).addAll(entry.getValue());
            }
        }
        return result;
    }
}
