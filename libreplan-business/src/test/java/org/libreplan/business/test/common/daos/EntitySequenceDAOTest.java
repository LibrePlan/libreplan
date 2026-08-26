/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

package org.libreplan.business.test.common.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.NonUniqueResultException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.daos.IEntitySequenceDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.common.entities.EntitySequence;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link EntitySequenceDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 *
 * No bootstrap data (@Resource IDataBootstrap beans) is loaded here, so no
 * EntitySequence rows pre-exist and entityName collisions with fixture data
 * are not a concern.
 */
public class EntitySequenceDAOTest {

    @Autowired
    private IEntitySequenceDAO entitySequenceDAO;

    private EntitySequence createValidEntitySequence(EntityNameEnum entityName, boolean active) {
        EntitySequence sequence = EntitySequence.create("PRE", entityName);
        sequence.setActive(active);
        entitySequenceDAO.save(sequence);
        return sequence;
    }

    @Test
    @Transactional
    public void testGetAllIncludesSaved() {
        EntitySequence sequence = createValidEntitySequence(EntityNameEnum.LABEL, false);
        boolean found = false;
        for (EntitySequence s : entitySequenceDAO.getAll()) {
            if (s.getId().equals(sequence.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testFindEntitySequencesNotInExcludesGivenOnes() {
        EntitySequence excluded = createValidEntitySequence(EntityNameEnum.LABEL, false);
        EntitySequence included = createValidEntitySequence(EntityNameEnum.MACHINE, false);
        // findEntitySequencesNotIn only adds an entity's id to its exclusion filter when
        // !isNewObject(); EntitySequence.create() marks the object new, and save() never
        // clears that flag, so a just-created-and-saved instance must be unmarked explicitly
        // to behave like a real DB-loaded entity (matching how callers normally use this method).
        excluded.dontPoseAsTransientObjectAnymore();

        List<EntitySequence> toExclude = new ArrayList<>();
        toExclude.add(excluded);

        List<EntitySequence> result = entitySequenceDAO.findEntitySequencesNotIn(toExclude);

        boolean includedFound = false;
        boolean excludedFound = false;
        for (EntitySequence s : result) {
            if (s.getId().equals(included.getId())) {
                includedFound = true;
            }
            if (s.getId().equals(excluded.getId())) {
                excludedFound = true;
            }
        }
        assertTrue(includedFound);
        assertFalse(excludedFound);
    }

    @Test
    @Transactional
    public void testFindEntitySequencesNotInWithEmptyListReturnsNothing() {
        // Characterizes a real (if surprising) Hibernate Criteria quirk: with an empty id
        // list, Restrictions.not(Restrictions.in("id", emptyList)) evaluates to always-false
        // rather than always-true, so this returns nothing rather than "everything", matching
        // what findEntitySequencesNotIn(nonEmptyListOfOnlyNonExistentIds) would also do.
        createValidEntitySequence(EntityNameEnum.WORKER, false);
        List<EntitySequence> result = entitySequenceDAO.findEntitySequencesNotIn(new ArrayList<EntitySequence>());
        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    public void testGetActiveEntitySequenceReturnsTheActiveOne() throws InstanceNotFoundException {
        createValidEntitySequence(EntityNameEnum.LABEL, false);
        EntitySequence active = createValidEntitySequence(EntityNameEnum.LABEL, true);

        assertEquals(active.getId(), entitySequenceDAO.getActiveEntitySequence(EntityNameEnum.LABEL).getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testGetActiveEntitySequenceThrowsWhenNoneActive() throws InstanceNotFoundException {
        createValidEntitySequence(EntityNameEnum.LABEL, false);
        entitySequenceDAO.getActiveEntitySequence(EntityNameEnum.LABEL);
    }

    @Test(expected = NonUniqueResultException.class)
    @Transactional
    public void testGetActiveEntitySequenceThrowsWhenMultipleActive() throws InstanceNotFoundException {
        // save() enforces isOnlyOneSequenceForEachEntityIsActiveConstraint (a bean-validation
        // rule, not a DB constraint), so saveWithoutValidating is needed to construct this
        // otherwise-disallowed state and exercise getActiveEntitySequence's NonUniqueResultException.
        EntitySequence first = EntitySequence.create("PRE", EntityNameEnum.LABEL);
        first.setActive(true);
        entitySequenceDAO.saveWithoutValidating(first);
        EntitySequence second = EntitySequence.create("PRE2", EntityNameEnum.LABEL);
        second.setActive(true);
        entitySequenceDAO.saveWithoutValidating(second);

        entitySequenceDAO.getActiveEntitySequence(EntityNameEnum.LABEL);
    }

    @Test
    @Transactional
    public void testExistOtherActiveSequenceByEntityNameForNewObjectTrueWhenOtherActiveExists() {
        createValidEntitySequence(EntityNameEnum.LABEL, true);
        EntitySequence newOne = EntitySequence.create("NEW", EntityNameEnum.LABEL);
        newOne.setActive(true);

        assertTrue(entitySequenceDAO.existOtherActiveSequenceByEntityNameForNewObject(newOne));
    }

    @Test
    @Transactional
    public void testExistOtherActiveSequenceByEntityNameForNewObjectFalseWhenNoneActive() {
        EntitySequence newOne = EntitySequence.create("NEW", EntityNameEnum.LABEL);
        newOne.setActive(true);

        assertFalse(entitySequenceDAO.existOtherActiveSequenceByEntityNameForNewObject(newOne));
    }

}
