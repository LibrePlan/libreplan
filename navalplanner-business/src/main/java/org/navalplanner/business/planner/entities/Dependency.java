package org.navalplanner.business.planner.entities;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Dependency {

    public enum Type {
        END_START, START_START, END_END, START_END;
    }

    public static Dependency createDependency(TaskElement origin,
            TaskElement destination, Type type) {
        Dependency dependency = new Dependency(origin, destination, type);
        origin.add(dependency);
        destination.add(dependency);
        return dependency;
    }

    private TaskElement origin;

    private TaskElement destination;

    private Type type;

    /**
     * It's needed by Hibernate. DO NOT USE.
     */
    public Dependency() {
    }

    private Dependency(TaskElement origin, TaskElement end, Type type) {
        this.origin = origin;
        this.destination = end;
        this.type = type;
    }

    public TaskElement getOrigin() {
        return origin;
    }

    public TaskElement getDestination() {
        return destination;
    }

    public Type getType() {
        return type;
    }
}
