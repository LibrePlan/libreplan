package org.navalplanner.business.resources.services;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;

/**
 * Services for aggregate {@link Criterion} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface CriterionService {

    List<Criterion> list();

    void remove(Criterion criterion) throws InstanceNotFoundException;

    void save(Criterion entity);

    void add(CriterionSatisfaction criterionSatisfaction);

    Collection<Resource> getResourcesSatisfying(ICriterion criterion);

    Collection<Resource> getResourcesSatisfying(ICriterion criterion,
            Date begin, Date end);

    Collection<CriterionSatisfaction> getSatisfactionsFor(
            ICriterionType<?> criterionType);

    Collection<CriterionSatisfaction> getSatisfactionsFor(
            ICriterionType<?> criterionType, Date begin, Date end);

    void createIfNotExists(Criterion criterion);

    boolean exists(Criterion criterion);

}
