/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.common.daos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.NonUniqueResultException;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.entities.EntityNameEnum;
import org.navalplanner.business.common.entities.EntitySequence;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.i18n.I18nHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link EntitySequence}.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EntitySequenceDAO extends
        GenericDAOHibernate<EntitySequence, Long> implements IEntitySequenceDAO {

    @Autowired
    private IAdHocTransactionService transactionService;

    @Override
    public List<EntitySequence> getAll() {
        return list(EntitySequence.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<EntitySequence> findEntitySquencesNotIn(
            List<EntitySequence> entitySequences) {
        List<Long> entitySequenceIds = new ArrayList<Long>();
        for (EntitySequence entitySequence : entitySequences) {
            if (!entitySequence.isNewObject()) {
                entitySequenceIds.add(entitySequence.getId());
            }
        }

        return getSession().createCriteria(EntitySequence.class).add(
                Restrictions.not(Restrictions.in("id", entitySequenceIds)))
                .list();
    }

    @Override
    public void remove(final EntitySequence entitySequence)
            throws InstanceNotFoundException, IllegalArgumentException {
        if (entitySequence.getLastValue() > 0) {
            throw new IllegalArgumentException(
                    I18nHelper
                            ._("You can not remove this order sequence, it is already in use"));
        }

        remove(entitySequence.getId());
    }

    @Override
    public EntitySequence getActiveEntitySequence(EntityNameEnum entityName)
            throws InstanceNotFoundException, NonUniqueResultException {
        return (EntitySequence) getSession().createCriteria(
                EntitySequence.class).add(
                Restrictions.eq("entityName", entityName)).add(
                Restrictions.eq("active", true)).uniqueResult();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getNextEntityCode(EntityNameEnum entityName) {
        return getNextEntityCodeWithoutTransaction(entityName);
    }

    public String getNextEntityCodeWithoutTransaction(EntityNameEnum entityName) {
        for (int i = 0; i < 5; i++) {
            try {
                String code;
                Integer cont = 0;
                EntitySequence entitySequence = getActiveEntitySequence(entityName);

                do {
                    entitySequence.incrementLastValue();
                    code = entitySequence.getCode();
                    cont++;
                } while (entityName.getIntegrationEntityDAO()
                        .existsByCode(code)
                        && cont < 100);

                save(entitySequence);
                return code;
            } catch (HibernateOptimisticLockingFailureException e) {
                // Do nothing (optimistic approach 5 attempts)
            } catch (InstanceNotFoundException e) {

            } catch (NonUniqueResultException e) {

            }
        }

        return null;
    }

    @Override
    public boolean existOtherActiveSequenceByEntityNameForNewObject(
            EntitySequence entitySequence) {
        Validate.notNull(entitySequence);
        try {
            EntitySequence t = getActiveEntitySequence(entitySequence
                    .getEntityName());
            return (t != null && t != entitySequence);
        } catch (InstanceNotFoundException e) {
            return false;
        } catch (NonUniqueResultException e) {
            return true;
        }
    }

    @Override
    public Integer getNumberOfDigitsCode(EntityNameEnum entityName) {
        int numberOfDigits = 5;
        try {
            EntitySequence entitySequence = getActiveEntitySequence(entityName);
            numberOfDigits = entitySequence.getNumberOfDigits();
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NonUniqueResultException e) {
            throw new RuntimeException(e);
        }
        return numberOfDigits;
    }
}