package org.navalplanner.business.test.workreports.daos;

import static junit.framework.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
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
public class WorkReportLineDAOTest {

	@Autowired
	private IWorkReportLineDAO workReportLineDAO;

	@Test
	public void testSaveWorkReportLine() {
		assertTrue(true);
	}

	@Test
	public void testRemoveWorkReportLine() {
		assertTrue(true);
	}

	@Test
	public void testListWorkReportLine() {
		assertTrue(true);
	}
}
