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

package org.libreplan.web.planner.allocation.streches;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.planner.daos.IAssignmentFunctionDAO;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.AssignmentFunction;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.Stretch;
import org.libreplan.business.planner.entities.StretchesFunction;
import org.libreplan.business.planner.entities.StretchesFunctionTypeEnum;
import org.libreplan.business.planner.entities.Task;
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
            stretchesFunction.setResourceAllocation(resourceAllocation);
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
    public List<Stretch> getStretchesDefinedByUser() {
        if (stretchesFunction == null) {
            return Collections.emptyList();
        }
        return stretchesFunction.getStretches();
    }

    @Override
    public List<Stretch> getAllStretches() {
        if (stretchesFunction == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(allStretches());
    }

    /**
     * Returns an empty stretch plus the stretches from stretchesFunction and
     * the consolidated stretch if any
     *
     * @return
     */
    private List<Stretch> allStretches() {
        return stretchesFunction.getStretchesPlusConsolidated();
    }

    @Override
    public List<Stretch> getStretchesPlusConsolidated() {
        if (stretchesFunction == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(stretchesFunction
                .getStretchesPlusConsolidated());
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
            if (stretchesFunction.isInterpolated()) {
                if (!stretchesFunction.checkHasAtLeastTwoStretches()) {
                    throw new ValidationException(
                            _("There must be at least 2 stretches for doing interpolation"));
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

    public static boolean areValidForInterpolation(List<Stretch> stretches) {
        return atLeastThreeStreches(stretches)
                && areStretchesSortedAndWithIncrements(stretches);
    }

    private static boolean atLeastThreeStreches(List<Stretch> stretches) {
        return stretches.size() >= 3;
    }

    private static boolean areStretchesSortedAndWithIncrements(
            List<Stretch> stretches) {
        if (stretches.isEmpty()) {
            return false;
        }

        Iterator<Stretch> iterator = stretches.iterator();
        Stretch previous = iterator.next();
        while (iterator.hasNext()) {
            Stretch current = iterator.next();
            if (current.getLengthPercentage().compareTo(
                    previous.getLengthPercentage()) <= 0) {
                return false;
            }
            if (current.getAmountWorkPercentage().compareTo(
                    previous.getAmountWorkPercentage()) <= 0) {
                return false;
            }
            previous = current;
        }
        return true;
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
            startDate = consolidatedStretch.getDateIn(resourceAllocation)
                    .plusDays(1);
            amountWorkPercent = consolidatedStretch.getAmountWorkPercentage().add(BigDecimal.ONE.divide(BigDecimal.valueOf(100)));
        }
        return Stretch.create(startDate, resourceAllocation, amountWorkPercent);
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
    public Date getStretchDate(Stretch stretch) {
        return stretch.getDateIn(resourceAllocation).toDateTimeAtStartOfDay()
                .toDate();
    }

    @Override
    public void setStretchDate(Stretch stretch, Date date)
            throws IllegalArgumentException {
        if (date.compareTo(task.getStartDate()) < 0) {
            throw new IllegalArgumentException(
                    _("Stretch date must not be before task start date: "
                            + sameFormatAsDefaultZK(task.getStartDate())));
        }

        if (date.compareTo(taskEndDate) > 0) {
            throw new IllegalArgumentException(
                    _("Stretch date must not be after task end date: "
                            + sameFormatAsDefaultZK(taskEndDate)));
        }

        stretch.setDateIn(resourceAllocation, new LocalDate(date));
    }

    private String sameFormatAsDefaultZK(Date date) {
        DateFormat zkDefaultDateFormatter = DateFormat.getDateInstance(
                DateFormat.DEFAULT, Locales.getCurrent());
        DateFormat formatter = zkDefaultDateFormatter;
        return formatter.format(date);
    }

    @Override
    public void setStretchLengthPercentage(Stretch stretch,
            BigDecimal lengthPercentage) throws IllegalArgumentException {
        stretch.setLengthPercentage(lengthPercentage);
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

    @Override
    public ResourceAllocation<?> getResourceAllocation() {
        return resourceAllocation;
    }

}
