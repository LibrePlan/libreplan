/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package importers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.entities.CalendarException;
import org.libreplan.business.calendars.entities.CalendarExceptionType;
import org.libreplan.business.calendars.entities.CalendarExceptionTypeColor;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.importers.ImportRosterFromTim;
import org.libreplan.importers.TimSoapClient;
import org.libreplan.importers.tim.Data;
import org.libreplan.importers.tim.Department;
import org.libreplan.importers.tim.Filter;
import org.libreplan.importers.tim.Period;
import org.libreplan.importers.tim.Person;
import org.libreplan.importers.tim.Roster;
import org.libreplan.importers.tim.RosterCategory;
import org.libreplan.importers.tim.RosterRequest;
import org.libreplan.importers.tim.RosterResponse;
import org.libreplan.web.resources.worker.IWorkerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test for {@link ImportRosterFromTim}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
public class ImportRosterFromTimTest {
    private Properties properties = null;
    private RosterResponse rosterResponse = null;

    @Autowired
    IWorkerDAO workerDAO;

    @Autowired
    IWorkerModel workerModel;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Before
    public void loadProperties() throws FileNotFoundException, IOException {
        String filename = System.getProperty("user.dir")
                + "/../scripts/tim-connector/tim-conn.properties";
        properties = new Properties();
        properties.load(new FileInputStream(filename));
    }

    @Before
    public void createRosterResponseFromFile() {
        String filename = System.getProperty("user.dir")
                + "/../scripts/tim_test/rosterResponse.xml";
        File file = new File(filename);
        rosterResponse = TimSoapClient.unmarshalRosterFromFile(file);

    }

    private List<Worker> getAllWorkers() {
        return transactionService
                .runOnAnotherTransaction(new IOnTransaction<List<Worker>>() {
                    @Override
                    public List<Worker> execute() {
                        return workerDAO.findAll();
                    }
                });
    }

    private RosterRequest createtRosterRequest() {
        Roster roster = new Roster();
        roster.setStartDate(new LocalDate());
        roster.setEndDate(new LocalDate().plusDays(7));
        roster.setResourcePlanning(false);
        roster.setDayPlanning(false);
        roster.setCalendar(false);
        roster.setNonPlaned(true);
        roster.setFullDay(false);
        roster.setConcept(false);

        Period periode = new Period();
        periode.setStart(new org.joda.time.DateTime());
        periode.setEnd(new org.joda.time.DateTime().plusDays(7));
        List<Period> periods = new ArrayList<Period>();
        periods.add(periode);

        Department department = new Department();
        department.setRef("4");

        RosterCategory rosterCategory = new RosterCategory();
        rosterCategory.setName(new String());
        List<RosterCategory> rosterCategories = new ArrayList<RosterCategory>();
        rosterCategories.add(rosterCategory);

        Filter filter = new Filter();
        filter.setPeriods(periods);
        filter.setDepartment(department);

        roster.setFilter(filter);

        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person());
        roster.setPersons(persons);

        roster.setRosterCategories(rosterCategories);

        roster.setDepartment(department);

        roster.setPrecence(new String());

        roster.setPeriods(periods);

        RosterRequest exportRosterRequest = new RosterRequest();
        Data<Roster> data = new Data<Roster>();
        data.setData(roster);

        exportRosterRequest.setData(data);
        return exportRosterRequest;

    }

    private Map<String, List<Roster>> getRosterExceptionPerWorker() {
        Map<String, List<Roster>> rosterExceptionMap = new HashMap<String, List<Roster>>();
        List<Roster> rosters = rosterResponse.getRosters();
        for (Roster roster : rosters) {
            if (roster.getPrecence().equals("Afwezig")) {
                String personsNetWorkName = roster.getPersons().get(0)
                        .getNetworkName();
                if (!rosterExceptionMap.containsKey(personsNetWorkName)) {
                    rosterExceptionMap.put(personsNetWorkName,
                            new ArrayList<Roster>());
                }
                rosterExceptionMap.get(personsNetWorkName).add(roster);
            }
        }
        return rosterExceptionMap;
    }

    private List<Roster> getTheFirstRosterException() {
        Map<String, List<Roster>> rosterExceptionMap = getRosterExceptionPerWorker();
        return (List<Roster>) rosterExceptionMap.entrySet().iterator().next()
                .getValue();
    }

    private String getExceptionType(List<Roster> rosters) {
        for (Roster roster : rosters) {
            return roster.getRosterCategories().get(0).getName();
        }
        return null;
    }

    private Entry<LocalDate, EffortDuration> getTheFirstEffortDuration(
            List<Roster> rosters) {
        EffortDuration sum = EffortDuration.zero();
        Map<LocalDate, EffortDuration> map = new TreeMap<LocalDate, EffortDuration>();
        for (Roster roster : rosters) {
            EffortDuration duration = EffortDuration
                    .parseFromFormattedString(roster.getDuration());
            sum = EffortDuration.sum(sum, duration);
            map.put(roster.getDate(), sum);
        }
        return map.entrySet().iterator().next();
    }

    private Worker getWorker(final String nif) {
        for (Worker worker : getAllWorkers()) {
            if (worker.getNif().equals(nif)) {
                return worker;
            }
        }

        Worker worker = Worker.create();
        worker.setFirstName("First name");
        worker.setSurname("Surname");
        worker.setNif(nif);
        return worker;
    }

    // @Test
    public void testImportRostersFromServer() {
        RosterResponse rosterResponse = TimSoapClient
                .sendRequestReceiveResponse(properties.getProperty("url"),
                        properties.getProperty("username"),
                        properties.getProperty("password"),
                        createtRosterRequest(), RosterResponse.class);
        assertTrue(rosterResponse != null);
    }

    @Test
    public void testImportRosterAndCheckRosterExceptionsPerWorker() {
        Map<String, List<Roster>> result = getRosterExceptionPerWorker();
        assertTrue(!result.isEmpty());
    }

    @Test
    public void testRosterExceptionType() {
        List<Roster> rosters = getTheFirstRosterException();
        String exceptionType = getExceptionType(rosters);
        assertEquals(exceptionType, "3 Vakantie");
    }

    @Test
    public void testRosterExceptionsTotalEffortDuration() {
        List<Roster> rosters = getTheFirstRosterException();
        Entry<LocalDate, EffortDuration> effortDurationMap = getTheFirstEffortDuration(rosters);
        EffortDuration duration = effortDurationMap.getValue();
        assertThat(duration.getHours(), equalTo(9));
    }

    @Test
    public void testCreateCalendarException() {
        List<Roster> rosters = getTheFirstRosterException();
        String nif = rosters.get(0).getPersons().get(0).getNetworkName();

        Entry<LocalDate, EffortDuration> dateDurationEntry = getTheFirstEffortDuration(rosters);
        LocalDate date = dateDurationEntry.getKey();
        EffortDuration duration = dateDurationEntry.getValue();

        Capacity capacity = Capacity.create(duration);
        Worker worker = getWorker(nif);

        ResourceCalendar resourceCalendar = ResourceCalendar.create();
        resourceCalendar.setName("test");
        resourceCalendar.setCodeAutogenerated();
        resourceCalendar.setCapacity(capacity.getStandardEffort().getHours());
        worker.setCalendar(resourceCalendar);

        CalendarExceptionType type = CalendarExceptionType.create("TEST",
                CalendarExceptionTypeColor.DEFAULT, true);
        CalendarException calendarException = CalendarException.create(date,
                capacity, type);
        resourceCalendar.addExceptionDay(calendarException);
        assertEquals(capacity.getStandardEffort().getHours(), resourceCalendar
                .getCapacity().intValue());
    }

}
