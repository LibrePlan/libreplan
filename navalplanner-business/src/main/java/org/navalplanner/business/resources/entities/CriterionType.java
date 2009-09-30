package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

    public static CriterionType create(String name,String description) {
        CriterionType criterionType = new CriterionType(name,description);
        criterionType.setNewObject(true);
        return criterionType;
    }

    public static CriterionType create(String name,String description,
            boolean allowHierarchy,boolean allowSimultaneousCriterionsPerResource,
            boolean enabled,ResourceEnum resource) {
        CriterionType criterionType = new CriterionType(name,description, allowHierarchy,
                allowSimultaneousCriterionsPerResource,enabled,resource);
        criterionType.setNewObject(true);
        return criterionType;
    }

    @NotEmpty
    private String name;

    private String description;

    private Boolean allowHierarchy = true;

    private Boolean allowSimultaneousCriterionsPerResource = true;

    private Boolean enabled = true;

    private ResourceEnum resource = ResourceEnum.getDefault();

    private Set<Criterion> criterions = new HashSet<Criterion>();

    private int numCriterions;

    /**
     * Constructor for hibernate. Do not use!
     */
    public CriterionType() {

    }

    private CriterionType(String name,String description) {
        this.name = name;
        this.description = description;
    }

    private CriterionType(String name,String description, boolean allowHierarchy,
            boolean allowSimultaneousCriterionsPerResource, boolean enabled,
            ResourceEnum resource) {

        this.allowHierarchy = allowHierarchy;
        this.allowSimultaneousCriterionsPerResource = allowSimultaneousCriterionsPerResource;
        this.enabled = enabled;
        this.name = name;
        this.description = description;
        this.resource = resource;
    }

    public static CriterionType asCriterionType(ICriterionType criterionType) {
        return create(criterionType.getName(),criterionType.getDescription(),
                criterionType.allowHierarchy(), criterionType
        .isAllowSimultaneousCriterionsPerResource(),
                criterionType.isEnabled(),
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

    public void setName(String name) {
        this.name = name;
    }

    public Set<Criterion> getCriterions() {
        return criterions;
    }

    public void setCriterions(Set<Criterion> criterions) {
        this.criterions = criterions;
    }

    @Override
    public boolean allowHierarchy() {
        return allowHierarchy == null ? false : allowHierarchy;
    }

    public void setAllowHierarchy(boolean allowHierarchy) {
        this.allowHierarchy = allowHierarchy;
    }

    @Override
    public boolean isAllowSimultaneousCriterionsPerResource() {
        return allowSimultaneousCriterionsPerResource == null ? false : allowSimultaneousCriterionsPerResource;
    }

    public void setAllowSimultaneousCriterionsPerResource(boolean allowSimultaneousCriterionsPerResource) {
        this.allowSimultaneousCriterionsPerResource = allowSimultaneousCriterionsPerResource;
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

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled == null ? false : enabled;
    }

    @Override
    public boolean isImmutable(){
        return !isEnabled();
    }


    public int getNumCriterions(){
        return criterions.size();
    }
}
