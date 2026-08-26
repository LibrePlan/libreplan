/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

package org.libreplan.business.externalcompanies.daos;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate DAO for {@link ExternalCompany}.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ExternalCompanyDAO extends GenericDAOHibernate<ExternalCompany, Long> implements IExternalCompanyDAO {

    @Override
    public boolean existsByName(String name) {
        try {
            findUniqueByName(name);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByNameInAnotherTransaction(String name) {
        return existsByName(name);
    }

    @Override
    public ExternalCompany findUniqueByName(String name) throws InstanceNotFoundException {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<ExternalCompany> cq = cb.createQuery(ExternalCompany.class);
        Root<ExternalCompany> root = cq.from(ExternalCompany.class);
        cq.where(cb.equal(root.get("name"), name));

        ExternalCompany found = getSession().createQuery(cq).uniqueResult();
        if (found == null) {
            throw new InstanceNotFoundException(name, ExternalCompany.class.getName());
        }

        return found;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public ExternalCompany findUniqueByNameInAnotherTransaction(String name) throws InstanceNotFoundException {
        return findUniqueByName(name);
    }

    @Override
    public boolean existsByNif(String nif) {
        try {
            findUniqueByNif(nif);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByNifInAnotherTransaction(String nif) {
        return existsByNif(nif);
    }

    @Override
    public ExternalCompany findUniqueByNif(String nif) throws InstanceNotFoundException {

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<ExternalCompany> cq = cb.createQuery(ExternalCompany.class);
        Root<ExternalCompany> root = cq.from(ExternalCompany.class);
        cq.where(cb.equal(root.get("nif"), nif));
        ExternalCompany found = getSession().createQuery(cq).uniqueResult();

        if (found == null) {
            throw new InstanceNotFoundException(nif, ExternalCompany.class.getName());
        }

        return found;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public ExternalCompany findUniqueByNifInAnotherTransaction(String nif) throws InstanceNotFoundException {
        return findUniqueByNif(nif);
    }

    @Override
    public List<ExternalCompany> findSubcontractor() {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<ExternalCompany> cq = cb.createQuery(ExternalCompany.class);
        Root<ExternalCompany> root = cq.from(ExternalCompany.class);
        cq.where(cb.equal(root.get("subcontractor"), true));
        return getSession().createQuery(cq).getResultList();
    }

    public List<ExternalCompany> getAll() {
        return list(ExternalCompany.class);
    }

    @Override
    public List<ExternalCompany> getExternalCompaniesAreClient() {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<ExternalCompany> cq = cb.createQuery(ExternalCompany.class);
        Root<ExternalCompany> root = cq.from(ExternalCompany.class);
        cq.where(cb.equal(root.get("client"), true));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public boolean isAlreadyInUse(ExternalCompany company) {
        if (company.isNewObject()) {
            return false;
        }

        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        CriteriaQuery<Order> orderCq = cb.createQuery(Order.class);
        Root<Order> orderRoot = orderCq.from(Order.class);
        orderCq.where(cb.equal(orderRoot.get("customer"), company));
        boolean usedInOrders = !getSession().createQuery(orderCq).getResultList().isEmpty();

        CriteriaQuery<SubcontractedTaskData> taskDataCq = cb.createQuery(SubcontractedTaskData.class);
        Root<SubcontractedTaskData> taskDataRoot = taskDataCq.from(SubcontractedTaskData.class);
        taskDataCq.where(cb.equal(taskDataRoot.get("externalCompany"), company));
        boolean usedInSubcontractedTask = !getSession().createQuery(taskDataCq).getResultList().isEmpty();

        return usedInOrders || usedInSubcontractedTask;
    }

}
