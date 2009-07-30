package org.navalplanner.business.resources.entities;

import java.util.Arrays;
import java.util.List;

/**
 * This class defines some criterion types known a priori<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public enum PredefinedCriterionTypes implements ICriterionType<Criterion> {

   WORK_RELATIONSHIP(false, false, false, false, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return WorkingRelationship.getCriterionNames();
        }
    },
    LOCATION_GROUP(false, true, true, true, ResourceEnum.RESOURCE) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    },
    LEAVE(false, false, false, false, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return LeaveCriterions.getCriterionNames();
        }
    },
    TRAINING(true, true, true, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    },
    JOB(true, true, true, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    },
    CATEGORY(true, true, true, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    };

    private final boolean allowHierarchy;

    private final boolean allowSimultaneousCriterionsPerResource;

    private final boolean allowAdding;

    private final boolean allowEditing;

    private final ResourceEnum resource;

    private PredefinedCriterionTypes(boolean allowHierarchy,
            boolean allowSimultaneousCriterionsPerResource,
            boolean allowAdding, boolean allowEditing,
            ResourceEnum resource) {
        this.allowHierarchy = allowHierarchy;
        this.allowSimultaneousCriterionsPerResource = allowSimultaneousCriterionsPerResource;
        this.allowAdding = allowAdding;
        this.allowEditing = allowEditing;
        this.resource = resource;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public boolean allowHierarchy() {
        return allowHierarchy;
    }

    @Override
    public boolean allowSimultaneousCriterionsPerResource() {
        return allowSimultaneousCriterionsPerResource;
    }

    @Override
    public boolean allowAdding() {
        return allowAdding;
    }

    @Override
    public boolean allowEditing() {
        return allowEditing;
    }

    @Override
    public Criterion createCriterion(String name) {
        return new Criterion(name, CriterionType.asCriterionType(this));
    }

    @Override
    public Criterion createCriterionWithoutNameYet() {
        return createCriterion("");
    }

    @Override
    public boolean contains(ICriterion criterion) {
        if (criterion instanceof Criterion) {
            Criterion c = (Criterion) criterion;
            return CriterionType.asCriterionType(this).equals(c.getType());
        }

        return false;
    }

    @Override
    public boolean criterionCanBeRelatedTo(Class<? extends Resource> klass) {
        return resource.isAssignableFrom(klass);
    }

    public abstract List<String> getPredefined();
}
