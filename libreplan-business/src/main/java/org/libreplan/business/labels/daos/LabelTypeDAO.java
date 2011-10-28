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

package org.libreplan.business.labels.daos;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.NonUniqueResultException;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.labels.entities.LabelType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link LabelType}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LabelTypeDAO extends IntegrationEntityDAO<LabelType> implements
        ILabelTypeDAO {

    @Override
    public List<LabelType> getAll() {
        return list(LabelType.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean isUnique(LabelType labelType) {
        try {
            LabelType result = findUniqueByName(labelType);
            return (result == null || result.getId().equals(labelType.getId()));
        } catch (InstanceNotFoundException e) {
            return true;
        } catch (NonUniqueResultException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsByName(LabelType labelType) {
        try {
            return findUniqueByName(labelType) != null;
        } catch (InstanceNotFoundException e) {
            return false;
        } catch (NonUniqueResultException e) {
            return true;
        }
    }

    private LabelType findUniqueByName(LabelType labelType)
            throws InstanceNotFoundException, NonUniqueResultException {
        Validate.notNull(labelType);

        return findUniqueByName(labelType.getName());
    }

    @Override
    public LabelType findUniqueByName(String name)
            throws InstanceNotFoundException, NonUniqueResultException {
        Criteria c = getSession().createCriteria(LabelType.class);
        c.add(Restrictions.eq("name", name));
        LabelType labelType = (LabelType) c.uniqueResult();

        if (labelType == null) {
            throw new InstanceNotFoundException(null, "LabelType");
        }
        return labelType;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByNameAnotherTransaction(LabelType labelType) {
        return existsByName(labelType);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public LabelType findUniqueByNameAnotherTransaction(String name)
            throws InstanceNotFoundException {
        return findUniqueByName(name);
    }

}
