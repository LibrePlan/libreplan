package org.navalplanner.web.resources.worker;

import java.util.Collection;
import java.util.List;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;

public interface IMultipleCriterionActiveAssigner {

    public List<CriterionSatisfaction> getHistoric();

    public List<CriterionSatisfaction> getActiveSatisfactions();

    public List<Criterion> getCriterionsNotAssigned();

    public void unassign(
            Collection<? extends CriterionSatisfaction> satisfactions);

    public void assign(Collection<? extends Criterion> criterions);

    public void applyChanges();

}