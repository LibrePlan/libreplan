/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.business.test.effortsummary.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.effortsummary.daos.IEffortSummaryDAO;
import org.libreplan.business.effortsummary.entities.EffortSummary;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class EffortSummaryDAOTest {

    @Autowired
    private IEffortSummaryDAO effortSummaryDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @javax.annotation.Resource
    private IDataBootstrap configurationBootstrap;

    @Before
    public void loadRequiredaData() {
        configurationBootstrap.loadRequiredData();
    }

    public static Worker generateValidWorker() {
        Worker worker = Worker.create();
        worker.setFirstName("First name");
        worker.setSurname("Surname");
        worker.setNif("NIF" + UUID.randomUUID().toString());
        return worker;
    }

    public void createConsecutiveEffortSummaryItems(int numberOfItems,
            Resource resource, LocalDate startDate) {
        LocalDate startDate2 = new LocalDate(startDate);
        LocalDate endDate = startDate.plusDays(numberOfItems - 1);
        int[] availableEffort = new int[numberOfItems];
        int[] assignedEffort = new int[numberOfItems];
        Random generator = new Random();

        resourceDAO.save(resource);

        for (int i = 0; i < numberOfItems; i++) {
            availableEffort[i] = generator.nextInt(86400);
            assignedEffort[i] = generator.nextInt(86400);
        }
        EffortSummary summary = EffortSummary.create(startDate2, endDate,
                availableEffort, assignedEffort, resource);
        effortSummaryDAO.save(summary);
    }

    @Test
    public void testList() {
        final int numberOfItems = 1000;
        Worker worker = generateValidWorker();
        LocalDate date = new LocalDate();
        createConsecutiveEffortSummaryItems(numberOfItems, worker, date);

        List<EffortSummary> list = effortSummaryDAO.list();
        assertEquals(1, list.size());
        EffortSummary effort = list.get(0);
        assertEquals(numberOfItems, effort.getAssignedEffort().length);
        assertEquals(numberOfItems, effort.getAvailableEffort().length);
    }

    @Test
    public void testListBetweenDates() {
        final int numberOfItems = 1000;
        Worker worker = generateValidWorker();
        LocalDate startDate = new LocalDate();
        LocalDate endDate = startDate.plusDays(numberOfItems - 2);
        createConsecutiveEffortSummaryItems(numberOfItems, worker,
                startDate);

        EffortSummary effort = effortSummaryDAO
                .listForResourceBetweenDates(worker, startDate, endDate);
        assertEquals(numberOfItems - 1, effort.getAssignedEffort().length);
    }

    @Test
    public void testFindByResource() {
        final int numberOfItems = 1000;
        Worker worker = generateValidWorker();
        LocalDate date = new LocalDate();
        createConsecutiveEffortSummaryItems(numberOfItems, worker, date);
        EffortSummary effort = effortSummaryDAO.findGlobalInformationForResource(worker);
        assertEquals(effort.getResource().getId(), worker.getId());
    }

    @Test
    public void testFindByResourceNotFound() {
        Worker worker = generateValidWorker();
        resourceDAO.save(worker);
        EffortSummary effort = effortSummaryDAO.findGlobalInformationForResource(worker);
        assertNull(effort);
    }
}
