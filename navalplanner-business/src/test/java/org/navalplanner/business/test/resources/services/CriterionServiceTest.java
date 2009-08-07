package org.navalplanner.business.test.resources.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Collection;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.hibernate.validator.InvalidStateException;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionOnData;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Interval;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.ICriterionService;
import org.navalplanner.business.resources.services.IResourceService;
import org.navalplanner.business.test.resources.daos.CriterionDAOTest;
import org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest;
import org.navalplanner.business.test.resources.entities.ResourceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test cases for {@link ICriterionService} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionServiceTest {

    @Autowired
    private ICriterionService criterionService;

    @Autowired
    IAdHocTransactionService adHocTransactionService;

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private SessionFactory sessionFactory;

    @Test(expected = InvalidStateException.class)
    public void testCantSaveCriterionWithoutNameAndType() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion("valido");
        criterion.setName("");
        criterionService.save(criterion);
        sessionFactory.getCurrentSession().flush();
    }

    @Test
    public void testAddCriterion() throws Exception {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionService.save(criterion);
    }

    @Test
    @NotTransactional
    public void testEditingCriterion() throws Exception {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        int initial = criterionService.getCriterionsFor(
                PredefinedCriterionTypes.WORK_RELATIONSHIP).size();
        criterionService.save(criterion);
        assertThat("after saving one more", criterionService.getCriterionsFor(
                PredefinedCriterionTypes.WORK_RELATIONSHIP).size(),
                equalTo(initial + 1));
        criterion.setActive(false);
        String newName = UUID.randomUUID().toString() + "random";
        criterion.setName(newName);
        criterionService.save(criterion);
        assertThat("after editing there are the same", criterionService
                .getCriterionsFor(PredefinedCriterionTypes.WORK_RELATIONSHIP)
                .size(), equalTo(initial + 1));
        Criterion retrieved = criterionService.load(criterion);
        assertThat(retrieved.getName(), equalTo(newName));
        criterionService.remove(criterion);
    }

    @Test
    public void testSaveCriterionTwice() throws ValidationException {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionService.save(criterion);
        criterionService.save(criterion);
    }

    @Test(expected=ValidationException.class)
    @NotTransactional
    public void testCannotExistTwoDifferentCriterionsWithSameNameAndType() throws ValidationException {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionService.save(criterion);
        Criterion criterion2 = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionService.save(criterion2);
    }

    @Test
    public void testCreateIfNotExists() throws ValidationException {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionService.createIfNotExists(criterion);
        assertTrue(criterionService.exists(criterion));
        criterionService
                .createIfNotExists(PredefinedCriterionTypes.WORK_RELATIONSHIP
                        .createCriterion(unique));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testCreateCriterionSatisfactionOnTransientCriterion()
            throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<?> type = createTypeThatMatches(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        worker.addSatisfaction(new CriterionWithItsType(type, criterion));
        assertTrue(criterion.isSatisfiedBy(worker));
        resourceService.saveResource(worker);
        assertTrue(criterion.isSatisfiedBy(worker));
        assertThat(criterionService.getResourcesSatisfying(criterion).size(),
                equalTo(1));
    }

    @NotTransactional
    @Test
    public void testInNoTransaction() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        ICriterionType<?> type = createTypeThatMatches(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        worker.addSatisfaction(new CriterionWithItsType(type, criterion));
        assertTrue(criterion.isSatisfiedBy(worker));
        resourceService.saveResource(worker);
        assertTrue(criterion.isSatisfiedBy(worker));
        assertThat(criterionService.getResourcesSatisfying(criterion).size(),
                equalTo(1));
    }

    public void testCreateCriterionSatisfactionOnTransientResource()
            throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<?> type = createTypeThatMatches(criterion);
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        worker.addSatisfaction(new CriterionWithItsType(type, criterion));
        resourceService.saveResource(worker);
        assertThat(criterionService.getResourcesSatisfying(criterion).size(),
                equalTo(1));
        assertThat((Worker) criterionService.getResourcesSatisfying(criterion)
                .iterator().next(), equalTo(worker));
    }

    @Test
    public void testGetSetOfResourcesSatisfyingCriterion() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        ICriterionType<?> type = createTypeThatMatches(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        worker.addSatisfaction(new CriterionWithItsType(type, criterion));
        assertEquals(1, criterionService.getResourcesSatisfying(criterion)
                .size());
    }

    public static class Prueba extends Resource {

        @Override
        public int getDailyCapacity() {
            return 0;
        }

        @Override
        public String getDescription() {
            return "";
        }

    }

    @Test
    public void testGetSetOfResourcesSubclassSatisfyingCriterion() throws ValidationException {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        ICriterionType<Criterion> type = createTypeThatMatches(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        worker.addSatisfaction(new CriterionWithItsType(type, criterion));

        assertThat(criterionService.getResourcesSatisfying(Resource.class,
                criterion).size(), is(1));
        assertThat(criterionService.getResourcesSatisfying(Worker.class,
                criterion).size(), is(1));
        assertThat(criterionService.getResourcesSatisfying(Prueba.class,
                criterion).size(), is(0));
    }

    @Test
    public void shouldLetCreateCriterionOnData() throws ValidationException {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<?> type = createTypeThatMatches(criterion);
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        worker.addSatisfaction(new CriterionWithItsType(type, criterion), Interval.from(CriterionSatisfactionDAOTest.year(2000)));
        ICriterionOnData criterionOnData = criterionService.empower(criterion);
        assertTrue(criterionOnData.isSatisfiedBy(worker));
        assertEquals(1, criterionOnData.getResourcesSatisfying().size());
        assertTrue(criterionOnData.getResourcesSatisfying().contains(worker));
        assertTrue(criterionOnData.getResourcesSatisfying(
                CriterionSatisfactionDAOTest.year(1990),
                CriterionSatisfactionDAOTest.year(2005)).isEmpty());
        assertEquals(1, criterionOnData.getResourcesSatisfying(
                CriterionSatisfactionDAOTest.year(2001),
                CriterionSatisfactionDAOTest.year(2005)).size());
    }

    @Test
    @NotTransactional
    public void testCriterionIsEquivalentOnDetachedAndProxifiedCriterion()
            throws Exception {
        final Worker worker1 = new Worker("worker-1", "worker-2-surname",
                "11111111A", 8);
        resourceService.saveResource(worker1);
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        createTypeThatMatches(criterion);
        worker1.addSatisfaction(new CriterionWithItsType(criterion.getType(), criterion));
        resourceService.saveResource(worker1);
        Resource workerReloaded = adHocTransactionService
                .onTransaction(new IOnTransaction<Resource>() {

                    @Override
                    public Resource execute() {
                        try {
                            Resource result = resourceService
                                    .findResource(worker1.getId());
                            forceLoadSatisfactions(result);
                            return result;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        Collection<CriterionSatisfaction> satisfactionsFor = workerReloaded
                .getSatisfactionsFor(criterion.getType());
        Criterion reloadedCriterion = satisfactionsFor.iterator().next()
                .getCriterion();
        Assume.assumeTrue(!reloadedCriterion.getClass().equals(
                criterion.getClass()));
        assertTrue(reloadedCriterion.isEquivalent(criterion));
    }

    private void forceLoadSatisfactions(Resource resource) {
        for (CriterionSatisfaction criterionSatisfaction : resource
                .getAllSatisfactions()) {
            criterionSatisfaction.getCriterion().getName();
            criterionSatisfaction.getCriterion().getType().getName();
        }
    }

    @Test
    @NotTransactional
    public void shouldntThrowExceptionDueToTransparentProxyGotcha() throws ValidationException {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<Criterion> type = createTypeThatMatches(criterion);
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        worker.addSatisfaction(new CriterionWithItsType(type, criterion), Interval.from(CriterionSatisfactionDAOTest.year(2000)));
        ICriterionOnData criterionOnData = criterionService.empower(criterion);
        criterionOnData.getResourcesSatisfying();
        criterionOnData.getResourcesSatisfying(CriterionSatisfactionDAOTest
                .year(2001), CriterionSatisfactionDAOTest.year(2005));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustBeCorrectInterval() throws ValidationException {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        criterionService.getResourcesSatisfying(criterion,
                CriterionSatisfactionDAOTest.year(2005),
                CriterionSatisfactionDAOTest.year(2003));
    }

    @Test
    public void testSearchInInterval() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        ICriterionType<Criterion> type = createTypeThatMatches(true, criterion);
        CriterionWithItsType criterionWithItsType = new CriterionWithItsType(
                type, criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        worker.addSatisfaction(criterionWithItsType, Interval.from(CriterionSatisfactionDAOTest
        .year(2000)));

        assertEquals(1, criterionService.getResourcesSatisfying(criterion,
                CriterionSatisfactionDAOTest.year(2001),
                CriterionSatisfactionDAOTest.year(2005)).size());
        assertEquals(0, criterionService.getResourcesSatisfying(criterion,
                CriterionSatisfactionDAOTest.year(1999),
                CriterionSatisfactionDAOTest.year(2005)).size());

        worker.addSatisfaction(new CriterionWithItsType(type, criterion), Interval.from(CriterionSatisfactionDAOTest.year(1998)));

        assertEquals(1, criterionService.getResourcesSatisfying(criterion,
                CriterionSatisfactionDAOTest.year(1999),
                CriterionSatisfactionDAOTest.year(2005)).size());
    }

    @Test
    public void testSearchResourcesForCriterionType() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<Criterion> type = createTypeThatMatches(true, criterion);
        CriterionWithItsType criterionWithItsType = new CriterionWithItsType(
                type, criterion);
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        worker.addSatisfaction(criterionWithItsType, Interval.from(CriterionSatisfactionDAOTest
        .year(2000)));
        worker.addSatisfaction(criterionWithItsType, Interval.from(CriterionSatisfactionDAOTest
        .year(1998)));

        ICriterionType<?> criterionType = ResourceTest
                .createTypeThatMatches(criterion);

        assertEquals(2, criterionService.getSatisfactionsFor(criterionType,
                CriterionSatisfactionDAOTest.year(2001),
                CriterionSatisfactionDAOTest.year(2005)).size());
        assertEquals(1, criterionService.getSatisfactionsFor(criterionType,
                CriterionSatisfactionDAOTest.year(1999),
                CriterionSatisfactionDAOTest.year(2005)).size());
        assertEquals(0, criterionService.getSatisfactionsFor(criterionType,
                CriterionSatisfactionDAOTest.year(1997),
                CriterionSatisfactionDAOTest.year(2005)).size());

        worker.addSatisfaction(criterionWithItsType, Interval.from(CriterionSatisfactionDAOTest
        .year(1997)));
        assertEquals(2, criterionService.getSatisfactionsFor(criterionType,
                CriterionSatisfactionDAOTest.year(1999),
                CriterionSatisfactionDAOTest.year(2005)).size());
    }

    @Test
    public void testCriterionsForType() throws Exception {
        final Criterion one = CriterionDAOTest.createValidCriterion();
        Criterion other = CriterionDAOTest.createValidCriterion();
        criterionService.save(one);
        criterionService.save(other);
        ICriterionType<Criterion> type = createTypeThatMatches(one);
        Collection<Criterion> criterions = criterionService
                .getCriterionsFor(type);
        assertEquals(1, criterions.size());
        assertTrue(criterions.contains(one));
    }

    public static ICriterionType<Criterion> createTypeThatMatches(
            final Criterion criterion) {
        return createTypeThatMatches(false, criterion);
    }

    public static ICriterionType<Criterion> createTypeThatMatches(
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

}
