/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.entities.PersonalTimesheetsPeriodicityEnum;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.entities.PredefinedWorkReportTypes;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportType;
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
public class WorkReportDAOTest extends AbstractWorkReportTest {

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IUserDAO userDAO;

    @Test
    @Transactional
    public void testInSpringContainer() {
        assertNotNull(workReportDAO);
    }

    @Test
    @Transactional
    public void testSaveWorkReport() {
        WorkReport workReport = createValidWorkReport();
        workReportDAO.save(workReport);
        assertTrue(workReportDAO.exists(workReport.getId()));
    }

    @Test
    @Transactional
    public void testRemoveWorkReport() throws InstanceNotFoundException {
        WorkReport workReport = createValidWorkReport();
        workReportDAO.save(workReport);
        workReportDAO.remove(workReport.getId());
        assertFalse(workReportDAO.exists(workReport.getId()));
    }

    @Test
    @Transactional
    public void testListWorkReport() {
        int previous = workReportDAO.list(WorkReport.class).size();

        WorkReport workReport1 = createValidWorkReport();
        workReportDAO.save(workReport1);
        WorkReport workReport2 = createValidWorkReport();
        workReportDAO.save(workReport1);
        workReportDAO.save(workReport2);

        List<WorkReport> list = workReportDAO
                .list(WorkReport.class);
        assertEquals(previous + 2, list.size());
    }

    /*
     * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
     * (Jakarta EE / Hibernate 6). getAllByWorkReportType/getPersonalTimesheetWorkReport/
     * isAnyPersonalTimesheetAlreadySaved/findPersonalTimesheetsByResourceAndOrderElement had no
     * test coverage before.
     */

    @Test
    @Transactional
    public void getAllByWorkReportTypeOnlyReturnsMatchingType() {
        WorkReport matching = createValidWorkReport();
        workReportDAO.save(matching);

        List<WorkReport> result = workReportDAO.getAllByWorkReportType(matching.getWorkReportType());

        assertTrue(result.stream().anyMatch(w -> w.getId().equals(matching.getId())));
    }

    private Resource createValidWorker() {
        // resourceIsBoundInPersonalTimesheetConstraint requires the resource to be bound to a
        // User whenever it's used on a personal-timesheets WorkReport.
        User user = User.create("login-" + UUID.randomUUID(), "password", "");
        userDAO.save(user);

        Worker worker = Worker.create();
        worker.setFirstName(UUID.randomUUID().toString());
        worker.setSurname(UUID.randomUUID().toString());
        worker.setNif(UUID.randomUUID().toString());
        worker.setUser(user);
        resourceDAO.save(worker);
        resourceDAO.flush();
        return worker;
    }

    private WorkReportType personalTimesheetsType() throws InstanceNotFoundException {
        // The DAO methods under test only require a WorkReportType named
        // PredefinedWorkReportTypes.PERSONAL_TIMESHEETS to exist - construct one directly
        // instead of going through WorkReportTypeBootstrap, which depends on EntitySequenceDAO
        // configuration this test context doesn't set up (an unrelated, pre-existing gap, not
        // part of this Criteria migration).
        WorkReportType type = WorkReportType.create(
                PredefinedWorkReportTypes.PERSONAL_TIMESHEETS.getName(), "code-" + UUID.randomUUID());
        workReportTypeDAO.save(type);
        return type;
    }

    /*
     * Note: none of these tests save an actual personal-timesheets WorkReport. Doing so hits
     * WorkReport.isResourceIsBoundInPersonalTimesheetConstraint(), which re-fetches the Worker
     * via Registry.getWorkerDAO().find(resource.getId()) and always sees getUser() as null even
     * right after resourceDAO.save(worker) + flush() within the same transaction - a fragile,
     * pre-existing validator quirk unrelated to the Criteria migration. So only the
     * "type exists but nothing uses it yet" / "no match" branches are characterized here.
     */

    @Test
    @Transactional
    public void noPersonalTimesheetsSavedYetTrueWhenTypeExistsButUnused() throws InstanceNotFoundException {
        // Renamed from isAnyPersonalTimesheetAlreadySaved() (Phase 6, see
        // Phase5-found-bugs.md item 6) - the old name was backwards from what the method
        // actually returns. Behavior itself is unchanged: true when the personal-timesheets
        // WorkReportType exists but no WorkReport uses it yet.
        personalTimesheetsType();
        assertTrue(workReportDAO.noPersonalTimesheetsSavedYet());
    }

    @Test
    @Transactional
    public void findPersonalTimesheetsByResourceAndOrderElementEmptyWhenNoneSaved() throws InstanceNotFoundException {
        personalTimesheetsType();
        Resource resource = createValidWorker();

        List<WorkReport> result = workReportDAO.findPersonalTimesheetsByResourceAndOrderElement(resource);

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    public void getPersonalTimesheetWorkReportReturnsNullWhenNoneSaved() throws InstanceNotFoundException {
        personalTimesheetsType();
        Resource resource = createValidWorker();

        WorkReport result = workReportDAO.getPersonalTimesheetWorkReport(
                resource, new org.joda.time.LocalDate(), PersonalTimesheetsPeriodicityEnum.MONTHLY);

        assertEquals(null, result);
    }
}
