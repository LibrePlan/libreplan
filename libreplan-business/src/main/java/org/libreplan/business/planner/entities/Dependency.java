/*
 * This file is part of LibrePlan
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

package org.libreplan.business.planner.entities;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.libreplan.business.util.deepcopy.Strategy;
import org.libreplan.business.util.deepcopy.OnCopy;

/**
 * Entity which represents an associated with properties
 * between two @{link Task}
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
public class Dependency extends BaseEntity {

    public enum Type {
        END_START {
            @Override
            public boolean modifiesDestinationStart() {
                return true;
            }

            @Override
            public boolean modifiesDestinationEnd() {
                return false;
            }
        },
        START_START {
            @Override
            public boolean modifiesDestinationStart() {
                return true;
            }

            @Override
            public boolean modifiesDestinationEnd() {
                return false;
            }
        },
        END_END {
            @Override
            public boolean modifiesDestinationStart() {
                return false;
            }

            @Override
            public boolean modifiesDestinationEnd() {
                return true;
            }
        },
        START_END {
            @Override
            public boolean modifiesDestinationStart() {
                return false;
            }

            @Override
            public boolean modifiesDestinationEnd() {
                return true;
            }
        };

        public abstract boolean modifiesDestinationStart();

        public abstract boolean modifiesDestinationEnd();
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

    @OnCopy(Strategy.IGNORE)
    private LimitingResourceQueueDependency queueDependency;

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

    public void setQueueDependency(LimitingResourceQueueDependency queueDependency) {
        this.queueDependency = queueDependency;
    }

    public LimitingResourceQueueDependency getQueueDependency() {
        return queueDependency;
    }

    public boolean isDependencyBetweenLimitedAllocatedTasks() {
        return getOrigin().hasLimitedResourceAllocation() &&
            getDestination().hasLimitedResourceAllocation();
    }

    public boolean hasLimitedQueueDependencyAssociated() {
        return queueDependency != null;
    }

    public Date getDateFromOrigin() {
        switch (type) {
        case END_START:
        case END_END:
            return origin.getEndDate();
        case START_END:
        case START_START:
            return origin.getStartDate();
        default:
            throw new RuntimeException("unexpected type");
        }
    }
}
