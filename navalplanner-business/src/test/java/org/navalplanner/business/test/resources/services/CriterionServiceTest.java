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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.resources.bootstrap.ICriterionsBootstrap;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionOnData;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.CriterionService;
import org.navalplanner.business.resources.services.ResourceService;
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
 * Test cases for {@link CriterionService} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionServiceTest {

    @Autowired
    private CriterionService criterionService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private ICriterionsBootstrap criterionsBootstrap;

    @Test(expected = InvalidStateException.class)
    public void testCantSaveCriterionWithoutNameAndType() throws Exception {
        Criterion criterion = new Criterion("", "");
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

    @Test(expected = Exception.class)
    public void testUniqueNameForCriterion() {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionService.save(criterion);
        criterionService.save(PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique));
    }

    @Test
    public void testCreateIfNotExists() {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionService.createIfNotExists(criterion);
        assertTrue(criterionService.exists(criterion));
        criterionService
                .createIfNotExists(PredefinedCriterionTypes.WORK_RELATIONSHIP
                        .createCriterion(unique));
    }

    @Test
    public void testPersistingDoesNotChangeEquality() throws Exception {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        Criterion other = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        assertEquals(criterion, other);
        criterionService.save(criterion);
        assertEquals(criterion, other);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testCreateCriterionSatisfactionOnTransientCriterion()
            throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<?> type = createTypeThatMatches(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        worker.activate(new CriterionWithItsType(type, criterion));
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
        worker.activate(new CriterionWithItsType(type, criterion));
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
        worker.activate(new CriterionWithItsType(type, criterion));
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
        worker.activate(new CriterionWithItsType(type, criterion));
        assertEquals(1, criterionService.getResourcesSatisfying(criterion)
                .size());
    }

    public static class Prueba extends Resource {

        @Override
        public int getDailyCapacity() {
            return 0;
        }

    }

    @Test
    public void testGetSetOfResourcesSubclassSatisfyingCriterion() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        ICriterionType<Criterion> type = createTypeThatMatches(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        worker.activate(new CriterionWithItsType(type, criterion));

        assertThat(criterionService.getResourcesSatisfying(Resource.class,
                criterion).size(), is(1));
        assertThat(criterionService.getResourcesSatisfying(Worker.class,
                criterion).size(), is(1));
        assertThat(criterionService.getResourcesSatisfying(Prueba.class,
                criterion).size(), is(0));
    }

    @Test
    public void shouldLetCreateCriterionOnData() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<?> type = createTypeThatMatches(criterion);
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        worker.activate(new CriterionWithItsType(type, criterion),
                CriterionSatisfactionDAOTest.year(2000));
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
    public void shouldntThrowExceptionDueToTransparentProxyGotcha() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        ICriterionType<Criterion> type = createTypeThatMatches(criterion);
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        worker.activate(new CriterionWithItsType(type, criterion),
                CriterionSatisfactionDAOTest.year(2000));
        ICriterionOnData criterionOnData = criterionService.empower(criterion);
        criterionOnData.getResourcesSatisfying();
        criterionOnData.getResourcesSatisfying(CriterionSatisfactionDAOTest
                .year(2001), CriterionSatisfactionDAOTest.year(2005));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustBeCorrectInterval() {
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
        worker.activate(criterionWithItsType, CriterionSatisfactionDAOTest
                .year(2000));

        assertEquals(1, criterionService.getResourcesSatisfying(criterion,
                CriterionSatisfactionDAOTest.year(2001),
                CriterionSatisfactionDAOTest.year(2005)).size());
        assertEquals(0, criterionService.getResourcesSatisfying(criterion,
                CriterionSatisfactionDAOTest.year(1999),
                CriterionSatisfactionDAOTest.year(2005)).size());

        worker.activate(new CriterionWithItsType(type, criterion),
                CriterionSatisfactionDAOTest.year(1998));

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
        worker.activate(criterionWithItsType, CriterionSatisfactionDAOTest
                .year(2000));
        worker.activate(criterionWithItsType, CriterionSatisfactionDAOTest
                .year(1998));

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

        worker.activate(criterionWithItsType, CriterionSatisfactionDAOTest
                .year(1997));
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
            final boolean allowMultipleActiveCriterionsPerResource,
            final Criterion criterion) {
        return new ICriterionType<Criterion>() {

            @Override
            public boolean allowHierarchy() {
                return false;
            }

            @Override
            public boolean allowMultipleActiveCriterionsPerResource() {
                return allowMultipleActiveCriterionsPerResource;
            }

            @Override
            public boolean contains(ICriterion c) {
                return criterion == c;
            }

            @Override
            public Criterion createCriterion(String name) {
                return null;
            }

            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean allowAdding() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean allowEditing() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean criterionCanBeRelatedTo(
                    Class<? extends Resource> klass) {
                return true;
            }
        };
    }

}
