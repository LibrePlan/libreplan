package org.navalplanner.business.resources.entities;

import org.apache.commons.lang.Validate;

/**
 * Base implementation of {@link ICriterionType} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class CriterionTypeBase implements ICriterionType<Criterion> {

    private final boolean allowHierarchy;

    private final boolean allowSimultaneousCriterionsPerResource;

    private final String name;

    private final boolean allowAdding;

    private final boolean allowEditing;

    protected CriterionTypeBase(String name, boolean allowHierarchy,
            boolean allowSimultaneousCriterionsPerResource,
            boolean allowAdding, boolean allowEditing) {
        Validate.notNull(name, "name is not null");
        this.allowHierarchy = allowHierarchy;
        this.allowSimultaneousCriterionsPerResource = allowSimultaneousCriterionsPerResource;
        this.name = name;
        this.allowAdding = allowAdding;
        this.allowEditing = allowEditing;
    }

    @Override
    public boolean allowHierarchy() {
        return allowHierarchy;
    }

    @Override
    public boolean allowSimultaneousCriterionsPerResource() {
        return allowSimultaneousCriterionsPerResource;
    }

    public String getName() {
        return name;
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
