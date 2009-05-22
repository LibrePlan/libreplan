package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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

    private long version;

    private Set<CriterionSatisfaction> criterionSatisfactions = new HashSet<CriterionSatisfaction>();

    public Long getId() {
        return id;
    }

    public abstract int getDailyCapacity();

    public long getVersion() {
        return version;
    }

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
        return new HashSet(criterionSatisfactions);
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

    public List<CriterionSatisfaction> getSatisfactionsFor(ICriterion criterion) {
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (CriterionSatisfaction criterionSatisfaction : getAllSatisfactions()) {
            if (criterionSatisfaction.getCriterion().equals(criterion)) {
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
        Validate.isTrue(end == null || start.before(end));
        Collection<CriterionSatisfaction> satisfactionsFor = getSatisfactionsFor(criterionType);
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (CriterionSatisfaction criterionSatisfaction : satisfactionsFor) {
            if (end == null && criterionSatisfaction.isActiveAt(start)
                    || end != null
                    && criterionSatisfaction.isActiveIn(start, end)) {
                result.add(criterionSatisfaction);
            }
        }
        return result;
    }

    public Collection<CriterionSatisfaction> getActiveSatisfactionsAt(
            ICriterionType<?> criterionType, Date pointInTime) {
        return getActiveSatisfactionsForIn(criterionType, pointInTime, null);
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
        Validate.isTrue(end == null || start.before(end));
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        Collection<CriterionSatisfaction> allSatisfactionsFor = getAllSatisfactionsFor(criterion);
        for (CriterionSatisfaction criterionSatisfaction : allSatisfactionsFor) {
            if (criterionSatisfaction.isActiveIn(start, end)) {
                result.add(criterionSatisfaction);
            }
        }
        return result;
    }

    public void activate(CriterionWithItsType criterionWithItsType) {
        activate(criterionWithItsType, new Date());
    }

    public void activate(CriterionWithItsType criterionWithItsType, Date start) {
        activate(criterionWithItsType, start, null);
    }

    public void activate(CriterionWithItsType criterionWithItsType, Date start,
            Date finish) {
        ICriterionType<?> type = criterionWithItsType.getType();
        Criterion criterion = criterionWithItsType.getCriterion();
        if (canBeActivated(criterionWithItsType, start, finish)) {
            CriterionSatisfaction newSatisfaction = new CriterionSatisfaction(
                    start, criterion, this);
            criterionSatisfactions.add(newSatisfaction);
            if (finish != null)
                newSatisfaction.finish(finish);
            if (!type.allowMultipleActiveCriterionsPerResource()) {
                for (CriterionSatisfaction criterionSatisfaction : getActiveSatisfactionsAt(
                        type, start)) {
                    if (newSatisfaction != criterionSatisfaction) {
                        criterionSatisfaction.finish(start);
                    }
                }
                Set<CriterionSatisfaction> posterior = getSatisfactionsPosteriorTo(
                        type, newSatisfaction);
                Date earliest = getEarliestStartDate(posterior);
                if (earliest != null) {
                    newSatisfaction.finish(earliest);
                }
            }
        } else {
            throw new IllegalStateException(
                    "this resource is activaved for other criterion of the same type");
        }

    }

    private static Date getEarliestStartDate(
            Set<CriterionSatisfaction> posterior) {
        Date earliest = null;
        for (CriterionSatisfaction criterionSatisfaction : posterior) {
            if (earliest == null)
                earliest = criterionSatisfaction.getStartDate();
            earliest = Collections.min(Arrays.asList(earliest,
                    criterionSatisfaction.getStartDate()));
        }
        return earliest;
    }

    private Set<CriterionSatisfaction> getSatisfactionsPosteriorTo(
            ICriterionType<?> type, CriterionSatisfaction newSatisfaction) {
        Date start = newSatisfaction.getStartDate();
        Date finish = newSatisfaction.isFinished() ? newSatisfaction
                .getEndDate() : null;
        Set<CriterionSatisfaction> posterior = new HashSet<CriterionSatisfaction>();
        if (finish != null) {
            posterior.addAll(getActiveSatisfactionsAt(type, finish));
        } else {
            ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
            for (CriterionSatisfaction satisfaction : getSatisfactionsFor(type)) {
                if (!satisfaction.isFinished()
                        && satisfaction.getStartDate().after(start)) {
                    result.add(satisfaction);
                }
            }
            posterior.addAll(result);
        }
        posterior.remove(newSatisfaction);
        return posterior;
    }

    public void deactivate(CriterionWithItsType criterionWithItsType) {
        for (CriterionSatisfaction criterionSatisfaction : getActiveSatisfactionsFor(criterionWithItsType
                .getCriterion())) {
            criterionSatisfaction.finish(new Date());
        }
    }

    private boolean noneOf(CriterionWithItsType criterionWithItsType,
            Date start, Date end) {
        ICriterionType<?> type = criterionWithItsType.getType();
        Criterion criterion = criterionWithItsType.getCriterion();
        return getActiveSatisfactionsForIn(type, start, end).size() == getActiveSatisfactionsForIn(
                criterion, start, end).size();
    }

    public boolean canBeActivated(CriterionWithItsType criterionWithItsType) {
        return canBeActivated(criterionWithItsType, new Date());
    }

    public boolean canBeActivated(CriterionWithItsType criterionWithItsType,
            Date start) {
        return canBeActivated(criterionWithItsType, start, null);
    }

    public boolean canBeActivated(CriterionWithItsType criterionWithItsType,
            Date start, Date finish) {
        ICriterionType<?> type = criterionWithItsType.getType();
        return type.criterionCanBeRelatedTo(getClass())
                && (type.allowMultipleActiveCriterionsPerResource() || noneOf(
                        criterionWithItsType, start, finish));
    }

}
