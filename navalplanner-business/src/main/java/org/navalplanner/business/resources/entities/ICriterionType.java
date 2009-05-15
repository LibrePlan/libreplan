package org.navalplanner.business.resources.entities;

/**
 * Parametrizes the behaviour of some criterions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionType {

    public boolean allowMultipleActiveCriterionsPerResource();

    public boolean allowHierarchy();

    public ICriterion createCriterion();

    public boolean contains(ICriterion criterion);

}
