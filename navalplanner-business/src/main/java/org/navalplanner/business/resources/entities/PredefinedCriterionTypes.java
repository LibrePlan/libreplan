
package org.navalplanner.business.resources.entities;

import java.util.List;

/**
 * This class defines some criterion types known a priori<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public enum PredefinedCriterionTypes implements ICriterionType<Criterion> {

    WORK_RELATIONSHIP(false, false) {
        @Override
        public List<Criterion> getPredefined() {
            return WorkingRelationship.getCriterions();
        }
    };

    private final boolean allowHierarchy;

    private final boolean allowMultipleActiveCriterionsPerResource;

    private PredefinedCriterionTypes(boolean allowHierarchy,
            boolean allowMultipleActiveCriterionsPerResource) {
        this.allowHierarchy = allowHierarchy;
        this.allowMultipleActiveCriterionsPerResource = allowMultipleActiveCriterionsPerResource;
    }

    @Override
    public boolean allowHierarchy() {
        return allowHierarchy;
    }

    @Override
    public boolean allowMultipleActiveCriterionsPerResource() {
        return allowMultipleActiveCriterionsPerResource;
    }

    @Override
    public boolean contains(ICriterion criterion) {
        if (criterion instanceof Criterion) {
            Criterion c = (Criterion) criterion;
            return this.getType().equals(c.getType());
        } else
            return false;
    }

    @Override
    public Criterion createCriterion(String name) {
        return new Criterion(name, getType());
    }

    public abstract List<Criterion> getPredefined();

    private String getType() {
        return name();
    }

    public static ICriterionType<Criterion> getType(String type) {
        for (PredefinedCriterionTypes predefinedType : PredefinedCriterionTypes
                .values()) {
            if (predefinedType.name().equals(type))
                return predefinedType;
        }
        throw new RuntimeException("not found "
                + PredefinedCriterionTypes.class.getName() + " type for "
                + type);
    }

}