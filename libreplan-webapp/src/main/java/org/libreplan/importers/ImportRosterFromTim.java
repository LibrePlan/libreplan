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
import org.libreplan.importers.tim.DataDTO;
import org.libreplan.importers.tim.DepartmentDTO;
import org.libreplan.importers.tim.FilterDTO;
import org.libreplan.importers.tim.PeriodDTO;
import org.libreplan.importers.tim.PersonDTO;
import org.libreplan.importers.tim.RosterCategoryDTO;
import org.libreplan.importers.tim.RosterDTO;
import org.libreplan.importers.tim.RosterRequestDTO;
import org.libreplan.importers.tim.RosterResponseDTO;
import org.libreplan.web.calendars.IBaseCalendarModel;
import org.libreplan.web.resources.worker.IWorkerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of import roosters from tim
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
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

        RosterRequestDTO rosterRequestDTO = createRosterRequest(nrDaysRosterFromTim);
        RosterResponseDTO rosterResponseDTO = TimSoapClient
                .sendRequestReceiveResponse(url, userName, password,
                        rosterRequestDTO, RosterResponseDTO.class);

        updateWorkersCalendarException(rosterResponseDTO);
    }


    /**
     * updates workers Exception calendar
     *
     * @param rosterResponse
     *            the response from Tim SOAP server
     */
    private void updateWorkersCalendarException(
            final RosterResponseDTO rosterResponse) {
        adHocTransactionService
                .runOnAnotherTransaction(new IOnTransaction<Void>() {

                    @Override
                    public Void execute() {
                        addAllWorkers();
                        List<RosterException> rosterExceptions = getRosterExceptions(rosterResponse);
                        if (!rosterExceptions.isEmpty()) {
                            updateCalendarException(rosterExceptions);
                        } else {
                            LOG.info("No roster-exceptions found in the response");
                        }
                        return null;
                    }
                });
    }

    /**
     * Loops through <code>rosterResponseDTO</code> and creates
     * {@link RosterException}s and link them to the <code>worker</code>
     *
     * @param rosterResponseDTO
     *            the response
     * @return a list of RosterExceptions
     */
    private List<RosterException> getRosterExceptions(
            RosterResponseDTO rosterResponseDTO) {
        Map<String, List<RosterDTO>> map = getRosterExceptionPerWorker(rosterResponseDTO);
        List<RosterException> rosterExceptions = new ArrayList<RosterException>();

        for (Map.Entry<String, List<RosterDTO>> entry : map.entrySet()) {
            Worker worker = getWorker(entry.getKey());
            if (worker != null) {
                List<RosterDTO> list = entry.getValue();
                Collections.sort(list, new Comparator<RosterDTO>() {
                    @Override
                    public int compare(RosterDTO o1, RosterDTO o2) {
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
     * Filters the roster on exceptions(absence) and creates a map with
     * <code>personsNetwork-name</name> as key
     * and list of <code>roster-exception</code> as value
     *
     * @param rosterResponseDTO
     *            the response
     * @return person-roster exception map
     */
    private Map<String, List<RosterDTO>> getRosterExceptionPerWorker(
            RosterResponseDTO rosterResponseDTO) {
        Map<String, List<RosterDTO>> rosterMap = new HashMap<String, List<RosterDTO>>();
        List<RosterDTO> rosterDTOs = rosterResponseDTO.getRosters();
        for (RosterDTO rosterDTO : rosterDTOs) {
            if (rosterDTO.getPrecence().equals("Afwezig")) {
                String personsNetWorkName = rosterDTO.getPersons().get(0)
                        .getNetworkName();
                if (!rosterMap.containsKey(personsNetWorkName)) {
                    rosterMap.put(personsNetWorkName, new ArrayList<RosterDTO>());
                }
                rosterMap.get(personsNetWorkName).add(rosterDTO);
            }
        }
        return rosterMap;
    }


    /**
     * updates the workers calendar exception
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
     * updates the calendar exception of the specified
     * <code>{@link Worker}</code> for the specified <code>date</code>
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
            LOG.error("Exception name should not be empty");
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
     * returns {@link Worker} for the specified <code>nif</code>
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
     * creates and returns {@link RosterRequestDTO}
     *
     * @param nrDaysRosterFromTim
     *            nr of days required to set the end date
     */
    private RosterRequestDTO createRosterRequest(int nrDaysRosterFromTim) {
        RosterDTO rosterDTO = createRoster(nrDaysRosterFromTim);

        PeriodDTO periodeDTO = new PeriodDTO();
        periodeDTO.setStart(new org.joda.time.DateTime());
        periodeDTO.setEnd(new org.joda.time.DateTime()
                .plusDays(nrDaysRosterFromTim));
        List<PeriodDTO> periodDTOs = new ArrayList<PeriodDTO>();
        periodDTOs.add(periodeDTO);

        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setRef("4"); // TODO: make this configurable

        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setPeriods(periodDTOs);
        filterDTO.setDepartment(departmentDTO);

        rosterDTO.setFilter(filterDTO);

        rosterDTO.setPersons(createPerson());

        rosterDTO.setRosterCategories(createRosterCategory());

        rosterDTO.setDepartment(departmentDTO);

        rosterDTO.setPrecence(new String());
        rosterDTO.setPeriods(periodDTOs);

        RosterRequestDTO exportRosterRequestDTO = new RosterRequestDTO();
        DataDTO<RosterDTO> dataDTO = new DataDTO<RosterDTO>();
        dataDTO.setData(rosterDTO);

        exportRosterRequestDTO.setData(dataDTO);
        return exportRosterRequestDTO;

    }

    /**
     * creates and returns list of {@link PersonDTO}
     */
    private List<PersonDTO> createPerson() {
        List<PersonDTO> personDTOs = new ArrayList<PersonDTO>();
        personDTOs.add(new PersonDTO());
        return personDTOs;
    }

    /**
     * creates and returns list of {@link RosterCategoryDTO}
     */
    private List<RosterCategoryDTO> createRosterCategory() {
        List<RosterCategoryDTO> rosterCategorieDTOs = new ArrayList<RosterCategoryDTO>();
        RosterCategoryDTO rosterCategoryDTO = new RosterCategoryDTO();
        rosterCategoryDTO.setName(new String());
        rosterCategorieDTOs.add(rosterCategoryDTO);
        return rosterCategorieDTOs;
    }

    /**
     * creates and returns {@link RosterDTO}
     */
    private RosterDTO createRoster(int nrDaysRosterFromTim) {
        RosterDTO rosterDTO = new RosterDTO();
        rosterDTO.setStartDate(new LocalDate());
        rosterDTO.setEndDate(new LocalDate().plusDays(nrDaysRosterFromTim));
        rosterDTO.setResourcePlanning(false);
        rosterDTO.setDayPlanning(false);
        rosterDTO.setCalendar(false);
        rosterDTO.setNonPlaned(true);
        rosterDTO.setFullDay(false);
        rosterDTO.setConcept(false);
        return rosterDTO;
    }
}
