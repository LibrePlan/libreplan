package org.navalplanner.business.resources.entities;

/**
 * Parametrizes the behaviour of some criterions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionType<C extends ICriterion> {

    public boolean allowMultipleActiveCriterionsPerResource();

    public boolean allowHierarchy();

    public C createCriterion(String name);

    public boolean contains(ICriterion criterion);

}
