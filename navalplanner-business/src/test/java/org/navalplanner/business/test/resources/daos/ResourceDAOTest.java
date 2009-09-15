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
        assertNotNull(foundResource.getCalendar().getId());
        assertThat(foundResource.getCalendar().getId(),
                equalTo(resourceCalendar.getId()));
    }

    private ResourceCalendar givenValidResourceCalendar() {
        ResourceCalendar resourceCalendar = ResourceCalendar.create();
        resourceCalendar.setName("Calendar");
        return resourceCalendar;
    }

    private Worker givenValidWorker() {
        Worker worker = Worker.create();
        worker.setFirstName("First name");
        worker.setSurname("Surname");
        worker.setNif("NIF");
        return worker;
    }

}
