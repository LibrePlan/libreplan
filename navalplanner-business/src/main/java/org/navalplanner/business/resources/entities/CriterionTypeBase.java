package org.navalplanner.business.resources.entities;

/**
 * Base implementation of {@link ICriterionType} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class CriterionTypeBase implements ICriterionType {

    private final boolean allowHierarchy;

    private final boolean allowMultipleValuesPerResource;

    protected CriterionTypeBase(boolean allowHierarchy,
            boolean allowMultipleValuesPerResource) {
        this.allowHierarchy = allowHierarchy;
        this.allowMultipleValuesPerResource = allowMultipleValuesPerResource;
    }

    @Override
    public boolean allowHierarchy() {
        return allowHierarchy;
    }

    @Override
    public boolean allowMultipleActiveCriterionsPerResource() {
        return allowMultipleValuesPerResource;
    }

}
