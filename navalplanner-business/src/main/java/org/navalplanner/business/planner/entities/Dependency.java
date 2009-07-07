package org.navalplanner.business.planner.entities;

import org.apache.commons.lang.Validate;

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

    private Long id;

    private Long version;

    private TaskElement origin;

    private TaskElement destination;

    private Type type;

    /**
     * It's needed by Hibernate. DO NOT USE.
     */
    public Dependency() {
    }

    private Dependency(TaskElement origin, TaskElement destination, Type type) {
        Validate.notNull(origin);
        Validate.notNull(destination);
        Validate.notNull(type);
        Validate.isTrue(!origin.equals(destination),
                "a dependency must have a different origin than destination");
        this.origin = origin;
        this.destination = destination;
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

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }
}
