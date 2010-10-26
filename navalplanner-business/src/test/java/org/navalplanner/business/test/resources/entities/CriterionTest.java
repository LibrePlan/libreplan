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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.resources.entities.CriterionCompounder.atom;
import static org.navalplanner.business.resources.entities.CriterionCompounder.build;
import static org.navalplanner.business.resources.entities.CriterionCompounder.not;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionCompounder;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Tests for criterion. <br />
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
    public void testCompounding() throws Exception {
        Worker worker1 = Worker.create();
        Worker worker2 = Worker.create();

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
    public void testWorkerSatisfySeveralCriterions() {
        Worker worker1 = Worker.create();
        Worker worker2 = Worker.create();

        ICriterion criterion1 = justThisResourcesCriterion(worker1);
        ICriterion criterion2 = justThisResourcesCriterion(worker1);
        ICriterion criterion3 = justThisResourcesCriterion(worker2);
        ICriterion criterion4 = justThisResourcesCriterion(worker1, worker2);

        assertTrue(criterion1.isSatisfiedBy(worker1));
        assertFalse(criterion1.isSatisfiedBy(worker2));
        assertTrue(criterion2.isSatisfiedBy(worker1));
        assertFalse(criterion2.isSatisfiedBy(worker2));
        assertFalse(criterion3.isSatisfiedBy(worker1));
        assertTrue(criterion3.isSatisfiedBy(worker2));
        assertTrue(criterion4.isSatisfiedBy(worker1));
        assertTrue(criterion4.isSatisfiedBy(worker2));

        List<ICriterion> criterionList1 = Arrays.asList(criterion1, criterion2);
        List<ICriterion> criterionList2 = Arrays.asList(criterion1, criterion2,
                criterion3);
        List<ICriterion> criterionList3 = Arrays.asList(criterion3, criterion4);

        ICriterion compositedCriterion1 = CriterionCompounder.buildAnd(
                criterionList1).getResult();
        ICriterion compositedCriterion2 = CriterionCompounder.buildAnd(
                criterionList2).getResult();
        ICriterion compositedCriterion3 = CriterionCompounder.buildAnd(
                criterionList3).getResult();

        assertTrue(compositedCriterion1.isSatisfiedBy(worker1));
        assertFalse(compositedCriterion1.isSatisfiedBy(worker2));
        assertFalse(compositedCriterion2.isSatisfiedBy(worker1));
        assertFalse(compositedCriterion2.isSatisfiedBy(worker2));
        assertFalse(compositedCriterion3.isSatisfiedBy(worker1));
        assertTrue(compositedCriterion3.isSatisfiedBy(worker2));
    }

    @Test
    public void testEmptyMatchesAll() throws Exception {
        assertTrue(build().getResult().isSatisfiedBy(Worker.create()));
    }

    @Test
    public void testSimpleNegation() throws Exception {
        Worker worker1 = Worker.create();
        Worker worker2 = Worker.create();
        Worker worker3 = Worker.create();
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
        Worker worker1 = Worker.create();
        Worker worker2 = Worker.create();
        Worker worker3 = Worker.create();
        ICriterion criterionForWorker1 = justThisResourcesCriterion(worker1);
        ICriterion both = justThisResourcesCriterion(worker1, worker2);
        ICriterion andNegated = not(atom(criterionForWorker1).and(both));
        assertTrue(andNegated.isSatisfiedBy(worker2));
        assertTrue(andNegated.isSatisfiedBy(worker3));
        assertFalse(andNegated.isSatisfiedBy(worker1));
    }

    @Test
    public void testOr() throws Exception {
        Worker worker1 = Worker.create();
        Worker worker2 = Worker.create();
        Worker worker3 = Worker.create();
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
        Worker worker1 = Worker.create();
        Worker worker2 = Worker.create();
        Worker worker3 = Worker.create();
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
                .criterionCanBeRelatedTo(Worker.class));
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

            @Override
            public boolean isSatisfiedBy(Resource resource, Date atThisDate) {
                return isSatisfiedBy(resource);
            }
        };
    }

}
