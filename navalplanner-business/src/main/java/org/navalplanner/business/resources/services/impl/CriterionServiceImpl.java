package org.navalplanner.business.resources.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.impl.CriterionDAO;
import org.navalplanner.business.resources.daos.impl.CriterionSatisfactionDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.services.CriterionService;
import org.navalplanner.business.resources.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link CriterionService} using {@link CriterionDAO} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Transactional
public class CriterionServiceImpl implements CriterionService {

    @Autowired
    private CriterionDAO criterionDAO;

    @Autowired
    private CriterionSatisfactionDAO criterionSatisfactionDAO;

    @Autowired
    private ResourceService resourceService;

    public boolean exists(Long id) {
        return criterionDAO.exists(id);
    }

    public Criterion find(Long id) throws InstanceNotFoundException {
        return criterionDAO.find(id);
    }

    public List<Criterion> list() {
        return criterionDAO.list(Criterion.class);
    }

    public void remove(Criterion criterion) throws InstanceNotFoundException {
        criterionDAO.remove(criterion.getId());
    }

    public void remove(Long id) throws InstanceNotFoundException {
        criterionDAO.remove(id);
    }

    public void save(Criterion entity) {
        criterionDAO.save(entity);
    }

    @Override
    public void add(CriterionSatisfaction criterionSatisfaction) {
        criterionSatisfactionDAO.save(criterionSatisfaction);
    }

    @Override
    public Collection<Resource> getResourcesSatisfying(ICriterion criterion) {
        List<Resource> resources = resourceService.getResources();
        ArrayList<Resource> result = new ArrayList<Resource>();
        for (Resource resource : resources) {
            if (criterion.isSatisfiedBy(resource)) {
                result.add(resource);
            }
        }
        return result;
    }

    @Override
    public Collection<Resource> getResourcesSatisfying(ICriterion criterion,
            Date start, Date end) {
        Validate.isTrue(start.before(end), "start must be before than end");
        List<Resource> resources = resourceService.getResources();
        ArrayList<Resource> result = new ArrayList<Resource>();
        for (Resource resource : resources) {
            if (criterion.isSatisfiedBy(resource, start, end)) {
                result.add(resource);
            }
        }
        return result;
    }

    @Override
    public Collection<CriterionSatisfaction> getSatisfactionsFor(
            ICriterionType criterionType) {
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (Resource resource : resourceService.getResources()) {
            result.addAll(resource.getActiveSatisfactionsFor(criterionType));
        }
        return result;
    }

    @Override
    public Collection<CriterionSatisfaction> getSatisfactionsFor(
            ICriterionType criterionType, Date start, Date end) {
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (Resource resource : resourceService.getResources()) {
            result.addAll(resource.getActiveSatisfactionsForIn(criterionType,
                    start, end));
        }
        return result;
    }
}
