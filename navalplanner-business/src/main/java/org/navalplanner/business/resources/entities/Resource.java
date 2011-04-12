/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.resources.entities;

import static org.navalplanner.business.workingday.EffortDuration.zero;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertFalse;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ICalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.MultipleInstancesException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.navalplanner.business.planner.entities.AvailabilityCalculator;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;

/**
 * This class acts as the base class for all resources.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public abstract class Resource extends IntegrationEntity {

    public static class AllResourceAssignments implements IAssignmentsOnResourceCalculator {

        @Override
        public List<DayAssignment> getAssignments(Resource resource) {
            return resource.getAssignments();
        }
    }

    public static List<Machine> machines(
            Collection<? extends Resource> resources) {
        return filter(Machine.class, resources);
    }

    public static List<Worker> workers(Collection<? extends Resource> resources) {
        return filter(Worker.class, resources);
    }

    public static <T extends Resource> List<T> filter(Class<T> klass,
            Collection<? extends Resource> resources) {
        List<T> result = new ArrayList<T>();
        for (Resource each : resources) {
            if (klass.isInstance(each)) {
                result.add(klass.cast(each));
            }
        }
        return result;
    }

    public static List<Resource> sortByName(List<Resource> resources) {
        Collections.sort(resources, new Comparator<Resource>() {

            @Override
            public int compare(Resource o1, Resource o2) {
                if (o1.getName() == null) {
                    return 1;
                }
                if (o2.getName() == null) {
                    return -1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return resources;
    }

    public static String getCaptionFor(ResourceAllocation<?> resourceAllocation) {
        return getCaptionFor(resourceAllocation.getAssociatedResources());
    }

    public static String getCaptionFor(List<Resource> resources) {
        List<String> values = new ArrayList<String>();
        for (Resource each: resources) {
            values.add(each.getShortDescription());
        }
        return StringUtils.join(values, ", ");
    }

    private ResourceCalendar calendar;

    private Set<CriterionSatisfaction> criterionSatisfactions = new HashSet<CriterionSatisfaction>();

    private Set<DayAssignment> dayAssignments = new HashSet<DayAssignment>();

    private Map<LocalDate, List<DayAssignment>> assignmentsByDayCached = null;

    private Set<ResourcesCostCategoryAssignment> resourcesCostCategoryAssignments =
        new HashSet<ResourcesCostCategoryAssignment>();

    private ResourceType resourceType = ResourceType.NON_LIMITING_RESOURCE;

    private LimitingResourceQueue limitingResourceQueue;

    private void clearCachedData() {
        assignmentsByDayCached = null;
        dayAssignmentsState.clearCachedData();
    }

    private List<DayAssignment> getAssignmentsForDay(LocalDate date) {
        if (assignmentsByDayCached == null) {
            assignmentsByDayCached = DayAssignment.byDay(getAssignments());
        }
        List<DayAssignment> list = assignmentsByDayCached.get(date);
        if (list == null){
            return Collections.emptyList();
        }
        return list;
    }

    private abstract class DayAssignmentsState {

        private List<DayAssignment> cachedAssignments;

        abstract List<DayAssignment> calculateAssignments();

        List<DayAssignment> getAssignments() {
            if (cachedAssignments != null) {
                return cachedAssignments;
            }
            return cachedAssignments = calculateAssignments();
        }

        void clearCachedData() {
            cachedAssignments = null;
        }
    }

    private class UsingScenarioManager extends DayAssignmentsState {

        @Override
        List<DayAssignment> calculateAssignments() {
            List<DayAssignment> result = new ArrayList<DayAssignment>();
            Scenario current = Registry.getScenarioManager().getCurrent();
            for (DayAssignment each : dayAssignments) {
                if (each.getScenario() != null
                        && each.getScenario().equals(current)) {
                    result.add(each);
                }
            }
            return result;
        }
    }

    private class OnSpecifiedScenario extends DayAssignmentsState {
        private final Scenario currentScenario;

        private OnSpecifiedScenario(Scenario currentScenario) {
            Validate.notNull(currentScenario);
            this.currentScenario = currentScenario;
        }

        @Override
        List<DayAssignment> calculateAssignments() {
            List<DayAssignment> result = new ArrayList<DayAssignment>();
            for (DayAssignment each : dayAssignments) {
                if (isTransient(each)
                        || each.getScenario().equals(currentScenario)) {
                    result.add(each);
                }
            }
            return result;
        }

        private boolean isTransient(DayAssignment each) {
            return each.getScenario() == null;
        }
    }

    private DayAssignmentsState dayAssignmentsState = new UsingScenarioManager();

    @Valid
    public Set<CriterionSatisfaction> getCriterionSatisfactions() {
        Set<CriterionSatisfaction> satisfactionActives =
                new HashSet<CriterionSatisfaction>();
        for(CriterionSatisfaction satisfaction:criterionSatisfactions){
            if(!satisfaction.isIsDeleted()) {
                satisfactionActives.add(satisfaction);
            }
        }
        return satisfactionActives;
    }

    public CriterionSatisfaction getCriterionSatisfactionByCode(String code)
        throws InstanceNotFoundException {

        if (StringUtils.isBlank(code)) {
            throw new InstanceNotFoundException(code,
                 CriterionSatisfaction.class.getName());
        }

        for (CriterionSatisfaction i : criterionSatisfactions) {
            if (i.getCode().equalsIgnoreCase(StringUtils.trim(code))) {
                return i;
            }
        }

        throw new InstanceNotFoundException(code,
            CriterionSatisfaction.class.getName());

    }

    public abstract String getShortDescription();

    public abstract String getName();

    private interface IPredicate {
        public boolean accepts(CriterionSatisfaction satisfaction);
    }

    public class Query {

        private List<IPredicate> predicates = new ArrayList<IPredicate>();
        private Query() {

        }

        public Query from(final ICriterionType<?> type) {
            return withNewPredicate(new IPredicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    return type.contains(satisfaction.getCriterion());
                }
            });
        }

        private Query withNewPredicate(IPredicate newPredicate) {
            predicates.add(newPredicate);
            return this;
        }

        public Query at(LocalDate date) {
            return enforcedInAll(Interval.point(date));
        }

        public Query between(LocalDate start, LocalDate end) {
            return enforcedInAll(Interval.range(start, end));
        }

        public Query enforcedInAll(final Interval interval) {
            return withNewPredicate(new IPredicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    return satisfaction.isAlwaysEnforcedIn(interval);
                }
            });
        }

        public Query overlapsWith(final Interval interval) {
            return withNewPredicate(new IPredicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    return satisfaction.overlapsWith(interval);
                }
            });
        }

        public Query from(final Criterion criterion) {
            return withNewPredicate(new IPredicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    return satisfaction.getCriterion()
                            .isEquivalentOrIncludedIn(criterion);
                }
            });
        }

        /**
         * Method called to retrieve the result. If no predicate was set, it
         * returns all satisfactions
         * @return the satisfactions matched by all predicates specified ordered
         *         by start date.
         */
        public List<CriterionSatisfaction> result() {
            ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
            for (CriterionSatisfaction criterionSatisfaction : getCriterionSatisfactions()) {
                if (isAcceptedByAllPredicates(criterionSatisfaction)) {
                    result.add(criterionSatisfaction);
                }
            }
            Collections.sort(result, CriterionSatisfaction.BY_START_COMPARATOR);
            return result;
        }

        public List<CriterionSatisfaction> result(Set<CriterionSatisfaction> list) {
            ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
            for (CriterionSatisfaction criterionSatisfaction : list) {
                if (isAcceptedByAllPredicates(criterionSatisfaction)) {
                    result.add(criterionSatisfaction);
                }
            }
            Collections.sort(result, CriterionSatisfaction.BY_START_COMPARATOR);
            return result;
        }

        private boolean isAcceptedByAllPredicates(
                CriterionSatisfaction criterionSatisfaction) {
            for (IPredicate predicate : predicates) {
                if (!predicate.accepts(criterionSatisfaction)) {
                    return false;
                }
            }
            return true;
        }

        public Query current() {
            return withNewPredicate(new IPredicate() {

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

        public Query oneOf(ICriterionType<?>[] laboralRelatedTypes) {
            return oneOf(Arrays.asList(laboralRelatedTypes));
        }

        public Query oneOf(final Collection<? extends ICriterionType<?>> types) {
            return withNewPredicate(new IPredicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    for (ICriterionType<?> criterionType : types) {
                        if (criterionType.contains(satisfaction.getCriterion())) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

    }

    public Query query() {
        return new Query();
    }

    public Set<CriterionSatisfaction> getAllSatisfactions() {
        return new HashSet<CriterionSatisfaction>(criterionSatisfactions);
    }

    public Collection<CriterionSatisfaction> getSatisfactionsFor(
            ICriterionType<?> type) {
        return query().from(type).result();
    }

    public List<CriterionSatisfaction> getSatisfactionsFor(Criterion criterion) {
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
            Criterion criterion) {
        return query().from(criterion).current().result();
    }

    public CriterionSatisfaction addSatisfaction(
            CriterionWithItsType criterionWithItsType) {
        LocalDate today = new LocalDate();
        return addSatisfaction(criterionWithItsType, Interval.from(today));
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

        private final Resource resource;

        private final CriterionWithItsType type;

        private final Interval interval;

        CriterionSatisfaction addSatisfaction() {
            return resource.addSatisfaction(type, interval);
        }

        boolean canAddSatisfaction() {
            return resource.canAddSatisfaction(type, interval);
        }

    }

    public CriterionSatisfaction addSatisfaction(ICriterionType<?> type,
            CriterionSatisfaction satisfaction) {
        return new EnsureSatisfactionIsCorrect(this, type, satisfaction)
                .addSatisfaction();
    }

    public CriterionSatisfaction addSatisfaction(
            CriterionWithItsType criterionWithItsType, Interval interval){
        Criterion criterion = criterionWithItsType.getCriterion();
        ICriterionType<?> type = criterionWithItsType.getType();
        CriterionSatisfaction newSatisfaction = createNewSatisfaction(interval,
                criterion);
        if (canAddSatisfaction(criterionWithItsType, interval)) {
            newSatisfaction.validate();
            criterionSatisfactions.add(newSatisfaction);
            return newSatisfaction;
        }
        final String message = getReasonForNotAddingSatisfaction(type);
        throw new IllegalStateException(message);
    }

    private String getReasonForNotAddingSatisfaction(ICriterionType<?> type) {
        if (cannotApplyResourceToCriterionType(type)) {
            return "Cannot apply criterion of type " + type.getName()
                    + " to a " + getClass().getSimpleName();
        } else {
            return "Criterion satisfaction overlaps with other criterion satisfactions";
        }
    }

    private boolean cannotApplyResourceToCriterionType(ICriterionType<?> type) {
        return (type != null && !type.criterionCanBeRelatedTo(getClass()));
    }

    private CriterionSatisfaction createNewSatisfaction(Interval interval,
            Criterion criterion) {
        CriterionSatisfaction newSatisfaction = CriterionSatisfaction.create(criterion, this, interval);
        return newSatisfaction;
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
        LocalDate today = new LocalDate();
        return finishEnforcedAt(criterionWithItsType.getCriterion(), today);
    }

    public List<CriterionSatisfaction> finishEnforcedAt(Criterion criterion,
            LocalDate date) {
        ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
        for (CriterionSatisfaction criterionSatisfaction : query().from(
                criterion).at(date).result()) {
            criterionSatisfaction.finish(date);
            result.add(criterionSatisfaction);
        }
        return result;
    }

    public void modifySatisfaction(CriterionSatisfaction original,
            Interval interval){
        /* Create a temporal criterion satisfaction. */
        CriterionType type = original.getCriterion().getType();
        CriterionSatisfaction temporal = createNewSatisfaction(interval,
                original.getCriterion());
        temporal.setResource(this);

        boolean canAdd=false;
        if (contains(original)) {
            try{
                removeCriterionSatisfaction(original);
                canAdd = canAddSatisfaction(type, temporal);
                if(canAdd){
                    //update original
                    original.setStartDate(interval.getStart());
                    original.finish(interval.getEnd());
                }
                original.validate();
                criterionSatisfactions.add(original);
                if(!canAdd){
                    throw new IllegalStateException(
                        "This interval "+original.getCriterion().getName()+" not is valid because exists overlap with other criterion satisfaction");
                }
            }catch(IllegalArgumentException e){
                throw new IllegalArgumentException (original.getCriterion().getName()+" : "+e.getMessage());
            }
        }else{
             throw new IllegalStateException(
                    "The criterion satisfaction "+original.getCriterion().getName()+" not is activated for this resource");
        }
    }

    public boolean canAddSatisfaction(
            CriterionWithItsType criterionWithItsType, Interval interval) {
        CriterionSatisfaction satisfaction = createNewSatisfaction(interval, criterionWithItsType.getCriterion());
        return canAddSatisfaction(criterionWithItsType.getType(), satisfaction, this.getCriterionSatisfactions());
    }

    private boolean canAddSatisfaction(ICriterionType<?> type, CriterionSatisfaction satisfaction, Set<CriterionSatisfaction> satisfactions) {
        final Criterion criterion = satisfaction.getCriterion();
        final Interval interval = Interval.range(satisfaction.getStartDate(), satisfaction.getEndDate());

        if (!type.criterionCanBeRelatedTo(getClass())) {
            return false;
        }

        CriterionSatisfaction previousSameCriterion = getPreviousSameCriterion
        (criterion, satisfaction, satisfactions);
        CriterionSatisfaction posteriorSameCriterion = getNextSameCriterion
        (criterion, satisfaction, satisfactions);

        boolean canAdd = ((previousSameCriterion == null ||
                !previousSameCriterion.overlapsWith(interval)) &&
                ( posteriorSameCriterion == null ||
                        !posteriorSameCriterion.overlapsWith(interval)));

        if(!canAdd) {
            return false;
        }
        if (type.isAllowSimultaneousCriterionsPerResource()){
            return true;
        }

        CriterionSatisfaction previous = getPrevious(type , satisfaction, satisfactions);
        CriterionSatisfaction posterior = getNext(type, satisfaction, satisfactions);

        return (previous == null || !previous.overlapsWith(interval)) &&
        ( posterior == null || !posterior.overlapsWith(interval));
    }

    public boolean _canAddSatisfaction(
            CriterionWithItsType criterionWithItsType, Interval interval) {

        ICriterionType<?> type = criterionWithItsType.getType();
        Criterion criterion = criterionWithItsType.getCriterion();
        if (!type.criterionCanBeRelatedTo(getClass())) {
            return false;
        }
        CriterionSatisfaction newSatisfaction = createNewSatisfaction(interval,
                criterion);

        CriterionSatisfaction previousSameCriterion = getPreviousSameCriterion
                (criterion, newSatisfaction,this.getCriterionSatisfactions());
        CriterionSatisfaction posteriorSameCriterion = getNextSameCriterion
                (criterion, newSatisfaction,this.getCriterionSatisfactions());

        boolean canAdd = ((previousSameCriterion == null ||
                !previousSameCriterion.overlapsWith(interval)) &&
                ( posteriorSameCriterion == null ||
                !posteriorSameCriterion.overlapsWith(interval)));

        if(!canAdd) {
            return false;
        }
        if (type.isAllowSimultaneousCriterionsPerResource()){
            return true;
        }

        CriterionSatisfaction previous = getPrevious(criterionWithItsType
                .getType(), newSatisfaction,this.getCriterionSatisfactions());
        CriterionSatisfaction posterior = getNext(criterionWithItsType
                .getType(), newSatisfaction,this.getCriterionSatisfactions());

        return (previous == null || !previous.overlapsWith(interval)) &&
                ( posterior == null || !posterior.overlapsWith(interval));
    }

    public boolean canAddSatisfaction(ICriterionType<?> type,
            CriterionSatisfaction satisfaction) {
        EnsureSatisfactionIsCorrect ensureSatisfactionIsCorrect = new EnsureSatisfactionIsCorrect(
                this, type, satisfaction);
        return ensureSatisfactionIsCorrect.canAddSatisfaction();
    }

    private CriterionSatisfaction getNext(ICriterionType<?> type,
            CriterionSatisfaction newSatisfaction,Set<CriterionSatisfaction> list) {
        List<CriterionSatisfaction> ordered = query().from(type).result(list);
        int position = findPlace(ordered, newSatisfaction);
        CriterionSatisfaction next = position != ordered.size() ? ordered
                .get(position) : null;
        return next;
    }

    private CriterionSatisfaction getPrevious(ICriterionType<?> type,
            CriterionSatisfaction newSatisfaction,Set<CriterionSatisfaction> list) {
        List<CriterionSatisfaction> ordered = query().from(type).result(list);
        int position = findPlace(ordered, newSatisfaction);
        CriterionSatisfaction previous = position > 0 ? ordered
                .get(position - 1) : null;
        return previous;
    }


    private CriterionSatisfaction getNextSameCriterion(Criterion criterion,
            CriterionSatisfaction newSatisfaction,Set<CriterionSatisfaction> list) {
        List<CriterionSatisfaction> ordered = query().from(criterion).result(list);
        int position = findPlace(ordered, newSatisfaction);
        CriterionSatisfaction next = position != ordered.size() ? ordered
                .get(position) : null;
        return next;
    }

    private CriterionSatisfaction getPreviousSameCriterion(Criterion criterion,
            CriterionSatisfaction newSatisfaction,Set<CriterionSatisfaction> list) {
        List<CriterionSatisfaction> ordered = query().from(criterion).result(list);
        int position = findPlace(ordered, newSatisfaction);
        CriterionSatisfaction previous = position > 0 ? ordered
                .get(position - 1) : null;
        return previous;
    }

    public void removeCriterionSatisfaction(CriterionSatisfaction satisfaction) {
        criterionSatisfactions.remove(satisfaction);
    }

    public boolean contains(CriterionSatisfaction satisfaction) {
        return criterionSatisfactions.contains(satisfaction);
    }

    /**
     * @throws IllegalArgumentException in case of overlapping
     */
    public void checkNotOverlaps() {
        checkNotOverlaps(getRelatedTypes());
    }

    private List<CriterionType> getRelatedTypes() {
        List<CriterionType> types = new ArrayList<CriterionType>();
        for (CriterionSatisfaction criterionSatisfaction : this.getCriterionSatisfactions()) {
            types.add(criterionSatisfaction.getCriterion().getType());
        }
        return types;
    }

    /**
     * @throws IllegalArgumentException in case of overlapping
     */
    private void checkNotOverlaps(List<CriterionType> types) {
        for (CriterionType criterionType : types) {
            List<CriterionSatisfaction> satisfactions = query().from(
                    criterionType).result();
            ListIterator<CriterionSatisfaction> listIterator = satisfactions
                    .listIterator();
            while (listIterator.hasNext()) {
                CriterionSatisfaction current = listIterator.next();
                CriterionSatisfaction previous = getPrevious(listIterator);
                CriterionSatisfaction next = getNext(listIterator);
                if (previous != null) {
                    checkNotOverlaps(previous, current);
                }
                if (next != null) {
                    checkNotOverlaps(current, next);
                }
            }
        }
    }

    /**
     * IMPORTANT: <code>before</code> and <code>after</code> must refer to the
     * same <code>CriterionType</code>
     *
     * @throws IllegalArgumentException in case of overlapping
     */
    private void checkNotOverlaps(CriterionSatisfaction before,
            CriterionSatisfaction after) {

        CriterionType criterionType = before.getCriterion().getType();

        /*
         * If criterion satisfactions refer to the same Criterion, they must not
         * overlap (regardless of its CriterionType allows simultaneous
         * criterion satisfactions per resource).
         */
        if (before.getCriterion().equals(after.getCriterion()) &&
            !before.goesBeforeWithoutOverlapping(after)) {
                throw new IllegalArgumentException(createOverlapsMessage(before,
                        after));
         }

        /*
         * If CriterionType does not allow simultaneous criterion satisfactions
         * per resource, criterion satisfactions must not overlap (regardless
         * of they refer to different Criterion objects).
         */
        if (!criterionType.isAllowSimultaneousCriterionsPerResource() &&
            !before.goesBeforeWithoutOverlapping(after)) {
            throw new IllegalArgumentException(createOverlapsMessage(before,
                    after));
        }

    }

    private String createOverlapsMessage(CriterionSatisfaction before,
            CriterionSatisfaction after) {
        return new StringBuilder("the satisfaction").append(before).append(
                "overlaps with").append(after).toString();
    }

    private CriterionSatisfaction getNext(
            ListIterator<CriterionSatisfaction> listIterator) {
        if (listIterator.hasNext()) {
            CriterionSatisfaction result = listIterator.next();
            listIterator.previous();
            return result;
        }
        return null;
    }

    private CriterionSatisfaction getPrevious(
            ListIterator<CriterionSatisfaction> listIterator) {
        listIterator.previous();
        try {
            if (listIterator.hasPrevious()) {
                CriterionSatisfaction result = listIterator.previous();
                listIterator.next();
                return result;
            }
            return null;
        } finally {
            listIterator.next();
        }
    }

    public void setCalendar(ResourceCalendar calendar) {
        this.calendar = calendar;
        if (calendar != null) {
            calendar.setResource(this);
        }
    }

    public ResourceCalendar getCalendar() {
        return calendar;
    }

    public void setResourceCalendar(String calendarCode)
        throws InstanceNotFoundException, MultipleInstancesException {

        ResourceCalendar calendar;

        if (StringUtils.isBlank(calendarCode)) {
            calendar = Registry.getConfigurationDAO().getConfiguration().
                getDefaultCalendar().newDerivedResourceCalendar();

        } else {
            BaseCalendar baseCalendar = Registry.getBaseCalendarDAO()
                    .findByCode(calendarCode);
            calendar = baseCalendar.newDerivedResourceCalendar();
        }

        setCalendar(calendar);

    }

    public EffortDuration getAssignedEffort(LocalDate localDate) {
        return DayAssignment.sum(getAssignmentsForDay(localDate));
    }

    public EffortDuration getAssignedDurationDiscounting(
            Object alloationFromWhichDiscountHours, LocalDate day) {
        EffortDuration result = zero();
        for (DayAssignment dayAssignment : getAssignmentsForDay(day)) {
            if (!dayAssignment.belongsTo(alloationFromWhichDiscountHours)) {
                result = result.plus(dayAssignment.getDuration());
            }
        }
        return result;
    }

    public void addNewAssignments(Collection<? extends DayAssignment> assignments) {
        Validate.notNull(assignments);
        Validate.noNullElements(assignments);
        clearCachedData();
        this.dayAssignments.addAll(assignments);
    }

    public void removeAssignments(
            Collection<? extends DayAssignment> assignments) {
        Validate.noNullElements(assignments);
        clearCachedData();
        this.dayAssignments.removeAll(assignments);
    }

    public List<DayAssignment> getAssignments() {
        return dayAssignmentsState.getAssignments();
    }

    public void useScenario(Scenario scenario) {
        dayAssignmentsState = new OnSpecifiedScenario(scenario);
    }

    public int getTotalWorkHours(LocalDate start, LocalDate end) {
        return getTotalWorkHours(start, end, null);
    }

    public int getTotalWorkHours(LocalDate start, LocalDate endExclusive,
            ICriterion criterion) {
        return getTotalEffortFor(IntraDayDate.startOfDay(start),
                IntraDayDate.startOfDay(endExclusive), criterion)
                .roundToHours();
    }

    public EffortDuration getTotalEffortFor(IntraDayDate startInclusive,
            IntraDayDate endExclusive) {
        return getTotalEffortFor(startInclusive, endExclusive, null);
    }

    public EffortDuration getTotalEffortFor(IntraDayDate startInclusive,
            IntraDayDate endExclusive, ICriterion criterion) {
        return getTotalEffortFor(getCalendarOrDefault(), startInclusive,
                endExclusive, criterion);
    }

    public ICalendar getCalendarOrDefault() {
        return getCalendar() != null ? getCalendar() : SameWorkHoursEveryDay
                .getDefaultWorkingDay();
    }

    private EffortDuration getTotalEffortFor(ICalendar calendar,
            IntraDayDate startInclusive, IntraDayDate endExclusive,
            ICriterion criterionToSatisfy) {
        EffortDuration sum = zero();
        Iterable<PartialDay> daysBetween = startInclusive
                .daysUntil(endExclusive);
        for (PartialDay current : daysBetween) {
            EffortDuration capacityCurrent = calendar.getCapacityOn(current);
            if (capacityCurrent != null
                    && (criterionToSatisfy == null || satisfiesCriterionAt(
                            criterionToSatisfy, current.getDate()))) {
                sum = sum.plus(capacityCurrent);
            }
        }
        return sum;
    }

    private boolean satisfiesCriterionAt(ICriterion criterionToSatisfy,
            LocalDate current) {
        return criterionToSatisfy.isSatisfiedBy(this, current);
    }

    public void addUnvalidatedSatisfaction(CriterionSatisfaction
        criterionSatisfaction) {

        criterionSatisfactions.add(criterionSatisfaction);

    }

    public void addSatisfactions(Set<CriterionSatisfaction> addlist) throws ValidationException {
        //Create a newList with new Satisfactions and the old satisfactions
        Set<CriterionSatisfaction> newList = new HashSet<CriterionSatisfaction>(addlist);
        for(CriterionSatisfaction satisfaction : criterionSatisfactions){
            if(!newList.contains(satisfaction)){
                newList.add(satisfaction);
            }
        }
        //Create a activeList with not eliminated Satifaction
        Set<CriterionSatisfaction> activeList = new HashSet<CriterionSatisfaction>();
        for(CriterionSatisfaction satisfaction : addlist){
            if(!satisfaction.isIsDeleted()){
                activeList.add(satisfaction);
            }
        }
        validateSatisfactions(activeList);
        criterionSatisfactions.clear();
        criterionSatisfactions.addAll(newList);
    }

    private void validateSatisfactions(Set<CriterionSatisfaction> satisfactions)
    throws ValidationException {
        for (CriterionSatisfaction satisfaction : satisfactions) {
            final Set<CriterionSatisfaction> remainingSatisfactions = new HashSet<CriterionSatisfaction>();
            remainingSatisfactions.addAll(satisfactions);
            remainingSatisfactions.remove(satisfaction);
            validateSatisfaction(satisfaction, remainingSatisfactions);
        }
    }

    private void validateSatisfaction(CriterionSatisfaction satisfaction,
            Set<CriterionSatisfaction> satisfactions)
    throws ValidationException {

        if (!canAddSatisfaction(satisfaction, satisfactions)) {
            String message = getReasonForNotAddingSatisfaction(satisfaction
                    .getCriterion().getType());
            final InvalidValue invalidValue = new InvalidValue(message,
                    CriterionSatisfaction.class, "resource", this, satisfaction);
            throw new ValidationException(invalidValue);
        }
    }

    private boolean canAddSatisfaction(CriterionSatisfaction satisfaction, Set<CriterionSatisfaction> satisfactions) {
        final ICriterionType<?> type = satisfaction.getCriterion().getType();
        return canAddSatisfaction(type, satisfaction, satisfactions);
    }

    public boolean satisfiesCriterions(Collection<? extends ICriterion> criterions) {
        ICriterion compositedCriterion = CriterionCompounder.buildAnd(
                criterions).getResult();
        return compositedCriterion.isSatisfiedBy(this);
    }

    public boolean satisfiesCriterionsAtSomePoint(
            Collection<? extends Criterion> criterions) {
        AvailabilityTimeLine availability = AvailabilityCalculator
                .getCriterionsAvailabilityFor(criterions, this);
        return !availability.getValidPeriods().isEmpty();
    }

    @Valid
    public Set<ResourcesCostCategoryAssignment> getResourcesCostCategoryAssignments() {
        return resourcesCostCategoryAssignments;
    }

    public ResourcesCostCategoryAssignment
        getResourcesCostCategoryAssignmentByCode(String code)
        throws InstanceNotFoundException {

        if (StringUtils.isBlank(code)) {
            throw new InstanceNotFoundException(code,
                ResourcesCostCategoryAssignment.class.getName());
        }

        for (ResourcesCostCategoryAssignment i :
            resourcesCostCategoryAssignments) {

            if (i.getCode().equalsIgnoreCase(StringUtils.trim(code))) {
                return i;
            }

        }

        throw new InstanceNotFoundException(code,
            ResourcesCostCategoryAssignment.class.getName());

    }

    public void addResourcesCostCategoryAssignment(ResourcesCostCategoryAssignment assignment) {
        resourcesCostCategoryAssignments.add(assignment);
        if (assignment.getResource() != this) {
            assignment.setResource(this);
        }
    }

    public void addUnvalidatedResourcesCostCategoryAssignment(
        ResourcesCostCategoryAssignment assignment) {

        resourcesCostCategoryAssignments.add(assignment);

    }

    public void removeResourcesCostCategoryAssignment(ResourcesCostCategoryAssignment assignment) {
        resourcesCostCategoryAssignments.remove(assignment);
        if (assignment.getResource() == this) {
            assignment.setResource(null);
        }
    }

    @AssertTrue(message="Some criterion satisfactions overlap in time")
    public boolean checkConstraintCriterionSatisfactionsOverlapping() {

        /*
         * Check if time intervals in criterion satisfactions are correct in
         * isolation. If not, it does not make sense to check criterion
         * satisfaction overlapping.
         */
        for (CriterionSatisfaction i : getCriterionSatisfactions()) {

            if (!(i.isStartDateSpecified() &&
                  i.checkConstraintPositiveTimeInterval())) {
                return true;
            }

        }

        /*
         * Check assignment overlapping.
         */
        try {
            checkNotOverlaps();
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;

    }

    @AssertFalse(message="Some cost category assignments overlap in time")
    public boolean checkConstraintAssignmentsOverlapping() {

        /*
         * Check if time intervals in cost assignments are correct in isolation.
         * If not, it does not make sense to check assignment overlapping.
         */
        for (ResourcesCostCategoryAssignment each : getResourcesCostCategoryAssignments()) {
            if (!(each.isInitDateSpecified() && each
                    .checkConstraintPositiveTimeInterval())) {
                return false;
            }
        }

        /*
         * Check assignment overlapping.
         */
        List<ResourcesCostCategoryAssignment> assignmentsList =
            new ArrayList<ResourcesCostCategoryAssignment>();
        assignmentsList.addAll(getResourcesCostCategoryAssignments());

        try {
            CostCategory.validateCostCategoryOverlapping(assignmentsList);
        } catch (ValidationException e) {
            return true;
        }
        return false;

    }

    public abstract ResourceEnum getType();

    public boolean isVirtual() {
        return false;
    }

    @AssertTrue(message="there exist criterion satisfactions referring to " +
        "criterion types not applicable to this resource")
    public boolean checkConstraintCriterionSatisfactionsWithCorrectType() {

        for (CriterionSatisfaction c : getCriterionSatisfactions()) {
            if (!isCriterionSatisfactionOfCorrectType(c)) {
                return false;
            }
        }

        return true;

    }

    @AssertTrue(message="criterion satisfaction codes must be unique inside " +
        "a resource")
    public boolean checkConstraintNonRepeatedCriterionSatisfactionCodes() {
        return getFirstRepeatedCode(criterionSatisfactions) == null;
    }

    @AssertTrue(message="resources cost category assignment codes must be " +
        "unique inside a resource")
    public boolean checkConstraintNonRepeatedResourcesCostCategoryAssignmentCodes() {
        return getFirstRepeatedCode(resourcesCostCategoryAssignments) == null;
    }

    protected abstract boolean isCriterionSatisfactionOfCorrectType(
            CriterionSatisfaction c);

    protected IResourceDAO getIntegrationEntityDAO() {
        return Registry.getResourceDAO();
    }

    public Boolean isLimitingResource() {
        return (resourceType == ResourceType.LIMITING_RESOURCE);
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public LimitingResourceQueue getLimitingResourceQueue() {
        return limitingResourceQueue;
    }

    public void setLimitingResourceQueue(LimitingResourceQueue limitingResourceQueue) {
        limitingResourceQueue.setResource(this);
        this.limitingResourceQueue = limitingResourceQueue;
    }

}
