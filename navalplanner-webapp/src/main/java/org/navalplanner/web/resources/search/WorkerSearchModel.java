package org.navalplanner.web.resources.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class WorkerSearchModel implements IWorkerSearchModel {

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Override
    @Transactional(readOnly = true)
    public HashMap<CriterionType, Set<Criterion>> getCriterions() {
        HashMap<CriterionType, Set<Criterion>> result = new HashMap<CriterionType, Set<Criterion>>();

        List<Criterion> criterions = criterionDAO.getAll();
        for (Criterion criterion : criterions) {
            CriterionType key = criterion.getType();
            Set<Criterion> values = (!result.containsKey(key)) ?
                    new HashSet<Criterion>() : (Set<Criterion>) result.get(key);
            values.add(criterion);
            result.put(key, values);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> findWorkers(String name, List<Criterion> criterions) {
        return workerDAO.findByNameAndCriterions(name, criterions);
    }
}
