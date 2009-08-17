package org.navalplanner.business.planner.entities;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.BaseEntity;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Dependency extends BaseEntity {

    public enum Type {
        END_START, START_START, END_END, START_END;
    }

    public static Dependency create(TaskElement origin,
            TaskElement destination, Type type) {
        Dependency dependency = new Dependency(origin, destination, type);
        dependency.setNewObject(true);
        origin.add(dependency);
        destination.add(dependency);
        return dependency;
    }

    private TaskElement origin;

    private TaskElement destination;

    private Type type;

    /**
     * Constructor for hibernate. Do not use!
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

}
