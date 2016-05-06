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

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import org.libreplan.business.common.daos.IConnectorDAO;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.common.entities.PredefinedConnectorProperties;
import org.libreplan.business.common.entities.PredefinedConnectors;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
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
    private IWorkerDAO workerDAO;

    @Autowired
    private IConnectorDAO connectorDAO;

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    @Autowired
    private ICalendarExceptionTypeDAO calendarExceptionTypeDAO;

    @Autowired
    @Qualifier("subclass")
    private IBaseCalendarModel baseCalendarModel;

    private SynchronizationInfo synchronizationInfo;

    /**
     * Search criteria for roster exception days in RESPONSE message
     * {@link RosterDTO}
     */
    private static final String ABSENT = "Afwezig";

    /**
     * The word "Vakantie"(holiday) in RESPONSE message that would be translated
     * to {@link PredefinedCalendarExceptionTypes#RESOURCE_HOLIDAY }
     */
    private static final String HOLIDAY = "Vakantie";

    /**
     * The word "Feestdag"(bank holiday) in RESPONSE message that would be
     * translated to {@link PredefinedCalendarExceptionTypes#BANK_HOLIDAY}
     */
    private static final String BANK_HOLIDAY = "Feestdag";


    @Override
    @Transactional
    public List<SynchronizationInfo> importRosters() throws ConnectorException {
        Connector connector = connectorDAO
                .findUniqueByName(PredefinedConnectors.TIM.getName());
        if (connector == null) {
            throw new ConnectorException(_("Tim connector not found"));
        }

        if (!connector.areConnectionValuesValid()) {
            throw new ConnectorException(
                    _("Connection values of Tim connector are invalid"));
        }

        Map<String, String> properties = connector.getPropertiesAsMap();
        String url = properties.get(PredefinedConnectorProperties.SERVER_URL);

        String userName = properties
                .get(PredefinedConnectorProperties.USERNAME);

        String password = properties
                .get(PredefinedConnectorProperties.PASSWORD);

        int nrDaysRosterFromTim = Integer.parseInt(properties
                .get(PredefinedConnectorProperties.TIM_NR_DAYS_ROSTER));

        int productivityFactor = Integer.parseInt(properties
                .get(PredefinedConnectorProperties.TIM_PRODUCTIVITY_FACTOR));


        String departmentIds = properties
                .get(PredefinedConnectorProperties.TIM_DEPARTAMENTS_IMPORT_ROSTER);

        if (StringUtils.isBlank(departmentIds)) {
            LOG.warn("No departments configured");
            throw new ConnectorException(_("No departments configured"));
        }

        String[] departmentIdsArray = StringUtils.stripAll(StringUtils.split(
                departmentIds, ","));

        List<SynchronizationInfo> syncInfos = new ArrayList<SynchronizationInfo>();

        for (String department : departmentIdsArray) {
            LOG.info("Department: " + department);

            synchronizationInfo = new SynchronizationInfo(_(
                    "Import roster for department {0}", department));

            RosterRequestDTO rosterRequestDTO = createRosterRequest(department,
                    nrDaysRosterFromTim);
            RosterResponseDTO rosterResponseDTO = TimSoapClient
                    .sendRequestReceiveResponse(url, userName, password,
                            rosterRequestDTO, RosterResponseDTO.class);

            if (rosterResponseDTO != null) {
                updateWorkersCalendarException(rosterResponseDTO,
                        productivityFactor);
                if (!synchronizationInfo.isSuccessful()) {
                    syncInfos.add(synchronizationInfo);
                }
            } else {
                LOG.error("No valid response for department " + department);
                synchronizationInfo.addFailedReason(_(
                                "No valid response for department \"{0}\"",
                                department));
                syncInfos.add(synchronizationInfo);
            }
        }
        return syncInfos;
    }

    /**
     * updates workers Exception calendar
     *
     * @param rosterResponse
     *            the response from Tim SOAP server
     */
    private void updateWorkersCalendarException(
            final RosterResponseDTO rosterResponse, final int productivityFactor) {
        adHocTransactionService
                .runOnAnotherTransaction(new IOnTransaction<Void>() {

                    @Override
                    public Void execute() {
                        List<RosterException> rosterExceptions = getRosterExceptions(
                                rosterResponse, productivityFactor);
                        if (!rosterExceptions.isEmpty()) {
                            updateCalendarException(rosterExceptions);
                        } else {
                            LOG.info("No roster-exceptions found in the response");
                            synchronizationInfo
                                    .addFailedReason(_("No roster-exceptions found in the response"));
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
            RosterResponseDTO rosterResponseDTO, int productivityFactor) {
        Map<String, List<RosterDTO>> map = getRosterExceptionPerWorker(rosterResponseDTO);

        List<RosterException> rosterExceptions = new ArrayList<RosterException>();

        for (Map.Entry<String, List<RosterDTO>> entry : map.entrySet()) {
            Worker worker = null;
            String workerCode = entry.getKey();
            try {
                worker = workerDAO.findUniqueByNif(workerCode);
            } catch (InstanceNotFoundException e) {
                LOG.warn("Worker '" + workerCode + "' not found");
                synchronizationInfo.addFailedReason(_(
                        "Worker \"{0}\" not found",
                        workerCode));
            }
            if (worker != null) {
                List<RosterDTO> list = entry.getValue();
                Collections.sort(list, new Comparator<RosterDTO>() {
                    @Override
                    public int compare(RosterDTO o1, RosterDTO o2) {
                        return o1.getDate().compareTo(o2.getDate());
                    }
                });
                RosterException re = new RosterException(worker,
                        productivityFactor);
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
            if (rosterDTO.getPrecence().equals(ABSENT)) {
                String personsNetWorkName = rosterDTO.getPersons().get(0)
                        .getNetworkName();
                if (!rosterMap.containsKey(personsNetWorkName)) {
                    rosterMap.put(personsNetWorkName,
                            new ArrayList<RosterDTO>());
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
        CalendarExceptionType calendarExceptionType = getCalendarExceptionType(exceptionName);
        if (calendarExceptionType == null) {
            return;
        }
        ResourceCalendar resourceCalendar = (ResourceCalendar) worker
                .getCalendarOrDefault();
        CalendarException calendarExceptionDay = resourceCalendar
                .getExceptionDay(date);
        Capacity capacity = Capacity.create(effortDuration);
        if (calendarExceptionDay != null) {
            resourceCalendar.removeExceptionDay(calendarExceptionDay.getDate());
        }
        baseCalendarModel.initEdit(resourceCalendar);
        baseCalendarModel.updateException(calendarExceptionType, date, date,
                capacity);
        baseCalendarModel.confirmSave();
    }

    /**
     * Searches and returns the calendarExcptionType based on the specified
     * <code>name</code>
     *
     * If the specified parameter <code>name</code> contains the word
     * {@link ImportRosterFromTim#HOLIDAY}, the
     * <code>calendarExceptionType</code> assumed to be the
     * {@link PredefinedCalendarExceptionTypes#RESOURCE_HOLIDAY}, otherwise it
     * searches in {@link CalendarExceptionType} for unique
     * <code>calendarExceptionType</code>
     *
     * @param name
     *            the exception calendar name
     */
    private CalendarExceptionType getCalendarExceptionType(String name) {
        if (name == null || name.isEmpty()) {
            LOG.error("Exception name should not be empty");
            synchronizationInfo
                    .addFailedReason(_("Exception name should not be empty"));
            return null;
        }
        try {
            String nameToSearch = name;
            if (nameToSearch.contains(HOLIDAY)) {
                nameToSearch = PredefinedCalendarExceptionTypes.RESOURCE_HOLIDAY
                        .toString();
            } else if (nameToSearch.equals(BANK_HOLIDAY)) {
                nameToSearch = PredefinedCalendarExceptionTypes.BANK_HOLIDAY
                        .toString();
            }
            return calendarExceptionTypeDAO.findUniqueByName(nameToSearch);
        } catch (InstanceNotFoundException e) {
            LOG.error("Calendar exceptionType not found", e);
            synchronizationInfo
                    .addFailedReason(_("Calendar exception day not found"));
        }
        return null;
    }


    /**
     * creates and returns {@link RosterRequestDTO}
     *
     * @param nrDaysRosterFromTim
     *            nr of days required to set the end date
     */
    private RosterRequestDTO createRosterRequest(String department,
            int nrDaysRosterFromTim) {
        RosterDTO rosterDTO = createRoster(nrDaysRosterFromTim);

        PeriodDTO periodeDTO = new PeriodDTO();
        periodeDTO.setStart(new org.joda.time.DateTime());
        periodeDTO.setEnd(new org.joda.time.DateTime()
                .plusDays(nrDaysRosterFromTim));
        List<PeriodDTO> periodDTOs = new ArrayList<PeriodDTO>();
        periodDTOs.add(periodeDTO);

        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setRef(department);

        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setPeriods(periodDTOs);
        filterDTO.setDepartment(departmentDTO);

        rosterDTO.setFilter(filterDTO);

        rosterDTO.setPersons(createEmptyPerson());

        rosterDTO.setRosterCategories(createEmptyRosterCategory());

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
     * creates and returns list of {@link PersonDTO} with empty
     * {@link PersonDTO}
     *
     * This is an indication to Tim server that it should include this Person
     * information in the RESPONSE message
     */
    private List<PersonDTO> createEmptyPerson() {
        List<PersonDTO> personDTOs = new ArrayList<PersonDTO>();
        personDTOs.add(new PersonDTO());
        return personDTOs;
    }

    /**
     * creates and returns list of {@link RosterCategoryDTO} with empty
     * {@link RosterCategoryDTO}
     *
     * This is an indication to Tim server that it should include this
     * RosterCategory information in the RESPONSE message
     */
    private List<RosterCategoryDTO> createEmptyRosterCategory() {
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

    @Override
    public SynchronizationInfo getSynchronizationInfo() {
        return synchronizationInfo;
    }
}
