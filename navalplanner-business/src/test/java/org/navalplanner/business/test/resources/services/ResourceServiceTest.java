package org.navalplanner.business.test.resources.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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

    private Worker worker;

    private Worker[] invalidWorkers;

    private Criterion criterion;

    @Test
    public void afterRemovingASavedWorkerNoLongerExists()
            throws InstanceNotFoundException {
        givenSavedWorker();
        resourceService.removeResource(worker.getId());
        assertFalse(resourceDao.exists(worker.getId()));
    }

    private void givenSavedWorker() {
        this.worker = new Worker("worker-1", "worker-2-surname", "11111111A", 8);
        resourceService.saveResource(this.worker);
    }

    @Test
    public void getWorkersReturnsTheNewlyCreatedResources() {
        final int previousWorkers = resourceService.getWorkers().size();
        givenSavedWorker();
        givenSavedWorker();
        assertEquals("Two workers has been saved", previousWorkers + 2,
                resourceService.getWorkers().size());
    }

    @Test
    public void invalidValuesAreReportedByClassValidator() {
        givenInvalidWorkers();
        for (Worker invalidWorker : invalidWorkers) {
            thenHasSomeInvalidValue(invalidWorker);
        }
    }

    @Test
    @NotTransactional
    public void invalidWorkerCannotBeSaved() {
        givenInvalidWorkers();
        for (Worker invalidWorker : invalidWorkers) {
            thenCannotBeSaved(invalidWorker);
        }
    }

    private void thenCannotBeSaved(Worker invalidWorker) {
        try {
            resourceService.saveResource(invalidWorker);
            fail("must send invalid state exception");
        } catch (InvalidStateException e) {
            // ok
        }
    }

    private void thenHasSomeInvalidValue(Worker invalidWorker) {
        ClassValidator<Worker> workerValidator = new ClassValidator<Worker>(
                Worker.class);
        InvalidValue[] invalidValues = workerValidator
                .getInvalidValues(invalidWorker);
        assertEquals(1, invalidValues.length);
    }

    private void givenInvalidWorkers() {
        invalidWorkers = new Worker[] {
                new Worker("first name", null, "233233", 3),
                new Worker("first name", "second name", "233233", -1),
                new Worker(null, "second name", "233233", 3),
                new Worker("first name", "second name", null, 3) };
    }

    @Test
    @NotTransactional
    public void versionIsIncreased() {
        givenSavedWorker();

        long versionValueAfterSave = worker.getVersion();
        worker.setFirstName("blabla");
        resourceService.saveResource(worker);

        assertThat(worker.getVersion(), not(equalTo(versionValueAfterSave)));
    }

    @Test
    @NotTransactional
    public void versionIsIncreasedWhenAddingSatisfactions() throws Exception {
        givenSavedWorker();
        givenCriterion();

        long versionValueAfterSave = worker.getVersion();

        worker.addSatisfaction(new CriterionWithItsType(criterion.getType(),
                criterion));
        resourceService.saveResource(worker);

        assertThat(worker.getVersion(), not(equalTo(versionValueAfterSave)));
    }

    private void givenCriterion() {
        this.criterion = CriterionDAOTest.createValidCriterion();
        adHocTransactionService.onTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                criterionTypeDAO.save(criterion.getType());
                criterionDAO.save(criterion);
                return null;
            }
        });
    }

    @Test
    public void setOfResourcesSatisfyingReturnTheResourcesMatchedByCriterion() {
        givenSavedWorker();

        ICriterion criterion = CriterionTest.justThisResourcesCriterion(worker);

        assertEquals(1, resourceService.getSetOfResourcesSatisfying(
                criterion).size());
    }

}
