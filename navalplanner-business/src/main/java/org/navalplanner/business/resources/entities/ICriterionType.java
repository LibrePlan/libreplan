package org.navalplanner.business.resources.entities;

/**
 * Parametrizes the behaviour of some criterions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionType<C extends ICriterion> {

    public String getName();

    public boolean allowMultipleActiveCriterionsPerResource();

    public boolean allowHierarchy();

    public boolean allowAdding();

    public boolean allowEditing();

    public C createCriterion(String name);

    public C createCriterionWithoutNameYet();

    public boolean contains(ICriterion criterion);

    public boolean criterionCanBeRelatedTo(Class<? extends Resource> klass);


}
