/*
 * This file is part of LibrePlan
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

package org.libreplan.business.test.resources.daos;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.daos.ICriterionSatisfactionDAO;
import org.libreplan.business.resources.daos.ICriterionTypeDAO;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Description goes here. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionSatisfactionDAOTest {

    @Autowired
    private ICriterionSatisfactionDAO satisfactionDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    @Test
    public void testSaveCriterions() {
        CriterionSatisfaction criterionSatisfaction = createValidCriterionSatisfaction(2007);
        satisfactionDAO.save(criterionSatisfaction);
        assertNotNull(criterionSatisfaction.getId());
        assertTrue(satisfactionDAO.exists(criterionSatisfaction.getId()));
    }

    private CriterionSatisfaction createValidCriterionSatisfaction(int year) {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        saveCriterionType(criterion);
        criterionDAO.save(criterion);
        Worker worker = Worker.create("firstname", "surname", "nif");
        workerDAO.save(worker);
        CriterionSatisfaction criterionSatisfaction = CriterionSatisfaction.create(year(year), criterion, worker);
        return criterionSatisfaction;
    }

    private void saveCriterionType(Criterion criterion) {
        CriterionType criterionType = criterion.getType();
        if (criterionTypeDAO.existsOtherCriterionTypeByName(criterionType)) {
            try {
                criterionType = criterionTypeDAO.findUniqueByName(criterionType);
            } catch (InstanceNotFoundException ex) {
            }
        } else {
            criterionTypeDAO.save(criterionType);
        }
        criterion.setType(criterionType);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testNotSaveWithTransientCriterionAndWorker() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        saveCriterionType(criterion);
        Worker worker = Worker.create("firstname", "surname", "nif");
        CriterionSatisfaction criterionSatisfaction = CriterionSatisfaction.create(year(2007), criterion, worker);
        satisfactionDAO.save(criterionSatisfaction);
    }

    public static LocalDate year(int year) {
        return new LocalDate(year, 1, 1);
    }

    public static LocalDate date(int year, int month, int day) {
        return new LocalDate(year, month, day);
    }

    @Test
    public void testRemove() throws InstanceNotFoundException {
        CriterionSatisfaction satisfaction = createValidCriterionSatisfaction(2008);
        satisfactionDAO.save(satisfaction);
        assertTrue(satisfactionDAO.exists(satisfaction.getId()));
        satisfactionDAO.remove(satisfaction.getId());
        assertFalse(satisfactionDAO.exists(satisfaction.getId()));
    }

    @Test
    public void testList() {
        int previous = satisfactionDAO.list(CriterionSatisfaction.class).size();
        CriterionSatisfaction satisfaction1 = createValidCriterionSatisfaction(2007);
        CriterionSatisfaction satisfaction2 = createValidCriterionSatisfaction(2008);
        satisfactionDAO.save(satisfaction1);
        satisfactionDAO.save(satisfaction2);
        assertEquals(previous + 2, satisfactionDAO.list(
                CriterionSatisfaction.class).size());
    }
}
