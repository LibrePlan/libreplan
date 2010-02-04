/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.test.resources.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test cases for {@link ResourceDAOTest}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class ResourceDAOTest {

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void saveResourceWithCalendar() throws InstanceNotFoundException {
        Resource resource = givenValidWorker();
        ResourceCalendar resourceCalendar = givenValidResourceCalendar();

        resource.setCalendar(resourceCalendar);

        resourceDAO.save(resource);
        resourceDAO.flush();
        sessionFactory.getCurrentSession().evict(resource);

        Resource foundResource = resourceDAO.find(resource.getId());
        assertNotSame(resource, foundResource);
        assertNotNull(foundResource.getCalendar());
        assertThat(foundResource.getCalendar().getId(),
                equalTo(resourceCalendar.getId()));
    }

    private ResourceCalendar givenValidResourceCalendar() {
        ResourceCalendar resourceCalendar = ResourceCalendar.create();
        resourceCalendar.setName("Calendar");
        return resourceCalendar;
    }

    public static Worker givenValidWorker() {
        Worker worker = Worker.create();
        worker.setFirstName("First name");
        worker.setSurname("Surname");
        worker.setNif("NIF");
        return worker;
    }

}
