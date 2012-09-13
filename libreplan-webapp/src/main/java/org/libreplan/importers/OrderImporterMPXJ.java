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

package org.libreplan.importers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

import net.sf.mpxj.reader.ProjectReader;
import net.sf.mpxj.reader.ProjectReaderUtility;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.daos.IEntitySequenceDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.OrderLineGroup;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.planner.daos.IDependencyDAO;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.Dependency.Type;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.TaskMilestone;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.importers.DependencyDTO.TypeOfDependencyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * Has all the methods needed to successfully import some external project files
 * into Libreplan using MPXJ.
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderImporterMPXJ implements IOrderImporter {

    @Autowired
    private IEntitySequenceDAO entitySequenceDAO;

    @Autowired
    protected IAdHocTransactionService transactionService;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IDependencyDAO dependencyDAO;

    @Autowired
    private ITaskElementDAO taskDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    /**
     * Makes a {@link OrderDTO} from a InputStream.
     *
     * Uses the filename in order to get the specific ProjectReader for each
     * kind of file (.mpp, .planner, etc).
     *
     * @param file
     *            InputStream to extract data from.
     * @param filename
     *            String with the name of the original file of the InputStream.
     * @return ImportData with the data that we want to import.
     */
    @Override
    public OrderDTO getImportData(InputStream file, String filename) {
        try {

            ProjectReader reader = ProjectReaderUtility
                    .getProjectReader(filename);

            return MPXJProjectFileConversor
                    .convert(reader.read(file), filename);

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

    private String getCode(EntityNameEnum entity) {

        String code = entitySequenceDAO.getNextEntityCode(entity);

        if (code == null) {
            throw new ConcurrentModificationException(
                    "Could not retrieve Code. Please, try again later");
        }

        return code;
    }


    /**
     * Makes a {@link Order} from a {@link OrderDTO}.
     *
     * @param project
     *            ImportData to extract data from.
     * @return Order with all the data that we want.
     */
    @Override
    @Transactional(readOnly = true)
    public Order convertImportDataToOrder(OrderDTO project) {

        String code = getCode(EntityNameEnum.ORDER);

        Scenario current = scenarioManager.getCurrent();

        OrderVersion orderVersion = OrderVersion.createInitialVersion(current);

        Validate.notNull(orderVersion);

        OrderElement orderElement;

        orderElement = Order.createUnvalidated(code);

        orderElement.setCodeAutogenerated(true);

        ((Order) orderElement).setVersionForScenario(current, orderVersion);

        ((Order) orderElement).setDependenciesConstraintsHavePriority(true);

        BaseCalendar calendar = configurationDAO.getConfiguration()
                .getDefaultCalendar();

        ((Order) orderElement).setCalendar(calendar);

        orderElement.useSchedulingDataFor(orderVersion);

        List<OrderElement> children = new ArrayList<OrderElement>();

        for (OrderElementDTO task : project.tasks) {
            children.add(convertImportTaskToOrderElement(orderVersion, task));
        }

        for (OrderElement child : children) {
            ((OrderLineGroup) orderElement).add(child);
        }

        orderElement.setName(project.name + ": " + project.hashCode());
        orderElement.setCode(code);

        orderElement.setInitDate(project.startDate);

        orderElement.setDeadline(project.deadline);

        ((Order) orderElement).calculateAndSetTotalHours();

        project.order = (Order) orderElement;

        ((Order) orderElement).generateOrderElementCodes(entitySequenceDAO
                .getNumberOfDigitsCode(EntityNameEnum.ORDER));

        return (Order) orderElement;

    }

    /**
     * Private method.
     *
     * It makes a {@link OrderElement} from a {@link OrderElementDTO}
     *
     * @param task
     *            ImportTask to extract data from.
     * @param orderVersion
     *            Number of version.
     * @return OrderElement OrderElement that represent the data.
     */
    private OrderElement convertImportTaskToOrderElement(
            OrderVersion orderVersion, OrderElementDTO task) {

        Validate.notNull(orderVersion);
        OrderElement orderElement;

        if (task.children.size() == 0) {
            orderElement = OrderLine.createUnvalidatedWithUnfixedPercentage(
                    UUID.randomUUID().toString(), task.totalHours);

            if (!orderElement.getHoursGroups().isEmpty()) {
                orderElement.getHoursGroups().get(0)
                        .setCode(UUID.randomUUID().toString());
            }

        } else {

            orderElement = OrderLineGroup.createUnvalidated(UUID.randomUUID()
                    .toString());

            orderElement.useSchedulingDataFor(orderVersion);
        }

        List<OrderElement> children = new ArrayList<OrderElement>();

        for (OrderElementDTO childrenTask : task.children) {
            children.add(convertImportTaskToOrderElement(orderVersion,
                    childrenTask));
        }

        for (OrderElement child : children) {

            ((OrderLineGroup) orderElement).add(child);
        }

        orderElement.setName(task.name);

        orderElement.setDeadline(task.deadline);

        task.orderElement = orderElement;

        return orderElement;
    }

    /**
     * Creates a {@link TaskGroup} from a {@link ImportData}
     *
     * @param project
     *            ImportData to extract data from
     *
     * @return TaskGroup TaskGroup with the data extracted.
     */
    @Override
    @Transactional
    public TaskGroup createTask(OrderDTO project) {

        Order order = project.order;

        TaskSource taskSource = TaskSource.createForGroup(order
                .getCurrentSchedulingDataForVersion());

        TaskGroup taskGroup = taskSource
                .createTaskGroupWithoutDatesInitializedAndLinkItToTaskSource();

        BaseCalendar calendar = configurationDAO.getConfiguration()
                .getDefaultCalendar();

        taskGroup.setCalendar(calendar);

        List<TaskElement> taskElements = new ArrayList<TaskElement>();

        for (OrderElementDTO importTask : project.tasks) {

            taskElements.add(createTask(importTask));

        }

        for (MilestoneDTO milestone : project.milestones) {

            taskElements.add(createTaskMilestone(milestone));

        }

        for (TaskElement taskElement : taskElements) {
            taskGroup.addTaskElement(taskElement);
        }

        return taskGroup;

    }

    /**
     * Private method.
     *
     * It makes a {@link TaskMilestone} from a {@link MilsetoneDTO}
     *
     * @param milestone
     *            MilestoneDTO to extract data from.
     *
     * @return TaskElement TaskElement that represent the data.
     */
    @Transactional
    private TaskElement createTaskMilestone(MilestoneDTO milestone) {

        TaskElement taskMilestone = TaskMilestone.create(milestone.startDate);

        taskMilestone.setName(milestone.name);

        setPositionConstraint((TaskMilestone) taskMilestone, milestone);

        milestone.taskElement = taskMilestone;

        return taskMilestone;
    }

    /**
     * Private method.
     *
     * It makes a {@link TaskElement} from a {@link ImportTask}
     *
     * @param task
     *            ImportTask to extract data from.
     *
     * @return TaskElement TaskElement that represent the data.
     */
    private TaskElement createTask(OrderElementDTO task) {

        OrderElement orderElement = task.orderElement;

        TaskElement taskElement;

        TaskSource taskSource;

        if (task.children.size() == 0) {

            taskSource = TaskSource.create(
                    orderElement.getCurrentSchedulingDataForVersion(),
                    orderElement.getHoursGroups());

            taskElement = taskSource
                    .createTaskWithoutDatesInitializedAndLinkItToTaskSource();

            setPositionConstraint((Task) taskElement, task);

        } else {

            taskSource = TaskSource.createForGroup(orderElement
                    .getCurrentSchedulingDataForVersion());

            taskElement = taskSource
                    .createTaskGroupWithoutDatesInitializedAndLinkItToTaskSource();

            List<TaskElement> taskElements = new ArrayList<TaskElement>();

            for (OrderElementDTO importTask : task.children) {

                taskElements.add(createTask(importTask));

            }

            for (MilestoneDTO milestone : task.milestones) {

                taskElements.add(createTaskMilestone(milestone));

            }

            for (TaskElement childTaskElement : taskElements) {
                ((TaskGroup) taskElement).addTaskElement(childTaskElement);
            }

        }

        taskElement.setStartDate(task.startDate);
        taskElement.setEndDate(task.endDate);

        task.taskElement = taskElement;

        return taskElement;
    }

    /**
     * Private method.
     *
     * Sets the proper constraint to and a {@link Task}
     *
     * @param importTask
     *            OrderElementDTO to extract data from.
     * @param task
     *            Task to set data on.
     */
    private void setPositionConstraint(Task task, OrderElementDTO importTask) {

        switch (importTask.constraint) {

        case AS_SOON_AS_POSSIBLE:

            task.getPositionConstraint().asSoonAsPossible();

            return;

        case AS_LATE_AS_POSSIBLE:

            task.getPositionConstraint().asLateAsPossible();

            return;

        case START_IN_FIXED_DATE:

            task.setIntraDayStartDate(IntraDayDate.startOfDay(LocalDate
                    .fromDateFields(importTask.constraintDate)));

            Task.convertOnStartInFixedDate(task);

            return;

        case START_NOT_EARLIER_THAN:

            task.getPositionConstraint().notEarlierThan(
                    IntraDayDate.startOfDay(LocalDate
                            .fromDateFields(importTask.constraintDate)));

            return;

        case FINISH_NOT_LATER_THAN:

            task.getPositionConstraint().finishNotLaterThan(
                    IntraDayDate.startOfDay(LocalDate
                            .fromDateFields(importTask.constraintDate)));
            return;
        }

    }

    /**
     * Private method.
     *
     * Sets the proper constraint to and a {@link TaskMiletone}
     *
     * @param milestone
     *            MilestoneDTO to extract data from.
     * @param taskMilestone
     *            TaskMilestone to set data on.
     */
    private void setPositionConstraint(TaskMilestone taskMilestone, MilestoneDTO milestone) {

        switch (milestone.constraint) {

        case AS_SOON_AS_POSSIBLE:

            taskMilestone.getPositionConstraint().asSoonAsPossible();

            return;

        case AS_LATE_AS_POSSIBLE:

            taskMilestone.getPositionConstraint().asLateAsPossible();

            return;

        case START_IN_FIXED_DATE:

            taskMilestone.getPositionConstraint().notEarlierThan(
                    IntraDayDate.startOfDay(LocalDate
                            .fromDateFields(milestone.constraintDate)));

            return;

        case START_NOT_EARLIER_THAN:

            taskMilestone.getPositionConstraint().notEarlierThan(
                    IntraDayDate.startOfDay(LocalDate
                            .fromDateFields(milestone.constraintDate)));

            return;

        case FINISH_NOT_LATER_THAN:

            taskMilestone.getPositionConstraint().finishNotLaterThan(
                    IntraDayDate.startOfDay(LocalDate
                            .fromDateFields(milestone.constraintDate)));
            return;
        }

    }

    /**
     * Saves an {@link Order} which has all the data that we want to store in
     * the database. Also save all the related {@link TaskElement} and its
     * {@link TaskSource}
     *
     * @param Order
     *            Order with the data.
     * @param TaskGroup
     *            TaskGroup with the data. It also contains the link to the
     *            TaskSources.
     */
    @Override
    @Transactional
    public void storeOrder(final Order order, final TaskGroup taskGroup,
            final List<Dependency> dependencies) {

        final List<TaskSource> taskSources = new ArrayList<TaskSource>();

        taskSources.add(taskGroup.getTaskSource());

        for (TaskElement taskElement : taskGroup.getAllChildren()) {

            if (!taskElement.isMilestone()) {

                taskSources.add(taskElement.getTaskSource());

            }

        }

        orderDAO.save(order);

        taskDAO.save(taskGroup);

        for (TaskSource taskSource : taskSources) {

            taskSource.validate();
            taskSourceDAO.save(taskSource);

        }

        for(Dependency dependency: dependencies){

            dependencyDAO.save(dependency);

        }

    }

    /**
     * Creates a list of {@link Dependency} from a {@link ImportData}
     *
     * @param importData
     *            ImportData to extract data from
     *
     * @return List<Dependency> with the data extracted.
     */
    @Override
    public List<Dependency> createDependencies(OrderDTO importData) {

        List<Dependency> dependencies =  new ArrayList<Dependency>();

        for(DependencyDTO dependencyDTO: importData.dependencies){

            TaskElement origin = dependencyDTO.origin.getTaskAssociated();

            TaskElement destination = dependencyDTO.destination
                    .getTaskAssociated();

            dependencies.add(Dependency.create(origin, destination,
                    toLPType(dependencyDTO.type)));
        }

        return dependencies;
    }

    /**
     * Private method.
     *
     * Return the equivalent {@link Type} of a {@link TypeOfDependencyDTO}
     *
     * @param type
     *            TypeOfDependencyDTO to extract data from.
     * @return Type equivalent type.
     */
    private Type toLPType(TypeOfDependencyDTO type) {

        switch (type) {

        case END_START:

            return Type.END_START;

        case START_START:

            return Type.START_START;

        case END_END:

            return Type.END_END;

        case START_END:
            //TODO LP doesn't support START_END dependency at this moment. Fix this when it does.
            //Returns a END_START instead.
            return Type.END_START;

        default:
            return null;

        }
    }

}
