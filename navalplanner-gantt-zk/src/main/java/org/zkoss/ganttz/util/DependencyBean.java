package org.zkoss.ganttz.util;

import java.util.Collection;
import java.util.Date;

/**
 * This class represents a dependency. Contains the source and the destination.
 * It also specifies the type of the relationship. <br/>
 * Created at Apr 24, 2009
 * 
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * 
 */
public class DependencyBean {

    private enum Calculation {
        START, END;
    }

    public static Date calculateStart(TaskBean origin, Date current,
            Collection<? extends DependencyBean> dependencies) {
        return apply(Calculation.START, origin, current, dependencies);
    }

    public static Date calculateEnd(TaskBean origin, Date current,
            Collection<? extends DependencyBean> depencencies) {
        return apply(Calculation.END, origin, current, depencencies);
    }

    private static Date apply(Calculation calculation, TaskBean origin,
            Date current, Collection<? extends DependencyBean> dependencies) {
        for (DependencyBean dependency : dependencies) {
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

    private final TaskBean source;

    private final TaskBean destination;

    private final DependencyType type;

    public DependencyBean(TaskBean source, TaskBean destination,
            DependencyType type) {
        if (source == null)
            throw new IllegalArgumentException("source cannot be null");
        if (destination == null)
            throw new IllegalArgumentException("destination cannot be null");
        if (type == null)
            throw new IllegalArgumentException("type cannot be null");
        this.source = source;
        this.destination = destination;
        this.type = type;
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
        DependencyBean other = (DependencyBean) obj;
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

    public TaskBean getSource() {
        return source;
    }

    public TaskBean getDestination() {
        return destination;
    }

    public DependencyType getType() {
        return type;
    }

}
