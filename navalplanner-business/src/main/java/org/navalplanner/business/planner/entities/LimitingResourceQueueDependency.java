package org.navalplanner.business.planner.entities;

import static org.navalplanner.business.i18n.I18nHelper._;

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

    public enum Type {
        START_START, END_START, END_END
    };

    private LimitingResourceQueueElement hasAsOrigin;
    private LimitingResourceQueueElement hasAsDestiny;
    private Dependency ganttDependency;
    private Type type;

    public static LimitingResourceQueueDependency create(
            LimitingResourceQueueElement origin,
            LimitingResourceQueueElement destiny,
            Dependency ganttDependency,
            Type type) {
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
            Type type) {
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

    public Type getType() {
        return type;
    }

    public Dependency getGanttDependency() {
        return ganttDependency;
    }
}
