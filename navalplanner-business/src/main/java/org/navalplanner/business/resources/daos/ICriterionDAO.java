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

package org.navalplanner.business.resources.daos;

import java.util.List;

import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;

/**
 * Contract for {@link CriterionDAO} <br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface ICriterionDAO extends IIntegrationEntityDAO<Criterion> {

    public void removeByNameAndType(Criterion criterion);

    List<Criterion> findByNameAndType(Criterion criterion);

    Criterion findUniqueByNameAndType(Criterion criterion) throws InstanceNotFoundException;

    boolean existsByNameAndType(Criterion entity);

    Criterion find(Criterion criterion) throws InstanceNotFoundException;

    List<Criterion> findByType(ICriterionType<?> type);

    List<Criterion> getAll();

    List<Criterion> getAllSortedByTypeAndName();

    List<Criterion> getAllSorted();

    boolean thereIsOtherWithSameNameAndType(Criterion criterion);

    List<Criterion> findByNameAndType(String name, String type);

    public boolean existsPredefinedCriterion(Criterion criterion);

    public int numberOfRelatedRequirements(Criterion criterion);

    public int numberOfRelatedSatisfactions(Criterion criterion);

}
