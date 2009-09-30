package org.navalplanner.business.resources.entities;

import org.apache.commons.lang.Validate;

/**
 * Base implementation of {@link ICriterionType} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class CriterionTypeBase implements ICriterionType<Criterion> {

    private final boolean allowHierarchy;

    private final boolean allowSimultaneousCriterionsPerResource;

    private final boolean enabled;

    private final String name;

    private final String description;

    protected CriterionTypeBase(String name, String description, boolean allowHierarchy,
            boolean allowSimultaneousCriterionsPerResource,boolean enabled) {
        Validate.notNull(name, "name is not null");
        this.allowHierarchy = allowHierarchy;
        this.allowSimultaneousCriterionsPerResource = allowSimultaneousCriterionsPerResource;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
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
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isImmutable() {
        return !enabled;
    }
}
