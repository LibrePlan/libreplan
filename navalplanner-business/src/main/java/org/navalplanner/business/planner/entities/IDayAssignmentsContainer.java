/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
package org.navalplanner.business.planner.entities;

import java.util.Collection;
import java.util.Set;

import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.IntraDayDate;

/**
 * Represents a container of day assignments. Its purpose is to the day
 * assignments for each scenario.
 *
 * @author Óscar González Fernández
 */
public interface IDayAssignmentsContainer<T extends DayAssignment> {

    Set<T> getDayAssignments();

    Scenario getScenario();

    void addAll(Collection<? extends T> assignments);

    void removeAll(Collection<? extends DayAssignment> assignments);

    void resetTo(Collection<T> assignments);

    IntraDayDate getIntraDayStart();

    void setIntraDayStart(IntraDayDate intraDayStart);

    IntraDayDate getIntraDayEnd();

    void setIntraDayEnd(IntraDayDate intraDayEnd);

}