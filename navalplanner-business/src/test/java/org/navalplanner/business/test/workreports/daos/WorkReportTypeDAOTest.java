package org.navalplanner.business.test.workreports.daos;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/*
 * @author Diego Pino García <dpino@igalia.com>
 */
@Transactional
public class WorkReportTypeDAOTest extends AbstractWorkReportTest {

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Test
    public void testSaveWorkReportType() {
        WorkReportType workReportType = createValidWorkReportType();
        workReportTypeDAO.save(workReportType);
        assertTrue(workReportTypeDAO.exists(workReportType.getId()));
    }

    @Test
    public void testRemoveWorkReportType() throws InstanceNotFoundException {
        WorkReportType workReportType = createValidWorkReportType();
        workReportTypeDAO.save(workReportType);
        workReportTypeDAO.remove(workReportType.getId());
        assertFalse(workReportTypeDAO.exists(workReportType.getId()));
    }

    @Test
    public void testListWorkReportType() {
        int previous = workReportTypeDAO.list(WorkReportType.class).size();

        WorkReportType workReportType1 = createValidWorkReportType();
        workReportTypeDAO.save(workReportType1);
        WorkReportType workReportType2 = createValidWorkReportType();
        workReportTypeDAO.save(workReportType1);
        workReportTypeDAO.save(workReportType2);

        List<WorkReportType> list = workReportTypeDAO
                .list(WorkReportType.class);
        assertEquals(previous + 2, list.size());
    }
}
