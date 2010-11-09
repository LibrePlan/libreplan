/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
package org.zkoss.ganttz.data.criticalpath;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.biggerOrEqualThan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.IDependency;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues;

/**
 * Tests for {@link CriticalPathCalculator}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CriticalPathCalculatorTest {

    private ICriticalPathCalculable<ITaskFundamentalProperties> diagramGraphExample;

    private final LocalDate START = new LocalDate(2009, 12, 1);

    private ITaskFundamentalProperties createTask(LocalDate start,
            int durationDays) {
        ITaskFundamentalProperties result = createNiceMock(ITaskFundamentalProperties.class);
        expect(result.getBeginDate()).andReturn(toDate(start)).anyTimes();
        expect(result.getEndDate()).andReturn(
                toDate(start.plusDays(durationDays))).anyTimes();
        replay(result);
        return result;
    }

    private CriticalPathCalculator<ITaskFundamentalProperties, IDependency<ITaskFundamentalProperties>> buildCalculator() {
        return CriticalPathCalculator.create();
    }

    private ITaskFundamentalProperties createTaskWithBiggerOrEqualThanConstraint(
            LocalDate start, int durationDays, LocalDate date) {
        ITaskFundamentalProperties result = createNiceMock(ITaskFundamentalProperties.class);
        expect(result.getBeginDate()).andReturn(toDate(start)).anyTimes();
        expect(result.getEndDate()).andReturn(
                toDate(start.plusDays(durationDays))).anyTimes();
        GanttDate ganttDate = GanttDate.createFrom(date);
        Constraint<GanttDate> constraint = biggerOrEqualThan(ganttDate);
        expect(result.getStartConstraints()).andReturn(
                Arrays.asList(constraint)).anyTimes();

        replay(result);
        return result;
    }

    private ITaskFundamentalProperties createTaskWithEqualConstraint(
            LocalDate start, int durationDays, LocalDate date) {
        ITaskFundamentalProperties result = createNiceMock(ITaskFundamentalProperties.class);
        expect(result.getBeginDate()).andReturn(toDate(start)).anyTimes();
        expect(result.getEndDate()).andReturn(
                toDate(start.plusDays(durationDays))).anyTimes();

        GanttDate ganttDate = GanttDate.createFrom(date);
        Constraint<GanttDate> constraint = ConstraintOnComparableValues
                .equalTo(ganttDate);
        expect(result.getStartConstraints()).andReturn(
                Arrays.asList(constraint)).anyTimes();

        replay(result);
        return result;
    }

    private IDependency<ITaskFundamentalProperties> createDependency(
            ITaskFundamentalProperties source,
            ITaskFundamentalProperties destination,
            DependencyType dependencyType) {
        IDependency<ITaskFundamentalProperties> dependency = createNiceMock(IDependency.class);
        expect(dependency.getSource()).andReturn(source).anyTimes();
        expect(dependency.getDestination()).andReturn(destination).anyTimes();
        expect(dependency.getType()).andReturn(dependencyType).anyTimes();
        replay(dependency);
        return dependency;
    }

    private GanttDate toDate(LocalDate localDate) {
        return GanttDate.createFrom(localDate);
    }

    private int daysBetweenStartAndEnd(ITaskFundamentalProperties task) {
        LocalDate start = LocalDate.fromDateFields(task.getBeginDate()
                .toDayRoundedDate());
        LocalDate end = LocalDate.fromDateFields(task.getEndDate()
                .toDayRoundedDate());
        return Days.daysBetween(start, end).getDays();
    }

    /**
     * <pre>
     * #### T1 ####
     * </pre>
     */
    private void givenOneTask(int daysTask1) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        List<ITaskFundamentalProperties> listOfTasks = Arrays
                .asList(createTask(START, daysTask1));

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getLatestTasks())
                .andReturn(listOfTasks).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(
                diagramGraphExample
                        .getIncomingTasksFor(isA(ITaskFundamentalProperties.class)))
                .andReturn(new HashSet<ITaskFundamentalProperties>())
                .anyTimes();
        expect(
                diagramGraphExample
                        .getOutgoingTasksFor(isA(ITaskFundamentalProperties.class)))
                .andReturn(new HashSet<ITaskFundamentalProperties>())
                .anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    private void addTaskMethods(List<ITaskFundamentalProperties> listOfTasks) {
        for (ITaskFundamentalProperties task : listOfTasks) {
            expect(diagramGraphExample.getStartDate(task)).andReturn(
                    task.getBeginDate()).anyTimes();
            expect(diagramGraphExample.getStartConstraintsFor(task)).andReturn(
                    task.getStartConstraints()).anyTimes();
            expect(diagramGraphExample.getEndDateFor(task)).andReturn(
                    task.getEndDate()).anyTimes();
        }
    }

    /**
     * <pre>
     * #### T1 ####
     *
     * #### T2 ####
     * </pre>
     */
    private void givenTwoTasksNotConnected(int daysTask1, int daysTask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(
                createTask(START, daysTask1), createTask(START, daysTask2));

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getLatestTasks())
                .andReturn(listOfTasks).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(
                diagramGraphExample
                        .getIncomingTasksFor(isA(ITaskFundamentalProperties.class)))
                .andReturn(new HashSet<ITaskFundamentalProperties>())
                .anyTimes();
        expect(
                diagramGraphExample
                        .getOutgoingTasksFor(isA(ITaskFundamentalProperties.class)))
                .andReturn(new HashSet<ITaskFundamentalProperties>())
                .anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |---- #### S1 ####
     * </pre>
     */
    private void givenPairOfTasks(int daysTask1, int daysSubtask1) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                subtask1);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask1)).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |---- #### S1 ####
     *
     * #### T2 ####
     *       |---- #### S2 ####
     * </pre>
     */
    private void givenTwoPairOfTasksNotConnected(int daysTask1,
            int daysSubtask1, int daysTask2, int daysSubtask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties task2 = createTask(START, daysTask2);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties subtask2 = createTask(START, daysSubtask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                subtask1, task2, subtask2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1, task2)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask1, subtask2)).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task2)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |---- #### T2 ####
     *
     * #### IT ####
     * </pre>
     */
    private void givenTwoTaskConnectedAndOneIndependentTask(int daysTask1,
            int daysTask2, int daysIndependentTask) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties task2 = createTask(START, daysTask2);
        ITaskFundamentalProperties independentTask = createTask(START,
                daysIndependentTask);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                task2, independentTask);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1, independentTask)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(task2, independentTask)).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(independentTask))
                .andReturn(new HashSet<ITaskFundamentalProperties>())
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task2)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(independentTask))
                .andReturn(new HashSet<ITaskFundamentalProperties>())
                .anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |---- #### S1 ####
     *       |---- #### S2 ####
     * </pre>
     */
    private void givenOneTaskWithTwoDependantTasks(int daysTask1,
            int daysSubtask1, int daysSubtask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties subtask2 = createTask(START, daysSubtask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task,
                subtask1, subtask2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask1, subtask2)).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(subtask1,
                        subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |
     *       |---- #### S1 ####
     *       |
     * #### T2 ####
     * </pre>
     */
    private void givenTwoTaskWithOneCommonDependantTask(int daysTask1,
            int daysTask2, int daysSubtask1) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties task2 = createTask(START, daysTask2);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                task2, subtask1);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1, task2)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask1)).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1,
                        task2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     *       |---- #### S1 #### ----|
     *       |                      |
     * #### T1 ####                 |---- #### F1 ####
     *       |                      |
     *       |---- #### S2 #### ----|
     * </pre>
     */
    private void givenOneTaskWithTwoDependantTasksAndOneCommonDependantTask(
            int daysTask1, int daysSubtask1, int daysSubtask2,
            int daysFinalTask1) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties subtask2 = createTask(START, daysSubtask2);
        ITaskFundamentalProperties finalTask = createTask(START, daysFinalTask1);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task,
                subtask1, subtask2, finalTask);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(finalTask)).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(finalTask)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(subtask1,
                        subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(subtask1,
                        subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(finalTask))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(finalTask))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(finalTask)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 #### ----|                      |---- #### F1 ####
     *                  |                      |
     *                  |---- #### S1 #### ----|
     *                  |                      |
     * #### T2 #### ----|                      |---- #### F2 ####
     * </pre>
     */
    private void givenTwoTaskWithOneCommonDependantTaskWithTwoDependantTasks(
            int daysTask1, int daysTask2, int daysSubtask1, int daysFinalTask1,
            int daysFinalTask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties task2 = createTask(START, daysTask2);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties finalTask1 = createTask(START,
                daysFinalTask1);
        ITaskFundamentalProperties finalTask2 = createTask(START,
                daysFinalTask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                task2, subtask1, finalTask1, finalTask2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1, task2)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(finalTask1, finalTask2)).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1,
                        task2))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(finalTask1))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(finalTask2))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(
                        finalTask1, finalTask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(finalTask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(finalTask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### 4 #### ---------------|
     *      |                     |---- #### 2 ####
     *      |---- #### 5 #### ----|          |
     *      |                                |---- #### 10 ####
     *      |---- #### 8 #### ----|          |
     *                            |          |
     *                            |---- #### 3 ####
     *                            |
     * #### 6 #### ---------------|
     *      |
     *      |---- #### T ####
     * </pre>
     */
    private void givenExample(int daysTask) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task4 = createTask(START, 4);
        ITaskFundamentalProperties task5 = createTask(START, 5);
        ITaskFundamentalProperties task8 = createTask(START, 8);
        ITaskFundamentalProperties task2 = createTask(START, 2);
        ITaskFundamentalProperties task3 = createTask(START, 3);
        ITaskFundamentalProperties task10 = createTask(START, 10);
        ITaskFundamentalProperties task6 = createTask(START, 6);
        ITaskFundamentalProperties modifiableTask = createTask(START, daysTask);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task4,
                task5, task8, task2, task3, task10, task6, modifiableTask);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task4, task6)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(task10, modifiableTask)).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task4)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task6)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task5)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task4)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task8)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task4)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(modifiableTask))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(task6))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task4,
                        task5))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task3)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task8,
                        task6))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task10)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task2,
                        task3))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task4)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task2,
                        task5, task8))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task6)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(
                        modifiableTask, task3))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task5)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task2)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task8)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task3)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(modifiableTask))
                .andReturn(new HashSet<ITaskFundamentalProperties>())
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task10)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task3)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task10)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task10)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * |- #### T1 ####
     * |
     * |- #### T2 ####
     * </pre>
     */
    private void givenPairOfTasksStartStart(int daysTask1, int daysTask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties task2 = createTask(START, daysTask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                task2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(task1, task2)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependency = createDependency(
                task1, task2, DependencyType.START_START);
        expect(diagramGraphExample.getDependencyFrom(task1, task2)).andReturn(
                dependency).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task2)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * |- #### T1 ####
     * |        |---- #### S1 ####
     * |
     * |- #### T2 ####
     * </pre>
     */
    private void givenPairOfTasksStartStartFirstOfThemWithOneSubtask(
            int daysTask1, int daysSubtask1, int daysTask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties task2 = createTask(START, daysTask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                subtask1, task2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask1, task2)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependency = createDependency(
                task1, task2, DependencyType.START_START);
        expect(diagramGraphExample.getDependencyFrom(task1, task2)).andReturn(
                dependency).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(subtask1,
                        task2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |----------|- #### S1 ####
     *                  |
     * #### T2 ####     |
     *       |----------|- #### S2 ####
     * </pre>
     */
    private void givenTwoTasksWithSubtasksRelatedWithStartStart(int daysTask1,
            int daysSubtask1, int daysTask2, int daysSubtask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties task2 = createTask(START, daysTask2);
        ITaskFundamentalProperties subtask2 = createTask(START, daysSubtask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                subtask1, task2, subtask2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1, task2)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask1, subtask2)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependency = createDependency(
                subtask1, subtask2, DependencyType.START_START);
        expect(diagramGraphExample.getDependencyFrom(subtask1, subtask2))
                .andReturn(dependency).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task2,
                        subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |
     *       |---- #### S1 ####
     *       |
     *       |----|- #### S2 ####
     *            |
     *            |- #### S3 ####
     * </pre>
     */
    private void givenExampleStartStart(int daysTask1, int daysSubtask1,
            int daysSubtask2, int daysSubtask3) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties subtask2 = createTask(START, daysSubtask2);
        ITaskFundamentalProperties subtask3 = createTask(START, daysSubtask3);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                subtask1, subtask2, subtask3);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask1, subtask2, subtask3)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependency = createDependency(
                subtask2, subtask3, DependencyType.START_START);
        expect(diagramGraphExample.getDependencyFrom(subtask2, subtask3))
                .andReturn(dependency).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask3)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask2)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(subtask1,
                        subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask2))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask3))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask3)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 #### -|
     *               |
     * #### T2 #### -|
     * </pre>
     */
    private void givenPairOfTasksEndEnd(int daysTask1, int daysTask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties task2 = createTask(START, daysTask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                task2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1, task2)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(task2)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependency = createDependency(
                task1, task2, DependencyType.END_END);
        expect(diagramGraphExample.getDependencyFrom(task1, task2)).andReturn(
                dependency).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task2)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     *       |---- #### S1 ####
     *       |
     * #### T1 #### -|
     *               |
     * #### T2 #### -|
     * </pre>
     */
    private void givenPairOfTasksEndEndFirstOfThemWithOneSubtask(int daysTask1,
            int daysSubtask1, int daysTask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties task2 = createTask(START, daysTask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                subtask1, task2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1, task2)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(task2, subtask1)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependency = createDependency(
                task1, task2, DependencyType.END_END);
        expect(diagramGraphExample.getDependencyFrom(task1, task2)).andReturn(
                dependency).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(subtask1,
                        task2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |---- #### S1 #### -|
     *                           |
     * #### T2 ####              |
     *       |---- #### S2 #### -|
     * </pre>
     */
    private void givenTwoTasksWithSubtasksRelatedWithEndEnd(int daysTask1,
            int daysSubtask1, int daysTask2, int daysSubtask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties task2 = createTask(START, daysTask2);
        ITaskFundamentalProperties subtask2 = createTask(START, daysSubtask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                subtask1, task2, subtask2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1, task2)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask2)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependency = createDependency(
                subtask1, subtask2, DependencyType.END_END);
        expect(diagramGraphExample.getDependencyFrom(subtask1, subtask2))
                .andReturn(dependency).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task2,
                        subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task2))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |
     *       |---- #### S1 ####
     *       |
     *       |---- #### S2 #### -|
     *                           |
     *             #### S3 #### -|
     * </pre>
     */
    private void givenExampleEndEnd(int daysTask1, int daysSubtask1,
            int daysSubtask2, int daysSubtask3) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties subtask2 = createTask(START, daysSubtask2);
        ITaskFundamentalProperties subtask3 = createTask(START, daysSubtask3);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task1,
                subtask1, subtask2, subtask3);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1, subtask3)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask1, subtask3)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependency = createDependency(
                subtask2, subtask3, DependencyType.END_END);
        expect(diagramGraphExample.getDependencyFrom(subtask2, subtask3))
                .andReturn(dependency).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task1)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask3))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(subtask1,
                        subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask2))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask3))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask3)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |---- #### S1 ####
     *       |---- #### S2 ####
     * </pre>
     */
    private void givenOneTaskWithTwoDependantTasksLastOneWithEqualConstraint(
            int daysTask1, int daysSubtask1, int daysSubtask2,
            LocalDate dateConstraintSubtask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties subtask2 = createTaskWithEqualConstraint(
                START, daysSubtask2, dateConstraintSubtask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task,
                subtask1, subtask2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask1, subtask2)).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(subtask1,
                        subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 ####
     *       |---- #### S1 ####
     *       |---- #### S2 ####
     * </pre>
     */
    private void givenOneTaskWithTwoDependantTasksLastOneWithBiggerOrEqualThanConstraint(
            int daysTask1, int daysSubtask1, int daysSubtask2,
            LocalDate dateConstraintSubtask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task = createTask(START, daysTask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties subtask2 = createTaskWithBiggerOrEqualThanConstraint(
                START, daysSubtask2, dateConstraintSubtask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(task,
                subtask1, subtask2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(subtask1, subtask2)).anyTimes();
        expect(
                diagramGraphExample.getDependencyFrom(
                        isA(ITaskFundamentalProperties.class),
                        isA(ITaskFundamentalProperties.class))).andReturn(null)
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(task)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task)))
                .anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(task)))
                .anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays.asList(subtask1,
                        subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * _- #### TC #### -_
     * |                |
     * |- #### S1 #### -|
     * </pre>
     */
    private void givenTaskContainerWithOneSubtask(int daysSubtask1) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties taskContainer = createTask(START,
                daysSubtask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(
                taskContainer, subtask1);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(taskContainer)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(taskContainer)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependencyStartStart = createDependency(
                taskContainer, subtask1, DependencyType.START_START);
        expect(diagramGraphExample.getDependencyFrom(taskContainer, subtask1))
                .andReturn(dependencyStartStart).anyTimes();
        IDependency<ITaskFundamentalProperties> dependencyEndEnd = createDependency(
                subtask1, taskContainer, DependencyType.END_END);
        expect(diagramGraphExample.getDependencyFrom(subtask1, taskContainer))
                .andReturn(dependencyEndEnd).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(taskContainer))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(taskContainer))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();

        expect(diagramGraphExample.isContainer(taskContainer)).andReturn(true)
                .anyTimes();
        expect(diagramGraphExample.contains(taskContainer, subtask1))
                .andReturn(true).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * _- #### TC #### -_
     * |                |
     * |- #### S1 #### -|
     * |                |
     * |- #### S2 #### -|
     * </pre>
     */
    private void givenTaskContainerWithTwoSubtasks(int daysSubtask1,
            int daysSubtask2) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties taskContainer = createTask(START, Math.max(
                daysSubtask1, daysSubtask2));
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties subtask2 = createTask(START, daysSubtask2);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(
                taskContainer, subtask1, subtask2);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(taskContainer)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(taskContainer)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependencyStartStart = createDependency(
                taskContainer, subtask1, DependencyType.START_START);
        expect(diagramGraphExample.getDependencyFrom(taskContainer, subtask1))
                .andReturn(dependencyStartStart).anyTimes();
        IDependency<ITaskFundamentalProperties> dependencyEndEnd = createDependency(
                subtask1, taskContainer, DependencyType.END_END);
        expect(diagramGraphExample.getDependencyFrom(subtask1, taskContainer))
                .andReturn(dependencyEndEnd).anyTimes();
        IDependency<ITaskFundamentalProperties> dependencyStartStart2 = createDependency(
                taskContainer, subtask2, DependencyType.START_START);
        expect(diagramGraphExample.getDependencyFrom(taskContainer, subtask2))
                .andReturn(dependencyStartStart2).anyTimes();
        IDependency<ITaskFundamentalProperties> dependencyEndEnd2 = createDependency(
                subtask2, taskContainer, DependencyType.END_END);
        expect(diagramGraphExample.getDependencyFrom(subtask2, taskContainer))
                .andReturn(dependencyEndEnd2).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(taskContainer))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays.asList(
                                subtask1, subtask2))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(taskContainer))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays.asList(
                                subtask1, subtask2))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask2)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();

        expect(diagramGraphExample.isContainer(taskContainer)).andReturn(true)
                .anyTimes();
        expect(diagramGraphExample.contains(taskContainer, subtask1))
                .andReturn(true).anyTimes();
        expect(diagramGraphExample.contains(taskContainer, subtask2))
                .andReturn(true).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    /**
     * <pre>
     * #### T1 #### ----_- #### TC #### -_
     *                  |                |
     *                  |- #### S1 #### -|
     * </pre>
     */
    private void givenTaskContainerWithOneSubtaskDependingOnOneTask(
            int daysTask1, int daysSubtask1) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties task1 = createTask(START, daysTask1);
        ITaskFundamentalProperties taskContainer = createTask(START,
                daysSubtask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(
                task1,
                taskContainer, subtask1);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(task1)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(taskContainer)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependencyStartStart = createDependency(
                taskContainer, subtask1, DependencyType.START_START);
        expect(diagramGraphExample.getDependencyFrom(taskContainer, subtask1))
                .andReturn(dependencyStartStart).anyTimes();
        IDependency<ITaskFundamentalProperties> dependencyEndEnd = createDependency(
                subtask1, taskContainer, DependencyType.END_END);
        expect(diagramGraphExample.getDependencyFrom(subtask1, taskContainer))
                .andReturn(dependencyEndEnd).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(taskContainer))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays.asList(
                                task1, subtask1))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(taskContainer))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();

        expect(diagramGraphExample.isContainer(taskContainer)).andReturn(true)
                .anyTimes();
        expect(diagramGraphExample.contains(taskContainer, subtask1))
                .andReturn(true).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    @Test
    public void trivialBaseCase() {
        givenOneTask(10);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(1));
        assertThat(daysBetweenStartAndEnd(criticalPath.get(0)),
                equalTo(10));
    }

    /**
     * <pre>
     * _- #### TC #### -_---- #### T1 ####
     * |                |
     * |- #### S1 #### -|
     * </pre>
     */
    private void givenOneTaskDependingOnTaskContainerWithOneSubtask(
            int daysSubtask1, int daysTask1) {
        diagramGraphExample = createNiceMock(ICriticalPathCalculable.class);

        ITaskFundamentalProperties taskContainer = createTask(START,
                daysSubtask1);
        ITaskFundamentalProperties subtask1 = createTask(START, daysSubtask1);
        ITaskFundamentalProperties task1 = createTask(START, daysTask1);

        List<ITaskFundamentalProperties> listOfTasks = Arrays.asList(
                taskContainer, subtask1, task1);

        expect(diagramGraphExample.getTasks()).andReturn(listOfTasks)
                .anyTimes();
        expect(diagramGraphExample.getInitialTasks()).andReturn(
                Arrays.asList(taskContainer)).anyTimes();
        expect(diagramGraphExample.getLatestTasks()).andReturn(
                Arrays.asList(task1)).anyTimes();

        IDependency<ITaskFundamentalProperties> dependencyStartStart = createDependency(
                taskContainer, subtask1, DependencyType.START_START);
        expect(diagramGraphExample.getDependencyFrom(taskContainer, subtask1))
                .andReturn(dependencyStartStart).anyTimes();
        IDependency<ITaskFundamentalProperties> dependencyEndEnd = createDependency(
                subtask1, taskContainer, DependencyType.END_END);
        expect(diagramGraphExample.getDependencyFrom(subtask1, taskContainer))
                .andReturn(dependencyEndEnd).anyTimes();

        expect(diagramGraphExample.getIncomingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(taskContainer))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays
                                .asList(subtask1))).anyTimes();
        expect(diagramGraphExample.getIncomingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(task1)).andReturn(
                new HashSet<ITaskFundamentalProperties>()).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(taskContainer))
                .andReturn(
                        new HashSet<ITaskFundamentalProperties>(Arrays.asList(
                                subtask1, task1))).anyTimes();
        expect(diagramGraphExample.getOutgoingTasksFor(subtask1)).andReturn(
                new HashSet<ITaskFundamentalProperties>(Arrays
                        .asList(taskContainer))).anyTimes();

        expect(diagramGraphExample.isContainer(taskContainer)).andReturn(true)
                .anyTimes();
        expect(diagramGraphExample.contains(taskContainer, subtask1))
                .andReturn(true).anyTimes();

        addTaskMethods(listOfTasks);

        replay(diagramGraphExample);
    }

    @Test
    public void trivialBaseCaseWithTwoTasksNotConnected() {
        givenTwoTasksNotConnected(5, 10);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(1));
        assertThat(daysBetweenStartAndEnd(criticalPath.get(0)), equalTo(10));
    }

    @Test
    public void pairOfTasks() {
        givenPairOfTasks(10, 5);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(10), equalTo(5)));
        }
    }

    @Test
    public void twoPairOfTasksNotConnected() {
        givenTwoPairOfTasksNotConnected(10, 5, 6, 4);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(10), equalTo(5)));
        }
    }

    @Test
    public void twoPairOfTasksNotConnected2() {
        givenTwoPairOfTasksNotConnected(8, 1, 6, 4);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(6), equalTo(4)));
        }
    }

    @Test
    public void twoTaskConnectedAndOneIndependentTask() {
        givenTwoTaskConnectedAndOneIndependentTask(5, 10, 12);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(5), equalTo(10)));
        }
    }

    @Test
    public void twoTaskConnectedAndOneIndependentTask2() {
        givenTwoTaskConnectedAndOneIndependentTask(5, 10, 20);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(1));
        assertThat(daysBetweenStartAndEnd(criticalPath.get(0)), equalTo(20));
    }

    @Test
    public void twoTaskConnectedAndOneIndependentTaskWithTheSameDuration() {
        givenTwoTaskConnectedAndOneIndependentTask(10, 10, 20);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(3));
    }

    @Test
    public void oneTaskWithTwoDependantTasks() {
        givenOneTaskWithTwoDependantTasks(4, 5, 10);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(4), equalTo(10)));
        }
    }

    @Test
    public void oneTaskWithTwoDependantTasks2() {
        givenOneTaskWithTwoDependantTasks(4, 5, 1);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(4), equalTo(5)));
        }
    }

    @Test
    public void twoTaskWithOneCommonDependantTask() {
        givenTwoTaskWithOneCommonDependantTask(4, 2, 5);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task), anyOf(equalTo(4), equalTo(5)));
        }
    }

    @Test
    public void twoTaskWithOneCommonDependantTask2() {
        givenTwoTaskWithOneCommonDependantTask(4, 10, 5);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task), anyOf(equalTo(5), equalTo(10)));
        }
    }

    @Test
    public void oneTaskWithTwoDependantTasksAndOneCommonDependantTask() {
        givenOneTaskWithTwoDependantTasksAndOneCommonDependantTask(4, 2, 5, 10);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(3));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task), anyOf(equalTo(4), equalTo(5), equalTo(10)));
        }
    }

    @Test
    public void twoTaskWithOneCommonDependantTaskWithTwoDependantTasks() {
        givenTwoTaskWithOneCommonDependantTaskWithTwoDependantTasks(2, 6, 4, 8,
                10);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(3));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task), anyOf(equalTo(6), equalTo(4), equalTo(10)));
        }
    }

    @Test
    public void twoTaskWithOneCommonDependantTaskWithTwoDependantTasks2() {
        givenTwoTaskWithOneCommonDependantTaskWithTwoDependantTasks(4, 2, 10,
                8, 6);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(3));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task), anyOf(equalTo(4), equalTo(10), equalTo(8)));
        }
    }

    @Test
    public void example() {
        givenExample(20);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task), anyOf(equalTo(6), equalTo(20)));
        }
    }

    @Test
    public void example2() {
        givenExample(10);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(4));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task), anyOf(equalTo(4), equalTo(8), equalTo(3), equalTo(10)));
        }
    }

    @Test
    public void example3() {
        givenExample(19);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(6));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(
                    daysBetweenStartAndEnd(task),
                    anyOf(equalTo(4), equalTo(8), equalTo(3), equalTo(10),
                            equalTo(6), equalTo(19)));
        }
    }

    @Test
    public void pairOfTasksStartStart() {
        givenPairOfTasksStartStart(5, 10);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(5), equalTo(10)));
        }
    }

    @Test
    public void pairOfTasksStartStart2() {
        givenPairOfTasksStartStart(8, 4);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(1));
        assertThat(daysBetweenStartAndEnd(criticalPath.get(0)), equalTo(8));
    }

    @Test
    public void pairOfTasksStartStart3() {
        givenPairOfTasksStartStart(5, 5);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task), equalTo(5));
        }
    }

    @Test
    public void pairOfTasksStartStartFirstOfThemWithOneSubtask() {
        givenPairOfTasksStartStartFirstOfThemWithOneSubtask(4, 6, 5);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task), anyOf(equalTo(4),
                    equalTo(6)));
        }
    }

    @Test
    public void pairOfTasksStartStartFirstOfThemWithOneSubtask2() {
        givenPairOfTasksStartStartFirstOfThemWithOneSubtask(4, 6, 15);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(4),
                    equalTo(15)));
        }
    }

    @Test
    public void twoTasksWithSubtasksRelatedWithStartStart() {
        givenTwoTasksWithSubtasksRelatedWithStartStart(4, 3, 5, 6);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(5), equalTo(6)));
        }
    }

    @Test
    public void twoTasksWithSubtasksRelatedWithStartStart2() {
        givenTwoTasksWithSubtasksRelatedWithStartStart(2, 10, 4, 5);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(2),
                    equalTo(10)));
        }
    }

    @Test
    public void twoTasksWithSubtasksRelatedWithStartStart3() {
        givenTwoTasksWithSubtasksRelatedWithStartStart(4, 7, 5, 6);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(4));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(4),
                    equalTo(7), equalTo(5), equalTo(6)));
        }
    }

    @Test
    public void exampleStartStart() {
        givenExampleStartStart(5, 3, 10, 2);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(5),
                    equalTo(10)));
        }
    }

    @Test
    public void exampleStartStart2() {
        givenExampleStartStart(5, 3, 4, 8);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(3));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(5),
                    equalTo(4), equalTo(8)));
        }
    }

    @Test
    public void exampleStartStart3() {
        givenExampleStartStart(5, 8, 4, 2);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(5),
                    equalTo(8)));
        }
    }

    @Test
    public void pairOfTasksEndEnd() {
        givenPairOfTasksEndEnd(10, 5);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(10),
                    equalTo(5)));
        }
    }

    @Test
    public void pairOfTasksEndEnd2() {
        givenPairOfTasksEndEnd(5, 10);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(1));
        assertThat(daysBetweenStartAndEnd(criticalPath.get(0)), equalTo(10));
    }

    @Test
    public void pairOfTasksEndEndFirstOfThemWithOneSubtask() {
        givenPairOfTasksEndEndFirstOfThemWithOneSubtask(4, 3, 2);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(4),
                    equalTo(3)));
        }
    }

    @Test
    public void pairOfTasksEndEndFirstOfThemWithOneSubtask2() {
        givenPairOfTasksEndEndFirstOfThemWithOneSubtask(2, 3, 6);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(1));
        assertThat(daysBetweenStartAndEnd(criticalPath.get(0)), equalTo(6));
    }

    @Test
    public void twoTasksWithSubtasksRelatedWithEndEnd() {
        givenTwoTasksWithSubtasksRelatedWithEndEnd(5, 3, 4, 2);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(3));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(5),
                    equalTo(3), equalTo(2)));
        }
    }

    @Test
    public void twoTasksWithSubtasksRelatedWithEndEnd2() {
        givenTwoTasksWithSubtasksRelatedWithEndEnd(5, 2, 4, 6);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(4),
                    equalTo(6)));
        }
    }

    @Test
    public void exampleEndEnd() {
        givenExampleEndEnd(5, 4, 2, 3);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(5),
                    equalTo(4)));
        }
    }

    @Test
    public void exampleEndEnd2() {
        givenExampleEndEnd(5, 2, 4, 3);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(3));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(5),
                    equalTo(4), equalTo(3)));
        }
    }

    @Test
    public void exampleEndEnd3() {
        givenExampleEndEnd(2, 4, 3, 10);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(1));
        assertThat(daysBetweenStartAndEnd(criticalPath.get(0)), equalTo(10));
    }

    @Test
    public void oneTaskWithTwoDependantTasksLastOneWithEqualConstraint() {
        givenOneTaskWithTwoDependantTasksLastOneWithEqualConstraint(2, 5, 3,
                START.plusDays(5));
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(1));
        assertThat(daysBetweenStartAndEnd(criticalPath.get(0)), equalTo(3));
    }

    @Test
    public void oneTaskWithTwoDependantTasksLastOneWithEqualConstraint2() {
        givenOneTaskWithTwoDependantTasksLastOneWithEqualConstraint(2, 5, 3,
                START.plusDays(3));
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(3));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(2),
                    equalTo(5), equalTo(3)));
        }
    }

    @Test
    public void oneTaskWithTwoDependantTasksLastOneWithBiggerOrEqualThanConstraint() {
        givenOneTaskWithTwoDependantTasksLastOneWithBiggerOrEqualThanConstraint(
                2, 5, 3, START.plusDays(5));
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(1));
        assertThat(daysBetweenStartAndEnd(criticalPath.get(0)), equalTo(3));
    }

    @Test
    public void oneTaskWithTwoDependantTasksLastOneWithBiggerOrEqualThanConstraint2() {
        givenOneTaskWithTwoDependantTasksLastOneWithBiggerOrEqualThanConstraint(
                2, 6, 4, START.plusDays(3));
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(2),
                    equalTo(6)));
        }
    }

    @Test
    public void taskContainerWithOneSubtask() {
        givenTaskContainerWithOneSubtask(10);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    equalTo(10));
        }
    }

    @Test
    public void taskContainerWithTwoSubtasks() {
        givenTaskContainerWithTwoSubtasks(10, 5);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(2));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    equalTo(10));
        }
    }

    @Test
    public void taskContainerWithOneSubtaskDependingOnOneTask() {
        givenTaskContainerWithOneSubtaskDependingOnOneTask(10, 5);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(3));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(10),
                    equalTo(5)));
        }
    }

    @Test
    public void oneTaskDependingOnTaskContainerWithOneSubtask() {
        givenOneTaskDependingOnTaskContainerWithOneSubtask(10, 5);
        List<ITaskFundamentalProperties> criticalPath = buildCalculator()
                .calculateCriticalPath(diagramGraphExample);

        assertThat(criticalPath.size(), equalTo(3));
        for (ITaskFundamentalProperties task : criticalPath) {
            assertThat(daysBetweenStartAndEnd(task),
                    anyOf(equalTo(10), equalTo(5)));
        }
    }

}
