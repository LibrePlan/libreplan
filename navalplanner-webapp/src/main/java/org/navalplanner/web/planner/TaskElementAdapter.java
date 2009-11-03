/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;

/**
 * Responsible of adaptating a {@link TaskElement} into a
 * {@link ITaskFundamentalProperties} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TaskElementAdapter implements ITaskElementAdapter {

    private Order order;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private ITaskElementDAO taskDAO;

    private List<IOnMoveListener> listeners = new ArrayList<IOnMoveListener>();

    @Override
    public void setOrder(Order order) {
        this.order = order;
    }

    public TaskElementAdapter() {
    }

    private class TaskElementWrapper implements ITaskFundamentalProperties {

        private final TaskElement taskElement;

        protected TaskElementWrapper(TaskElement taskElement) {
            this.taskElement = taskElement;
        }

        @Override
        public void setName(String name) {
            taskElement.setName(name);
        }

        @Override
        public void setNotes(String notes) {
            taskElement.setNotes(notes);
        }

        @Override
        public String getName() {
            return taskElement.getName();
        }

        @Override
        public String getNotes() {
            return taskElement.getNotes();
        }

        @Override
        public Date getBeginDate() {
            return taskElement.getStartDate();
        }

        @Override
        public long getLengthMilliseconds() {
            return taskElement.getEndDate().getTime()
                    - taskElement.getStartDate().getTime();
        }

        @Override
        public long setBeginDate(final Date beginDate) {
            Long runOnReadOnlyTransaction = transactionService
                    .runOnReadOnlyTransaction(new IOnTransaction<Long>() {
                        @Override
                        public Long execute() {
                            taskDAO.reattach(taskElement);
                            Long result = setBeginDateInsideTransaction(beginDate);
                            fireTaskElementMoved(taskElement);
                            return result;
                        }
                    });
            return runOnReadOnlyTransaction;
        }

        private Long setBeginDateInsideTransaction(final Date beginDate) {
            taskElement.moveTo(beginDate);
            return getLengthMilliseconds();
        }

        @Override
        public void setLengthMilliseconds(long lengthMilliseconds) {
            updateEndDate(lengthMilliseconds);
        }

        private void updateEndDate(long lengthMilliseconds) {
            taskElement.setEndDate(new Date(getBeginDate().getTime()
                    + lengthMilliseconds));
        }

        @Override
        public Date getHoursAdvanceEndDate() {
            OrderElement orderElement = taskElement.getOrderElement();
            Integer assignedHours = orderElementDAO
                    .getAssignedHours(orderElement);

            LocalDate date = calculateLimitDate(assignedHours);
            if (date == null) {
                Integer hours = 0;
                if (orderElement != null) {
                    hours = orderElement.getWorkHours();
                }

                if (hours == 0) {
                    return getBeginDate();
                } else {
                    BigDecimal percentage = new BigDecimal(assignedHours)
                            .setScale(2).divide(new BigDecimal(hours),
                                    RoundingMode.DOWN);
                    date = calculateLimitDate(percentage);
                }
            }

            return date.toDateTimeAtStartOfDay().toDate();
        }

        @Override
        public BigDecimal getHoursAdvancePercentage() {
            OrderElement orderElement = taskElement.getOrderElement();
            if (orderElement != null) {
                return orderElementDAO.getHoursAdvancePercentage(orderElement);
            } else {
                return new BigDecimal(0);
            }
        }

        @Override
        public Date getAdvanceEndDate() {
            OrderElement orderElement = taskElement.getOrderElement();

            BigDecimal advancePercentage;
            Integer hours;
            if (orderElement != null) {
                advancePercentage = orderElement
                        .getAdvancePercentage();
                hours = orderElement.getWorkHours();
            } else {
                advancePercentage = new BigDecimal(0);
                hours = new Integer(0);
            }

            Integer advanceHours = advancePercentage.multiply(
                    new BigDecimal(hours)).intValue();

            LocalDate date = calculateLimitDate(advanceHours);
            if (date == null) {
                date = calculateLimitDate(advancePercentage);
            }

            return date.toDateTimeAtStartOfDay().toDate();
        }

        private LocalDate calculateLimitDate(BigDecimal advancePercentage) {
            Long totalMillis = getLengthMilliseconds();
            Long advanceMillis = advancePercentage.multiply(
                    new BigDecimal(totalMillis)).longValue();
            return new LocalDate(getBeginDate().getTime() + advanceMillis);
        }

        @Override
        public BigDecimal getAdvancePercentage() {
            if (taskElement.getOrderElement() != null) {
                return taskElement.getOrderElement().getAdvancePercentage();
            }
            return new BigDecimal(0);
        }

        private LocalDate calculateLimitDate(Integer hours) {
            boolean limitReached = false;

            Integer count = 0;
            LocalDate lastDay = null;
            Integer hoursLastDay = 0;

            Map<LocalDate, Integer> daysMap = taskElement
                    .getHoursAssignedByDay();
            if (daysMap.isEmpty()) {
                return null;
            }

            for (LocalDate day : daysMap.keySet()) {
                lastDay = day;
                hoursLastDay = daysMap.get(day);

                count += hoursLastDay;

                if (count >= hours) {
                    limitReached = true;
                    break;
                }
            }

            if (!limitReached) {
                while (count < hours) {
                    count += hoursLastDay;
                    lastDay = lastDay.plusDays(1);
                }
            }

            return lastDay.plusDays(1);
        }

        @Override
        @Transactional(readOnly = true)
        public String getTooltipText() {

            Set<Label> labels;

            String tooltip = "Advance: "
                    + getAdvancePercentage().multiply(new BigDecimal(100))
                            .toString() + "% , ";
            tooltip += "Hours invested: "
                    + getHoursAdvancePercentage().multiply(new BigDecimal(100))
                            .toString() + "% <br/>";

            if (taskElement.getOrderElement() != null) {
                labels = taskElement.getOrderElement().getLabels();

                if (labels.size() != 0) {
                    tooltip += "Labels: ";
                    for (Label label : labels) {
                        tooltip += label.getName() + ", ";
                    }
                    tooltip = (tooltip.substring(0, tooltip.length() - 2))
                            + ".";
                }
            }

            return tooltip;
        }
    }

    @Override
    public ITaskFundamentalProperties adapt(final TaskElement taskElement) {
        if (taskElement.getName() == null) {
            taskElement.setName(taskElement.getOrderElement().getName());
        }
        if (taskElement.getStartDate() == null) {
            if (order != null) {
                taskElement.setStartDate(order.getInitDate());
            } else {
                taskElement.setStartDate(taskElement.getOrderElement()
                        .getInitDate());
            }
        }
        if (taskElement.getEndDate() == null) {
            Integer workHours = taskElement.getWorkHours();
            long endDateTime = taskElement.getStartDate().getTime()
                    + (workHours * 3600l * 1000);
            taskElement.setEndDate(new Date(endDateTime));
        }
        return new TaskElementWrapper(taskElement);
    }

    @Override
    public List<DomainDependency<TaskElement>> getIncomingDependencies(
            TaskElement taskElement) {
        return toDomainDependencies(taskElement
                .getDependenciesWithThisDestination());
    }

    @Override
    public List<DomainDependency<TaskElement>> getOutcomingDependencies(
            TaskElement taskElement) {
        return toDomainDependencies(taskElement
                .getDependenciesWithThisOrigin());
    }

    private List<DomainDependency<TaskElement>> toDomainDependencies(
            Collection<? extends Dependency> dependencies) {
        List<DomainDependency<TaskElement>> result = new ArrayList<DomainDependency<TaskElement>>();
        for (Dependency dependency : dependencies) {
            result.add(DomainDependency.createDependency(
                    dependency.getOrigin(),
                    dependency.getDestination(), toGanntType(dependency
                            .getType())));
        }
        return result;
    }

    private DependencyType toGanntType(Type type) {
        switch (type) {
        case END_START:
            return DependencyType.END_START;
        case START_START:
            return DependencyType.START_START;
        case END_END:
            return DependencyType.END_END;
        case START_END:
        default:
            throw new RuntimeException(_("{0} not supported yet", type));
        }
    }

    private Type toDomainType(DependencyType type) {
        switch (type) {
        case END_START:
            return Type.END_START;
        case START_START:
            return Type.START_START;
        case END_END:
            return Type.END_END;
        default:
            throw new RuntimeException(_("{0} not supported yet", type));
        }
    }

    @Override
    public void addDependency(DomainDependency<TaskElement> dependency) {
        TaskElement source = dependency.getSource();
        TaskElement destination = dependency.getDestination();
        Type domainType = toDomainType(dependency.getType());
        Dependency.create(source, destination, domainType);
    }

    @Override
    public boolean canAddDependency(DomainDependency<TaskElement> dependency) {
        return true;
    }

    @Override
    public void removeDependency(DomainDependency<TaskElement> dependency) {
        TaskElement source = dependency.getSource();
        Type type = toDomainType(dependency.getType());
        source.removeDependencyWithDestination(dependency.getDestination(),
                type);
    }

    private void fireTaskElementMoved(TaskElement taskElement) {
        for (IOnMoveListener moveListener : listeners) {
            moveListener.moved(taskElement);
        }
    }

    @Override
    public void addListener(IOnMoveListener moveListener) {
        Validate.notNull(moveListener);
        listeners.add(moveListener);
    }

    @Override
    public void removeListener(IOnMoveListener moveListener) {
        listeners.remove(moveListener);
    }

}
