/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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

package org.libreplan.business.test.expensesheet.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.expensesheet.daos.IExpenseSheetDAO;
import org.libreplan.business.expensesheet.daos.IExpenseSheetLineDAO;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.test.calendars.entities.BaseCalendarTest;
import org.libreplan.business.test.planner.daos.ResourceAllocationDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE, BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@ExpenseSheetDAO}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 */
@Transactional
public class ExpenseSheetTestDAO {

    @Before
    public void loadRequiredaData() {
        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                scenariosBootstrap.loadRequiredData();
                return null;
            }
        });
    }

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    IExpenseSheetLineDAO expenseSheetLineDAO;

    @Autowired
    IExpenseSheetDAO expenseSheetDAO;

    @Autowired
    IOrderElementDAO orderElementDAO;

    @Autowired
    IOrderDAO orderDAO;

    @Autowired
    IResourceDAO resourceDAO;

    @Autowired
    private IBaseCalendarDAO calendarDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Test
    public void testInSpringContainer() {
        assertNotNull(orderDAO);
        assertNotNull(calendarDAO);
        assertNotNull(orderElementDAO);
        assertNotNull(expenseSheetDAO);
    }

    private Order createValidOrder(String name) {
        Order order = Order.create();
        order.setName(name);
        order.setCode(UUID.randomUUID().toString());
        order.setInitDate(new Date());
        BaseCalendar basicCalendar = BaseCalendarTest.createBasicCalendar();
        calendarDAO.save(basicCalendar);
        OrderVersion orderVersion = ResourceAllocationDAOTest.setupVersionUsing(scenarioManager,
                order);
        order.useSchedulingDataFor(orderVersion);
        return order;
    }

    private Worker givenValidWorker() {
        Worker worker = Worker.create();
        worker.setFirstName("First name");
        worker.setSurname("Surname");
        worker.setNif("NIF" + UUID.randomUUID().toString());
        return worker;
    }

    private OrderElement givenOrderElement(String orderLineCode) {
        Order order = createValidOrder(orderLineCode + "-order-1");
        OrderLine line = OrderLine.create();
        line.setCode(orderLineCode);
        line.setName(orderLineCode + "-name-1");
        line.setInitDate(new Date());
        order.add(line);
        return line;
    }

    @Test(expected = ValidationException.class)
    public void invalidExpenseSheetWithoutLines() {
        ExpenseSheet expense = ExpenseSheet.create();
        expense.setCode("UUID.randomUUID().toString()");

        try {
            expenseSheetDAO.save(expense);
            expenseSheetDAO.flush();
            fail("it must throw a exception because the total is empty");
        } catch (ValidationException e) {
        }

        expense.updateTotal();

        expenseSheetDAO.save(expense);
        expenseSheetDAO.flush();
        fail("it must throw a exception because the expense sheet has not lines");
    }

    @Test(expected = ValidationException.class)
    public void validExpenseSheetWithInvalidExpenseSheetLineData() {
        // check out the expenseSheet without lines
        ExpenseSheet expense = ExpenseSheet.create();
        expense.setCode("code-expense-sheet-1");

        // order element empty
        OrderElement orderElement = null;

        // a expense sheet with valid value,date but without order element.
        ExpenseSheetLine line1 = ExpenseSheetLine.create(BigDecimal.ZERO, "concept",
                new LocalDate(), orderElement);

        line1.setExpenseSheet(expense);
        expense.add(line1);

        expenseSheetDAO.save(expense);
        expenseSheetDAO.flush();
        fail("it must throw a exception. The order element not specified");
    }

    @Test
    public void validExpenseSheetWithInvalidValuesInLines() {
        // create the expenseSheet without lines
        ExpenseSheet expense = ExpenseSheet.create();
        expense.setCode("code-expense-sheet-1");

        // order element empty
        OrderElement orderElement = this.givenOrderElement("order-element-2");

        // a expense sheet with valid date and order element, but invalid value.
        ExpenseSheetLine line1 = ExpenseSheetLine.create(null, "concept", new LocalDate(),
                orderElement);

        // add the line to the expense sheet
        line1.setExpenseSheet(expense);
        expense.add(line1);

        try {
            expenseSheetDAO.save(expense);
            fail("it must throw a exception. The value is not specified in the expense sheet line.");
        } catch (ValidationException e) {
        }

        line1.setValue(new BigDecimal(-3));

        try {
            expenseSheetDAO.save(expense);
            fail("it must throw a exception. The total is invalid because it is less than 0.");
        } catch (ValidationException e) {
        }

        ExpenseSheetLine line2 = ExpenseSheetLine.create(BigDecimal.TEN, "concept",
                new LocalDate(), orderElement);

        // add other line to the expense sheet
        line2.setExpenseSheet(expense);
        expense.add(line2);

        try {
            expenseSheetDAO.save(expense);
            fail("it must throw a exception. The value of the line 1 is invalid because it is less than 0.");
        } catch (ValidationException e) {
        }

        // but if you change the value
        line1.setValue(BigDecimal.ONE);

        try {
            expenseSheetDAO.save(expense);
            assertThat(expense.getTotal(), equalTo(new BigDecimal(11)));
        } catch (ValidationException e) {
            fail("it must not throw a exception.");
        }
    }

    @Test
    public void validExpenseSheetWithInvalidDateInLines() {
        // create the expenseSheet without lines
        ExpenseSheet expense = ExpenseSheet.create();
        expense.setCode("code-expense-sheet-1");

        // order element empty
        OrderElement orderElement = this.givenOrderElement("order-element-2");

        // a expense sheet with valid date and order element, but invalid value.
        ExpenseSheetLine line1 = ExpenseSheetLine.create(BigDecimal.ONE, "concept", null,
                orderElement);

        // add the line to the expense sheet
        line1.setExpenseSheet(expense);
        expense.add(line1);

        try {
            expenseSheetDAO.save(expense);
            fail("it must throw a exception. The date is not specified in the expense sheet line.");
        } catch (ValidationException e) {
        }

        LocalDate today = new LocalDate();
        line1.setDate(today);

        assertThat(expense.getFirstExpense(), equalTo(today));
        assertThat(expense.getLastExpense(), equalTo(today));

        // add other line with other date
        LocalDate tomorrow = new LocalDate().plusDays(1);
        ExpenseSheetLine line2 = ExpenseSheetLine.create(BigDecimal.ONE, "concept", tomorrow,
                orderElement);
        line2.setExpenseSheet(expense);
        expense.add(line2);

        assertThat(expense.getFirstExpense(), equalTo(today));
        assertThat(expense.getLastExpense(), equalTo(tomorrow));

        try {
            expenseSheetDAO.save(expense);
        } catch (ValidationException e) {
            fail("it must not throw a exception.");
        }
    }

    @Test
    public void validExpenseSheetData() {
        // check out the expenseSheet without lines
        int previousExpenses = expenseSheetDAO.getAll().size();
        ExpenseSheet expense = ExpenseSheet.create();
        expense.setCode("code-expense-sheet-1");

        // check out the order element
        String orderLineCode = "order-line-code";
        OrderElement orderElement = givenSavedOrderElement(orderLineCode);

        // check out the resource
        Worker worker = givenSavedValidWorker();

        // add line 1 with a resource
        LocalDate today = new LocalDate();
        ExpenseSheetLine line1 = ExpenseSheetLine.create(BigDecimal.ONE, "concept 1", today,
                orderElement);
        line1.setResource(worker);
        line1.setExpenseSheet(expense);
        expense.add(line1);

        // add line 2 without resource
        LocalDate tomorrow = new LocalDate().plusDays(1);
        ExpenseSheetLine line2 = ExpenseSheetLine.create(BigDecimal.ONE, "concept 2", tomorrow,
                orderElement);
        line2.setExpenseSheet(expense);
        expense.add(line2);

        try {
            expenseSheetDAO.save(expense);
            expenseSheetDAO.flush();

        } catch (ValidationException e) {
            fail("it must not throw a exception");
        }

        assertThat(expenseSheetDAO.getAll().size(), equalTo(previousExpenses + 1));

        // check out the expense sheet
        ExpenseSheet expenseSheet = expenseSheetDAO.getAll().get(previousExpenses);
        assertNotNull(expenseSheet.getCode());
        assertThat(expenseSheet.getCode(), equalTo("code-expense-sheet-1"));
        assertThat(expenseSheet.getTotal(), equalTo(new BigDecimal(2)));
        assertThat(expenseSheet.getFirstExpense(), equalTo(today));
        assertThat(expenseSheet.getLastExpense(), equalTo(tomorrow));
        assertThat(expenseSheet.getExpenseSheetLines().size(), equalTo(2));

        // check out the expense sheet lines
        ExpenseSheetLine savedLine1 = expenseSheet.getExpenseSheetLines().last();
        ExpenseSheetLine savedLine2 = expenseSheet.getExpenseSheetLines().first();

        assertThat(savedLine1.getConcept(), equalTo("concept 1"));
        assertThat(savedLine1.getDate(), equalTo(today));
        assertThat(savedLine1.getValue(), equalTo(BigDecimal.ONE));
        assertNotNull(savedLine1.getOrderElement());
        assertNotNull(savedLine1.getResource());
        assertNotNull(savedLine1.getCode());

        assertThat(savedLine2.getConcept(), equalTo("concept 2"));
        assertThat(savedLine2.getDate(), equalTo(tomorrow));
        assertThat(savedLine2.getValue(), equalTo(BigDecimal.ONE));
        assertNotNull(savedLine2.getOrderElement());
        assertNotNull(savedLine2.getCode());

    }

    private Worker givenSavedValidWorker() {
        Worker worker = givenValidWorker();
        resourceDAO.save(worker);
        resourceDAO.flush();
        return worker;
    }

    private OrderElement givenSavedOrderElement(String orderLineCode) {
        OrderElement orderElement = givenOrderElement(orderLineCode);
        orderElementDAO.save(orderElement);
        orderElementDAO.flush();
        return orderElement;
    }

    @Test
    public void testSaveTwoExpenseSheetWithTheSameCode() {
        int previousExpenses = expenseSheetDAO.getAll().size();

        // create a valid order element
        String orderLineCode = "order-line-code";
        OrderElement orderElement = givenSavedOrderElement(orderLineCode);

        ExpenseSheet expense = getValidExpenseSheet("code-expense-sheet-1", orderElement);
        try {
            expenseSheetDAO.save(expense);
            expenseSheetDAO.flush();
        } catch (ValidationException e) {
            fail("it must not throw a exception");
        }

        assertThat(expenseSheetDAO.getAll().size(), equalTo(previousExpenses + 1));

        // check out the expense sheet
        ExpenseSheet expenseSheet = expenseSheetDAO.getAll().get(previousExpenses);
        assertNotNull(expenseSheet.getCode());
        assertThat(expenseSheet.getCode(), equalTo("code-expense-sheet-1"));

        // create other expense sheet with one line

        ExpenseSheet expense2 = getValidExpenseSheet("code-expense-sheet-1", orderElement);
        try {
            expenseSheetDAO.save(expense2);
            expenseSheetDAO.flush();
            fail("it must throw a exception");
        } catch (Exception e) {

        }
    }

    private ExpenseSheet getValidExpenseSheet(String code, OrderElement orderElement) {
        ExpenseSheet expense = ExpenseSheet.create();
        expense.setCode(code);

        // add line 1
        LocalDate today = new LocalDate();
        ExpenseSheetLine line1 = ExpenseSheetLine.create(BigDecimal.ONE, "concept 1", today,
                orderElement);
        line1.setExpenseSheet(expense);
        expense.add(line1);
        return expense;
    }

    @Test
    public void testSaveTwoValidExpenseSheets() {
        int previousExpenses = expenseSheetDAO.getAll().size();

        // create a valid order element
        String orderLineCode = "order-line-code";
        OrderElement orderElement = givenSavedOrderElement(orderLineCode);

        ExpenseSheet expense = getValidExpenseSheet("code-expense-sheet-1", orderElement);
        try {
            expenseSheetDAO.save(expense);
            expenseSheetDAO.flush();
        } catch (ValidationException e) {
            fail("it must not throw a exception");
        }

        assertThat(expenseSheetDAO.getAll().size(), equalTo(previousExpenses + 1));

        // check out the expense sheet
        ExpenseSheet expenseSheet = expenseSheetDAO.getAll().get(previousExpenses);
        assertNotNull(expenseSheet.getCode());
        assertThat(expenseSheet.getCode(), equalTo("code-expense-sheet-1"));

        // create other expense sheet with one line

        ExpenseSheet expense2 = getValidExpenseSheet("code-expense-sheet-2", orderElement);
        try {
            expenseSheetDAO.save(expense2);
            expenseSheetDAO.flush();
        } catch (ValidationException e) {
            fail("it must not throw a exception");
        }

        assertThat(expenseSheetDAO.getAll().size(), equalTo(previousExpenses + 2));

        // check out the expense sheet
        try {
            ExpenseSheet expenseSheet2 = expenseSheetDAO.findByCode("code-expense-sheet-2");
            assertNotNull(expenseSheet2);
            assertThat(expenseSheet2.getExpenseSheetLines().size(), equalTo(1));
        } catch (InstanceNotFoundException e) {
            fail("it must not throw a exception");
        }
    }

    @Test(expected = ValidationException.class)
    public void testSaveExpenseSheetWithoutCode() {
        ExpenseSheet expense = ExpenseSheet.create();
        expense.setCode(null);
        try {
        expenseSheetDAO.save(expense);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Test
    public void testRemoveExpenseSheet() throws InstanceNotFoundException {
        int previousExpenses = expenseSheetDAO.getAll().size();
        int previousExpenseLines = expenseSheetLineDAO.findAll().size();

        // create a valid order element
        String orderLineCode = "order-line-code";
        OrderElement orderElement = givenSavedOrderElement(orderLineCode);

        ExpenseSheet expense = getValidExpenseSheet("code-expense-sheet-1", orderElement);
        try {
            expenseSheetDAO.save(expense);
            expenseSheetDAO.flush();
        } catch (ValidationException e) {
            fail("it must not throw a exception");
        }

        assertThat(expenseSheetDAO.getAll().size(), equalTo(previousExpenses + 1));
        assertThat(expenseSheetLineDAO.findAll().size(), equalTo(previousExpenseLines + 1));

        expenseSheetDAO.remove(expense.getId());
        expenseSheetDAO.flush();

        assertThat(expenseSheetDAO.getAll().size(), equalTo(previousExpenses));
        assertThat(expenseSheetLineDAO.findAll().size(), equalTo(previousExpenseLines));
    }

    @Test
    public void testRemoveExpenseSheetLine() {
        int previousExpenseLines = expenseSheetLineDAO.findAll().size();
        int previousExpenses = expenseSheetDAO.getAll().size();

        ExpenseSheet expense = ExpenseSheet.create();
        expense.setCode("code-expense-sheet-1");

        // check out the order element
        String orderLineCode = "order-line-code";
        OrderElement orderElement = givenSavedOrderElement(orderLineCode);

        // check out the resource
        Worker worker = givenSavedValidWorker();

        // add line 1 with a resource
        LocalDate today = new LocalDate();
        ExpenseSheetLine line1 = ExpenseSheetLine.create(BigDecimal.ONE, "concept 1", today,
                orderElement);
        line1.setResource(worker);
        line1.setExpenseSheet(expense);
        expense.add(line1);

        // add line 2 without resource
        LocalDate tomorrow = new LocalDate().plusDays(1);
        ExpenseSheetLine line2 = ExpenseSheetLine.create(BigDecimal.ONE, "concept 2", tomorrow,
                orderElement);
        line2.setCode("code-line-2");
        line2.setExpenseSheet(expense);
        expense.add(line2);

        try {
            expenseSheetDAO.save(expense);
            expenseSheetDAO.flush();

        } catch (ValidationException e) {
            fail("it must not throw a exception");
        }

        assertThat(expenseSheetDAO.getAll().size(), equalTo(previousExpenses + 1));
        try {
            ExpenseSheet expenseSheet = expenseSheetDAO.findByCode("code-expense-sheet-1");
            assertNotNull(expenseSheet);
            assertThat(expenseSheet.getExpenseSheetLines().size(), equalTo(2));

            assertThat(expenseSheetLineDAO.findAll().size(), equalTo(previousExpenseLines + 2));

            expenseSheet.remove(line2);
            expenseSheetDAO.save(expenseSheet);
            expenseSheetDAO.flush();

            assertThat(expenseSheetDAO.getAll().size(), equalTo(previousExpenses + 1));
            assertThat(expenseSheetLineDAO.findAll().size(), equalTo(previousExpenseLines + 1));

        } catch (InstanceNotFoundException e) {
            fail("It must not throw a exception");
        }
    }

}
