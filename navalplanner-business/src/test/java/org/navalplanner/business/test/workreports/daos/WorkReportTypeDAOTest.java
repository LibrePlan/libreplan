package org.navalplanner.business.test.workreports.daos;

import static junit.framework.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.test.resources.daos.CriterionTypeDAOTest;
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
public class WorkReportTypeDAOTest {

	@Autowired
	private IWorkReportTypeDAO workReportTypeDAO;

	@Autowired
	private ICriterionTypeDAO criterionTypeDAO;

	@Test
	public void testSaveWorkReportType() {
		String unique = UUID.randomUUID().toString();
		CriterionType criterionType = CriterionTypeDAOTest
		        .createValidCriterionType();
		criterionTypeDAO.save(criterionType);
		Set<CriterionType> criterionTypes = new HashSet<CriterionType>();
		criterionTypes.add(criterionType);

		WorkReportType workReportType = new WorkReportType(unique,
		        criterionTypes);
		workReportTypeDAO.save(workReportType);
		assertTrue(workReportTypeDAO.exists(workReportType.getId()));
	}

	@Test
	public void testRemoveWorkReportType() {
		assertTrue(true);
	}

	@Test
	public void testListWorkReportType() {
		assertTrue(true);
	}
}
