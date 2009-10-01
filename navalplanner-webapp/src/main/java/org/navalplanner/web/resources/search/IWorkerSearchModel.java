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

package org.navalplanner.web.resources.search;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Conversation for worker search
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IWorkerSearchModel {

    /**
     * Gets all {@link Criterion} and groups then by {@link CriterionType}
     *
     * @return HashMap<CriterionType, Set<Criterion>>
     */
    HashMap<CriterionType, Set<Criterion>> getCriterions();

    /**
     * Queries database for retrieving all workers that match to the parameters
     *
     * @param name
     *            matches name/NIF of {@link Worker}
     * @param criterions
     *            {@link Worker} that complies all criterions
     * @return
     */
    @SuppressWarnings("unchecked")
    List<Worker> findWorkers(String name, List<Criterion> criterions);
}
