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
package org.libreplan.business.planner.limiting.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import java.util.EnumMap;

import org.apache.commons.lang.Validate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.Dependency.Type;


/**
 * Entity which represents the relationships between two
 * @{link LimitingResourceQueueElement}. One of the
 * @{link LimitingResourceQueueElement} is the origin of the relationship
 * and the other is the destiny of the relationship.
 *
 * @author Javier Moran Rua <jmoran@igalia.com>
 *
 */
public class LimitingResourceQueueDependency extends BaseEntity {

    public static enum QueueDependencyType {
        START_START(Type.START_START),
        END_START(Type.END_START),
        END_END(Type.END_END),
        START_END(Type.START_END);

        private static EnumMap<Type, QueueDependencyType> toQueueDependencyType;

        private static EnumMap<QueueDependencyType, Type> toDependencyType;

        static {
            toQueueDependencyType = new EnumMap<Type, QueueDependencyType>(
                    Type.class);
            toDependencyType = new EnumMap<QueueDependencyType, Type>(
                    QueueDependencyType.class);
            for (QueueDependencyType each : QueueDependencyType.values()) {
                toQueueDependencyType.put(each.associatedType, each);
                toDependencyType.put(each, each.associatedType);
            }
        }

        public static LimitingResourceQueueDependency.QueueDependencyType toQueueDependencyType(
                Type type) {
            return toQueueDependencyType.get(type);
        }

        public static Type toDependencyType(QueueDependencyType type) {
            return toDependencyType.get(type);
        }

        private final Type associatedType;

        private QueueDependencyType(Type associatedType) {
            this.associatedType = associatedType;
        }

        boolean propagatesThrough(QueueDependencyType nextType) {
            switch (this) {
            case END_START:
            case START_START:
                return true;
            case START_END:
            case END_END:
                return nextType.comesFromEnd();
            default:
                throw new RuntimeException("unknown type: " + this);
            }
        }

        private boolean comesFromEnd() {
            switch (this) {
            case START_END:
            case START_START:
                return false;
            case END_START:
            case END_END:
                return true;
            default:
                throw new RuntimeException("unknown type: " + this);
            }
        }

        private Dependency.Type getDependencyType() {
            return QueueDependencyType.toDependencyType(this);
        }

        public boolean modifiesDestinationStart() {
            return getDependencyType().modifiesDestinationStart();
        }

        public boolean modifiesDestinationEnd() {
            return getDependencyType().modifiesDestinationEnd();
        }

        public DateAndHour calculateDateTargetFrom(DateAndHour previousStartTime,
                DateAndHour previousEndTime) {
            switch (this) {
            case START_END:
            case START_START:
                return previousStartTime;
            case END_END:
            case END_START:
                return previousEndTime;
            default:
                throw new RuntimeException("unknown type: " + this);
            }
        }

    };

    public static LimitingResourceQueueDependency.QueueDependencyType toQueueDependencyType(
            Dependency.Type type) {
        return QueueDependencyType.toQueueDependencyType(type);
    }

    private LimitingResourceQueueElement hasAsOrigin;
    private LimitingResourceQueueElement hasAsDestiny;
    private Dependency ganttDependency;
    private QueueDependencyType type;

    public static LimitingResourceQueueDependency create(
            LimitingResourceQueueElement origin,
            LimitingResourceQueueElement destiny,
            Dependency ganttDependency,
            QueueDependencyType type) {
        LimitingResourceQueueDependency dependency = new
        LimitingResourceQueueDependency(origin,destiny,ganttDependency,type);
        dependency.setNewObject(true);
        origin.add(dependency);
        destiny.add(dependency);
        ganttDependency.setQueueDependency(dependency);
        return dependency;
    }

    /**
     * Contructor for Hibernate. Do not use !
     */
    public LimitingResourceQueueDependency() {}

    private LimitingResourceQueueDependency(LimitingResourceQueueElement origin,
            LimitingResourceQueueElement destiny,
            Dependency ganttDependency,
            QueueDependencyType type) {
        Validate.notNull(origin);
        Validate.notNull(destiny);
        Validate.notNull(ganttDependency);
        Validate.isTrue(!origin.equals(destiny), _("A queue dependency has to " +
            "have an origin different from destiny"));
        this.hasAsOrigin = origin;
        this.hasAsDestiny = destiny;
        this.ganttDependency = ganttDependency;
        this.type = type;
    }

    public LimitingResourceQueueElement getHasAsOrigin() {
        return hasAsOrigin;
    }

    public void setOrigin(LimitingResourceQueueElement origin) {
        this.hasAsOrigin = origin;
    }

    public LimitingResourceQueueElement getHasAsDestiny() {
        return hasAsDestiny;
    }

    public void setDestiny(LimitingResourceQueueElement destiny) {
        this.hasAsDestiny = destiny;
    }

    public QueueDependencyType getType() {
        return type;
    }

    public Dependency getGanttDependency() {
        return ganttDependency;
    }

    public boolean isOriginNotDetached() {
        return !hasAsOrigin.isDetached();
    }

    public boolean modifiesDestinationStart() {
        return type.modifiesDestinationStart();
    }

    public boolean modifiesDestinationEnd() {
        return type.modifiesDestinationEnd();
    }

    public DateAndHour getDateFromOrigin() {
        if (hasAsOrigin.isDetached()) {
            throw new IllegalStateException("origin detached");
        }
        return type.calculateDateTargetFrom(hasAsOrigin.getStartTime(), hasAsOrigin
                .getEndTime());
    }

    public boolean propagatesThrough(LimitingResourceQueueDependency transitive) {
        return getHasAsDestiny().equals(transitive.getHasAsOrigin())
                && type.propagatesThrough(transitive.getType());
    }
}
