package org.navalplanner.web.resources.search;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Conversation for worker search
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IWorkerSearchModel {

    /**
     * Gets all {@link Criterion} and groups then by {@link CriterionType}
     *
     * @return HashMap<CriterionType, Set<Criterion>>
     */
    HashMap<CriterionType, Set<Criterion>> getCriterions();

    /**
     * Queries database for retrieving all workers that match to the parameters
     *
     * @param name
     *            matches name/NIF of {@link Worker}
     * @param criterions
     *            {@link Worker} that complies all criterions
     * @return
     */
    @SuppressWarnings("unchecked")
    List<Worker> findWorkers(String name, List<Criterion> criterions);
}
