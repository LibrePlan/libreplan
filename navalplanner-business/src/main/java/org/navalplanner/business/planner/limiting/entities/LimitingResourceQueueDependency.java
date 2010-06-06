package org.navalplanner.business.planner.limiting.entities;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.EnumMap;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Dependency.Type;


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

    public LimitingResourceQueueElement getHasAsDestiny() {
        return hasAsDestiny;
    }

    public QueueDependencyType getType() {
        return type;
    }

    public Dependency getGanttDependency() {
        return ganttDependency;
    }
}
