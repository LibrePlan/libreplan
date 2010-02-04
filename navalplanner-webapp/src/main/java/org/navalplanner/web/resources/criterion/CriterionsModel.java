/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.resources.criterion;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
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

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IResourceDAO resourceDAO;

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
        for (ICriterionType<?> each : getTypes()) {
            if (each.contains(criterion)) {
                return each;
            }
        }
        throw new RuntimeException(_("{0} not found type for criterion ", criterion));
    }

    @Override
    @Transactional
    public void saveCriterion() throws ValidationException {
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
        if (criterionDAO.thereIsOtherWithSameNameAndType(entity)) {
            InvalidValue[] invalidValues = {
                new InvalidValue(
                    _("{0} already exists", entity.getName()),
                    Criterion.class, "name",
                    entity.getName(), entity
                )};
            throw new ValidationException(invalidValues,
                        _("Could not save new criterion"));
        }
        criterionDAO.save(entity);
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
        if (criterion == null) {
            return new ArrayList<T>();
        }
        return getResourcesSatisfying(klass, criterion);
    }

    private <T extends Resource> List<T> getResourcesSatisfying(
            Class<T> resourceType, Criterion criterion) {
        Validate.notNull(resourceType, _("ResourceType must be not-null"));
        Validate.notNull(criterion, _("Criterion must be not-null"));
        List<T> result = new ArrayList<T>();
        for (T r : resourceDAO.list(resourceType)) {
            if (criterion.isSatisfiedBy(r)) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> getAllWorkers() {
        return resourceDAO.getWorkers();
    }

    @Override
    public boolean isChangeAssignmentsDisabled() {
        return criterionType == null
                || !criterionType.isAllowSimultaneousCriterionsPerResource();
    }

    @Override
    @Transactional
    public void activateAll(Collection<? extends Resource> resources) {
        for (Resource resource : resources) {
            Resource reloaded = find(resource.getId());
            reloaded
                    .addSatisfaction(new CriterionWithItsType(criterionType, criterion));
            resourceDAO.save(reloaded);
        }
    }

    @Override
    @Transactional
    public void deactivateAll(Collection<? extends Resource> resources) {
        for (Resource resource : resources) {
            Resource reloaded = find(resource.getId());
            reloaded.finish(new CriterionWithItsType(criterionType,
                    criterion));
            resourceDAO.save(reloaded);
        }
    }

    private Resource find(Long id) {
        Resource reloaded;
        try {
            reloaded = resourceDAO.find(id);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        return reloaded;
    }

}
