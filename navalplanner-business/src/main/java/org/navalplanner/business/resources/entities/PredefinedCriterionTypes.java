package org.navalplanner.business.resources.entities;

import java.util.Arrays;
import java.util.List;

/**
 * This class defines some criterion types known a priori<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public enum PredefinedCriterionTypes implements ICriterionType<Criterion> {

    WORK_RELATIONSHIP(false, false, false, false, Worker.class) {
        @Override
        public List<Criterion> getPredefined() {
            return WorkingRelationship.getCriterions();
        }
    },
    LOCATION_GROUP(false, true, true, true, Resource.class) {
        @Override
        public List<Criterion> getPredefined() {
            return Arrays.asList();
        }
    },
    LEAVE(false, false, false, false, Worker.class) {
        @Override
        public List<Criterion> getPredefined() {
            return LeaveCriterions.getCriterions();
        }
    };

    private final boolean allowHierarchy;

    private final boolean allowMultipleActiveCriterionsPerResource;

    private final boolean allowAdding;

    private final boolean allowEditing;

    private List<Class<? extends Resource>> classes;

    private PredefinedCriterionTypes(boolean allowHierarchy,
            boolean allowMultipleActiveCriterionsPerResource,
            boolean allowAdding, boolean allowEditing,
            Class<? extends Resource>... klasses) {
        this.allowHierarchy = allowHierarchy;
        this.allowMultipleActiveCriterionsPerResource = allowMultipleActiveCriterionsPerResource;
        this.allowAdding = allowAdding;
        this.allowEditing = allowEditing;
        this.classes = Arrays.asList(klasses);
    }

    @Override
    public boolean criterionCanBeRelatedTo(Class<? extends Resource> klass) {
        for (Class<? extends Resource> c : classes) {
            if (c.isAssignableFrom(klass))
                return true;
        }
        return false;
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
        return Criterion.withNameAndType(name, getType());
    }

    @Override
    public Criterion createCriterionWithoutNameYet() {
        return Criterion.ofType(getType());
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

    public String getName() {
        return name();
    }

    @Override
    public boolean allowAdding() {
        return allowAdding;
    }

    @Override
    public boolean allowEditing() {
        return allowEditing;
    }

}