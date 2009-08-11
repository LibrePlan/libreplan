package org.navalplanner.business.resources.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.CriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionOnData;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Interval;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ICriterionService} using {@link CriterionDAO} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional
public class CriterionServiceImpl implements ICriterionService {

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    public boolean exists(Criterion criterion) {
        return criterionDAO.exists(criterion.getId())
                || criterionDAO.existsByNameAndType(criterion);
    }

    public Criterion find(Criterion criterion) throws InstanceNotFoundException {
        return criterionDAO.find(criterion);
    }

    public List<Criterion> list() {
        return criterionDAO.list(Criterion.class);
    }

    public void remove(Criterion criterion) throws InstanceNotFoundException {
        if (criterion.getId() != null ) {
            criterionDAO.remove(criterion.getId());
        } else {
            criterionDAO.removeByNameAndType(criterion);
        }
    }

    @Transactional(rollbackFor=ValidationException.class)
    @Override
    public void save(Criterion entity) throws ValidationException {

        // Save criterion.type if it's new
        CriterionType criterionType = entity.getType();
        if (criterionType.getId() == null) {
            entity.setType(saveCriterionType(criterionType));
        }

        criterionDAO.save(entity);
    }

    private CriterionType saveCriterionType(CriterionType criterionType) throws ValidationException {
        if (criterionTypeDAO.exists(criterionType.getId())
                || criterionTypeDAO.existsByName(criterionType)) {
            try {
                criterionType = criterionTypeDAO
                        .findUniqueByName(criterionType.getName());
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            criterionTypeDAO.save(criterionType);
        }

        return criterionType;
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
            result.addAll(resource.getCurrentSatisfactionsFor(criterionType));
        }
        return result;
    }

    @Override
    public Collection<CriterionSatisfaction> getSatisfactionsFor(
            ICriterionType<?> criterionType, Date start, Date end) {
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (Resource resource : resourceService.getResources()) {
            result.addAll(resource.query().from(criterionType).enforcedInAll(
                    Interval.range(start, end)).result());
        }
        return result;
    }

    @Override
    public void createIfNotExists(Criterion criterion) throws ValidationException {
        if (!exists(criterion))
            save(criterion);
    }

    @Override
    public ICriterionOnData empower(final ICriterion criterion) {
        final ICriterionService criterionService = getProxifiedCriterionService();
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
    private ICriterionService getProxifiedCriterionService() {
        return (ICriterionService) applicationContext.getBeansOfType(
                ICriterionService.class).values().iterator().next();
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

    @Override
    public <T extends Resource> List<T> getResourcesSatisfying(
            Class<T> resourceType, Criterion criterion) {
        Validate.notNull(resourceType, "resourceType must be not null");
        Validate.notNull(criterion, "criterion must be not null");
        List<T> result = new ArrayList<T>();
        for (T r : resourceService.getResources(resourceType)) {
            if (criterion.isSatisfiedBy(r)) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public Criterion load(Criterion criterion) {
        try {
            return criterionDAO.find(criterion);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
