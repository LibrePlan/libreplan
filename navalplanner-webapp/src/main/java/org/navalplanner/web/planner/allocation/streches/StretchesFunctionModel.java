/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.planner.allocation.streches;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.planner.daos.IAssignmentFunctionDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.AssignmentFunction;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Stretch;
import org.navalplanner.business.planner.entities.StretchesFunction;
import org.navalplanner.business.planner.entities.StretchesFunction.Interval;
import org.navalplanner.business.planner.entities.StretchesFunctionTypeEnum;
import org.navalplanner.business.planner.entities.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.util.Locales;

/**
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Model for UI operations related to {@link StretchesFunction}
 *         configuration.
 *
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StretchesFunctionModel implements IStretchesFunctionModel {

    public static StretchesFunction createDefaultStretchesFunction(LocalDate endDate) {
        StretchesFunction stretchesFunction = StretchesFunction.create();
        stretchesFunction.addStretch(Stretch.create(endDate, BigDecimal.ONE,
                BigDecimal.ONE));
        return stretchesFunction;
    }

    /**
     * Conversation state
     */
    private StretchesFunction stretchesFunction;

    private Task task;

    private Date taskEndDate;

    private StretchesFunction originalStretchesFunction;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IAssignmentFunctionDAO assignmentFunctionDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    private ResourceAllocation<?> resourceAllocation;

    @Override
    @Transactional(readOnly = true)
    public void init(
            StretchesFunction stretchesFunction,
            ResourceAllocation<?> resourceAllocation,
            StretchesFunctionTypeEnum type) {
        if (stretchesFunction != null) {
            assignmentFunctionDAO.reattach(stretchesFunction);

            // Initialize resourceAllocation and task
            this.resourceAllocation = resourceAllocation;
            this.task = resourceAllocation.getTask();
            forceLoadData();
            this.taskEndDate = task.getEndDate();

            // Initialize stretchesFunction
            this.originalStretchesFunction = stretchesFunction;
            this.stretchesFunction = stretchesFunction.copy();
            this.stretchesFunction.changeTypeTo(type);
            addConsolidatedStretchIfAny();
        }
    }

    private void addConsolidatedStretchIfAny() {
        Stretch consolidated = consolidatedStretchFor(resourceAllocation);
        if (consolidated != null) {
            stretchesFunction.setConsolidatedStretch(consolidated);
        }
    }

    private Stretch consolidatedStretchFor(ResourceAllocation<?> resourceAllocation) {
        return Stretch.buildFromConsolidatedProgress(resourceAllocation);
    }

    private void forceLoadData() {
        taskSourceDAO.reattach(task.getTaskSource());
        taskElementDAO.reattach(task);
        task.getCalendar();
        this.resourceAllocation.getAssignedHours();
    }

    @Override
    public List<Stretch> getStretches() {
        if (stretchesFunction == null) {
            return Collections.emptyList();
        }
        return allStretches();
    }

    /**
     * Returns an empty stretch plus the stretches from stretchesFunction and
     * the consolidated stretch if any
     *
     * @return
     */
    private List<Stretch> allStretches() {
        List<Stretch> result = new ArrayList<Stretch>();
        result.add(firstStretch());
        result.addAll(stretchesFunction.getStretchesPlusConsolidated());
        return result;
    }

    /**
     * Defines an initial read-only stretch with 0% hours worked and 0% progress
     *
     * @return
     */
    private Stretch firstStretch() {
        Stretch result = Stretch.create(task.getStartAsLocalDate(),
                BigDecimal.ZERO, BigDecimal.ZERO);
        result.readOnly(true);
        return result;
    }

    @Override
    public void confirm() throws ValidationException {
        if (stretchesFunction != null) {
            if (!stretchesFunction.checkNoEmpty()) {
                throw new ValidationException(
                        _("At least one stretch is needed"));
            }
            if (!stretchesFunction.checkStretchesOrder()) {
                throw new ValidationException(
                        _("Some stretch has higher or equal values than the "
                                + "previous stretch"));
            }
            if (!stretchesFunction.checkOneHundredPercent()) {
                throw new ValidationException(
                        _("Last stretch should have one hundred percent for "
                                + "length and amount of work percentage"));
            }
            if (!stretchesFunction.ifInterpolatedMustHaveAtLeastTwoStreches()) {
                throw new ValidationException(
                        _("For interpolation at least two stretches are needed"));
            }
            if (stretchesFunction.getDesiredType() == StretchesFunctionTypeEnum.INTERPOLATED) {
                if (!atLeastTwoStreches(getStretches())) {
                    throw new ValidationException(
                            _("There must be at least 2 stretches for doing interpolation"));
                }
                if (!theFirstIntervalIsPosteriorToFirstDay(getStretches(),
                        getTaskStartDate())) {
                    throw new ValidationException(
                            _("The first stretch must be after the first day for doing interpolation"));
                }
            }
            if (originalStretchesFunction != null) {
                originalStretchesFunction
                        .resetToStrechesFrom(stretchesFunction);
                originalStretchesFunction.changeTypeTo(stretchesFunction
                                .getDesiredType());
                stretchesFunction = originalStretchesFunction;
            }
        }
    }

    public static boolean areValidForInterpolation(List<Stretch> stretches,
            LocalDate start) {
        return atLeastTwoStreches(stretches)
                && theFirstIntervalIsPosteriorToFirstDay(stretches, start);
    }

    private static boolean atLeastTwoStreches(List<Stretch> stretches) {
        return stretches.size() >= 2;
    }

    private static boolean theFirstIntervalIsPosteriorToFirstDay(
            List<Stretch> stretches, LocalDate start) {
        List<Interval> intervals = StretchesFunction.intervalsFor(stretches);
        if (intervals.isEmpty()) {
            return false;
        }
        Interval first = intervals.get(0);
        return first.getEnd().compareTo(start) > 0;
    }

    @Override
    public void cancel() {
        stretchesFunction = originalStretchesFunction;
    }

    @Override
    public void addStretch() {
        if (stretchesFunction != null) {
            stretchesFunction.addStretch(newStretch());
        }
    }

    private Stretch newStretch() {
        LocalDate startDate = getTaskStartDate();
        BigDecimal amountWorkPercent = BigDecimal.ZERO;

        Stretch consolidatedStretch = stretchesFunction
                .getConsolidatedStretch();
        if (consolidatedStretch != null) {
            startDate = consolidatedStretch.getDate().plusDays(1);
            amountWorkPercent = consolidatedStretch.getAmountWorkPercentage().add(BigDecimal.ONE.divide(BigDecimal.valueOf(100)));
        }
        return Stretch.create(startDate, task, amountWorkPercent);
    }

    @Override
    public LocalDate getTaskStartDate() {
        if (task == null) {
            return null;
        }
        return task.getStartAsLocalDate();
    }

    @Override
    public void removeStretch(Stretch stretch) {
        if (stretchesFunction != null) {
            stretchesFunction.removeStretch(stretch);
        }
    }

    @Override
    public AssignmentFunction getStretchesFunction() {
        return stretchesFunction;
    }

    @Override
    public void setStretchDate(Stretch stretch, Date date)
            throws IllegalArgumentException {
        if (date.compareTo(task.getStartDate()) < 0) {
            throw new IllegalArgumentException(
                    _("Stretch date must not be less than task start date: "
                            + sameFormatAsDefaultZK(task.getStartDate())));
        }

        if (date.compareTo(taskEndDate) > 0) {
            throw new IllegalArgumentException(
                    _("Stretch date must not be greater than the task's end date: "
                            + sameFormatAsDefaultZK(taskEndDate)));
        }

        stretch.setDate(new LocalDate(date));

        if ((date.compareTo(taskEndDate) > 0)
                || (stretch.getAmountWorkPercentage().compareTo(BigDecimal.ONE) == 0)) {
            taskEndDate = date;
            recalculateStretchesPercentages();
        } else {
            calculatePercentage(stretch);
        }
    }

    private String sameFormatAsDefaultZK(Date date) {
        DateFormat zkDefaultDateFormatter = DateFormat.getDateInstance(
                DateFormat.DEFAULT, Locales.getCurrent());
        DateFormat formatter = zkDefaultDateFormatter;
        return formatter.format(date);
    }

    private void recalculateStretchesPercentages() {
        List<Stretch> stretches = stretchesFunction.getStretches();
        if (!stretches.isEmpty()) {
            for (Stretch stretch : stretches) {
                calculatePercentage(stretch);
            }
        }
    }

    private void calculatePercentage(Stretch stretch) {
        long stretchDate = stretch.getDate().toDateTimeAtStartOfDay().toDate()
                .getTime();
        long startDate = task.getStartDate().getTime();
        long endDate = taskEndDate.getTime();

        // (stretchDate - startDate) / (endDate - startDate)
        BigDecimal lengthPercenage = (new BigDecimal(stretchDate - startDate)
                .setScale(2)).divide(new BigDecimal(endDate - startDate),
                RoundingMode.DOWN);
        stretch.setLengthPercentage(lengthPercenage);
    }

    @Override
    public void setStretchLengthPercentage(Stretch stretch,
            BigDecimal lengthPercentage) throws IllegalArgumentException {
        stretch.setLengthPercentage(lengthPercentage);

        long startDate = task.getStartDate().getTime();
        long endDate = taskEndDate.getTime();

        // startDate + (percentage * (endDate - startDate))
        long stretchDate = startDate + lengthPercentage.multiply(
                new BigDecimal(endDate - startDate)).longValue();
        stretch.setDate(new LocalDate(stretchDate));
    }

    @Override
    public Integer getAllocationHours() {
        if (task == null) {
            return null;
        }
        return resourceAllocation.getAssignedHours();
    }

    @Override
    public BaseCalendar getTaskCalendar() {
        if (task == null) {
            return null;
        }
        return task.getCalendar();
    }

}
