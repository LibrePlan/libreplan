package org.navalplanner.business.test.resources.services;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.hibernate.SessionFactory;
import org.hibernate.validator.InvalidStateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.CriterionService;
import org.navalplanner.business.resources.services.ResourceService;
import org.navalplanner.business.test.resources.daos.CriterionDAOTest;
import org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest;
import org.navalplanner.business.test.resources.entities.ResourceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
        criterionService.createIfNotExists(PredefinedCriterionTypes.WORK_RELATIONSHIP
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

    @Test
    public void testCreateCriterionSatisfactionButNotSave() {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        new CriterionSatisfaction(CriterionSatisfactionDAOTest.year(2000),
                criterion, worker);
        assertEquals(1, worker.getAllSatisfactions().size());
    }

    /*
     * It sends a dataIntegrityViolationException when adding a
     * criterionSatisfaction with a criterion that doesn't exist yet
     */
    @Test(expected = DataIntegrityViolationException.class)
    public void testCreateCriterionSatisfactionOnTransientCriterion()
            throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);

        criterionService.add(new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(2000), criterion, worker));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testCreateCriterionSatisfactionOnTransientResource()
            throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(2000), criterion, worker);
        criterionService.add(criterionSatisfaction);
    }

    @Test
    public void testCreatingButNotPersistingSatisfaction() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(2000), criterion, worker);
        resourceService.saveResource(worker);
        assertEquals(1, criterionService.getResourcesSatisfying(criterion)
                .size());
        sessionFactory.getCurrentSession().evict(worker);
        assertEquals(
                "once the worker has been evicted the satisfaction created is not taken into account",
                0, criterionService.getResourcesSatisfying(criterion).size());
    }

    @Test
    public void testGetSetOfResourcesSatisfyingCriterion() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(2000), criterion, worker);
        criterionService.add(criterionSatisfaction);
        assertEquals(1, criterionService.getResourcesSatisfying(criterion)
                .size());
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
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(2000), criterion, worker);

        criterionService.add(criterionSatisfaction);

        assertEquals(1, criterionService.getResourcesSatisfying(criterion,
                CriterionSatisfactionDAOTest.year(2001),
                CriterionSatisfactionDAOTest.year(2005)).size());
        assertEquals(0, criterionService.getResourcesSatisfying(criterion,
                CriterionSatisfactionDAOTest.year(1999),
                CriterionSatisfactionDAOTest.year(2005)).size());

        CriterionSatisfaction otherSatisfaction = new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(1998), criterion, worker);
        criterionService.add(otherSatisfaction);

        assertEquals(1, criterionService.getResourcesSatisfying(criterion,
                CriterionSatisfactionDAOTest.year(1999),
                CriterionSatisfactionDAOTest.year(2005)).size());
    }

    @Test
    public void testSearchResourcesForCriterionType() throws Exception {
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        criterionService.save(criterion);
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        resourceService.saveResource(worker);
        criterionService.add(new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(2000), criterion, worker));
        criterionService.add(new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(1998), criterion, worker));

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

        criterionService.add(new CriterionSatisfaction(
                CriterionSatisfactionDAOTest.year(1997), criterion, worker));
        assertEquals(2, criterionService.getSatisfactionsFor(criterionType,
                CriterionSatisfactionDAOTest.year(1999),
                CriterionSatisfactionDAOTest.year(2005)).size());

    }

}
