package org.navalplanner.business.test.resources.entities;

import java.util.Date;

import org.junit.Test;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.test.resources.daos.CriterionDAOTest;
import org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link CriterionSatisfaction} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionSatisfactionTest {

    @Test
    public void testFinish() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(2000), criterion, worker);
        Date end = CriterionSatisfactionDAOTest.year(2006);
        criterionSatisfaction.finish(end);
        assertTrue(criterionSatisfaction.isFinished());
        assertEquals(end, criterionSatisfaction.getEndDate());
        criterionSatisfaction.getEndDate().setTime(end.getTime() + 2000);
        assertEquals("endDate must be well encapsulated", end,
                criterionSatisfaction.getEndDate());
    }

    @Test
    public void canFinishWhenItStarted() throws Exception {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        Date start = CriterionSatisfactionDAOTest.year(2000);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                start, criterion, worker);
        criterionSatisfaction.finish(start);
        assertTrue(criterionSatisfaction.isFinished());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCantFinishBeforeStart() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(2000), criterion, worker);
        criterionSatisfaction.finish(CriterionSatisfactionDAOTest.year(1999));
    }

    @Test
    public void isValidAtDate() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(2000), criterion, worker);
        assertTrue(criterionSatisfaction
                .isActiveAt(CriterionSatisfactionDAOTest.year(3000)));
        assertTrue(criterionSatisfaction
                .isActiveAt(CriterionSatisfactionDAOTest.year(2000)));
        assertFalse(criterionSatisfaction
                .isActiveAt(CriterionSatisfactionDAOTest.year(1999)));
        criterionSatisfaction.finish(CriterionSatisfactionDAOTest.year(2005));
        assertFalse(criterionSatisfaction
                .isActiveAt(CriterionSatisfactionDAOTest.year(3000)));
        assertTrue(criterionSatisfaction
                .isActiveAt(CriterionSatisfactionDAOTest.year(2000)));
        assertFalse(criterionSatisfaction
                .isActiveAt(CriterionSatisfactionDAOTest.year(1999)));
    }

}
