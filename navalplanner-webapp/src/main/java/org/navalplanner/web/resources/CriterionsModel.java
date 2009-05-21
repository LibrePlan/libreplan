package org.navalplanner.web.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.bootstrap.ICriterionsBootstrap;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.CriterionService;
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

    private ClassValidator<Criterion> criterionValidator = new ClassValidator<Criterion>(
            Criterion.class);

    @Autowired
    private ICriterionsBootstrap criterionsBootstrap;

    @Autowired
    private CriterionService criterionService;

    private ICriterionType<?> criterionType;

    private Criterion criterion;

    private String nameForCriterion;

    @Override
    @Transactional(readOnly = true)
    public List<ICriterionType<?>> getTypes() {
        return criterionsBootstrap.getTypes();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Criterion> getCriterionsFor(ICriterionType<?> type) {
        return criterionService.getCriterionsFor(type);
    }

    @Override
    public Criterion getCriterion() {
        return criterion;
    }

    @Override
    public void setNameForCriterion(String name) {
        this.nameForCriterion = name;
    }

    @Override
    public void prepareForCreate(ICriterionType<?> criterionType) {
        this.criterionType = criterionType;
        this.criterion = null;
    }

    @Override
    public void prepareForEdit(Criterion criterion) {
        Validate.notNull(criterion);
        this.criterion = criterion;
        this.criterionType = getTypeFor(criterion);
    }

    @Override
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
        if (criterionType != null) {
            create();
        } else {
            saveEdit();
        }
        criterion = null;
        criterionType = null;
    }

    private void saveEdit() {
        criterionService.save(criterion);
    }

    private void create() throws ValidationException {
        Criterion criterion = (Criterion) criterionType
                .createCriterion(nameForCriterion);
        InvalidValue[] invalidValues = criterionValidator
                .getInvalidValues(criterion);
        if (invalidValues.length > 0)
            throw new ValidationException(invalidValues);
        criterionService.save(criterion);
    }

    @Override
    public String getNameForCriterion() {
        if (criterion == null) {
            return "";
        }
        return criterion.getName();
    }

    @Override
    public boolean isCriterionActive() {
        if (criterion == null)
            return false;
        return criterion.isActive();
    }

    @Override
    public boolean isEditing() {
        return criterion != null;
    }

    @Override
    public void setCriterionActive(boolean active) {
        criterion.setActive(active);
    }

    @Override
    public boolean isApplyableToWorkers() {
        return criterionType != null
                && criterionType.criterionCanBeRelatedTo(Worker.class);
    }

    @Override
    public <T extends Resource> List<T> getResourcesSatisfyingCurrentCriterionOfType(
            Class<T> klass) {
        if (criterion == null)
            return new ArrayList<T>();
        return criterionService.getResourcesSatisfying(klass, criterion);
    }
}
