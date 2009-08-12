package org.navalplanner.web.resources.criterion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.IResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for criterions. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component("criterionsModel")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CriterionsModel implements ICriterionsModel {

    private static final Log log = LogFactory.getLog(CriterionsModel.class);

    private ClassValidator<Criterion> criterionValidator = new ClassValidator<Criterion>(
            Criterion.class);

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IResourceService resourceService;

    private ICriterionType<?> criterionType;

    private Criterion criterion;

    @Override
    @Transactional(readOnly = true)
    public List<CriterionType> getTypes() {
        return criterionTypeDAO.getCriterionTypes();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Criterion> getCriterionsFor(ICriterionType<?> type) {
        return criterionDAO.findByType(type);
    }

    @Override
    public Criterion getCriterion() {
        return criterion;
    }

    @Override
    public void prepareForCreate(ICriterionType<?> criterionType) {
        this.criterionType = criterionType;
        this.criterion = (Criterion) criterionType
                .createCriterionWithoutNameYet();
    }

    @Override
    @Transactional(readOnly = true)
    public void workOn(Criterion criterion) {
        Validate.notNull(criterion);
        this.criterion = criterion;
        this.criterionType = getTypeFor(criterion);
    }

    @Override
    @Transactional(readOnly = true)
    public ICriterionType<?> getTypeFor(Criterion criterion) {
        for (ICriterionType<?> criterionType : getTypes()) {
            if (criterionType.contains(criterion))
                return criterionType;
        }
        throw new RuntimeException("not found type for criterion " + criterion);
    }

    @Override
    @Transactional
    public void saveCriterion() throws ValidationException {
        InvalidValue[] invalidValues = criterionValidator
                .getInvalidValues(criterion);
        if (invalidValues.length > 0)
            throw new ValidationException(invalidValues);
        try {
            save(criterion);
        } finally {
            criterion = null;
            criterionType = null;
        }
    }

    @Override
    @Transactional
    public void save(Criterion entity) throws ValidationException {
        if (thereIsOtherWithSameNameAndType(entity)) {
            InvalidValue[] invalidValues = { new InvalidValue(entity.getName()
                    + " already exists", Criterion.class, "name", entity
                    .getName(), entity) };
            throw new ValidationException(invalidValues,
                    "Couldn't save new criterion");
        }
        criterionDAO.save(entity);
    }

    private boolean thereIsOtherWithSameNameAndType(Criterion toSave) {
        List<Criterion> withSameNameAndType = criterionDAO
                .findByNameAndType(toSave);
        if (withSameNameAndType.isEmpty())
            return false;
        if (withSameNameAndType.size() > 1)
            return true;
        return !areSameInDB(withSameNameAndType.get(0), toSave);
    }

    private boolean areSameInDB(Criterion existentCriterion, Criterion other) {
        return existentCriterion.getId().equals(other.getId());
    }

    private CriterionType saveCriterionType(CriterionType criterionType)
            throws ValidationException {
        if (criterionTypeDAO.exists(criterionType.getId())
                || criterionTypeDAO.existsByName(criterionType)) {
            try {
                criterionType = criterionTypeDAO.findUniqueByName(criterionType
                        .getName());
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            criterionTypeDAO.save(criterionType);
        }

        return criterionType;
    }

    @Override
    public boolean isEditing() {
        return criterion != null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApplyableToWorkers(Criterion criterion) {
        ICriterionType<?> type = getTypeFor(criterion);
        return type != null && type.criterionCanBeRelatedTo(Worker.class);
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends Resource> List<T> getResourcesSatisfyingCurrentCriterionOfType(
            Class<T> klass) {
        if (criterion == null)
            return new ArrayList<T>();
        return getResourcesSatisfying(klass, criterion);
    }

    private <T extends Resource> List<T> getResourcesSatisfying(
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
    public List<Worker> getAllWorkers() {
        return resourceService.getWorkers();
    }

    @Override
    public boolean isChangeAssignmentsDisabled() {
        return criterionType == null
                || !criterionType.allowSimultaneousCriterionsPerResource();
    }

    @Override
    @Transactional
    public void activateAll(Collection<? extends Resource> resources) {
        for (Resource resource : resources) {
            Resource reloaded = find(resource.getId());
            reloaded.addSatisfaction(new CriterionWithItsType(criterionType,
                    criterion));
            resourceService.saveResource(reloaded);
        }
    }

    @Override
    @Transactional
    public void deactivateAll(Collection<? extends Resource> resources) {
        for (Resource resource : resources) {
            Resource reloaded = find(resource.getId());
            reloaded.finish(new CriterionWithItsType(criterionType, criterion));
            resourceService.saveResource(reloaded);
        }
    }

    private Resource find(Long id) {
        Resource reloaded;
        try {
            reloaded = resourceService.findResource(id);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        return reloaded;
    }

}
