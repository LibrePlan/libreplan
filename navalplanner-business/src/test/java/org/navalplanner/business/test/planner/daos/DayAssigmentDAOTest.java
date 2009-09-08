package org.navalplanner.business.test.planner.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.planner.daos.IDayAssigmentDAO;
import org.navalplanner.business.planner.entities.DayAssigment;
import org.navalplanner.business.planner.entities.GenericDayAssigment;
import org.navalplanner.business.planner.entities.SpecificDayAssigment;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * @author Diego Pino García <dpino@igalia.com>
 */
@Transactional
public class DayAssigmentDAOTest {

    @Autowired
    private IDayAssigmentDAO dayAssigmentDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    private SpecificDayAssigment createValidSpecificDayAssigment() {
        return SpecificDayAssigment.create(new LocalDate(2009, 1, 2), 8,
                createValidResource());
    }

    private Resource createValidResource() {
        Worker worker = Worker.create("first", "surname", "1221332132A", 5);
        resourceDAO.save(worker);
        return worker;
    }

    private GenericDayAssigment createValidGenericDayAssigment() {
        return GenericDayAssigment.create(new LocalDate(2009, 1, 2), 8,
                createValidResource());
    }

    @Test
    public void testInSpringContainer() {
        assertTrue(dayAssigmentDAO != null);
    }

    @Test
    public void testSaveSpecificResourceAllocation() {
        SpecificDayAssigment dayAssigment = createValidSpecificDayAssigment();
        dayAssigmentDAO.save(dayAssigment);
        assertTrue(dayAssigmentDAO.exists(dayAssigment.getId()));
    }

    @Test
    public void testSaveGenericResourceAllocation() {
        GenericDayAssigment dayAssigment = createValidGenericDayAssigment();
        dayAssigmentDAO.save(dayAssigment);
        assertTrue(dayAssigmentDAO.exists(dayAssigment.getId()));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void theRelatedResourceMustExistInOrderToSave() {
        Worker transientWorker = Worker.create("first", "surname", "1221332132A", 5);
        SpecificDayAssigment dayAssigment = SpecificDayAssigment.create(
                new LocalDate(2009, 1, 2), 8, transientWorker);
        dayAssigmentDAO.save(dayAssigment);
    }

    @Test
    public void testRemoveSpecificResourceAllocation()
            throws InstanceNotFoundException {
        SpecificDayAssigment dayAssigment = createValidSpecificDayAssigment();
        dayAssigmentDAO.save(dayAssigment);
        dayAssigmentDAO.remove(dayAssigment.getId());
        assertFalse(dayAssigmentDAO.exists(dayAssigment.getId()));
    }

    @Test
    public void testRemoveGenericResourceAllocation()
            throws InstanceNotFoundException {
        GenericDayAssigment dayAssigment = createValidGenericDayAssigment();
        dayAssigmentDAO.save(dayAssigment);
        dayAssigmentDAO.remove(dayAssigment.getId());
        assertFalse(dayAssigmentDAO.exists(dayAssigment.getId()));
    }

    @Test
    public void testListSpecificDayAssigment() {
        int previous = dayAssigmentDAO.list(DayAssigment.class)
                .size();

        SpecificDayAssigment dayAssigment1 = createValidSpecificDayAssigment();
        dayAssigmentDAO.save(dayAssigment1);
        SpecificDayAssigment dayAssigment2 = createValidSpecificDayAssigment();
        dayAssigmentDAO.save(dayAssigment1);
        dayAssigmentDAO.save(dayAssigment2);

        List<SpecificDayAssigment> list = dayAssigmentDAO
                .list(SpecificDayAssigment.class);
        assertEquals(previous + 2, list.size());
    }

    @Test
    public void testListGenericDayAssigment() {
        int previous = dayAssigmentDAO.list(DayAssigment.class)
                .size();

        GenericDayAssigment dayAssigment1 = createValidGenericDayAssigment();
        dayAssigmentDAO.save(dayAssigment1);
        GenericDayAssigment dayAssigment2 = createValidGenericDayAssigment();
        dayAssigmentDAO.save(dayAssigment1);
        dayAssigmentDAO.save(dayAssigment2);

        List<GenericDayAssigment> list = dayAssigmentDAO
                .list(GenericDayAssigment.class);
        assertEquals(previous + 2, list.size());
    }
}
