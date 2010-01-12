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
package org.navalplanner.business.planner.daos;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskSourceDAO extends GenericDAOHibernate<TaskSource, Long>
        implements ITaskSourceDAO {

    public TaskElement findUniqueByOrderElement(OrderElement orderElement) {
        String strQuery = "SELECT task " + "FROM TaskSource taskSource "
                + "LEFT OUTER JOIN taskSource.task task "
                + "LEFT OUTER JOIN taskSource.orderElement orderElement "
                + "WHERE task IN (SELECT task FROM Task task) ";

        if (orderElement != null) {
            strQuery += "AND orderElement = :orderElement ";
        }
        try {
            Query query = getSession().createQuery(strQuery);
            query.setParameter("orderElement", orderElement);
            query.setMaxResults(1);
            return (TaskElement) query.uniqueResult();
        } catch (HibernateException e) {
            return null;
        }
    }

}
