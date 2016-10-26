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

package org.libreplan.web.planner.chart;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.easymock.EasyMock;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;

import org.libreplan.business.planner.entities.IOrderEarnedValueCalculator;
import org.libreplan.business.planner.entities.SpecificDayAssignmentsContainer;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.workingday.ResourcesPerDay;
import org.libreplan.web.calendars.BaseCalendarModel;
import org.libreplan.web.calendars.IBaseCalendarModel;
import org.libreplan.web.common.IConfigurationModel;
import org.libreplan.web.orders.IOrderModel;
import org.libreplan.web.planner.order.PlanningStateCreator;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.libreplan.web.resources.worker.IAssignedCriterionsModel;
import org.libreplan.web.resources.worker.IWorkerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.zkforge.timeplot.Timeplot;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Desktop;

import javax.annotation.Resource;

/**
 * Tests for {@link ChartFiller}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE, WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE
})
@Transactional
public class ChartFillerTest {

    private ChartFiller chartFiller = new ChartFiller() {

        @Override
        public void fillChart(Timeplot chart, Interval interval, Integer size) {
        }

    };

    private static final LocalDate START_DAY = new LocalDate(2009, 12, 1);

    private static final LocalDate FIRST_DAY = new LocalDate(2009, 12, 5);

    private static final LocalDate LAST_DAY = new LocalDate(2009, 12, 15);

    private static final LocalDate FINISH_DAY = new LocalDate(2009, 12, 30);

    private SortedMap<LocalDate, BigDecimal> givenExampleMap() {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<>();

        result.put(FIRST_DAY, new BigDecimal(100));
        result.put(LAST_DAY, new BigDecimal(150));

        return result;
    }

    @Test
    public void testCalculatedValueForEveryDay() {
        SortedMap<LocalDate, BigDecimal> result =
                chartFiller.calculatedValueForEveryDay(givenExampleMap(), START_DAY, FINISH_DAY);

        assertThat(result.get(START_DAY), equalTo(BigDecimal.ZERO.setScale(2)));
        assertThat(result.get(START_DAY.plusDays(1)), equalTo(new BigDecimal(25).setScale(2)));

        assertThat(result.get(FIRST_DAY), equalTo(new BigDecimal(100).setScale(2)));
        assertThat(result.get(FIRST_DAY.plusDays(1)), equalTo(new BigDecimal(105).setScale(2)));

        assertThat(result.get(LAST_DAY), equalTo(new BigDecimal(150).setScale(2)));
        assertThat(result.get(LAST_DAY.plusDays(1)), equalTo(new BigDecimal(150).setScale(2)));

        assertThat(result.get(FINISH_DAY), equalTo(new BigDecimal(150).setScale(2)));
    }

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap scenariosBootstrap;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Before
    public void loadRequiredData() {

        IOnTransaction<Void> load = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                defaultAdvanceTypesBootstrapListener.loadRequiredData();
                configurationBootstrap.loadRequiredData();
                scenariosBootstrap.loadRequiredData();
                return null;
            }
        };

        transactionService.runOnAnotherTransaction(load);
    }

    // Testing BAC values
    // 1. Create Worker
    // 2. Create Project
    // 3. Create 2 Tasks
    // 4. Assign Resources to Tasks
    // 5. Check BAC values

    @Resource
    IBaseCalendarModel  baseCalendarModel;

    @Autowired
    IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    IConfigurationModel configurationModel;

    @Test
    public void testBAC() {
        createAndSaveWorker();
        createAndSaveProject();
        createAndSaveTwoTasksForProject();
        assignAndSaveResourcesForTasks();

        BigDecimal BAC = earnedValueCalculator.getBudgetAtCompletion(orderModel.getOrder());
        assertEquals(BAC, new BigDecimal(50));
    }

    /**  For creation of worker */

    @Autowired
    IWorkerModel workerModel;

    @Autowired
    IAssignedCriterionsModel assignedCriterionsModel;

    private void createAndSaveWorker() {
        workerModel.prepareForCreate();
        workerModel.getWorker().setFirstName("Neil");
        workerModel.getWorker().setSurname("Armstrong");
        workerModel.getWorker().setNif("666");
        workerModel.getAssignedCriterionsModel().prepareForCreate(workerModel.getWorker());
        workerModel.save();
    }

    /** For project creation */

    @Autowired
    IOrderModel orderModel;

    @Autowired
    private PlanningStateCreator planningStateCreator;

    @Autowired
    private IAdHocTransactionService adHocTransaction;

    @Autowired
    private IConfigurationDAO configurationDAO;

    private void createAndSaveProject() {
        final Order project = Order.create();
        project.setDescription("Goal of project: do not be thirsty");

        // Create initDate
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, 2016);

        project.setInitDate(calendar.getTime());
        project.setName("To be not thirsty");
        project.setResponsible("human");
        project.setCode("code-" + UUID.randomUUID());

        BaseCalendar baseCalendar = adHocTransaction.runOnReadOnlyTransaction(() -> {
            BaseCalendar result =
                    configurationDAO.getConfigurationWithReadOnlyTransaction().getDefaultCalendar();

            BaseCalendarModel.forceLoadBaseCalendar(result);
            return result;
        });

        project.setCalendar(baseCalendar);

        // Create planningState
        PlanningState planningState = adHocTransaction.runOnAnotherReadOnlyTransaction(
                () -> planningStateCreator.createOn(EasyMock.createNiceMock(Desktop.class), project));

        orderModel.setPlanningState(planningState);
        orderModel.save();
    }

    private void createAndSaveTwoTasksForProject(){
        OrderElement task1 = OrderLine.createOrderLineWithUnfixedPercentage(10);
        task1.setName("Take bottle");
        task1.setCode(UUID.randomUUID().toString());
        for (HoursGroup current : task1.getHoursGroups()) current.setCode(UUID.randomUUID().toString());

        OrderElement task2 = OrderLine.createOrderLineWithUnfixedPercentage(40);
        task2.setName("Drink water");
        task2.setCode(UUID.randomUUID().toString());
        for (HoursGroup current : task2.getHoursGroups()) current.setCode(UUID.randomUUID().toString());

        orderModel.getOrder().add(task1);
        orderModel.getOrder().add(task2);

        orderModel.save();
    }

    /** For assigning resources to tasks */
    @Autowired
    private IOrderEarnedValueCalculator earnedValueCalculator;

    private void assignAndSaveResourcesForTasks(){
        // Task 1
        Task task1 = (Task) orderModel.getOrder().getAllChildrenAssociatedTaskElements().get(0);
        SpecificResourceAllocation specificResourceAllocation1 = SpecificResourceAllocation.create(task1);
        specificResourceAllocation1.setResource(workerModel.getWorker());

        Scenario orderScenario1 = null;
        for (Scenario scenario : orderModel.getOrder().getScenarios().keySet()) {
            orderScenario1 = scenario;
        }
        specificResourceAllocation1.copyAssignments(orderScenario1, orderScenario1);

        specificResourceAllocation1.allocate(ResourcesPerDay.amount(1));

        for (SpecificDayAssignmentsContainer item : specificResourceAllocation1.getSpecificDayAssignmentsContainers()) {
            item.addAll(specificResourceAllocation1.getAssignments());
        }

        task1.addResourceAllocation(specificResourceAllocation1);


        // Task 2
        Task task2 = (Task) orderModel.getOrder().getAllChildrenAssociatedTaskElements().get(1);
        SpecificResourceAllocation specificResourceAllocation2 = SpecificResourceAllocation.create(task2);
        specificResourceAllocation2.setResource(workerModel.getWorker());

        Scenario orderScenario2 = null;
        for (Scenario scenario : orderModel.getOrder().getScenarios().keySet()) {
            orderScenario2 = scenario;
        }
        specificResourceAllocation2.copyAssignments(orderScenario2, orderScenario2);

        specificResourceAllocation2.allocate(ResourcesPerDay.amount(1));

        for (SpecificDayAssignmentsContainer item : specificResourceAllocation2.getSpecificDayAssignmentsContainers()) {
            item.addAll(specificResourceAllocation2.getAssignments());
        }

        task2.addResourceAllocation(specificResourceAllocation2);
    }

}
