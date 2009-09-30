package org.navalplanner.business.resources.entities;
import java.util.Arrays;
import java.util.List;

/**
 * This class defines some criterion types known a priori<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public enum PredefinedCriterionTypes implements ICriterionType<Criterion> {

   WORK_RELATIONSHIP("Relationship of the resource with the enterprise ",false, false,true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return WorkingRelationship.getCriterionNames();
        }
    },
    LOCATION_GROUP("Location where the resource work",false, true, true, ResourceEnum.RESOURCE) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    },
    LEAVE("Leave",false, false, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return LeaveCriterions.getCriterionNames();
        }
    },
    TRAINING("Training courses and labor training",true, true, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    },
    JOB("Job",true, true, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    },
    CATEGORY("Professional category",true, true, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    };

    private final String description;

    private final boolean allowHierarchy;

    private final boolean allowSimultaneousCriterionsPerResource;

    private final boolean enabled;

    private final ResourceEnum resource;

    private PredefinedCriterionTypes(String description, boolean allowHierarchy,
            boolean allowSimultaneousCriterionsPerResource,
            boolean enabled,
            ResourceEnum resource) {
        this.allowHierarchy = allowHierarchy;
        this.allowSimultaneousCriterionsPerResource = allowSimultaneousCriterionsPerResource;
        this.description = description;
        this.enabled = enabled;
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
    public boolean isAllowSimultaneousCriterionsPerResource() {
        return allowSimultaneousCriterionsPerResource;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public boolean isImmutable() {
        return !this.enabled;
    }

    @Override
    public Criterion createCriterion(String name) {
        return Criterion.create(name, CriterionType.asCriterionType(this));
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
