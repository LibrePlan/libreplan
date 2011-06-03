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

package org.navalplanner.business.planner.daos;

import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.DerivedDayAssignment;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.entities.Scenario;

/**
 * DAO interface for {@link DayAssignment}
 *
 * @author @author Diego Pino García <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IDayAssignmentDAO extends IGenericDAO<DayAssignment, Long> {

    public void removeDerived(
            Collection<? extends DerivedDayAssignment> derivedAllocations);

    public List<DayAssignment> getAllFor(Scenario scenario);

    public List<DayAssignment> getAllFor(Scenario scenario,
            LocalDate initInclusive, LocalDate endInclusive);

    List<DayAssignment> listFilteredByDate(LocalDate init, LocalDate end);

    public List<DayAssignment> findByResources(Scenario scenario, List<Resource> resources);

    public List<DayAssignment> findByResources(List<Resource> resources);

}
