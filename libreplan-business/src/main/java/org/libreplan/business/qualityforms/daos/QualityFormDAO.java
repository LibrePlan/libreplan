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

package org.libreplan.business.qualityforms.daos;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.advance.daos.IAdvanceTypeDAO;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.qualityforms.entities.QualityFormType;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IAdvanceTypeDAO advanceTypeDAO;

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

    @Override
    public void save(QualityForm entity) throws ValidationException {
        if (entity.isReportAdvance()) {
            String name = QualityForm.ADVANCE_TYPE_PREFIX + entity.getName();

            AdvanceType advanceType = entity.getAdvanceType();
            if (advanceType != null) {
                advanceTypeDAO.save(advanceType);
                advanceType.setUnitName(name);
            } else {
                advanceType = AdvanceType.create(name, new BigDecimal(100),
                        false, new BigDecimal(0.01), true, true, true);
                advanceTypeDAO.save(advanceType);

                entity.setAdvanceType(advanceType);
            }
        }

        super.save(entity);
    }

    @Override
    public void checkHasTasks(QualityForm qualityForm) throws ValidationException {
        Query query = getSession().createQuery(
                "FROM TaskQualityForm taskQualityForm JOIN taskQualityForm.qualityForm tq WHERE tq IN (:qualityForms)");
        query.setParameterList("qualityForms", Collections.singleton(qualityForm));
        if (!query.list().isEmpty()) {
            throw ValidationException
                    .invalidValue(
                            "Cannot delete quality form. It is being used at this moment by some task.",
                            qualityForm);
        }
    }
}
