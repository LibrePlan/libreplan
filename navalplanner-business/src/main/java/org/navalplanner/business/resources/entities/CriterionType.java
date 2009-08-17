package org.navalplanner.business.resources.entities;

import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.BaseEntity;
import org.springframework.stereotype.Component;

/**
 * Base implementation of {@link ICriterionType} <br />

 * @author Diego Pino García <dpino@igalia.com>
 */
@Component
public class CriterionType extends BaseEntity implements
        ICriterionType<Criterion> {

    public static CriterionType create() {
        CriterionType criterionType = new CriterionType();
        criterionType.setNewObject(true);
        return criterionType;
    }

    public static CriterionType create(String name) {
        CriterionType criterionType = new CriterionType(name);
        criterionType.setNewObject(true);
        return criterionType;
    }

    public static CriterionType create(String name, boolean allowHierarchy,
            boolean allowSimultaneousCriterionsPerResource,
            boolean allowAdding, boolean allowEditing, ResourceEnum resource) {
        CriterionType criterionType = new CriterionType(name, allowHierarchy,
                allowSimultaneousCriterionsPerResource, allowAdding,
                allowEditing, resource);
        criterionType.setNewObject(true);
        return criterionType;
    }

    @NotEmpty
    private String name;

    private boolean allowHierarchy = true;

    private boolean allowSimultaneousCriterionsPerResource = true;

    private boolean allowAdding = true;

    private boolean allowEditing = true;

    private ResourceEnum resource = ResourceEnum.getDefault();

    private Set<Criterion> criterions;

    /**
     * Constructor for hibernate. Do not use!
     */
    public CriterionType() {

    }

    private CriterionType(String name) {
        this.name = name;
    }

    private CriterionType(String name, boolean allowHierarchy,
            boolean allowSimultaneousCriterionsPerResource,
            boolean allowAdding, boolean allowEditing, ResourceEnum resource) {

        this.allowHierarchy = allowHierarchy;
        this.allowSimultaneousCriterionsPerResource = allowSimultaneousCriterionsPerResource;
        this.name = name;
        this.allowAdding = allowAdding;
        this.allowEditing = allowEditing;
        this.resource = resource;
    }

    public static CriterionType asCriterionType(ICriterionType criterionType) {
        return create(criterionType.getName(), criterionType
                .allowHierarchy(), criterionType
        .allowSimultaneousCriterionsPerResource(),
                criterionType.allowAdding(), criterionType.allowEditing(),
                CriterionType.getResource(criterionType));
    }

    private static ResourceEnum getResource(ICriterionType criterionType) {
        for (ResourceEnum resource : ResourceEnum.values()) {
            if (criterionType.criterionCanBeRelatedTo(resource.asClass())) {
                return resource;
            }
        }

        return ResourceEnum.getDefault();
    }

    @Override
    public String getName() {
        return name;
    }

    public Set<Criterion> getCriterions() {
        return criterions;
    }

    public void setCriterions(Set<Criterion> criterions) {
        this.criterions = criterions;
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

    public ResourceEnum resource() {
        return resource;
    }

    @Override
    public Criterion createCriterion(String name) {
        return Criterion.withNameAndType(name, this);
    }

    public static Criterion createCriterion(
            PredefinedCriterionTypes predefinedCriterionType, String name) {

        CriterionType criterionType = CriterionType
                .asCriterionType(predefinedCriterionType);

        return Criterion.withNameAndType(name, criterionType);
    }

    @Override
    public Criterion createCriterionWithoutNameYet() {
        return Criterion.ofType(this);
    }

    @Override
    public boolean contains(ICriterion criterion) {
        if (criterion instanceof Criterion) {
            Criterion c = (Criterion) criterion;
            return this.equals(c.getType());
        } else {
            return false;
        }
    }

    @Override
    public boolean criterionCanBeRelatedTo(Class<? extends Resource> klass) {
        for (ResourceEnum resource : ResourceEnum.values()) {
            if (resource.isAssignableFrom(klass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Two criterion types are equals if they both got the same name
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof CriterionType == false)
            return false;

        if (this == o)
            return true;

        CriterionType criterionType = (CriterionType) o;

        return new EqualsBuilder().append(criterionType.getName(),
                this.getName()).isEquals();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
