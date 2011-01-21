/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.test.resources.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest.date;
import static org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest.year;
import static org.navalplanner.business.workingday.EffortDuration.hours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionTypeBase;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Interval;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.bootstrap.IScenariosBootstrap;
import org.navalplanner.business.test.resources.daos.CriterionDAOTest;
import org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link Resource}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class ResourceTest {

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioManager scenarioManager;

    @Before
    public void loadRequiredData() {
        scenariosBootstrap.loadRequiredData();
    }

    @Test
    public void testGetSatisfactionsForCriterion() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion other = CriterionDAOTest.createValidCriterion();
        CriterionTypeBase type = createTypeThatMatches(false, criterion);
        CriterionTypeBase otherType = createTypeThatMatches(false, other);
        Worker worker = Worker.create("firstName", "surName", "2333232");
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
        Worker worker = Worker.create("firstName", "surName", "2333232");
        worker.query().from(CriterionDAOTest.createValidCriterion())
                .enforcedInAll(Interval.range(year(2000), year(1999))).result();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSatisfactionsForWrongIntervalForCriterionTypeThrowsException() {
        Worker worker = Worker.create("firstName", "surName", "2333232");
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
        Worker worker = Worker.create("firstName", "surName", "2333232");
        worker.addSatisfaction(criterionWithItsType, Interval
                .range(year(2000),year(3000)));
        worker.addSatisfaction(otherCriterionWithItsType, Interval
                .from(year(2000)));
        worker.addSatisfaction(criterionWithItsType, Interval
                .from(year(4000)));
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
        Worker worker = Worker.create("firstName", "surName", "2333232");
        assertThat(worker.getCurrentCriterionsFor(type).size(), equalTo(0));
        worker.addSatisfaction(criterionWithItsType, Interval
                .range(year(2000),year(2020)));
        assertThat(worker.getCurrentCriterionsFor(type).size(), equalTo(1));
        worker.addSatisfaction(criterionWithItsType, Interval
                .from(year(2020)));
        assertThat(worker.getCurrentCriterionsFor(type).size(), equalTo(1));
        worker.addSatisfaction(otherCriterionWithItsType, Interval
                .from(year(2000)));
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
        return new CriterionTypeBase("base","", true,
                allowMultipleCriterionsPerResource,true) {

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

        Worker worker = Worker.create("firstName", "surName", "2333232");
        worker.addSatisfaction(criterionWithItsType, Interval
                .range(year(2000),year(2002)));
        worker.addSatisfaction(criterionWithItsType, Interval
                .from(year(2003)));
        worker.addSatisfaction(otherCriterionWithItsType, Interval
                .from(year(2000)));

        assertEquals(2, worker.getSatisfactionsFor(criterionType).size());
        assertEquals(0, worker.query().from(criterionType).enforcedInAll(
                Interval.range(year(2001), year(2005))).current().result()
                .size());
        assertEquals(1, worker.query().from(criterionType).enforcedInAll(
                Interval.range(year(2000), year(2001))).result()
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
        Worker worker = Worker.create("firstName", "surName", "2333232");
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        assertThat(worker.getCurrentSatisfactionsFor(criterion).size(),
                equalTo(0));
        assertFalse(criterion.isSatisfiedBy(worker));
        Interval fromNow = Interval.from(new LocalDate());
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
        Worker worker = Worker.create("firstName", "surName", "2333232");
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), Interval.range(year(5000), year(6000)));
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), Interval.range(year(4000),year(5000)));

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
        Worker worker = Worker.create("firstName", "surName", "2333232");
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), Interval.from(year(4000)));
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                otherCriterion), Interval.range(year(3500),year(4000)));
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
        Worker worker = Worker.create("firstName", "surName", "2333232");
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
        Worker worker = Worker.create("firstName", "surName", "2333232");
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
        Worker worker = Worker.create("firstName", "surName", "2333232");
        Worker other = Worker.create("other", "surName", "2333232");
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

    @Test
    public void testCantAddCriterionSatisfactionWithOverlap() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232");
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion,otherCriterion);

        Interval intervalA = Interval.range(date(2009,10,14),date(2009,10,26));
        Interval intervalB = Interval.range(date(2009,10,15),date(2009,10,24));
        Interval intervalC = Interval.range(date(2009,10,12),date(2009,10,16));
        Interval intervalE = Interval.range(date(2009,10,26),date(2009,10,30));
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), intervalA);
        //Same Criterion
        assertFalse(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, criterion), intervalB));
        assertFalse(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, criterion), intervalC));
        //Distict Criterion
        assertFalse(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, otherCriterion), intervalC));
        assertTrue(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, criterion), intervalE));
    }

    @Test
    public void testCantAddCriterionSatisfactionWithTypeAllowMultipleResource() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232");
        ICriterionType<Criterion> criterionType = createTypeThatMatches(true,
                criterion,otherCriterion);

        Interval intervalA = Interval.range(date(2009,10,8),date(2009,10,25));
        Interval intervalB = Interval.range(date(2009,10,5),date(2009,10,9));
        Interval intervalC = Interval.range(date(2009,11,12),date(2009,11,16));
        Interval intervalE = Interval.range(date(2009,10,26),date(2009,10,30));
        worker.addSatisfaction(new CriterionWithItsType(criterionType,
                criterion), intervalA);
        //Same Criterion
        assertFalse(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, criterion), intervalB));
        //Distinct Criterion
        assertTrue(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, otherCriterion), intervalC));
        //without overlap
        assertTrue(worker.canAddSatisfaction(new CriterionWithItsType(
                criterionType, criterion), intervalE));
    }

    public void testAddCriterionSatisfaction() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232");
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
        Worker worker = Worker.create("firstName", "surName", "2333232");
        ICriterionType<?> type = new CriterionTypeBase("prueba","", false, false,
                true) {

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
        LocalDate today = new LocalDate();
        assertFalse(worker.canAddSatisfaction(criterionWithItsType,
                Interval.from(today)));
        worker.addSatisfaction(criterionWithItsType);
    }

    private Worker worker;
    private List<DayAssignment> assignments;

    @Test(expected = IllegalArgumentException.class)
    public void addNewAssignmentsMustReceiveNotNullArgument() {
        givenWorker();
        worker.addNewAssignments(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustHaveNoNullElements() {
        givenWorker();
        List<DayAssignment> list = new ArrayList<DayAssignment>();
        list.add(null);
        worker.addNewAssignments(list);
    }

    @Test
    public void newAssignmentsImportsTheAssignments() {
        givenWorker();
        LocalDate today = new LocalDate();
        LocalDate tomorrow = today.plus(Days.days(1));
        SpecificDayAssignment specificDayAssignment = new SpecificDayAssignment(
                today, hours(10), worker);
        SpecificDayAssignment another = new SpecificDayAssignment(tomorrow,
                hours(10),
                worker);
        givenWorkerWithAssignments(specificDayAssignment, another);


        assertTrue(worker.getAssignments().containsAll(assignments));
        assertTrue(worker.getAssignments().size() == assignments.size());
    }

    @Test
    public void addingAdditionalAssignmentsKeepOld() {
        givenWorker();
        LocalDate today = new LocalDate();
        LocalDate tomorrow = today.plus(Days.days(1));
        SpecificDayAssignment specificDayAssignment = new SpecificDayAssignment(
                today, hours(10), worker);
        SpecificDayAssignment another = new SpecificDayAssignment(tomorrow,
                hours(10),
                worker);
        givenWorkerWithAssignments(specificDayAssignment, another);

        DayAssignment other = new SpecificDayAssignment(today, hours(3), worker);
        worker.addNewAssignments(Arrays.asList(other));
        assertTrue(worker.getAssignments().size() == assignments.size() + 1);
    }

    @Test
    public void workerWithoutAssignmentsGivesNoAssignedHours() {
        givenWorker();
        LocalDate today = new LocalDate();
        assertThat(worker.getAssignedHours(today), equalTo(0));
    }

    @Test
    public void workerWithAssignmentsGivesTheSumOfAssignedHoursForThatDay() {
        givenWorker();
        LocalDate today = new LocalDate();
        SpecificDayAssignment specificDayAssignment = new SpecificDayAssignment(
                today, hours(10), worker);
        SpecificDayAssignment another = new SpecificDayAssignment(today,
                hours(3), worker);
        SpecificDayAssignment atAnotherDay = new SpecificDayAssignment(
                today.plusDays(1), hours(1), worker);
        givenWorkerWithAssignments(specificDayAssignment, another, atAnotherDay);

        assertThat(worker.getAssignedHours(today), equalTo(13));
    }

    @Test
    public void afterAddingAnotherDontReturnTheOldResult() {
        givenWorker();
        LocalDate today = new LocalDate();
        SpecificDayAssignment specificDayAssignment = new SpecificDayAssignment(
                today, hours(10), worker);
        givenWorkerWithAssignments(specificDayAssignment);
        worker.getAssignedHours(today);
        SpecificDayAssignment another = new SpecificDayAssignment(today,
                hours(3), worker);
        worker.addNewAssignments(Arrays.asList(another));

        assertThat(worker.getAssignedHours(today), equalTo(13));
    }

    private void givenWorkerWithAssignments(DayAssignment... assignments) {
        this.assignments = Arrays.asList(assignments);
        worker.addNewAssignments(this.assignments);
    }

    private void givenWorker() {
        worker = Worker.create("firstName", "surName", "2333232");
        worker.useScenario(scenarioManager.getCurrent());
    }

}
