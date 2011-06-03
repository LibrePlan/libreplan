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

package org.navalplanner.business.test.planner.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.Dependency.Type;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DependencyTest {
    private Dependency dependency;
    private TaskElement origin;
    private TaskElement destination;
    private Type type;

    public DependencyTest() {
        origin = TaskTest.createValidTask();
        destination = TaskTest.createValidTask();
        type = Type.END_START;
        dependency = Dependency.create(origin, destination, type);
    }

    @Test
    public void dependencyHasOriginProperty() {
        assertThat(dependency.getOrigin(), equalTo(origin));
    }

    @Test
    public void dependencyHasDestinationProperty() {
        assertThat(dependency.getDestination(), equalTo(destination));
    }

    @Test
    public void dependencyHasTypeProperty() {
        assertThat(dependency.getType(), equalTo(type));
    }

    @Test
    public void mustNotAllowANullValuesForAnyOfTheCreationArguments() {
        Object[] arguments = { origin, destination, type };
        for (int i = 0; i < arguments.length; i++) {
            Object[] cloned = arguments.clone();
            cloned[i] = null;
            TaskElement origin = (TaskElement) cloned[0];
            TaskElement destination = (TaskElement) cloned[1];
            Type type = (Type) cloned[2];
            try {
                Dependency.create(origin, destination, type);
                fail("must send IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // ok
            }
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void dependencyMustNotAllowTheSameOriginAndDestination() {
        Dependency.create(origin, origin, type);
    }

    @Test
    public void creatingDependencyImpliesAssociatingItWithTheRelatedTasks() {
        assertFalse(origin.getDependenciesWithThisDestination().contains(
                dependency));
        assertTrue(origin.getDependenciesWithThisOrigin().contains(dependency));

        assertTrue(destination.getDependenciesWithThisDestination().contains(
                dependency));
        assertFalse(destination.getDependenciesWithThisOrigin().contains(
                dependency));
    }
}
