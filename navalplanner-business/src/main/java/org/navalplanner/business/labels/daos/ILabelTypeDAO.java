/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.labels.daos;

import java.util.List;

import org.hibernate.NonUniqueResultException;
import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.labels.entities.LabelType;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface ILabelTypeDAO extends IIntegrationEntityDAO<LabelType> {

    List<LabelType> getAll();

    boolean existsByName(LabelType labelType);

    boolean isUnique(LabelType labelType);

    LabelType findUniqueByName(String type) throws InstanceNotFoundException,
            NonUniqueResultException;

    boolean existsByNameAnotherTransaction(LabelType labelType);

    LabelType findUniqueByNameAnotherTransaction(String name)
            throws InstanceNotFoundException;

}
