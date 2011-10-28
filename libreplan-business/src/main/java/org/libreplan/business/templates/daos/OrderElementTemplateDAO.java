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
package org.libreplan.business.templates.daos;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderElementTemplateDAO extends
        GenericDAOHibernate<OrderElementTemplate, Long> implements
        IOrderElementTemplateDAO {

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<OrderElementTemplate> getRootTemplates() {
        Query query = getSession().createQuery(
                "select t from OrderElementTemplate t where t.parent = NULL");
        return query.list();
    }

    @Override
    public OrderElementTemplate findUniqueRootByName(
            OrderElementTemplate orderElementTemplate)
            throws InstanceNotFoundException {
        Validate.notNull(orderElementTemplate);
        return findUniqueRootByName(orderElementTemplate.getName());
    }

    @Override
    public OrderElementTemplate findUniqueRootByName(String name)
            throws InstanceNotFoundException, NonUniqueResultException {

        // Prepare query
        String strQuery = "Select t " + "from OrderElementTemplate t "
                + "where t.parent = NULL "
                + "and  LOWER(t.infoComponent.name) like  LOWER(:name)";

        // Execute query
        Query query = getSession().createQuery(strQuery);
        query.setParameter("name", name);

        OrderElementTemplate orderElementTemplate = (OrderElementTemplate) query
                .uniqueResult();
        if (orderElementTemplate == null) {
            throw new InstanceNotFoundException(null, "OrderElemenetTemplate");
        }
        return orderElementTemplate;
    }

    @Override
    public boolean existsOtherRootOrderElementTemplateByName(
            OrderElementTemplate orderElementTemplate) {
        try {
            OrderElementTemplate t = findUniqueRootByName(orderElementTemplate);
            return (t != null && t != orderElementTemplate);
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsRootByNameAnotherTransaction(
            OrderElementTemplate orderElementTemplate) {
        return existsOtherRootOrderElementTemplateByName(orderElementTemplate);
    }

}
