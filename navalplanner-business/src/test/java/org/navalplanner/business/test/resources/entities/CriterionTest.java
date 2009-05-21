package org.navalplanner.business.test.resources.entities;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.junit.Test;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionCompounder;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.resources.entities.CriterionCompounder.atom;
import static org.navalplanner.business.resources.entities.CriterionCompounder.build;
import static org.navalplanner.business.resources.entities.CriterionCompounder.not;

/**
 * Tests for criterion. <br />
 * Created at May 12, 2009
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionTest {

    @Test
    public void testCreateWithAType() throws Exception {
        Criterion firedCriterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion("fired");
        assertTrue(PredefinedCriterionTypes.WORK_RELATIONSHIP
                .contains(firedCriterion));
    }

    @Test
    public void testCriterionNameAndTypeIsInmutableBusinessKey()
            throws Exception {
        Criterion criterion = new Criterion("name", "type");
        Criterion other = new Criterion("name", "type");
        assertEquals(criterion.hashCode(), other.hashCode());
    }

    @Test
    public void testCompounding() throws Exception {
        Worker worker1 = new Worker();
        Worker worker2 = new Worker();

        ICriterion criterionForWorker1 = justThisResourcesCriterion(worker1);
        ICriterion criterionForWorker2 = justThisResourcesCriterion(worker2);
        ICriterion criterionForWorkers1And2 = justThisResourcesCriterion(
                worker1, worker2);

        assertTrue(criterionForWorker1.isSatisfiedBy(worker1));
        assertTrue(criterionForWorker2.isSatisfiedBy(worker2));
        assertFalse(criterionForWorker2.isSatisfiedBy(worker1));
        assertTrue(criterionForWorkers1And2.isSatisfiedBy(worker1));
        assertTrue(criterionForWorkers1And2.isSatisfiedBy(worker2));

        ICriterion compositedCriterion = CriterionCompounder.atom(
                criterionForWorker1).and(criterionForWorkers1And2).getResult();
        ICriterion matchesNoneComposited = CriterionCompounder.build().and(
                criterionForWorker1).and(criterionForWorker2).getResult();

        assertFalse(matchesNoneComposited.isSatisfiedBy(worker2));
        assertFalse(matchesNoneComposited.isSatisfiedBy(worker1));

        assertTrue(compositedCriterion.isSatisfiedBy(worker1));
        assertFalse(compositedCriterion.isSatisfiedBy(worker2));
    }

    @Test
    public void testEmptyMatchesAll() throws Exception {
        assertTrue(build().getResult().isSatisfiedBy(new Worker()));
    }

    @Test
    public void testSimpleNegation() throws Exception {
        Worker worker1 = new Worker();
        Worker worker2 = new Worker();
        Worker worker3 = new Worker();
        ICriterion criterionForWorker1 = justThisResourcesCriterion(worker1);
        ICriterion criterionForWorker2 = justThisResourcesCriterion(worker2);
        ICriterion worker1Negated = not(criterionForWorker1);
        ICriterion compound = build().and(criterionForWorker1).and(
                not(criterionForWorker2)).getResult();
        assertFalse(worker1Negated.isSatisfiedBy(worker1));
        assertTrue(worker1Negated.isSatisfiedBy(worker2));
        assertFalse(compound.isSatisfiedBy(worker2));
        assertTrue(compound.isSatisfiedBy(worker1));
        assertFalse(compound.isSatisfiedBy(worker3));
    }

    @Test
    public void testNegateAnd() throws Exception {
        Worker worker1 = new Worker();
        Worker worker2 = new Worker();
        Worker worker3 = new Worker();
        ICriterion criterionForWorker1 = justThisResourcesCriterion(worker1);
        ICriterion both = justThisResourcesCriterion(worker1, worker2);
        ICriterion andNegated = not(atom(criterionForWorker1).and(both));
        assertTrue(andNegated.isSatisfiedBy(worker2));
        assertTrue(andNegated.isSatisfiedBy(worker3));
        assertFalse(andNegated.isSatisfiedBy(worker1));
    }

    @Test
    public void testOr() throws Exception {
        Worker worker1 = new Worker();
        Worker worker2 = new Worker();
        Worker worker3 = new Worker();
        ICriterion both = justThisResourcesCriterion(worker1, worker2);
        assertFalse(both.isSatisfiedBy(worker3));

        ICriterion all = atom(both).or(justThisResourcesCriterion(worker3))
                .getResult();

        assertTrue(all.isSatisfiedBy(worker1));
        assertTrue(all.isSatisfiedBy(worker2));
        assertTrue(all.isSatisfiedBy(worker3));
    }

    @Test
    public void testOrHasLessPrecendenceThanAnd() throws Exception {
        Worker worker1 = new Worker();
        Worker worker2 = new Worker();
        Worker worker3 = new Worker();
        ICriterion criterionForWorker1 = justThisResourcesCriterion(worker1);
        ICriterion both = justThisResourcesCriterion(worker1, worker2);

        ICriterion or = atom(criterionForWorker1).and(both).or(
                justThisResourcesCriterion(worker3)).getResult();

        assertTrue(or.isSatisfiedBy(worker1));
        assertFalse(or.isSatisfiedBy(worker2));
        assertTrue("or has less priority", or.isSatisfiedBy(worker3));
    }

    @Test
    public void testCanBeRelatedTo() throws Exception {
        assertTrue(PredefinedCriterionTypes.LOCATION_GROUP
                .criterionCanBeRelatedTo(Resource.class));
        assertTrue(PredefinedCriterionTypes.LOCATION_GROUP
                .criterionCanBeRelatedTo(Worker.class));
        assertFalse(PredefinedCriterionTypes.WORK_RELATIONSHIP
                .criterionCanBeRelatedTo(Resource.class));
        assertTrue(PredefinedCriterionTypes.WORK_RELATIONSHIP
                .criterionCanBeRelatedTo(Worker.class));
    }

    public static ICriterion justThisResourcesCriterion(
            final Resource... resources) {
        final HashSet<Resource> set = new HashSet<Resource>(Arrays
                .asList(resources));
        return new ICriterion() {

            @Override
            public boolean isSatisfiedBy(Resource resource) {
                return set.contains(resource);
            }

            @Override
            public boolean isSatisfiedBy(Resource resource, Date start, Date end) {
                return isSatisfiedBy(resource);
            }
        };
    }

}
