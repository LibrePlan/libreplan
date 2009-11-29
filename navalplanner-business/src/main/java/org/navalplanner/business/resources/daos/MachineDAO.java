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

package org.navalplanner.business.resources.daos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Machine;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate DAO for the <code>Machine</code> entity.
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class MachineDAO extends GenericDAOHibernate<Machine, Long>
    implements IMachineDAO {

    @SuppressWarnings("unchecked")
    @Override
    public List<Machine> findByNameAndCriterions(String name,
            List<Criterion> criterions) {

        // Find machines by name
        List<Machine> machines;
        if (name == null || name.isEmpty()) {
            machines = getAll();
        } else {
            machines = findByNameOrCode(name);
        }

        // If no criterions selected, returned found machines
        if (criterions.isEmpty()) {
            return machines;
        }

        // Filter by criterion
        final List<Machine> result = new ArrayList<Machine>();
        for (Machine machine : machines) {
            if (machine.satisfiesCriterions(new HashSet(criterions))) {
                result.add(machine);
            }
        }
        return result;
    }

    @Override
    public List<Machine> getAll() {
        return list(Machine.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Machine> findByNameOrCode(String name) {
        return getSession().createCriteria(Machine.class).add(
                Restrictions.or(Restrictions.ilike("name", "%" + name + "%"),
                        Restrictions
                        .ilike("code", name))).list();
    }

    @Override
    public Machine findUniqueByCode(String code)
            throws InstanceNotFoundException {
        Criteria criteria = getSession().createCriteria(Machine.class);
        criteria.add(Restrictions.eq("code", code).ignoreCase());

        List<Machine> list = criteria.list();
        if (list.size() != 1) {
            throw new InstanceNotFoundException(code, Machine.class.getName());
        }
        return list.get(0);
    }

    @Override
    @Transactional(readOnly= true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsMachineWithCode(String code) {
        try {
            findUniqueByCode(code);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly= true, propagation = Propagation.REQUIRES_NEW)
    public Machine findUniqueByCodeInAnotherTransaction(String code)
            throws InstanceNotFoundException {
        return findUniqueByCode(code);
    }

}
