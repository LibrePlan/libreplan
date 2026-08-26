/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 WirelessGalicia, S.L.
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

package org.libreplan.business.planner.daos;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.planner.entities.SubcontractorCommunication;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link SubcontractorCommunication}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SubcontractorCommunicationDAO
        extends GenericDAOHibernate<SubcontractorCommunication, Long>
        implements ISubcontractorCommunicationDAO {

    @Override
    public List<SubcontractorCommunication> getAll() {
        return list(SubcontractorCommunication.class);
    }

    @Override
    public List<SubcontractorCommunication> getAllNotReviewed(){
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<SubcontractorCommunication> cq = cb.createQuery(SubcontractorCommunication.class);
        Root<SubcontractorCommunication> root = cq.from(SubcontractorCommunication.class);
        cq.where(cb.equal(root.get("reviewed"), false));
        return getSession().createQuery(cq).getResultList();
    }
}
