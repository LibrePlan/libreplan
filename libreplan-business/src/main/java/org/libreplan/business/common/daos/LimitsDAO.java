/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2015 LibrePlan
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.common.daos;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.libreplan.business.common.entities.Limits;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO for {@link Limits}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Repository
public class LimitsDAO extends GenericDAOHibernate<Limits, Long> implements ILimitsDAO {

    @Override
    public List<Limits> getAll() {
        return list(Limits.class);
    }


    @Override
    public Limits getLimitsByType(String type) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Limits> cq = cb.createQuery(Limits.class);
        Root<Limits> root = cq.from(Limits.class);
        cq.where(cb.equal(root.get("type"), type));

        return getSession().createQuery(cq).uniqueResult();
    }

}
