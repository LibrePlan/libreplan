/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.test.ws.resources.api;

import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.IMachineDAO;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.resources.api.IResourceService;
import org.navalplanner.ws.resources.api.MachineDTO;
import org.navalplanner.ws.resources.api.ResourceDTO;
import org.navalplanner.ws.resources.api.ResourceListDTO;
import org.navalplanner.ws.resources.api.WorkerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for <code>IResourceService</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
    WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE })
@Transactional
public class ResourceServiceTest {

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private IMachineDAO machineDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    @Test
    public void testAddResourcesWithBasicContraintViolations()
        throws InstanceNotFoundException {

        String m1Code = getUniqueName();
        MachineDTO m1 = new MachineDTO(m1Code, "name", "desc");
        MachineDTO m2 = new MachineDTO("", null, ""); // Missing code and name
                                                      // (description is
                                                      // optional).
        String w1Nif = getUniqueName();
        WorkerDTO w1 = new WorkerDTO("w1-first-name", "w1-surname", w1Nif);
        WorkerDTO w2 = new WorkerDTO("", "", ""); // Missing first name,
                                                  // surname, and nif.

        List<ResourceDTO> resources = new ArrayList<ResourceDTO>();
        resources.add(m1);
        resources.add(m2);
        resources.add(w1);
        resources.add(w2);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            resourceService.addResources(new ResourceListDTO(resources)).
                instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.size() == 2);
        assertTrue(instanceConstraintViolationsList.get(0).
            constraintViolations.size() == 2); // m2 constraint violations.
        assertTrue(instanceConstraintViolationsList.get(1).
            constraintViolations.size() == 3); // w2 constraint violations.
        assertTrue(machineDAO.findByNameOrCode(m1Code).size() == 1);
        workerDAO.findUniqueByNif(w1Nif);

    }

    private String getUniqueName() {
        return UUID.randomUUID().toString();
    }

}
