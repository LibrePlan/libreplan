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

package org.libreplan.business.test.resources.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/*
 * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
 * (Jakarta EE / Hibernate 6). WorkerDAO had no test file at all before.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE, BUSINESS_SPRING_CONFIG_TEST_FILE })
public class WorkerDAOTest {

    @Autowired
    private IWorkerDAO workerDAO;

    private Worker createValidWorker(String firstName, String surname, String nif) {
        return Worker.create(firstName, surname, nif);
    }

    @Test
    @Transactional
    public void testInSpringContainer() {
        assertNotNull(workerDAO);
    }

    @Test
    @Transactional
    public void testSaveAndGetAll() {
        int previous = workerDAO.getAll().size();
        workerDAO.save(createValidWorker("firstname", "surname", "nif-" + UUID.randomUUID()));
        assertEquals(previous + 1, workerDAO.getAll().size());
    }

    @Test
    @Transactional
    public void testSaveWorkerWithNoId() throws ValidationException, InstanceNotFoundException {
        // Worker.getNif() is no longer @NotEmpty (Phase 6, see Phase5-found-bugs.md item 3) - the
        // "ID" field is a free-text, optional identifier, not a mandatory unique key.
        Worker worker = createValidWorker("firstname", "surname", null);
        workerDAO.save(worker);

        assertNull(workerDAO.find(worker.getId()).getNif());
    }

    @Test
    @Transactional
    public void findUniqueByNifMatchesTrimmedAndCaseInsensitive() throws InstanceNotFoundException {
        String nif = "NIF" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Worker worker = createValidWorker("firstname", "surname", nif);
        workerDAO.save(worker);

        Worker found = workerDAO.findUniqueByNif("  " + nif.toLowerCase() + "  ".trim());
        assertEquals(worker.getId(), found.getId());

        Worker foundLowerCase = workerDAO.findUniqueByNif(nif.toLowerCase());
        assertEquals(worker.getId(), foundLowerCase.getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void findUniqueByNifThrowsWhenNotFound() throws InstanceNotFoundException {
        workerDAO.findUniqueByNif("does-not-exist-" + UUID.randomUUID());
    }

    @Test
    @Transactional
    public void findByFirstNameCaseInsensitiveMatchesExactNameIgnoringCase() {
        String uniqueName = "Zqf" + UUID.randomUUID().toString().replace("-", "");
        Worker worker = createValidWorker(uniqueName, "surname", "nif-" + UUID.randomUUID());
        workerDAO.save(worker);

        List<Worker> result = workerDAO.findByFirstNameCaseInsensitive(uniqueName.toUpperCase());

        assertEquals(1, result.size());
        assertEquals(worker.getId(), result.get(0).getId());
    }

    @Test
    @Transactional
    public void findByFirstNameCaseInsensitiveDoesNotMatchSubpart() {
        // Restrictions.ilike("firstName", name) uses the literal value passed as the LIKE
        // pattern - no "%" wildcards are added here, so a partial name does NOT match.
        String uniqueName = "Zqg" + UUID.randomUUID().toString().replace("-", "");
        Worker worker = createValidWorker(uniqueName, "surname", "nif-" + UUID.randomUUID());
        workerDAO.save(worker);

        List<Worker> result = workerDAO.findByFirstNameCaseInsensitive(uniqueName.substring(0, 5));

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    public void findByFirstNameSecondNameAndNifMatchesAllThreeCaseRules() {
        String firstName = "Zqh" + UUID.randomUUID().toString().replace("-", "");
        String surname = "Zqi" + UUID.randomUUID().toString().replace("-", "");
        String nif = "NIF" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Worker worker = createValidWorker(firstName, surname, nif);
        workerDAO.save(worker);

        List<Worker> matchCaseInsensitiveNames = workerDAO.findByFirstNameSecondNameAndNif(
                firstName.toUpperCase(), surname.toLowerCase(), nif);
        assertEquals(1, matchCaseInsensitiveNames.size());

        // nif comparison is case-sensitive (Restrictions.like, not ilike)
        List<Worker> noMatchLowerCaseNif = workerDAO.findByFirstNameSecondNameAndNif(
                firstName, surname, nif.toLowerCase());
        assertTrue(noMatchLowerCaseNif.isEmpty());
    }

    @Test
    @Transactional
    public void findByFirstNameSecondNameMatchesCaseInsensitively() {
        String firstName = "Zqj" + UUID.randomUUID().toString().replace("-", "");
        String surname = "Zqk" + UUID.randomUUID().toString().replace("-", "");
        Worker worker = createValidWorker(firstName, surname, "nif-" + UUID.randomUUID());
        workerDAO.save(worker);

        List<Worker> result = workerDAO.findByFirstNameSecondName(firstName.toUpperCase(), surname.toLowerCase());

        assertEquals(1, result.size());
        assertEquals(worker.getId(), result.get(0).getId());
    }

    @Test
    @Transactional
    public void getBoundOnlyReturnsWorkersWithAUser() {
        Worker withoutUser = createValidWorker("firstname", "surname", "nif-" + UUID.randomUUID());
        workerDAO.save(withoutUser);

        List<Worker> bound = workerDAO.getBound();
        for (Worker worker : bound) {
            assertTrue(worker.getUser() != null);
        }
        assertTrue(bound.stream().noneMatch(w -> w.getId().equals(withoutUser.getId())));
    }

    @Test
    @Transactional
    public void getCurrentWorkerFindsSavedWorkerById() {
        Worker worker = createValidWorker("firstname", "surname", "nif-" + UUID.randomUUID());
        workerDAO.save(worker);

        Worker found = workerDAO.getCurrentWorker(worker.getId());
        assertNotNull(found);
        assertEquals(worker.getId(), found.getId());
    }

}
