package org.navalplanner.business.resources.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.services.ICriterionService;
import org.navalplanner.business.resources.services.ICriterionTypeService;
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
    private ICriterionService criterionService;

    @Autowired
    private ICriterionTypeService criterionTypeService;

    @Autowired
    private List<ICriterionTypeProvider> providers;

    public CriterionsBootstrap() {
    }

    @Override
    @Transactional
    public void loadRequiredData() {
        LOG.debug("### loadRequiredData()");

        Map<CriterionType, List<String>> typesWithCriterions = getTypesWithCriterions();

        // Insert predefined criterions
        for (Entry<CriterionType, List<String>> entry :
                    typesWithCriterions.entrySet()) {
            // Create PredefinedCriterionType
            CriterionType criterionType = entry.getKey();
            try {
                criterionTypeService.createIfNotExists(criterionType);
            } catch (ValidationException e) {

            }
            // Retrieve existing criterionType if not exists
            if (criterionType.getId() == null) {
                criterionType = criterionTypeService.findUniqueByName(criterionType.getName());
            }

            // Create predefined criterions for criterionType
            for (String criterionName : entry.getValue()) {
                try {
                    Criterion criterion = new Criterion(criterionName, criterionType);
                    criterionService.createIfNotExists(criterion);

                    LOG.debug("### Create criterion: ("  + criterionName +
                            "; " + criterionType.getName());
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
            }
        }
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
