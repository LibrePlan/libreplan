package org.navalplanner.business.resources.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads all {@link ICriterionTypeProvider} and if there is any criterion that
 * doesn't exist, creates them.<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Component
@Scope("singleton")
public class CriterionsBootstrap implements ICriterionsBootstrap {

    private static final Log LOG = LogFactory.getLog(CriterionsBootstrap.class);

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private List<ICriterionTypeProvider> providers;

    public CriterionsBootstrap() {
    }

    @Override
    @Transactional
    public void loadRequiredData() {
        Map<CriterionType, List<String>> typesWithCriterions = getTypesWithCriterions();

        // Insert predefined criterions
        for (Entry<CriterionType, List<String>> entry : typesWithCriterions
                .entrySet()) {
            CriterionType criterionType = retrieveOrCreate(entry.getKey());
            // Create predefined criterions for criterionType
            for (String criterionName : entry.getValue()) {
                ensureCriterionExists(criterionName, criterionType);
            }
        }
    }

    private void ensureCriterionExists(String criterionName,
            CriterionType criterionType) {
        Criterion criterion = new Criterion(criterionName, criterionType);
        if (!criterionDAO.existsByNameAndType(criterion)) {
            criterionDAO.save(criterion);
        }
    }

    private CriterionType retrieveOrCreate(CriterionType criterionType) {
        if (!criterionTypeDAO.exists(criterionType.getId())
                && !criterionTypeDAO.existsByName(criterionType)) {
            criterionTypeDAO.save(criterionType);
        } else {
            try {
                criterionType = criterionTypeDAO.findUniqueByName(criterionType
                        .getName());
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return criterionType;
    }

    private Map<CriterionType, List<String>> getTypesWithCriterions() {
        HashMap<CriterionType, List<String>> result = new HashMap<CriterionType, List<String>>();
        for (ICriterionTypeProvider provider : providers) {
            for (Entry<CriterionType, List<String>> entry : provider
                    .getRequiredCriterions().entrySet()) {
                if (!result.containsKey(entry.getKey())) {
                    result.put(entry.getKey(), new ArrayList<String>());
                }
                result.get(entry.getKey()).addAll(entry.getValue());
            }
        }
        return result;
    }
}
