package org.zkoss.ganttz.data;

import java.util.Collection;
import java.util.Date;

/**
 * This class represents a dependency. Contains the source and the destination.
 * It also specifies the type of the relationship. <br/>
 * Created at Apr 24, 2009
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Dependency {

    private enum Calculation {
        START, END;
    }

    public static Date calculateStart(Task origin, Date current,
            Collection<? extends Dependency> dependencies) {
        return apply(Calculation.START, origin, current, dependencies);
    }

    public static Date calculateEnd(Task origin, Date current,
            Collection<? extends Dependency> depencencies) {
        return apply(Calculation.END, origin, current, depencencies);
    }

    private static Date apply(Calculation calculation, Task origin,
            Date current, Collection<? extends Dependency> dependencies) {
        for (Dependency dependency : dependencies) {
            switch (calculation) {
            case START:
                current = dependency.getType().calculateStartDestinyTask(
                        dependency.getSource(), current);
                break;
            case END:
                current = dependency.getType().calculateEndDestinyTask(
                        dependency.getSource(), current);
                break;
            default:
                throw new RuntimeException("unexpected calculation "
                        + calculation);
            }
        }
        return current;
    }

    private final Task source;

    private final Task destination;

    private final DependencyType type;

    private final boolean visible;

    public Dependency(Task source, Task destination,
            DependencyType type, boolean visible) {
        if (source == null)
            throw new IllegalArgumentException("source cannot be null");
        if (destination == null)
            throw new IllegalArgumentException("destination cannot be null");
        if (type == null)
            throw new IllegalArgumentException("type cannot be null");
        this.source = source;
        this.destination = destination;
        this.type = type;
        this.visible = visible;
    }

    public Dependency(Task source, Task destination,
            DependencyType type) {
        this(source, destination, type, true);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Dependency other = (Dependency) obj;
        if (destination == null) {
            if (other.destination != null)
                return false;
        } else if (!destination.equals(other.destination))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        return true;
    }

    public Task getSource() {
        return source;
    }

    public Task getDestination() {
        return destination;
    }

    public DependencyType getType() {
        return type;
    }

    public boolean isVisible() {
        return visible;
    }

}
