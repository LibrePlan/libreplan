package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ResourcesDaoRegistry;

// FIXME: Alternatively, Resource can be modeled with the style:
// Resource.getParent() & Resource.getChilds(). This way, Resource does not
// depend on ResourceGroup. However, such an option allows combinations not
// semantically correct (e.g. a simple resource, such as Worker, could be the 
// child another simple resource, general methods like getChilds() do not make
// sense for simple entities, etc.). In consequence, I prefer the modeling 
// option shown below.

/**
 * This class acts as the base class for all resources.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public abstract class Resource {

    private Long id;

    @SuppressWarnings("unused")
    private long version;

    private Set<CriterionSatisfaction> criterionSatisfactions = new HashSet<CriterionSatisfaction>();

    public Long getId() {
        return id;
    }

    public abstract int getDailyCapacity();

    /**
     * It removes the resource from the database and updates references. The
     * default implementation removes the resource from the resource group it
     * belongs to (if it belongs to someone) and from the database. This
     * implementation should be valid for simple resources.
     */
    public void remove() {
        /* Remove from the database. */
        try {
            ResourcesDaoRegistry.getResourceDao().remove(getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<CriterionSatisfaction> getAllSatisfactions() {
        return Collections.unmodifiableSet(criterionSatisfactions);
    }

    public Collection<CriterionSatisfaction> getSatisfactionsFor(
            ICriterionType<?> type) {
        Set<CriterionSatisfaction> allSatisfactions = getAllSatisfactions();
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (CriterionSatisfaction criterionSatisfaction : allSatisfactions) {
            if (type.contains(criterionSatisfaction.getCriterion())) {
                result.add(criterionSatisfaction);
            }
        }
        return result;
    }

    public Collection<CriterionSatisfaction> getActiveSatisfactionsFor(
            ICriterionType<?> criterionType) {
        Collection<CriterionSatisfaction> satisfactionsFor = getSatisfactionsFor(criterionType);
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (CriterionSatisfaction criterionSatisfaction : satisfactionsFor) {
            if (criterionSatisfaction.isActiveNow()) {
                result.add(criterionSatisfaction);
            }
        }
        return result;
    }

    public Collection<CriterionSatisfaction> getActiveSatisfactionsForIn(
            ICriterionType<?> criterionType, Date start, Date end) {
        Validate.notNull(criterionType);
        Validate.isTrue(start.before(end));
        Collection<CriterionSatisfaction> satisfactionsFor = getSatisfactionsFor(criterionType);
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (CriterionSatisfaction criterionSatisfaction : satisfactionsFor) {
            if (criterionSatisfaction.isActiveIn(start, end)) {
                result.add(criterionSatisfaction);
            }
        }
        return result;
    }

    public Collection<CriterionSatisfaction> getActiveSatisfactionsFor(
            ICriterion criterion) {
        Set<CriterionSatisfaction> result = new HashSet<CriterionSatisfaction>();
        for (CriterionSatisfaction criterionSatisfaction : getAllSatisfactionsFor(criterion)) {
            if (criterionSatisfaction.isActiveNow()) {
                result.add(criterionSatisfaction);
            }
        }
        return result;
    }

    private Collection<CriterionSatisfaction> getAllSatisfactionsFor(
            ICriterion criterion) {
        Set<CriterionSatisfaction> result = new HashSet<CriterionSatisfaction>();
        for (CriterionSatisfaction satisfaction : criterionSatisfactions) {
            if (satisfaction.getCriterion().equals(criterion)) {
                result.add(satisfaction);
            }
        }
        return result;
    }

    public Collection<CriterionSatisfaction> getActiveSatisfactionsForIn(
            ICriterion criterion, Date start, Date end) {
        Validate.isTrue(start.before(end));
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        Collection<CriterionSatisfaction> allSatisfactionsFor = getAllSatisfactionsFor(criterion);
        for (CriterionSatisfaction criterionSatisfaction : allSatisfactionsFor) {
            if (criterionSatisfaction.isActiveIn(start, end)) {
                result.add(criterionSatisfaction);
            }
        }
        return result;
    }

    void add(CriterionSatisfaction criterionSatisfaction) {
        Validate.notNull(criterionSatisfaction,
                "criterionSatisfaction must be not null");
        criterionSatisfactions.add(criterionSatisfaction);
    }

}
