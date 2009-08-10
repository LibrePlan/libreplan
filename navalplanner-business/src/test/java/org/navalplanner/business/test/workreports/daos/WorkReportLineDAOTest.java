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
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.entities.WorkReportLine;
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
public class WorkReportLineDAOTest extends AbstractWorkReportTest {

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Test
    public void testSaveWorkReportLine() {
        WorkReportLine workReportLine = createValidWorkReportLine();
        workReportLineDAO.save(workReportLine);
        assertTrue(workReportLineDAO.exists(workReportLine.getId()));
    }

    @Test
    public void testRemoveWorkReportLine() throws InstanceNotFoundException {
        WorkReportLine workReportLine = createValidWorkReportLine();
        workReportLineDAO.save(workReportLine);
        workReportLineDAO.remove(workReportLine.getId());
        assertFalse(workReportLineDAO.exists(workReportLine.getId()));
    }

    @Test
    public void testListWorkReportLine() {
        int previous = workReportLineDAO.list(WorkReportLine.class).size();

        WorkReportLine workReportType1 = createValidWorkReportLine();
        workReportLineDAO.save(workReportType1);
        WorkReportLine workReportType2 = createValidWorkReportLine();
        workReportLineDAO.save(workReportType1);
        workReportLineDAO.save(workReportType2);

        List<WorkReportLine> list = workReportLineDAO
                .list(WorkReportLine.class);
        assertEquals(previous + 2, list.size());
    }
}
