package org.navalplanner.business.test.resources.services;

import org.hibernate.SessionFactory;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.IResourceDao;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.ResourceService;
import org.navalplanner.business.test.resources.entities.CriterionTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

/**
 * A class for testing <code>ResourceService</code>. The service and the
 * resource DAOs are autowired.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class ResourceServiceTest {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private IResourceDao resourceDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testRemoveResource() throws InstanceNotFoundException {

        /* A group of three workers. */
        Worker worker1 = new Worker("worker-1", "worker-2-surname",
                "11111111A", 8);
        Worker worker2 = new Worker("worker-2", "worker-3-surname",
                "22222222B", 6);
        Worker worker3 = new Worker("worker-3", "worker-3-surname",
                "33333333C", 4);
        resourceService.saveResource(worker1);
        resourceService.saveResource(worker2);
        resourceService.saveResource(worker3);

        resourceService.removeResource(worker3.getId());

        assertFalse(resourceDao.exists(worker3.getId()));
        assertTrue(resourceDao.exists(worker2.getId()));
    }

    @Test
    public void testListWorkers() throws Exception {
        final int previousWorkers = resourceService.getWorkers().size();
        Worker worker1 = new Worker("worker-1", "worker-2-surname",
                "11111111A", 8);
        Worker worker2 = new Worker("worker-2", "worker-3-surname",
                "22222222B", 6);
        Resource worker3 = new Worker("worker-3", "worker-3-surname",
                "33333333C", 4);
        resourceService.saveResource(worker1);
        resourceService.saveResource(worker2);
        assertEquals("Two workers have been created", previousWorkers + 2,
                resourceService.getWorkers().size());
        resourceService.saveResource(worker3);
        assertEquals("Three workers has been created", previousWorkers + 3,
                resourceService.getWorkers().size());
    }

    @Test
    @NotTransactional
    public void testWorkerValidation() throws Exception {
        ClassValidator<Worker> workerValidator = new ClassValidator<Worker>(
                Worker.class);
        Worker[] invalidWorkers = {
                new Worker("first name", null, "233233", 3),
                new Worker("first name", "second name", "233233", -1),
                new Worker(null, "second name", "233233", 3),
                new Worker("first name", "second name", null, 3) };
        for (Worker invalidWorker : invalidWorkers) {
            InvalidValue[] invalidValues = workerValidator
                    .getInvalidValues(invalidWorker);
            assertEquals(1, invalidValues.length);
            try {
                resourceService.saveResource(invalidWorker);
                fail("must send invalid state exception");
            } catch (InvalidStateException e) {
                // ok
            }
        }
    }

    public void testResourcesSatisfying() {
        Worker worker1 = new Worker("worker-1", "worker-2-surname",
                "11111111A", 8);
        Worker worker2 = new Worker("worker-2", "worker-3-surname",
                "22222222B", 6);
        resourceService.saveResource(worker1);
        resourceService.saveResource(worker2);
        ICriterion firstCriterion = CriterionTest
                .justThisResourcesCriterion(worker1);
        ICriterion secondCriterion = CriterionTest
                .justThisResourcesCriterion(worker2);
        ICriterion bothCriterion = CriterionTest.justThisResourcesCriterion(
                worker1, worker2);
        assertEquals(1, resourceService.getSetOfResourcesSatisfying(
                firstCriterion).size());
        assertEquals(worker1, resourceService.getSetOfResourcesSatisfying(
                firstCriterion).iterator().next());
        assertEquals(1, resourceService.getSetOfResourcesSatisfying(
                secondCriterion).size());
        assertEquals(2, resourceService.getSetOfResourcesSatisfying(
                bothCriterion).size());
    }

}
