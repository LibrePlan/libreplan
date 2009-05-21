package org.navalplanner.business.resources.entities;

import org.apache.commons.lang.Validate;

/**
 * A {@link ICriterion} with his associated {@link ICriterionType} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionWithItsType {

    private final ICriterionType<?> type;

    private final Criterion criterion;

    public CriterionWithItsType(ICriterionType<?> type, Criterion criterion) {
        Validate.notNull(type);
        Validate.notNull(criterion);
        Validate.isTrue(type.contains(criterion),
                "the criterion must be belong to the type");
        this.type = type;
        this.criterion = criterion;
    }

    public ICriterionType<?> getType() {
        return type;
    }

    public Criterion getCriterion() {
        return criterion;
    }
}
