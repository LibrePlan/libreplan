package org.navalplanner.business.resources.bootstrap;

import java.util.List;
import java.util.Map.Entry;

import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.services.CriterionService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Loads all {@link ICriterionTypeProvider} and if there is any criterion that
 * doesn't exist, creates them.<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope("singleton")
public class CriterionsBootstrap implements IDataBootstrap {

    @Autowired
    private CriterionService criterionService;
   
    @Autowired
    private List<ICriterionTypeProvider> providers;

    @Override
    public void loadRequiredData() {
        for (ICriterionTypeProvider provider: providers) {
            for (Entry<ICriterionType<?>, List<Criterion>> entry : provider
                    .getRequiredCriterions()
                    .entrySet()) {
                for (Criterion criterion : entry.getValue()) {
                    criterionService.createIfNotExists(criterion);
                }
            }
        }
    }   
}
