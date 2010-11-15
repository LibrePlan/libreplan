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

package org.zkoss.ganttz.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.zkoss.ganttz.data.GanttDiagramGraph.GanttZKAdapter;
import org.zkoss.ganttz.data.GanttDiagramGraph.TaskPoint;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.constraint.Constraint.IConstraintViolationListener;
import org.zkoss.ganttz.util.ConstraintViolationNotificator;

/**
 * This class represents a dependency. Contains the source and the destination.
 * It also specifies the type of the relationship. <br/>
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Dependency implements IDependency<Task> {

    private enum Calculation {
        START {
            @Override
            public List<Constraint<GanttDate>> toConstraints(Task source,
                    DependencyType type) {
                return type.getStartConstraints(source);
            }
        },
        END {
            @Override
            public List<Constraint<GanttDate>> toConstraints(Task source,
                    DependencyType type) {
                return type.getEndConstraints(source);
            }
        };

        abstract List<Constraint<GanttDate>> toConstraints(Task source,
                DependencyType type);
    }

    public static List<Constraint<GanttDate>> getStartConstraints(
            Collection<Dependency> dependencies) {
        return getConstraintsFor(dependencies, Calculation.START);
    }

    public static List<Constraint<GanttDate>> getEndConstraints(
            Collection<Dependency> incoming) {
        return getConstraintsFor(incoming, Calculation.END);
    }

    private static List<Constraint<GanttDate>> getConstraintsFor(
            Collection<Dependency> dependencies, Calculation calculation) {
        List<Constraint<GanttDate>> result = new ArrayList<Constraint<GanttDate>>();
        for (Dependency dependency : dependencies) {
            result.addAll(dependency.toConstraints(calculation));
        }
        return result;
    }

    private final Task source;

    private final Task destination;

    private DependencyType type;

    private final boolean visible;

    private ConstraintViolationNotificator<GanttDate> violationsNotificator = ConstraintViolationNotificator
            .create();

    public Dependency(Task source, Task destination,
            DependencyType type, boolean visible) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        if (destination == null) {
            throw new IllegalArgumentException("destination cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.source = source;
        this.destination = destination;
        this.type = type;
        this.visible = visible;
    }

    public Dependency(Task source, Task destination,
            DependencyType type) {
        this(source, destination, type, true);
    }

    private List<Constraint<GanttDate>> toConstraints(Calculation calculation) {
        return violationsNotificator.withListener(calculation.toConstraints(
                source, type));
    }

    public void addConstraintViolationListener(
            IConstraintViolationListener<GanttDate> listener) {
        violationsNotificator.addConstraintViolationListener(listener);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(source).append(destination).append(
                type).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Dependency other = (Dependency) obj;
        return new EqualsBuilder().append(this.destination, other.destination)
                .append(this.source, other.source)
                .append(this.type, other.type).isEquals();
    }

    @Override
    public Task getSource() {
        return source;
    }

    @Override
    public Task getDestination() {
        return destination;
    }

    @Override
    public DependencyType getType() {
        return type;
    }

    public boolean isVisible() {
        return visible;
    }

    public Dependency createWithType(DependencyType type) {
        return new Dependency(source, destination, type, visible);
    }

    public TaskPoint<Task, Dependency> getDestinationPoint() {
        return new TaskPoint<Task, Dependency>(new GanttZKAdapter(),
                destination, type.getPointModified());
    }

}