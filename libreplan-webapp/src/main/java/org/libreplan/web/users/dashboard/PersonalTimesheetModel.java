/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.web.users.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.NonUniqueResultException;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.daos.IEntitySequenceDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.common.entities.PersonalTimesheetsPeriodicityEnum;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.ISumChargedEffortDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.daos.IResourceAllocationDAO;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.business.workreports.daos.IWorkReportTypeDAO;
import org.libreplan.business.workreports.entities.PredefinedWorkReportTypes;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.web.UserUtil;
import org.libreplan.web.calendars.BaseCalendarModel;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for creation/edition of a personal timesheet
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/myaccount/userDashboard.zul")
public class PersonalTimesheetModel implements IPersonalTimesheetModel {

    private User user;

    private LocalDate date;

    private LocalDate firstDay;

    private LocalDate lastDay;

    private List<OrderElement> orderElements;

    private WorkReport workReport;

    private Map<LocalDate, EffortDuration> capacityMap;

    private boolean modified;

    private Map<OrderElement, Set<LocalDate>> modifiedMap;

    private boolean currentUser;

    private boolean otherReports;

    private Map<Long, EffortDuration> otherEffortPerOrderElement;

    private Map<LocalDate, EffortDuration> otherEffortPerDay;

    private PersonalTimesheetsPeriodicityEnum periodicity;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Autowired
    private ISumChargedEffortDAO sumChargedEffortDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IEntitySequenceDAO entitySequenceDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Override
    @Transactional(readOnly = true)
    public void initCreateOrEdit(LocalDate date) {
        currentUser = true;
        user = UserUtil.getUserFromSession();
        if (!user.isBound()) {
            throw new RuntimeException(
                    "This page only can be used by users bound to a resource");
        }
        initFields(date);
    }

    private void initFields(LocalDate date) {
        this.date = date;

        periodicity = getPersonalTimesheetsPeriodicity();

        initDates();

        initCapacityMap();

        initWorkReport();
        initOrderElements();

        initOtherMaps();

        modified = false;
        modifiedMap = new HashMap<OrderElement, Set<LocalDate>>();
    }

    @Override
    @Transactional(readOnly = true)
    public void initCreateOrEdit(LocalDate date, Resource resource) {
        currentUser = false;
        try {
            user = userDAO.find(((Worker) resource).getUser().getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        initFields(date);
    }

    private void initDates() {
        firstDay = periodicity.getStart(date);
        lastDay = periodicity.getEnd(date);
    }

    private void initCapacityMap() {
        forceLoad(getWorker().getCalendar());

        capacityMap = new HashMap<LocalDate, EffortDuration>();
        for (LocalDate day = firstDay; day.compareTo(lastDay) <= 0; day = day
                .plusDays(1)) {
            capacityMap.put(
                    day,
                    getWorker().getCalendar().getCapacityOn(
                            PartialDay.wholeDay(day)));
        }
    }

    private void forceLoad(ResourceCalendar calendar) {
        BaseCalendarModel.forceLoadBaseCalendar(calendar);
    }

    private void initWorkReport() {
        // Get work report representing this personal timesheet
        workReport = workReportDAO.getPersonalTimesheetWorkReport(
                user.getWorker(), date, periodicity);
        if (workReport == null) {
            // If it doesn't exist yet create a new one
            workReport = WorkReport
                    .create(getPersonalTimesheetsWorkReportType());
            workReport
                    .setCode(entitySequenceDAO
                            .getNextEntityCodeWithoutTransaction(EntityNameEnum.WORK_REPORT));
            workReport.setCodeAutogenerated(true);
            workReport.setResource(user.getWorker());
        }
        forceLoad(workReport.getWorkReportType());
    }

    private WorkReportType getPersonalTimesheetsWorkReportType() {
        try {
            WorkReportType workReportType = workReportTypeDAO
                    .findUniqueByName(PredefinedWorkReportTypes.PERSONAL_TIMESHEETS
                            .getName());
            return workReportType;
        } catch (NonUniqueResultException e) {
            throw new RuntimeException(e);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void forceLoad(WorkReportType workReportType) {
        workReportType.getLineFields().size();
        workReportType.getWorkReportLabelTypeAssigments().size();
        workReportType.getHeadingFields().size();
    }

    private void initOrderElements() {
        List<SpecificResourceAllocation> resourceAllocations = resourceAllocationDAO
                .findSpecificAllocationsRelatedTo(scenarioManager.getCurrent(),
                        UserDashboardUtil.getBoundResourceAsList(user),
                        firstDay, lastDay);

        orderElements = new ArrayList<OrderElement>();
        for (SpecificResourceAllocation each : resourceAllocations) {
            OrderElement orderElement = each.getTask().getOrderElement();
            forceLoad(orderElement);
            orderElements.add(orderElement);
        }

        for (WorkReportLine each : workReport.getWorkReportLines()) {
            OrderElement orderElement = each.getOrderElement();
            if (isNotInOrderElements(orderElement)) {
                forceLoad(orderElement);
                orderElements.add(orderElement);
            }
        }
    }

    private boolean isNotInOrderElements(OrderElement orderElement) {
        return !Util.contains(orderElements, orderElement);
    }

    private void forceLoad(OrderElement orderElement) {
        orderElement.getName();
        if (orderElement.getParent() != null) {
            forceLoad(orderElement.getParent());
        }
    }

    private void initOtherMaps() {
        List<WorkReportLine> workReportLines = workReportLineDAO
                .findByResourceFilteredByDateNotInWorkReport(
                getWorker(), firstDay.toDateTimeAtStartOfDay().toDate(),
                lastDay.toDateTimeAtStartOfDay().toDate(),
                workReport.isNewObject() ? null : workReport);

        otherReports = !workReportLines.isEmpty();

        otherEffortPerOrderElement = new HashMap<Long, EffortDuration>();
        otherEffortPerDay = new HashMap<LocalDate, EffortDuration>();

        for (WorkReportLine line : workReportLines) {
            OrderElement orderElement = line.getOrderElement();
            EffortDuration effort = line.getEffort();
            LocalDate date = LocalDate.fromDateFields(line.getDate());

            initMapKey(otherEffortPerOrderElement, orderElement.getId());
            increaseMap(otherEffortPerOrderElement, orderElement.getId(),
                    effort);

            initMapKey(otherEffortPerDay, date);
            increaseMap(otherEffortPerDay, date, effort);

            if (isNotInOrderElements(orderElement)) {
                forceLoad(orderElement);
                orderElements.add(orderElement);
            }
        }
    }

    private void initMapKey(Map<Long, EffortDuration> map, Long key) {
        if (map.get(key) == null) {
            map.put(key, EffortDuration.zero());
        }
    }

    private void increaseMap(Map<Long, EffortDuration> map, Long key,
            EffortDuration valueToIncrease) {
        map.put(key, map.get(key).plus(valueToIncrease));
    }

    private void initMapKey(Map<LocalDate, EffortDuration> map, LocalDate key) {
        if (map.get(key) == null) {
            map.put(key, EffortDuration.zero());
        }
    }

    private void increaseMap(Map<LocalDate, EffortDuration> map, LocalDate key,
            EffortDuration valueToIncrease) {
        map.put(key, map.get(key).plus(valueToIncrease));
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public LocalDate getFirstDay() {
        return firstDay;
    }

    @Override
    public LocalDate getLastDate() {
        return lastDay;
    }

    @Override
    public Worker getWorker() {
        return user.getWorker();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderElement> getOrderElements() {
        Collections.sort(orderElements, new Comparator<OrderElement>() {

            @Override
            public int compare(OrderElement o1, OrderElement o2) {
                Order order1 = getOrder(o1);
                Order order2 = getOrder(o2);

                int compareOrderName = order1.getName().compareTo(
                        order2.getName());
                if (compareOrderName != 0) {
                    return compareOrderName;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return orderElements;
    }

    @Override
    public EffortDuration getEffortDuration(OrderElement orderElement,
            LocalDate date) {
        WorkReportLine workReportLine = getWorkReportLine(orderElement, date);
        if (workReportLine == null) {
            return null;
        }
        return workReportLine.getEffort();
    }

    private WorkReportLine getWorkReportLine(OrderElement orderElement,
            LocalDate date) {
        for (WorkReportLine line : workReport.getWorkReportLines()) {
            if (line.getOrderElement().equals(orderElement)
                    && LocalDate.fromDateFields(line.getDate()).equals(date)) {
                return line;
            }
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public void setEffortDuration(OrderElement orderElement, LocalDate date,
            EffortDuration effortDuration) {
        WorkReportLine workReportLine = getWorkReportLine(orderElement, date);
        if (workReportLine == null) {
            workReportLine = createWorkReportLine(orderElement, date);
            workReport.addWorkReportLine(workReportLine);
        }
        workReportLine.setEffort(effortDuration);
        modified = true;
        markAsModified(orderElement, date);
    }

    private void markAsModified(OrderElement orderElement, LocalDate date) {
        if (modifiedMap.get(orderElement) == null) {
            modifiedMap.put(orderElement, new HashSet<LocalDate>());
        }
        modifiedMap.get(orderElement).add(date);
    }

    private WorkReportLine createWorkReportLine(OrderElement orderElement,
            LocalDate date) {
        WorkReportLine workReportLine = WorkReportLine.create(workReport);
        workReportLine.setCodeAutogenerated(true);
        workReportLine.setOrderElement(orderElement);
        workReportLine.setDate(date.toDateTimeAtStartOfDay().toDate());
        workReportLine.setTypeOfWorkHours(getTypeOfWorkHours());
        workReportLine.setEffort(EffortDuration.zero());
        return workReportLine;
    }

    private TypeOfWorkHours getTypeOfWorkHours() {
        return configurationDAO.getConfiguration()
                .getPersonalTimesheetsTypeOfWorkHours();
    }

    @Override
    @Transactional
    public void save() {
        if (workReport.getWorkReportLines().isEmpty()
                && workReport.isNewObject()) {
            // Do nothing.
            // A new work report if it doesn't have work report lines is not
            // saved as it will not be possible to find it later with
            // WorkReportDAO.getPersonalTimesheetWorkReport() method.
        } else {
            sumChargedEffortDAO
                    .updateRelatedSumChargedEffortWithWorkReportLineSet(workReport
                            .getWorkReportLines());
            workReport.generateWorkReportLineCodes(entitySequenceDAO
                    .getNumberOfDigitsCode(EntityNameEnum.WORK_REPORT));
            workReportDAO.save(workReport);
        }

        resetModifiedFields();
    }

    private void resetModifiedFields() {
        modified = false;
        modifiedMap = new HashMap<OrderElement, Set<LocalDate>>();
    }

    @Override
    public void cancel() {
        user = null;
        date = null;
        orderElements = null;
        workReport = null;
        resetModifiedFields();
    }

    @Override
    public EffortDuration getEffortDuration(OrderElement orderElement) {
        EffortDuration result = EffortDuration.zero();
        for (WorkReportLine line : workReport.getWorkReportLines()) {
            if (line.getOrderElement().equals(orderElement)) {
                result = result.plus(line.getEffort());
            }
        }
        return result;
    }

    @Override
    public EffortDuration getEffortDuration(LocalDate date) {
        EffortDuration result = EffortDuration.zero();
        for (WorkReportLine line : workReport.getWorkReportLines()) {
            if (LocalDate.fromDateFields(line.getDate()).equals(date)) {
                result = result.plus(line.getEffort());
            }
        }
        return result;
    }

    @Override
    public EffortDuration getTotalEffortDuration() {
        return workReport.getTotalEffortDuration();
    }

    @Override
    public EffortDuration getResourceCapacity(LocalDate date) {
        return capacityMap.get(date);
    }

    @Override
    public void addOrderElement(OrderElement orderElement) {
        if (isNotInOrderElements(orderElement)) {
            orderElements.add(orderElement);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(OrderElement orderElement) {
        return orderDAO.loadOrderAvoidingProxyFor(orderElement);
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFirstPeriod() {
        LocalDate activationDate = getWorker().getCalendar()
                .getFistCalendarAvailability().getStartDate();
        return firstDay.equals(periodicity.getStart(activationDate));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLastPeriod() {
        return firstDay.equals(periodicity.getStart(new LocalDate()
                .plusMonths(1)));
    }

    @Override
    public boolean wasModified(OrderElement orderElement, LocalDate date) {
        Set<LocalDate> dates = modifiedMap.get(orderElement);
        return (dates != null) && dates.contains(date);
    }

    @Override
    public boolean isCurrentUser() {
        return currentUser;
    }

    @Override
    public boolean hasOtherReports() {
        return otherReports;
    }

    @Override
    public EffortDuration getOtherEffortDuration(OrderElement orderElement) {
        EffortDuration effort = otherEffortPerOrderElement.get(orderElement
                .getId());
        return effort == null ? EffortDuration.zero() : effort;
    }

    @Override
    public EffortDuration getOtherEffortDuration(LocalDate date) {
        EffortDuration effort = otherEffortPerDay.get(date);
        return effort == null ? EffortDuration.zero() : effort;
    }

    @Override
    public EffortDuration getTotalOtherEffortDuration() {
        EffortDuration result = EffortDuration.zero();
        for (EffortDuration effort : otherEffortPerOrderElement.values()) {
            result = result.plus(effort);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalTimesheetsPeriodicityEnum getPersonalTimesheetsPeriodicity() {
        return configurationDAO.getConfiguration()
                .getPersonalTimesheetsPeriodicity();
    }

    @Override
    public String getTimesheetString() {
        return PersonalTimesheetDTO.toString(periodicity, date);
    }

    @Override
    public LocalDate getPrevious() {
        return periodicity.previous(date);
    }

    @Override
    public LocalDate getNext() {
        return periodicity.next(date);
    }

}
