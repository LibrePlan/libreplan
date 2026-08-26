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
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.resources.daos.IMachineDAO;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Machine;
import org.libreplan.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/*
 * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
 * (Jakarta EE / Hibernate 6). ResourcesSearcher had no dedicated test file before - its Criteria
 * based join+distinct behavior was indirectly exercised via ResourceDAOTest, but byName()
 * filtering had no coverage at all.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE, BUSINESS_SPRING_CONFIG_TEST_FILE })
public class ResourcesSearcherTest {

    @Autowired
    private IResourcesSearcher resourcesSearcher;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private IMachineDAO machineDAO;

    private Worker createValidWorker(String firstName, String surname, String nif) {
        Worker worker = Worker.create(firstName, surname, nif);
        return worker;
    }

    private Machine createValidMachine(String name, String code) {
        Machine machine = Machine.create();
        machine.setCode(code);
        machine.setName(name);
        machine.setDescription("description");
        return machine;
    }

    @Test
    @Transactional
    public void byNameMatchesWorkerFirstNameCaseInsensitively() {
        String unique = "Zqx" + UUID.randomUUID().toString().replace("-", "");
        Worker worker = createValidWorker(unique, "surname", "nif-" + UUID.randomUUID());
        workerDAO.save(worker);

        List<Worker> result = resourcesSearcher.searchWorkers().byName(unique.toLowerCase()).execute();

        assertEquals(1, result.size());
        assertEquals(worker.getId(), result.get(0).getId());
    }

    @Test
    @Transactional
    public void byNameMatchesWorkerSurnameCaseInsensitively() {
        String unique = "Zqy" + UUID.randomUUID().toString().replace("-", "");
        Worker worker = createValidWorker("firstname", unique, "nif-" + UUID.randomUUID());
        workerDAO.save(worker);

        List<Worker> result = resourcesSearcher.searchWorkers().byName(unique.toUpperCase()).execute();

        assertEquals(1, result.size());
        assertEquals(worker.getId(), result.get(0).getId());
    }

    @Test
    @Transactional
    public void byNameMatchesWorkerNifCaseInsensitively() {
        // addQueryByName() used to use a case-sensitive like() for nif, unlike the case-
        // insensitive ilike() already used for firstName/surname - inconsistent, and confirmed
        // (per Jeroen) not intentional: the "ID" field is free text, not a formal identifier with
        // a fixed case. Fixed to match firstName/surname's case-insensitive behavior (Phase 6, see
        // Phase5-found-bugs.md item 3).
        String unique = "NIF" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Worker worker = createValidWorker("firstname", "surname", unique);
        workerDAO.save(worker);

        List<Worker> exactCaseResult = resourcesSearcher.searchWorkers().byName(unique).execute();
        List<Worker> lowerCaseResult = resourcesSearcher.searchWorkers().byName(unique.toLowerCase()).execute();

        assertEquals(1, exactCaseResult.size());
        assertEquals(1, lowerCaseResult.size());
        assertEquals(worker.getId(), lowerCaseResult.get(0).getId());
    }

    @Test
    @Transactional
    public void byNameMatchesMachineNameOrCodeCaseInsensitively() {
        String uniqueName = "Zqn" + UUID.randomUUID().toString().replace("-", "");
        String uniqueCode = "Zqc" + UUID.randomUUID().toString().replace("-", "");
        Machine machine = createValidMachine(uniqueName, uniqueCode);
        machineDAO.save(machine);

        List<Machine> byName = resourcesSearcher.searchMachines().byName(uniqueName.toLowerCase()).execute();
        List<Machine> byCode = resourcesSearcher.searchMachines().byName(uniqueCode.toUpperCase()).execute();

        assertEquals(1, byName.size());
        assertEquals(machine.getId(), byName.get(0).getId());
        assertEquals(1, byCode.size());
        assertEquals(machine.getId(), byCode.get(0).getId());
    }

    @Test
    @Transactional
    public void byNameReturnsNothingWhenNoMatch() {
        Machine machine = createValidMachine("name-" + UUID.randomUUID(), "code-" + UUID.randomUUID());
        machineDAO.save(machine);

        List<Machine> result =
                resourcesSearcher.searchMachines().byName("does-not-exist-" + UUID.randomUUID()).execute();

        assertTrue(result.isEmpty());
    }

}
