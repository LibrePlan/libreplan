/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertFalse;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.Valid;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.IWorkHours;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.navalplanner.business.planner.entities.DayAssignment;

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
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public abstract class Resource extends BaseEntity{

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

    private ResourceCalendar calendar;

    private Set<CriterionSatisfaction> criterionSatisfactions = new HashSet<CriterionSatisfaction>();

    private Set<DayAssignment> dayAssignments = new HashSet<DayAssignment>();

    private Map<LocalDate, List<DayAssignment>> assignmentsByDayCached = null;

    private Set<ResourcesCostCategoryAssignment> resourcesCostCategoryAssignments =
        new HashSet<ResourcesCostCategoryAssignment>();

    private void clearCachedData() {
        assignmentsByDayCached = null;
    }

    private List<DayAssignment> getAssignmentsForDay(LocalDate date) {
        if (assignmentsByDayCached == null) {
            assignmentsByDayCached = DayAssignment.byDay(dayAssignments);
        }
        List<DayAssignment> list = assignmentsByDayCached.get(date);
        if (list == null){
            return Collections.emptyList();
        }
        return list;
    }

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

        public Query at(Date date) {
            return enforcedInAll(Interval.point(date));
        }

        public Query between(Date start, Date end) {
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

        public Query from(final ICriterion criterion) {
            return withNewPredicate(new IPredicate() {

                @Override
                public boolean accepts(CriterionSatisfaction satisfaction) {
                    return satisfaction.getCriterion().isEquivalent(criterion);
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
        return finishEnforcedAt(criterionWithItsType.getCriterion(), new Date());
    }

    public List<CriterionSatisfaction> finishEnforcedAt(Criterion criterion,
            Date date) {
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

    private void checkNotOverlaps(List<CriterionType> types) {
        for (CriterionType criterionType : types) {
            if (!criterionType.isAllowSimultaneousCriterionsPerResource()) {
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

    }

    private void checkNotOverlaps(CriterionSatisfaction before,
            CriterionSatisfaction after) {
        if (!before.goesBeforeWithoutOverlapping(after)) {
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

    public int getAssignedHours(LocalDate localDate) {
        int sum = 0;
        for (DayAssignment dayAssignment : getAssignmentsForDay(localDate)) {
            sum += dayAssignment.getHours();
        }
        return sum;
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
        return new ArrayList<DayAssignment>(dayAssignments);
    }

    public int getTotalWorkHours(LocalDate start, LocalDate end) {
        return getTotalWorkHoursFor(calendarOrDefault(), start, end);
    }

    private IWorkHours calendarOrDefault() {
        return getCalendar() != null ? getCalendar() : SameWorkHoursEveryDay
                .getDefaultWorkingDay();
    }

    private int getTotalWorkHoursFor(IWorkHours calendar, LocalDate start,
            LocalDate end) {
        int sum = 0;
        final int days = Days.daysBetween(start, end).getDays();
        for (int i = 0; i < days; i++) {
            LocalDate current = start.plusDays(i);
            Integer workableHours = calendar.getCapacityAt(current);
            if (workableHours != null) {
                sum += workableHours;
            }
        }
        return sum;
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

    @Valid
    public Set<ResourcesCostCategoryAssignment> getResourcesCostCategoryAssignments() {
        return resourcesCostCategoryAssignments;
    }

    public void addResourcesCostCategoryAssignment(ResourcesCostCategoryAssignment assignment) {
        resourcesCostCategoryAssignments.add(assignment);
        if(assignment.getResource()!=this)
            assignment.setResource(this);
    }

    public void removeResourcesCostCategoryAssignment(ResourcesCostCategoryAssignment assignment) {
        resourcesCostCategoryAssignments.remove(assignment);
        if(assignment.getResource()==this)
            assignment.setResource(null);
    }

    @AssertFalse(message="Two assignments overlap in time")
    public boolean checkAssignmentsOverlap() {
        List<ResourcesCostCategoryAssignment> assignmentsList =
            new ArrayList<ResourcesCostCategoryAssignment>();
        assignmentsList.addAll(getResourcesCostCategoryAssignments());
        for(int i=0; i<assignmentsList.size(); i++) {
            LocalDate initDate = assignmentsList.get(i).getInitDate();
            LocalDate endDate = assignmentsList.get(i).getEndDate();
            for(int j=i+1; j<assignmentsList.size(); j++) {
                ResourcesCostCategoryAssignment listElement = assignmentsList.get(j);
                if (initDate == null || listElement.getInitDate() == null) {
                    //this is not exactly an overlapping but a
                    //problem with missing compulsory fields
                    return true;
                }
                if (endDate == null && listElement.getEndDate() == null) {
                    return true;
                }
                else if((endDate == null && listElement.getEndDate().compareTo(initDate)>=0) ||
                        (listElement.getEndDate() == null && listElement.getInitDate().compareTo(endDate)<=0)) {
                    return true;
                }
                else if((endDate != null && listElement.getEndDate() != null) &&
                        ((listElement.getEndDate().compareTo(initDate)>=0 &&
                        listElement.getEndDate().compareTo(endDate)<=0) ||
                        (listElement.getInitDate().compareTo(initDate)>=0 &&
                                listElement.getInitDate().compareTo(endDate)<=0))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isVirtual() {
        return false;
    }

    @AssertTrue(message="There are criterion satisfactions referring to " +
        "criterion types not applicable to this resource")
    public boolean checkConstraintCriterionSatisfactionsWithCorrectType() {

        for (CriterionSatisfaction c : getCriterionSatisfactions()) {
            if (!isCriterionSatisfactionOfCorrectType(c)) {
                return false;
            }
        }

        return true;

    }

    protected boolean isCriterionSatisfactionOfCorrectType(
        CriterionSatisfaction c) {

        return c.getResourceType().equals(ResourceEnum.RESOURCE);

    }

}
