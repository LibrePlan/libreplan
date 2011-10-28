/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.test.workreports.daos;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.business.workreports.entities.WorkReportLine;
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
        workReportLine.getWorkReport().removeWorkReportLine(workReportLine);
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
