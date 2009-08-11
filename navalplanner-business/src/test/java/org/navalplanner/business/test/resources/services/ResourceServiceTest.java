package org.navalplanner.business.test.resources.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.IResourceService;
import org.navalplanner.business.test.resources.daos.CriterionDAOTest;
import org.navalplanner.business.test.resources.entities.CriterionTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

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
    private IResourceService resourceService;

    @Autowired
    private IResourceDAO resourceDao;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

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

    @Test
    @NotTransactional
    public void versionIsIncreased() throws Exception {
        Worker worker1 = new Worker("worker-1", "worker-2-surname",
                "11111111A", 8);
        resourceService.saveResource(worker1);
        long versionValueAfterSave = worker1.getVersion();
        worker1.setFirstName("blabla");
        resourceService.saveResource(worker1);
        assertThat(worker1.getVersion(), not(equalTo(versionValueAfterSave)));
    }

    @Test
    @NotTransactional
    public void versionIsIncreasedWhenAddingSatisfactions()
            throws Exception {
        Worker worker1 = new Worker("worker-1", "worker-2-surname",
                "11111111A", 8);
        resourceService.saveResource(worker1);
        long versionValueAfterSave = worker1.getVersion();
        final Criterion criterion = CriterionDAOTest.createValidCriterion();

        adHocTransactionService.onTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                if (!(criterionTypeDAO.exists(criterion.getType().getId()) || criterionTypeDAO
                        .existsByName(criterion.getType()))) {
                    criterionTypeDAO.save(criterion.getType());
                }
                criterionDAO.save(criterion);
                return null;
            }
        });

        ICriterionType<Criterion> type = createTypeThatMatches(criterion);
        worker1.addSatisfaction(new CriterionWithItsType(type, criterion));
        resourceService.saveResource(worker1);
        assertThat(worker1.getVersion(), not(equalTo(versionValueAfterSave)));
    }

    private static ICriterionType<Criterion> createTypeThatMatches(
            final Criterion criterion) {
        return createTypeThatMatches(false, criterion);
    }

    private static ICriterionType<Criterion> createTypeThatMatches(
            final boolean allowSimultaneousCriterionsPerResource,
            final Criterion criterion) {
        return new ICriterionType<Criterion>() {

            @Override
            public boolean allowSimultaneousCriterionsPerResource() {
                return allowSimultaneousCriterionsPerResource;
            }

            @Override
            public boolean allowHierarchy() {
                return false;
            }

            @Override
            public boolean contains(ICriterion c) {
                return criterion.isEquivalent(c);
            }

            @Override
            public Criterion createCriterion(String name) {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public boolean allowAdding() {
                return false;
            }

            @Override
            public boolean allowEditing() {
                return false;
            }

            @Override
            public boolean criterionCanBeRelatedTo(
                    Class<? extends Resource> klass) {
                return true;
            }

            @Override
            public Criterion createCriterionWithoutNameYet() {
                return null;
            }
        };
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
