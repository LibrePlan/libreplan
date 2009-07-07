package org.navalplanner.business.test.planner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
        dependency = Dependency.createDependency(origin, destination, type);
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
