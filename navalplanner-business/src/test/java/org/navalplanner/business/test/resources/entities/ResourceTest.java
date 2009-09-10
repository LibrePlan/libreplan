package org.navalplanner.business.test.resources.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest.year;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.planner.entities.DayAssigment;
import org.navalplanner.business.planner.entities.SpecificDayAssigment;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionTypeBase;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Interval;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.test.resources.daos.CriterionDAOTest;
import org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest;

/**
 * Tests for {@link Resource}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ResourceTest {

    @Test
    public void testGetSatisfactionsForCriterion() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion other = CriterionDAOTest.createValidCriterion();
        CriterionTypeBase type = createTypeThatMatches(false, criterion);
        CriterionTypeBase otherType = createTypeThatMatches(false, other);
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        assertThat(worker.getSatisfactionsFor(criterion).size(), equalTo(0));
        worker.addSatisfaction(new CriterionWithItsType(type, criterion));
        assertTrue(criterion.isSatisfiedBy(worker));
        assertThat(worker.getSatisfactionsFor(criterion).size(), equalTo(1));
        worker.addSatisfaction(new CriterionWithItsType(otherType, other));
        assertTrue(other.isSatisfiedBy(worker));
        assertThat(worker.getSatisfactionsFor(other).size(), equalTo(1));
        assertThat(worker.getSatisfactionsFor(criterion).size(), equalTo(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSatisfactionsForWrongIntervalThrowsException() {
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        worker.query().from(CriterionDAOTest.createValidCriterion())
                .enforcedInAll(Interval.range(year(2000), year(1999))).result();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSatisfactionsForWrongIntervalForCriterionTypeThrowsException() {
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        worker.query().from(createTypeThatMatches()).enforcedInAll(
                Interval.range(year(2000), year(1999))).current().result();
    }

    @Test
    public void testRetrieveActiveCriterionsForCriterionType() throws Exception {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<Criterion> criterionType = createTypeThatMatches(criterion);
        CriterionWithItsType criterionWithItsType = new CriterionWithItsType(
                criterionType, criterion);
        CriterionWithItsType otherCriterionWithItsType = new CriterionWithItsType(
                createTypeThatMatches(otherCriterion), otherCriterion);
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        worker.addSatisfaction(criterionWithItsType, Interval
                .from(CriterionSatisfactionDAOTest.year(2000)));
        worker.addSatisfaction(otherCriterionWithItsType, Interval
                .from(CriterionSatisfactionDAOTest.year(2000)));
        worker.addSatisfaction(criterionWithItsType, Interval
                .from(CriterionSatisfactionDAOTest.year(4000)));
        assertEquals(2, worker.getSatisfactionsFor(criterionType).size());
        assertEquals(1, worker.getCurrentSatisfactionsFor(criterionType).size());
    }

    @Test
    public void testActiveCriterions() throws Exception {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<Criterion> type = createTypeThatMatches(criterion,
                otherCriterion);
        CriterionWithItsType criterionWithItsType = new CriterionWithItsType(
                type, criterion);
        CriterionWithItsType otherCriterionWithItsType = new CriterionWithItsType(
                type, otherCriterion);
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        assertThat(worker.getCurrentCriterionsFor(type).size(), equalTo(0));
        worker.addSatisfaction(criterionWithItsType, Interval
                .from(CriterionSatisfactionDAOTest.year(2000)));
        assertThat(worker.getCurrentCriterionsFor(type).size(), equalTo(1));
        worker.addSatisfaction(criterionWithItsType, Interval
                .from(CriterionSatisfactionDAOTest.year(2002)));
        assertThat(worker.getCurrentCriterionsFor(type).size(), equalTo(1));
        worker.addSatisfaction(otherCriterionWithItsType, Interval
                .from(CriterionSatisfactionDAOTest.year(2000)));
        assertThat(worker.getCurrentCriterionsFor(type).size(), equalTo(2));
    }

    public static CriterionTypeBase createTypeThatMatches(
            final Criterion... criterions) {
        return createTypeThatMatches(true, criterions);
    }

    public static CriterionTypeBase createTypeThatMatches(
            boolean allowMultipleCriterionsPerResource,
            final Criterion... criterions) {
        final HashSet<Criterion> criterionsSet = new HashSet<Criterion>(Arrays
                .asList(criterions));
        return new CriterionTypeBase("base", true,
                allowMultipleCriterionsPerResource, false, false) {

            @Override
            public boolean contains(ICriterion c) {
                return criterionsSet.contains(c);
            }

            @Override
            public Criterion createCriterion(String name) {
                return null;
            }

            @Override
            public boolean criterionCanBeRelatedTo(
                    Class<? extends Resource> klass) {
                return true;
            }

            @Override
            public Criterion createCriterionWithoutNameYet() {
                return null;
            }
        };
    }

    @Test
    public void testRetrieveSatisfactionsInIntervalForCriterionType()
            throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<Criterion> criterionType = createTypeThatMatches(criterion);
        CriterionWithItsType criterionWithItsType = new CriterionWithItsType(
                criterionType, criterion);
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        CriterionWithItsType otherCriterionWithItsType = new CriterionWithItsType(
                createTypeThatMatches(otherCriterion), otherCriterion);

        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        worker.addSatisfaction(criterionWithItsType, Interval
                .from(CriterionSatisfactionDAOTest.year(2000)));
        worker.addSatisfaction(criterionWithItsType, Interval
                .from(CriterionSatisfactionDAOTest.year(2003)));
        worker.addSatisfaction(otherCriterionWithItsType, Interval
                .from(CriterionSatisfactionDAOTest.year(2000)));

        assertEquals(2, worker.getSatisfactionsFor(criterionType).size());
        assertEquals(1, worker.query().from(criterionType).enforcedInAll(
                Interval.range(year(2001), year(2005))).current().result()
                .size());
        assertEquals(2, worker.query().from(criterionType).enforcedInAll(
                Interval.range(year(2004), year(2005))).current().result()
                .size());
        assertEquals(0, worker.query().from(criterionType).enforcedInAll(
                Interval.range(year(1999), year(2005))).current().result()
                .size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidadCriterionWithItsTypeNotAllowd() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<Criterion> criterionType = createTypeThatMatches(criterion);
        new CriterionWithItsType(criterionType, otherCriterion);
    }

    @Test
    public void testAddAndRemoveSatisfactions() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        assertThat(worker.getCurrentSatisfactionsFor(criterion).size(),
                equalTo(0));
        assertFalse(criterion.isSatisfiedBy(worker));
        Interval fromNow = Interval.from(new Date());
        assertTrue(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, criterion), fromNow));
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), fromNow);
        assertTrue(criterion.isSatisfiedBy(worker));
        assertThat(worker.getCurrentSatisfactionsFor(criterion).size(),
                equalTo(1));
        assertFalse(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, otherCriterion), fromNow));
        try {
            worker.addSatisfaction(new CriterionWithItsType(criterionType,
                    otherCriterion));
            fail("must send exception since it already is activated for a criterion of the same type and the type doesn't allow repeated criterions per resource");
        } catch (IllegalStateException e) {
            // ok
        }
        assertThat(worker.query().from(criterionType).enforcedInAll(fromNow)
                .result().size(), equalTo(1));
        List<CriterionSatisfaction> finished = worker.finishEnforcedAt(
                criterion, fromNow.getStart());
        assertThat(finished.size(), equalTo(1));
        assertTrue("all satisfactions are finished", worker.query().from(
                criterionType).enforcedInAll(fromNow).result().isEmpty());
        assertTrue(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, criterion), fromNow));
    }

    @Test
    public void testAddAtDate() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), Interval.range(year(5000), year(6000)));
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), Interval.from(year(4000)));

        assertThat(worker.query().from(criterion).enforcedInAll(
                Interval.range(year(4001), year(4999))).result().size(),
                equalTo(1));
        assertThat(worker.query().from(criterion).enforcedInAll(
                Interval.range(year(4001), year(5000))).result().size(),
                equalTo(0));
        assertThat(worker.query().from(criterion).enforcedInAll(
                Interval.range(year(5000), year(5001))).result().size(),
                equalTo(1));
        assertThat(worker.query().from(criterion).enforcedInAll(
                Interval.range(year(5001), year(5500))).result().size(),
                equalTo(1));

        worker.finish(new CriterionWithItsType(criterionType, criterion));

        assertThat(worker.query().from(criterion).enforcedInAll(
                Interval.range(year(4001), year(4999))).result().size(),
                equalTo(1));
        assertThat(worker.query().from(criterion).enforcedInAll(
                Interval.range(year(5001), year(5500))).result().size(),
                equalTo(1));

        assertFalse(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, otherCriterion), Interval
                .from(CriterionSatisfactionDAOTest.year(4001))));
    }

    @Test
    // when type doesnt allow multiple active criterions per resource
    public void addOnlyUntilNextCriterionIsActive() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), Interval.from(year(4000)));
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                otherCriterion), Interval.from(year(3500)));
        assertThat(worker.getSatisfactionsFor(otherCriterion).size(),
                equalTo(1));
        CriterionSatisfaction satisfaction = worker.getSatisfactionsFor(
                otherCriterion).iterator().next();
        assertThat(satisfaction.getEndDate(), equalTo(year(4000)));
    }

    @Test(expected = IllegalStateException.class)
    // when type doesnt allow multiple active criterions per resource
    public void testCantAddOverlappingTotally() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                otherCriterion), Interval.from(year(3500)));
        assertFalse(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, criterion), Interval.range(year(4000),
                year(5000))));
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), Interval.range(year(4000), year(5000)));
    }

    @Test(expected = IllegalStateException.class)
    public void testCantAddIfOverlapsPartially() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                otherCriterion), Interval.range(year(3500), year(4500)));
        assertFalse(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, criterion), Interval.range(year(3600),
                year(3800))));
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), Interval.range(year(3600), year(3800)));
    }

    @Test
    public void testCantAddWrongCriterionSatisfaction() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        Worker other = Worker.create("other", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion);
        List<CriterionSatisfaction> wrongSatisfactions = new ArrayList<CriterionSatisfaction>();
        CriterionSatisfaction satisfaction = createValid(criterion, worker);
        satisfaction.setResource(other);
        wrongSatisfactions.add(satisfaction);
        satisfaction = createValid(criterion, worker);
        satisfaction.setResource(null);
        wrongSatisfactions.add(satisfaction);
        satisfaction = createValid(criterion, worker);
        satisfaction.setCriterion(otherCriterion);
        wrongSatisfactions.add(satisfaction);
        for (CriterionSatisfaction wrong : wrongSatisfactions) {
            try {
                worker.addSatisfaction(criterionType, wrong);
                fail("must send exception");
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }

    public void testAddCriterionSatisfaction() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion);
        CriterionSatisfaction satisfaction = createValid(criterion, worker);
        worker.addSatisfaction(criterionType, satisfaction);
        assertThat(worker.getAllSatisfactions().size(), equalTo(1));
    }

    private CriterionSatisfaction createValid(Criterion criterion, Worker worker) {
        CriterionSatisfaction satisfaction = CriterionSatisfaction.create();
        satisfaction.setResource(worker);
        satisfaction.setStartDate(year(2000));
        satisfaction.setCriterion(criterion);
        satisfaction.setEndDate(year(2004));
        return satisfaction;
    }

    @Test(expected = IllegalStateException.class)
    public void shouldntAdd() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232", 10);
        ICriterionType<?> type = new CriterionTypeBase("prueba", false, false,
                false, false) {

            @Override
            public boolean contains(ICriterion c) {
                return true;
            }

            @Override
            public Criterion createCriterion(String name) {
                return null;
            }

            @Override
            public boolean criterionCanBeRelatedTo(
                    Class<? extends Resource> klass) {
                return false;
            }

            @Override
            public Criterion createCriterionWithoutNameYet() {
                return null;
            }
        };
        CriterionWithItsType criterionWithItsType = new CriterionWithItsType(
                type, criterion);
        assertFalse(worker.canAddSatisfaction(criterionWithItsType, Interval
                .from(new Date())));
        worker.addSatisfaction(criterionWithItsType);
    }

    private Worker worker;
    private List<DayAssigment> assigments;

    @Test(expected = IllegalArgumentException.class)
    public void addNewAssigmentsMustReceiveNotNullArgument() {
        givenWorker();
        worker.addNewAssigments(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustHaveNoNullElements() {
        givenWorker();
        List<DayAssigment> list = new ArrayList<DayAssigment>();
        list.add(null);
        worker.addNewAssigments(list);
    }

    @Test
    public void newAssigmentsImportsTheAssigments() {
        givenWorker();
        LocalDate today = new LocalDate();
        LocalDate tomorrow = today.plus(Days.days(1));
        SpecificDayAssigment specificDayAssigment = new SpecificDayAssigment(
                today, 10, worker);
        SpecificDayAssigment another = new SpecificDayAssigment(tomorrow, 10,
                worker);
        givenWorkerWithAssigments(specificDayAssigment, another);


        assertTrue(worker.getAssigments().containsAll(assigments));
        assertTrue(worker.getAssigments().size() == assigments.size());
    }

    @Test
    public void addingAdditionalAssigmentsKeepOld() {
        givenWorker();
        LocalDate today = new LocalDate();
        LocalDate tomorrow = today.plus(Days.days(1));
        SpecificDayAssigment specificDayAssigment = new SpecificDayAssigment(
                today, 10, worker);
        SpecificDayAssigment another = new SpecificDayAssigment(tomorrow, 10,
                worker);
        givenWorkerWithAssigments(specificDayAssigment, another);

        DayAssigment other = new SpecificDayAssigment(today, 3, worker);
        worker.addNewAssigments(Arrays.asList(other));
        assertTrue(worker.getAssigments().size() == assigments.size() + 1);
    }


    private void givenWorkerWithAssigments(DayAssigment... assigments) {
        this.assigments = Arrays.asList(assigments);
        worker.addNewAssigments(this.assigments);
    }

    private void givenWorker() {
        worker = Worker.create("firstName", "surName", "2333232", 10);
    }

}
