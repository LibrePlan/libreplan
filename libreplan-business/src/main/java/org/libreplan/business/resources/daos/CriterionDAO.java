/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.resources.daos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.ICriterionType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for Criterion. <br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CriterionDAO extends IntegrationEntityDAO<Criterion>
    implements ICriterionDAO {

    private static final Log log = LogFactory.getLog(CriterionDAO.class);

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean thereIsOtherWithSameNameAndType(Criterion criterion) {
        List<Criterion> withSameNameAndType = findByNameAndType(criterion);
        if (withSameNameAndType.isEmpty()) {
            return false;
        }
        if (withSameNameAndType.size() > 1) {
            return true;
        }
        return areDifferentInDB(withSameNameAndType.get(0), criterion);
    }

    private boolean areDifferentInDB(Criterion existentCriterion,
            Criterion other) {
        return !existentCriterion.getId().equals(other.getId());
    }

    @Override
    public List<Criterion> findByNameAndType(Criterion criterion) {
        if (criterion.getType() == null) {
            return new ArrayList<Criterion>();
        }
        return findByNameAndType(criterion.getName(), criterion.getType()
                .getName());
    }

    @Override
    public List<Criterion> findByNameAndType(String name, String type) {
        if ((name == null) || (type == null)) {
            return new ArrayList<Criterion>();
        }

        Criteria c = getSession().createCriteria(Criterion.class);
        c.add(Restrictions.eq("name", name).ignoreCase())
                .createCriteria("type").add(
                        Restrictions.eq("name", type).ignoreCase());

        return (List<Criterion>) c.list();
    }

    public Criterion findUniqueByNameAndType(Criterion criterion) throws InstanceNotFoundException {
        List<Criterion> list = findByNameAndType(criterion);

        if (list.size() != 1) {
            throw new InstanceNotFoundException(criterion, Criterion.class
                    .getName());
        }

        return list.get(0);
    }

    public boolean existsByNameAndType(Criterion criterion) {
        try {
            return findUniqueByNameAndType(criterion) != null;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean existsPredefinedCriterion(Criterion predefinedCriterion) {
        Validate.notNull(predefinedCriterion
                .getPredefinedCriterionInternalName());
        return existsByNameAndType(predefinedCriterion)
                || existsByInternalCode(predefinedCriterion);
    }

    private boolean existsByInternalCode(Criterion criterion) {
        Criteria c = getSession().createCriteria(Criterion.class);
        c.add(Restrictions.eq("predefinedCriterionInternalName",
                criterion.getPredefinedCriterionInternalName()).ignoreCase());
        return c.list().size() > 0;
    }

    @Override
    public Criterion find(Criterion criterion) throws InstanceNotFoundException {
        if (criterion.getId() != null) {
            return super.find(criterion.getId());
        }
        Criterion result = findUniqueByNameAndType(criterion);

        return result;
    }

    @Override
    public void removeByNameAndType(Criterion criterion) {
        try {
            Criterion reloaded = findUniqueByNameAndType(criterion);
            remove(reloaded.getId());
        } catch (InstanceNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Criterion> findByType(ICriterionType<?> type) {
        List<Criterion> list = list(Criterion.class);
        ArrayList<Criterion> result = new ArrayList<Criterion>();
        for (Criterion criterion : list) {
            if (type.contains(criterion)) {
                result.add(criterion);
            }
        }
        return result;
    }

    public List<Criterion> getAll() {
        return list(Criterion.class);
    }

    public List<Criterion> getAllSorted() {
        Criteria c = getSession().createCriteria(Criterion.class);
        c.addOrder(Order.asc("name"));
        return (List<Criterion>) c.list();
    }


    @Override
    public List<Criterion> getAllSortedByTypeAndName() {
        Query query = getSession()
                .createQuery(
                "select criterion from Criterion criterion "
                        + "JOIN criterion.type type "
                        + "order by type.name asc, criterion.name asc");
        return (List<Criterion>) query.list();
    }

    @Override
    public int numberOfRelatedRequirements(Criterion criterion) {
        Criteria c = getSession().createCriteria(CriterionRequirement.class)
                .add(Restrictions.eq("criterion", criterion)).setProjection(
                        Projections.rowCount());
        return Integer.valueOf(c.uniqueResult().toString()).intValue();
    }

    @Override
    public int numberOfRelatedSatisfactions(Criterion criterion) {
        Criteria c = getSession().createCriteria(CriterionSatisfaction.class)
                .add(Restrictions.eq("criterion", criterion)).setProjection(
                        Projections.rowCount());
        return Integer.valueOf(c.uniqueResult().toString()).intValue();
    }

}
