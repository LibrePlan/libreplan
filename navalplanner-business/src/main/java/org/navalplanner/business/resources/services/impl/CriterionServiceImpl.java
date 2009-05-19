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
import org.navalplanner.business.resources.entities.ICriterionOnData;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.services.CriterionService;
import org.navalplanner.business.resources.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

    public boolean exists(Criterion criterion) {
        return criterionDAO.exists(criterion);
    }

    public Criterion find(Criterion criterion) throws InstanceNotFoundException {
        return criterionDAO.find(criterion);
    }

    public List<Criterion> list() {
        return criterionDAO.list(Criterion.class);
    }

    public void remove(Criterion criterion) throws InstanceNotFoundException {
        criterionDAO.remove(criterion);
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
            ICriterionType<?> criterionType) {
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (Resource resource : resourceService.getResources()) {
            result.addAll(resource.getActiveSatisfactionsFor(criterionType));
        }
        return result;
    }

    @Override
    public Collection<CriterionSatisfaction> getSatisfactionsFor(
            ICriterionType<?> criterionType, Date start, Date end) {
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (Resource resource : resourceService.getResources()) {
            result.addAll(resource.getActiveSatisfactionsForIn(criterionType,
                    start, end));
        }
        return result;
    }

    @Override
    public void createIfNotExists(Criterion criterion) {
        if (!exists(criterion))
            save(criterion);
    }

    @Override
    public ICriterionOnData empower(final ICriterion criterion) {
        final CriterionService criterionService = getProxifiedCriterionService();
        return new ICriterionOnData() {
            @Override
            public boolean isSatisfiedBy(Resource resource) {
                return criterion.isSatisfiedBy(resource);
            }

            @Override
            public boolean isSatisfiedBy(Resource resource, Date start, Date end) {
                return criterion.isSatisfiedBy(resource, start, end);
            }

            @Override
            public Collection<Resource> getResourcesSatisfying() {
                return criterionService.getResourcesSatisfying(criterion);
            }

            @Override
            public Collection<Resource> getResourcesSatisfying(Date start,
                    Date end) throws IllegalArgumentException {
                return criterionService.getResourcesSatisfying(criterion,
                        start, end);
            }
        };
    }

    @Autowired
    private ApplicationContext applicationContext;

    // this is a hack to avoid using the this variable in empower method. The
    // this instance is not proxified because spring uses an transparent proxy,
    // so it doesn't open the transacion
    private CriterionService getProxifiedCriterionService() {
        return (CriterionService) applicationContext.getBeansOfType(
                CriterionService.class).values().iterator().next();
    }

    @Override
    public Collection<Criterion> getCriterionsFor(ICriterionType<?> type) {
        List<Criterion> list = criterionDAO.list(Criterion.class);
        ArrayList<Criterion> result = new ArrayList<Criterion>();
        for (Criterion criterion : list) {
            if (type.contains(criterion)) {
                result.add(criterion);
            }
        }
        return result;
    }
}
