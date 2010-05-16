package org.navalplanner.business.planner.entities;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.EnumMap;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.BaseEntity;


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
        START_START, END_START, END_END, START_END
    };

    private static
        EnumMap<Dependency.Type,
        LimitingResourceQueueDependency.QueueDependencyType> translationMap;

    static {
        translationMap = new EnumMap<Dependency.Type,
        LimitingResourceQueueDependency.
            QueueDependencyType>(Dependency.Type.class);
        translationMap.put(Dependency.Type.START_START,
                QueueDependencyType.START_START);
        translationMap.put(Dependency.Type.START_END,
                QueueDependencyType.START_END);
        translationMap.put(Dependency.Type.END_START,
                QueueDependencyType.END_START);
        translationMap.put(Dependency.Type.END_END,
                QueueDependencyType.END_END);
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

    public static LimitingResourceQueueDependency.QueueDependencyType
        convertFromTypeToQueueDepedencyType(Dependency.Type type) {
        return translationMap.get(type);
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
