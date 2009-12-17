/*
 * This file is part of ###PROJECT_NAME###
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

package org.navalplanner.business.qualityforms.daos;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.NonUniqueResultException;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.QualityFormType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link QualityForm}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class QualityFormDAO extends GenericDAOHibernate<QualityForm, Long>
        implements IQualityFormDAO {

    @Override
    public List<QualityForm> getAll() {
        return list(QualityForm.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean isUnique(QualityForm qualityForm) {
        try {
            QualityForm result = findUniqueByName(qualityForm);
            return (result == null || result.getId()
                    .equals(qualityForm.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public QualityForm findByNameAndType(String name, QualityFormType type) {
        return (QualityForm) getSession().createCriteria(QualityForm.class)
                .add(Restrictions.eq("name", name)).add(
                        Restrictions.eq("qualityFormType", type))
                .uniqueResult();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<QualityForm> getAllByType(QualityFormType type) {
        Criteria c = getSession().createCriteria(QualityForm.class).add(
                Restrictions.eq("qualityFormType", type));
        return ((List<QualityForm>) c.list());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public QualityForm findUniqueByName(QualityForm qualityForm)
            throws InstanceNotFoundException {
        Validate.notNull(qualityForm);
        return findUniqueByName(qualityForm.getName());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public QualityForm findUniqueByName(String name)
            throws InstanceNotFoundException, NonUniqueResultException {
        Criteria c = getSession().createCriteria(QualityForm.class);
        c.add(Restrictions.eq("name", name));
        QualityForm qualityForm = (QualityForm) c.uniqueResult();

        if (qualityForm == null) {
            throw new InstanceNotFoundException(null, "QualityForm");
        }
        return qualityForm;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean existsOtherWorkReportTypeByName(QualityForm qualityForm) {
        try {
            QualityForm t = findUniqueByName(qualityForm);
            return (t != null && t != qualityForm);
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean existsByNameAnotherTransaction(QualityForm qualityForm) {
        return existsOtherWorkReportTypeByName(qualityForm);
    }

}
