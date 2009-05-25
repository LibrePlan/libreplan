package org.navalplanner.business.test.resources.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionTypeBase;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
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
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        assertThat(worker.getSatisfactionsFor(criterion).size(), equalTo(0));
        worker.activate(new CriterionWithItsType(type, criterion));
        assertTrue(criterion.isSatisfiedBy(worker));
        assertThat(worker.getSatisfactionsFor(criterion).size(), equalTo(1));
        worker.activate(new CriterionWithItsType(otherType, other));
        assertTrue(other.isSatisfiedBy(worker));
        assertThat(worker.getSatisfactionsFor(other).size(), equalTo(1));
        assertThat(worker.getSatisfactionsFor(criterion).size(), equalTo(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSatisfactionsForWrongIntervalThrowsException() {
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        worker.getActiveSatisfactionsForIn(CriterionDAOTest
                .createValidCriterion(), CriterionSatisfactionDAOTest
                .year(2000), CriterionSatisfactionDAOTest.year(1999));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSatisfactionsForWrongIntervalForCriterionTypeThrowsException() {
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        worker.getActiveSatisfactionsForIn(createTypeThatMatches(),
                CriterionSatisfactionDAOTest.year(2000),
                CriterionSatisfactionDAOTest.year(1999));
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
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        worker.activate(criterionWithItsType, CriterionSatisfactionDAOTest
                .year(2000));
        worker.activate(otherCriterionWithItsType, CriterionSatisfactionDAOTest
                .year(2000));
        worker.activate(criterionWithItsType, CriterionSatisfactionDAOTest
                .year(4000));
        assertEquals(2, worker.getSatisfactionsFor(criterionType).size());
        assertEquals(1, worker.getActiveSatisfactionsFor(criterionType).size());
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

        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        worker.activate(criterionWithItsType, CriterionSatisfactionDAOTest
                .year(2000));
        worker.activate(criterionWithItsType, CriterionSatisfactionDAOTest
                .year(2003));
        worker.activate(otherCriterionWithItsType, CriterionSatisfactionDAOTest
                .year(2000));

        assertEquals(2, worker.getSatisfactionsFor(criterionType).size());
        assertEquals(1, worker.getActiveSatisfactionsForIn(criterionType,
                CriterionSatisfactionDAOTest.year(2001),
                CriterionSatisfactionDAOTest.year(2005)).size());
        assertEquals(2, worker.getActiveSatisfactionsForIn(criterionType,
                CriterionSatisfactionDAOTest.year(2004),
                CriterionSatisfactionDAOTest.year(2005)).size());
        assertEquals(0, worker.getActiveSatisfactionsForIn(criterionType,
                CriterionSatisfactionDAOTest.year(1999),
                CriterionSatisfactionDAOTest.year(2005)).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidadCriterionWithItsTypeNotAllowd() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<Criterion> criterionType = createTypeThatMatches(criterion);
        new CriterionWithItsType(criterionType, otherCriterion);
    }

    @Test
    public void testActivateAndDeactivateCriterion() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        assertThat(worker.getActiveSatisfactionsFor(criterion).size(),
                equalTo(0));
        assertFalse(criterion.isSatisfiedBy(worker));
        assertTrue(worker.canBeActivated(new CriterionWithItsType(
                criterionType, criterion)));
        worker.activate(new CriterionWithItsType(criterionType, criterion));
        assertTrue(criterion.isSatisfiedBy(worker));
        assertThat(worker.getActiveSatisfactionsFor(criterion).size(),
                equalTo(1));
        assertFalse(worker.canBeActivated(new CriterionWithItsType(
                criterionType, otherCriterion)));
        try {
            worker.activate(new CriterionWithItsType(criterionType,
                    otherCriterion));
            fail("must send exception since it already is activated for a criterion of the same type and the type doesn't allow repeated criterions per resource");
        } catch (IllegalStateException e) {
            // ok
        }
        assertTrue(worker.canBeActivated(new CriterionWithItsType(
                criterionType, criterion)));
        assertThat(worker.getActiveSatisfactionsFor(criterion).size(),
                equalTo(1));
        worker.deactivate(new CriterionWithItsType(criterionType, criterion));
        assertTrue("the satisfactions are deactivated", worker
                .getActiveSatisfactionsFor(criterion).isEmpty());
    }

    @Test
    public void testActivateInDate() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        worker.activate(new CriterionWithItsType(criterionType, criterion),
                CriterionSatisfactionDAOTest.year(4000));
        worker.activate(new CriterionWithItsType(criterionType, criterion),
                CriterionSatisfactionDAOTest.year(5000),
                CriterionSatisfactionDAOTest.year(6000));

        assertThat(worker.getActiveSatisfactionsForIn(criterion,
                CriterionSatisfactionDAOTest.year(4001),
                CriterionSatisfactionDAOTest.year(4999)).size(), equalTo(1));
        assertThat(worker.getActiveSatisfactionsForIn(criterion,
                CriterionSatisfactionDAOTest.year(5001),
                CriterionSatisfactionDAOTest.year(5500)).size(), equalTo(1));

        worker.deactivate(new CriterionWithItsType(criterionType, criterion));

        assertThat(worker.getActiveSatisfactionsForIn(criterion,
                CriterionSatisfactionDAOTest.year(4001),
                CriterionSatisfactionDAOTest.year(4999)).size(), equalTo(1));
        assertThat(worker.getActiveSatisfactionsForIn(criterion,
                CriterionSatisfactionDAOTest.year(5001),
                CriterionSatisfactionDAOTest.year(5500)).size(), equalTo(1));

        assertFalse(worker.canBeActivated(new CriterionWithItsType(
                criterionType, otherCriterion), CriterionSatisfactionDAOTest
                .year(4001)));
    }

    @Test
    // when type doesnt allow multiple active criterions per resource
    public void activateOnlyUntilNextCriterionIsActive() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        worker.activate(new CriterionWithItsType(criterionType, criterion),
                CriterionSatisfactionDAOTest.year(4000));
        worker.activate(
                new CriterionWithItsType(criterionType, otherCriterion),
                CriterionSatisfactionDAOTest.year(3500));
        assertThat(worker.getSatisfactionsFor(otherCriterion).size(),
                equalTo(1));
        CriterionSatisfaction satisfaction = worker.getSatisfactionsFor(
                otherCriterion).iterator().next();
        assertThat(satisfaction.getEndDate(),
                equalTo(CriterionSatisfactionDAOTest.year(4000)));
    }

    @Test(expected = IllegalStateException.class)
    // when type doesnt allow multiple active criterions per resource
    public void deactivatePrevious() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(false,
                criterion, otherCriterion);
        worker.activate(
                new CriterionWithItsType(criterionType, otherCriterion),
                CriterionSatisfactionDAOTest.year(3500));
        assertFalse(worker.canBeActivated(new CriterionWithItsType(
                criterionType, criterion), CriterionSatisfactionDAOTest
                .year(4000)));
        worker.activate(new CriterionWithItsType(criterionType, criterion),
                CriterionSatisfactionDAOTest.year(4000));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldntActivate() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
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
        assertFalse(worker.canBeActivated(criterionWithItsType));
        worker.activate(criterionWithItsType);
    }

}
