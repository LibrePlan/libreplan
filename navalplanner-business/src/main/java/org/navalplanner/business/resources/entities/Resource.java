package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

    private interface Predicate {
        public boolean accepts(CriterionSatisfaction satisfaction);
    }

    public class Query {

        private List<Predicate> predicates = new ArrayList<Predicate>();
        private boolean sort = false;

        private Query() {

        }

        public Query from(final ICriterionType<?> type) {
            return withNewPredicate(new Predicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    return type.contains(satisfaction.getCriterion());
                }
            });
        }

        private Query withNewPredicate(Predicate newPredicate) {
            predicates.add(newPredicate);
            return this;
        }

        public Query at(Date date) {
            return enforcedInAll(Interval.point(date));
        }

        public Query between(Date start, Date end) {
            return enforcedInAll(Interval.range(start, end));
        }

        public Query enforcedInAll(final Interval interval) {
            return withNewPredicate(new Predicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    return satisfaction.isAlwaysEnforcedIn(interval);
                }
            });
        }

        public Query overlapsWith(final Interval interval) {
            return withNewPredicate(new Predicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    return satisfaction.overlapsWith(interval);
                }
            });
        }

        public Query from(final ICriterion criterion) {
            return withNewPredicate(new Predicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    return satisfaction.getCriterion().isEquivalent(criterion);
                }
            });
        }

        public Query sortByStartDate() {
            sort = true;
            return this;
        }

        public List<CriterionSatisfaction> result() {
            ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
            for (CriterionSatisfaction criterionSatisfaction : criterionSatisfactions) {
                if (isAcceptedByAllPredicates(criterionSatisfaction)) {
                    result.add(criterionSatisfaction);
                }
            }
            Collections.sort(result, CriterionSatisfaction.BY_START_COMPARATOR);
            return result;
        }

        private boolean isAcceptedByAllPredicates(
                CriterionSatisfaction criterionSatisfaction) {
            for (Predicate predicate : predicates) {
                if (!predicate.accepts(criterionSatisfaction))
                    return false;
            }
            return true;
        }

        public Query current() {
            return withNewPredicate(new Predicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    return satisfaction.isCurrent();
                }
            });
        }

        public List<Criterion> asCriterions() {
            LinkedHashSet<Criterion> result = new LinkedHashSet<Criterion>();
            for (CriterionSatisfaction criterionSatisfaction : result()) {
                result.add(criterionSatisfaction.getCriterion());
            }
            return new ArrayList<Criterion>(result);
        }

    }

    public Query query() {
        return new Query();
    }

    public Long getId() {
        return id;
    }

    public void forceLoadSatisfactions() {
        for (CriterionSatisfaction criterionSatisfaction : criterionSatisfactions) {
            criterionSatisfaction.getCriterion().getName();
            criterionSatisfaction.getCriterion().getType();
        }
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
        return new HashSet<CriterionSatisfaction>(criterionSatisfactions);
    }

    public Collection<CriterionSatisfaction> getSatisfactionsFor(
            ICriterionType<?> type) {
        return query().from(type).result();
    }

    public List<CriterionSatisfaction> getSatisfactionsFor(ICriterion criterion) {
        return query().from(criterion).result();
    }

    public List<Criterion> getCurrentCriterionsFor(ICriterionType<?> type) {
        return query().from(type).current().asCriterions();
    }

    public Collection<CriterionSatisfaction> getCurrentSatisfactionsFor(
            ICriterionType<?> criterionType) {
        return query().from(criterionType).current().result();
    }

    public List<CriterionSatisfaction> getCurrentSatisfactionsFor(
            ICriterion criterion) {
        return query().from(criterion).current().result();
    }

    public CriterionSatisfaction addSatisfaction(
            CriterionWithItsType criterionWithItsType) {
        return addSatisfaction(criterionWithItsType, Interval.from(new Date()));
    }

    private static class EnsureSatisfactionIsCorrect {

        private EnsureSatisfactionIsCorrect(Resource resource,
                ICriterionType<?> type, CriterionSatisfaction satisfaction) {
            Validate.notNull(resource);
            Validate.notNull(satisfaction.getResource());
            Validate.notNull(satisfaction);
            if (!satisfaction.getResource().equals(resource)) {
                throw new IllegalArgumentException(
                        "the satisfaction is not related to this resource");
            }
            this.type = new CriterionWithItsType(type, satisfaction
                    .getCriterion());
            this.interval = satisfaction.getInterval();
            this.resource = resource;
        }

        final Resource resource;

        final CriterionWithItsType type;

        final Interval interval;

        CriterionSatisfaction addSatisfaction() {
            return resource.addSatisfaction(type, interval);
        }

        boolean canAddSatisfaction() {
            return resource.canAddSatisfaction(type, interval);
        }

    }

    public CriterionSatisfaction addSatisfaction(
            ICriterionType<Criterion> type, CriterionSatisfaction satisfaction) {
        return new EnsureSatisfactionIsCorrect(this, type, satisfaction)
                .addSatisfaction();
    }

    public CriterionSatisfaction addSatisfaction(
            CriterionWithItsType criterionWithItsType, Interval interval) {
        Criterion criterion = criterionWithItsType.getCriterion();
        ICriterionType<?> type = criterionWithItsType.getType();
        CriterionSatisfaction newSatisfaction = createNewSatisfaction(interval,
                criterion);
        if (canAddSatisfaction(criterionWithItsType, interval)) {
            Date finish = getFinishDate(type, newSatisfaction, interval);
            if (finish != null) {
                newSatisfaction.finish(finish);
            }
            criterionSatisfactions.add(newSatisfaction);
            return newSatisfaction;
        } else {
            throw new IllegalStateException(
                    "this resource is activaved for other criterion of the same type");
        }
    }

    private CriterionSatisfaction createNewSatisfaction(Interval interval,
            Criterion criterion) {
        CriterionSatisfaction newSatisfaction = new CriterionSatisfaction(
                criterion, this, interval);
        return newSatisfaction;
    }

    private Date getFinishDate(ICriterionType<?> type,
            CriterionSatisfaction newSatisfaction, Interval interval) {
        if (!type.allowSimultaneousCriterionsPerResource()) {
            CriterionSatisfaction posterior = getNext(type, newSatisfaction);
            if (posterior != null && posterior.overlapsWith(interval)) {
                assert !posterior.overlapsWith(Interval.range(interval
                        .getStart(), posterior.getStartDate()));
                return posterior.getStartDate();
            }
        }
        return interval.getEnd();
    }

    /**
     * @param orderedSatisfactions
     * @param newSatisfaction
     * @return the position in which if newSatisfaction is inserted would comply
     *         with the following:
     *         <ul>
     *         <li>newSatisfaction startDate would be equal or posterior to all
     *         the previous satisfactions</li>
     *         <li>newSatisfaction startDate would be previous to all the
     *         posterior satisfactions</li>
     *         </ul>
     */
    private int findPlace(List<CriterionSatisfaction> orderedSatisfactions,
            CriterionSatisfaction newSatisfaction) {
        int position = Collections.binarySearch(orderedSatisfactions,
                newSatisfaction, CriterionSatisfaction.BY_START_COMPARATOR);
        if (position >= 0) {
            return position + 1;
        } else {
            return Math.abs(position) - 1;
        }
    }

    public List<CriterionSatisfaction> finish(
            CriterionWithItsType criterionWithItsType) {
        return finishEnforcedAt(criterionWithItsType, new Date());
    }

    public List<CriterionSatisfaction> finishEnforcedAt(
            CriterionWithItsType criterionWithItsType, Date date) {
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (CriterionSatisfaction criterionSatisfaction : query().from(
                criterionWithItsType.getType()).at(date).result()) {
            criterionSatisfaction.finish(date);
            result.add(criterionSatisfaction);
        }
        return result;
    }

    public boolean canAddSatisfaction(
            CriterionWithItsType criterionWithItsType, Interval interval) {
        ICriterionType<?> type = criterionWithItsType.getType();
        if (!type.criterionCanBeRelatedTo(getClass())) {
            return false;
        }
        if (type.allowSimultaneousCriterionsPerResource()) {
            return true;
        }
        CriterionSatisfaction newSatisfaction = createNewSatisfaction(interval,
                criterionWithItsType.getCriterion());
        CriterionSatisfaction previous = getPrevious(criterionWithItsType
                .getType(), newSatisfaction);
        return previous == null || !previous.overlapsWith(interval);
    }

    public boolean canAddSatisfaction(ICriterionType<?> type,
            CriterionSatisfaction satisfaction) {
        return new EnsureSatisfactionIsCorrect(this, type, satisfaction)
                .canAddSatisfaction();
    }

    private CriterionSatisfaction getNext(ICriterionType<?> type,
            CriterionSatisfaction newSatisfaction) {
        List<CriterionSatisfaction> ordered = query().from(type)
                .sortByStartDate().result();
        int position = findPlace(ordered, newSatisfaction);
        CriterionSatisfaction next = position != ordered.size() ? ordered
                .get(position) : null;
        return next;
    }

    private CriterionSatisfaction getPrevious(ICriterionType<?> type,
            CriterionSatisfaction newSatisfaction) {
        List<CriterionSatisfaction> ordered = query().from(type)
                .sortByStartDate().result();
        int position = findPlace(ordered, newSatisfaction);
        CriterionSatisfaction previous = position > 0 ? ordered
                .get(position - 1) : null;
        return previous;
    }

    public void removeCriterionSatisfaction(CriterionSatisfaction satisfaction)
            throws InstanceNotFoundException {
        criterionSatisfactions.remove(satisfaction);
    }

    public boolean contains(CriterionSatisfaction satisfaction) {
        return criterionSatisfactions.contains(satisfaction);
    }

}
