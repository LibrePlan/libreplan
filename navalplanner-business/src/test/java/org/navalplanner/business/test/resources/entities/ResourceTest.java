package org.navalplanner.business.test.resources.entities;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionTypeBase;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.test.resources.daos.CriterionDAOTest;
import org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@link Resource}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ResourceTest {

    @Test
    public void testRelationResourceWithCriterionSatisfaction()
            throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(2000),
                criterion, worker);
        assertEquals(1, worker.getAllSatisfactions().size());
    }

    @Test
    public void testGetActiveSatisfactionsForCriterion() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(2000),
                criterion, worker);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(4000),
                criterion, worker);
        assertEquals(1, worker.getActiveSatisfactionsFor(criterion).size());
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
    public void tesGetSatisfactionsInIntervalForCriterion() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(2000),
                criterion, worker);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(1997),
                criterion, worker);
        assertEquals(1, worker.getActiveSatisfactionsForIn(criterion,
                CriterionSatisfactionDAOTest.year(1999),
                CriterionSatisfactionDAOTest.year(2010)).size());
    }

    @Test
    public void testRetrieveActiveCriterionsForCriterionType() throws Exception {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(2000),
                criterion, worker);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(2000),
                otherCriterion, worker);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(4000),
                criterion, worker);
        ICriterionType<Criterion> criterionType = createTypeThatMatches(criterion);
        assertEquals(2, worker.getSatisfactionsFor(criterionType).size());
        assertEquals(1, worker.getActiveSatisfactionsFor(criterionType).size());
    }

    public static CriterionTypeBase createTypeThatMatches(
            final Criterion... criterions) {
        final HashSet<Criterion> criterionsSet = new HashSet<Criterion>(Arrays
                .asList(criterions));
        return new CriterionTypeBase(true, true) {

            @Override
            public boolean contains(ICriterion c) {
                return criterionsSet.contains(c);
            }

            @Override
            public Criterion createCriterion(String name) {
                return null;
            }
        };
    }

    @Test
    public void testRetrieveSatisfactionsInIntervalForCriterionType()
            throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Criterion otherCriterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(2000),
                criterion, worker);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(2003),
                criterion, worker);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(2000),
                otherCriterion, worker);

        ICriterionType<Criterion> criterionType = createTypeThatMatches(criterion);

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

}
