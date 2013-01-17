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

package org.libreplan.importers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.libreplan.business.calendars.entities.CalendarException;
import org.libreplan.business.calendars.entities.CalendarExceptionType;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.calendars.entities.PredefinedCalendarExceptionTypes;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IAppPropertiesDAO;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.importers.RosterException.RosterExceptionItem;
import org.libreplan.importers.tim.Data;
import org.libreplan.importers.tim.Department;
import org.libreplan.importers.tim.Filter;
import org.libreplan.importers.tim.Period;
import org.libreplan.importers.tim.Person;
import org.libreplan.importers.tim.Roster;
import org.libreplan.importers.tim.RosterCategory;
import org.libreplan.importers.tim.RosterRequest;
import org.libreplan.importers.tim.RosterResponse;
import org.libreplan.web.calendars.IBaseCalendarModel;
import org.libreplan.web.resources.worker.IWorkerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ImportRosterFromTim implements IImportRosterFromTim {

    private static final Log LOG = LogFactory.getLog(ImportRosterFromTim.class);

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IWorkerModel workerModel;

    @Autowired
    private IAppPropertiesDAO appPropertiesDAO;

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    @Autowired
    private ICalendarExceptionTypeDAO calendarExceptionTypeDAO;

    @Autowired
    @Qualifier("subclass")
    private IBaseCalendarModel baseCalendarModel;

    private List<Worker> workers;

    @Override
    @Transactional
    public void importRosters() {
        Map<String, String> prop = appPropertiesDAO.findByMajorId("Tim");
        String url = prop.get("Server");
        String userName = prop.get("Username");
        String password = prop.get("Password");
        int nrDaysRosterFromTim = Integer.parseInt(prop
                .get("NrDaysRosterFromTim"));

        RosterRequest rosterRequest = createRosterRequest(nrDaysRosterFromTim);
        RosterResponse rosterResponse = TimSoapClient
                .sendRequestReceiveResponse(url, userName, password,
                        rosterRequest, RosterResponse.class);

        updateWorkersCalendarException(rosterResponse);
    }


    /**
     * updates workers Exception calendar
     *
     * @param rosterResponse
     *            the response from Tim SOAP server
     */
    private void updateWorkersCalendarException(
            final RosterResponse rosterResponse) {
        adHocTransactionService
                .runOnAnotherTransaction(new IOnTransaction<Void>() {

                    @Override
                    public Void execute() {
                        addAllWorkers();
                        List<RosterException> rosterExceptions = getRosterExceptions(rosterResponse);
                        if (!rosterExceptions.isEmpty()) {
                            updateCalendarException(rosterExceptions);
                        } else {
                            LOG.info("No roster-exceptions found in the response object");
                        }
                        return null;
                    }
                });
    }

    /**
     * Loops through <code>rosterResponse</code> and creates
     * {@link RosterException}s if any and link them to the worker
     *
     * @param rosterResponse
     *            the response
     * @return a list of RosterExceptions
     */
    private List<RosterException> getRosterExceptions(
            RosterResponse rosterResponse) {
        Map<String, List<Roster>> map = getRosterExceptionPerWorker(rosterResponse);
        List<RosterException> rosterExceptions = new ArrayList<RosterException>();

        for (Map.Entry<String, List<Roster>> entry : map.entrySet()) {
            Worker worker = getWorker(entry.getKey());
            if (worker != null) {
                List<Roster> list = entry.getValue();
                Collections.sort(list, new Comparator<Roster>() {
                    @Override
                    public int compare(Roster o1, Roster o2) {
                        return o1.getDate().compareTo(o2.getDate());
                    }
                });
                RosterException re = new RosterException(worker);
                re.addRosterExceptions(list);
                rosterExceptions.add(re);
            }
        }
        return rosterExceptions;
    }

    /**
     * Filters the roster exceptions and creates map of personsNetwork name with
     * associated roster (exceptions)
     *
     * @param rosterResponse
     *            the response
     * @return person-roster exception map
     */
    private Map<String, List<Roster>> getRosterExceptionPerWorker(
            RosterResponse rosterResponse) {
        Map<String, List<Roster>> rosterMap = new HashMap<String, List<Roster>>();
        List<Roster> rosters = rosterResponse.getRosters();
        for (Roster roster : rosters) {
            if (roster.getPrecence().equals("Afwezig")) {
                String personsNetWorkName = roster.getPersons().get(0)
                        .getNetworkName();
                if (!rosterMap.containsKey(personsNetWorkName)) {
                    rosterMap.put(personsNetWorkName, new ArrayList<Roster>());
                }
                rosterMap.get(personsNetWorkName).add(roster);
            }
        }
        return rosterMap;
    }


    /**
     * updates workers calendar exception
     *
     * @param rosterExceptions
     *            list of roster exceptions
     */
    private void updateCalendarException(List<RosterException> rosterExceptions) {
        for (RosterException rosterException : rosterExceptions) {
            List<RosterExceptionItem> items = rosterException
                    .getRosterExceptionItems();
            for (RosterExceptionItem item : items) {
                updateCalendarExceptionPerWorker(rosterException.getWorker(),
                        item.getDate(), item.getExceptionType(),
                        item.getEffortDuration());
            }
        }

    }


    /**
     * updates calendar exception of the specified <code>{@link Worker}</code>
     * for the specified <code>date</code>
     *
     * @param worker
     *            the worker
     * @param date
     *            the date of the exception
     * @param exceptionName
     *            the exception name
     * @param effortDuration
     *            the exceptions effortDurtaion
     */
    private void updateCalendarExceptionPerWorker(Worker worker,
            LocalDate date, String exceptionName, EffortDuration effortDuration) {
        if (!effortDuration.isZero()) {
            CalendarExceptionType calendarExceptionType = getCalendarExceptionType(exceptionName);
            if (calendarExceptionType == null) {
                return;
            }
            ResourceCalendar resourceCalendar = worker.getCalendar();
            if (resourceCalendar == null) {
                LOG.warn("ResourceCalendar of worker '" + worker.getNif()
                        + "' not found");
                return;
            }
            CalendarException calendarExceptionDay = resourceCalendar
                    .getExceptionDay(date);
            Capacity capacity = Capacity.create(effortDuration);
            if (calendarExceptionDay != null) {
                resourceCalendar.removeExceptionDay(calendarExceptionDay
                        .getDate());
            }
            baseCalendarModel.initEdit(resourceCalendar);
            baseCalendarModel.updateException(calendarExceptionType, date,
                    date, capacity);
        }
    }

    /**
     * Searches and returns the calendarExcptionType based on the specified
     * <code>name</code>
     *
     * @param name
     *            the exception calendar name
     */
    private CalendarExceptionType getCalendarExceptionType(String name) {
        if (name == null || name.isEmpty()) {
            LOG.error("name should not be empty");
            return null;
        }
        try {
            String nameToSearch = name;
            if (nameToSearch.contains("Vakantie")) {
                nameToSearch = PredefinedCalendarExceptionTypes.RESOURCE_HOLIDAY
                        .toString();
            }
            return calendarExceptionTypeDAO.findUniqueByName(nameToSearch);
        } catch (InstanceNotFoundException e) {
            LOG.error("Calendar exceptionType not found", e);
        }
        return null;
    }


    /**
     * adds all existing workers to workers list
     */
    private void addAllWorkers() {
        workers = workerDAO.findAll();
    }

    /**
     * returns {@link Worker} based on the specified <code>nif</code>
     *
     * @param nif
     *            the worker's nif
     * @return Worker if found, otherwise null
     */
    private Worker getWorker(String nif) {
        for (Worker worker : workers) {
            if (worker.getNif().equals(nif)) {
                return worker;
            }
        }
        return null;
    }

    /**
     * creates and returns {@link RosterRequest}
     *
     * @param nrDaysRosterFromTim
     *            nr of days required to set the end date
     */
    private RosterRequest createRosterRequest(int nrDaysRosterFromTim) {
        Roster roster = createRoster(nrDaysRosterFromTim);

        Period periode = new Period();
        periode.setStart(new org.joda.time.DateTime());
        periode.setEnd(new org.joda.time.DateTime()
                .plusDays(nrDaysRosterFromTim));
        List<Period> periods = new ArrayList<Period>();
        periods.add(periode);

        Department department = new Department();
        department.setRef("4"); // TODO: make this configurable

        Filter filter = new Filter();
        filter.setPeriods(periods);
        filter.setDepartment(department);

        roster.setFilter(filter);

        roster.setPersons(createPerson());

        roster.setRosterCategories(createRosterCategory());

        roster.setDepartment(department);

        roster.setPrecence(new String());
        roster.setPeriods(periods);

        RosterRequest exportRosterRequest = new RosterRequest();
        Data<Roster> data = new Data<Roster>();
        data.setData(roster);

        exportRosterRequest.setData(data);
        return exportRosterRequest;

    }

    /**
     * creates and returns list of {@link Persoon}
     */
    private List<Person> createPerson() {
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person());
        return persons;
    }

    /**
     * creates and returns list of {@link RosterCategory}
     */
    private List<RosterCategory> createRosterCategory() {
        List<RosterCategory> rosterCategories = new ArrayList<RosterCategory>();
        RosterCategory rosterCategory = new RosterCategory();
        rosterCategory.setName(new String());
        rosterCategories.add(rosterCategory);
        return rosterCategories;
    }

    /**
     * creates and returns {@Roster}
     */
    private Roster createRoster(int nrDaysRosterFromTim) {
        Roster roster = new Roster();
        roster.setStartDate(new LocalDate());
        roster.setEndDate(new LocalDate().plusDays(nrDaysRosterFromTim));
        roster.setResourcePlanning(false);
        roster.setDayPlanning(false);
        roster.setCalendar(false);
        roster.setNonPlaned(true);
        roster.setFullDay(false);
        roster.setConcept(false);
        return roster;
    }
}
