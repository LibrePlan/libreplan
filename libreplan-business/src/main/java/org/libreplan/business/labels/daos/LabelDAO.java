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

package org.libreplan.business.labels.daos;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link Label}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LabelDAO extends IntegrationEntityDAO<Label> implements ILabelDAO {

    @Override
    public List<Label> getAll() {
        return list(Label.class);
    }

    @Override
    public Label findByNameAndType(String labelName, LabelType labelType) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Label> cq = cb.createQuery(Label.class);
        Root<Label> root = cq.from(Label.class);
        cq.where(cb.equal(root.get("name"), labelName), cb.equal(root.get("type"), labelType));
        return getSession().createQuery(cq).uniqueResult();
    }

    @Override
    public List<Label> findByType(LabelType labelType) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Label> cq = cb.createQuery(Label.class);
        Root<Label> root = cq.from(Label.class);
        cq.where(cb.equal(root.get("type"), labelType));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public boolean existsByName(String labelName) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Label> cq = cb.createQuery(Label.class);
        Root<Label> root = cq.from(Label.class);
        cq.where(cb.equal(root.get("name"), labelName));
        return getSession().createQuery(cq).uniqueResult() != null;
    }
}
