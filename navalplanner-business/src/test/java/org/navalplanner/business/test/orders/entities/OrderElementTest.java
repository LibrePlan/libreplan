package org.navalplanner.business.test.orders.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Resource;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceMeasurementComparator;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.AdvanceAssigment.Type;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssigmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link OrderElement}. <br />
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class OrderElementTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
    }

    private OrderLine givenOrderLine(String name, String code, Integer hours) {
        OrderLine orderLine = OrderLine
                .createOrderLineWithUnfixedPercentage(hours);
        orderLine.setName(name);
        orderLine.setCode(code);

        return orderLine;
    }

    private OrderLineGroup givenOrderLineGroupWithOneOrderLine(Integer hours) {
        OrderLineGroup orderLineGroup = OrderLineGroup.create();
        orderLineGroup.setName("OrderLineGroup1");
        orderLineGroup.setCode("1");

        OrderLine orderLine = givenOrderLine("OrderLine1", "1.1", hours);
        orderLineGroup.add(orderLine);

        return orderLineGroup;
    }

    private OrderLineGroup givenOrderLineGroupWithTwoOrderLines(Integer hours1,
            Integer hours2) {
        OrderLineGroup orderLineGroup = givenOrderLineGroupWithOneOrderLine(hours1);

        OrderLine orderLine = givenOrderLine("OrderLine2", "1.2", hours2);
        orderLineGroup.add(orderLine);

        return orderLineGroup;
    }

    private AdvanceAssigment givenAdvanceAssigement(BigDecimal maxValue,
            AdvanceType advanceType) {
        AdvanceAssigment advanceAssigment = AdvanceAssigment.create();
        advanceAssigment.setMaxValue(maxValue);
        advanceAssigment.setAdvanceType(advanceType);
        advanceAssigment.setReportGlobalAdvance(false);
        advanceAssigment.setType(Type.DIRECT);

        return advanceAssigment;
    }

    private void addAvanceAssigmentWithMeasurement(OrderElement orderElement,
            AdvanceType advanceType, BigDecimal maxValue,
            BigDecimal currentValue)
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
        AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create();
        advanceMeasurement.setDate(new LocalDate());
        advanceMeasurement.setValue(currentValue);

        AdvanceAssigment advanceAssigment = givenAdvanceAssigement(maxValue,
                advanceType);
        advanceAssigment.getAdvanceMeasurements().add(advanceMeasurement);

        orderElement.addAdvanceAssigment(advanceAssigment);
    }

    private AdvanceType givenAdvanceType(String name) {
        BigDecimal value = new BigDecimal(5000).setScale(2);
        BigDecimal precision = new BigDecimal(10).setScale(2);
        AdvanceType advanceType = AdvanceType.create(name, value, true,
                precision, true);
        return advanceType;
    }

    @Test
    public void checkAdvancePercentageEmptyOrderLine() {
        OrderLine orderLine = givenOrderLine("name", "code", 1000);
        assertThat(orderLine.getAdvancePercentage(), equalTo(new BigDecimal(0)
                .setScale(2)));
    }

    @Test
    public void checkAdvancePercentageOrderLineWithAdvanceAssigmentWithoutMesaurement()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
        OrderLine orderLine = givenOrderLine("name", "code", 1000);

        AdvanceAssigment advanceAssigment = givenAdvanceAssigement(
                new BigDecimal(5000), PredefinedAdvancedTypes.UNITS.getType());

        orderLine.addAdvanceAssigment(advanceAssigment);

        assertThat(orderLine.getAdvancePercentage(), equalTo(new BigDecimal(0)
                .setScale(2)));
    }

    @Test
    public void checkAdvancePercentageOrderLineWithTwoAssigments()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
        OrderLine orderLine = givenOrderLine("name", "code", 1000);

        addAvanceAssigmentWithMeasurement(orderLine, givenAdvanceType("test1"),
                new BigDecimal(2000), new BigDecimal(200));

        addAvanceAssigmentWithMeasurement(orderLine, givenAdvanceType("test2"),
                new BigDecimal(1000), new BigDecimal(600));

        assertThat(orderLine.getAdvancePercentage(), equalTo(new BigDecimal(35)
                .divide(new BigDecimal(100)).setScale(2)));
    }

    @Test
    public void checkAdvancePercentageOrderLineWithThreeAssigments()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
        OrderLine orderLine = givenOrderLine("name", "code", 1000);

        addAvanceAssigmentWithMeasurement(orderLine, givenAdvanceType("test1"),
                new BigDecimal(2000), new BigDecimal(200));

        addAvanceAssigmentWithMeasurement(orderLine, givenAdvanceType("test2"),
                new BigDecimal(1000), new BigDecimal(600));

        addAvanceAssigmentWithMeasurement(orderLine, givenAdvanceType("test3"),
                new BigDecimal(4000), new BigDecimal(800));

        assertThat(orderLine.getAdvancePercentage(), equalTo(new BigDecimal(30)
                .divide(new BigDecimal(100)).setScale(2)));
    }

    @Test
    public void checkAdvancePercentageOrderGroupLine()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();
        addAvanceAssigmentWithMeasurement(children.get(0),
                PredefinedAdvancedTypes.UNITS.getType(), new BigDecimal(1000),
                new BigDecimal(400));
        addAvanceAssigmentWithMeasurement(children.get(1),
                PredefinedAdvancedTypes.UNITS.getType(), new BigDecimal(2000),
                new BigDecimal(200));

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                20).divide(new BigDecimal(100)).setScale(2)));
    }

    @Test
    public void checkAdvancePercentageOrderGroupLineWithAssigments()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();
        addAvanceAssigmentWithMeasurement(children.get(0),
                PredefinedAdvancedTypes.UNITS.getType(), new BigDecimal(1000),
                new BigDecimal(400));
        addAvanceAssigmentWithMeasurement(children.get(1),
                PredefinedAdvancedTypes.UNITS.getType(), new BigDecimal(2000),
                new BigDecimal(200));

        addAvanceAssigmentWithMeasurement(orderElement,
                PredefinedAdvancedTypes.PERCENTAGE.getType(), new BigDecimal(
                        100), new BigDecimal(10));

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                15).divide(new BigDecimal(100)).setScale(2)));
    }

    @Test
    public void checkAdvanceMeasurementMerge()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();

        LocalDate one = new LocalDate(2009, 9, 1);
        LocalDate two = new LocalDate(2009, 9, 2);
        LocalDate three = new LocalDate(2009, 9, 3);
        LocalDate four = new LocalDate(2009, 9, 4);
        LocalDate five = new LocalDate(2009, 9, 5);

        AdvanceType advanceType = PredefinedAdvancedTypes.UNITS.getType();

        addAvanceAssigmentWithMeasurements(children.get(0),
                advanceType,
                new BigDecimal(1000), one, new BigDecimal(200), three,
                new BigDecimal(400), five, new BigDecimal(500));

        addAvanceAssigmentWithMeasurements(children.get(1),
                advanceType,
                new BigDecimal(1000), two, new BigDecimal(100), three,
                new BigDecimal(350), four, new BigDecimal(400));

        Set<AdvanceAssigment> advanceAssigments = orderElement.getAdvanceAssigments();
        assertThat(advanceAssigments.size(), equalTo(1));
        AdvanceAssigment advanceAssigment = advanceAssigments.iterator().next();
        assertThat(advanceAssigment.getMaxValue(),
                equalTo(new BigDecimal(2000)));

        SortedSet<AdvanceMeasurement> advanceMeasurements = advanceAssigment
                .getAdvanceMeasurements();
        assertThat(advanceMeasurements.size(), equalTo(5));

        ArrayList<AdvanceMeasurement> list = new ArrayList<AdvanceMeasurement>(
                advanceMeasurements);
        Collections.sort(list, new AdvanceMeasurementComparator());
        Collections.reverse(list);
        Iterator<AdvanceMeasurement> iterator = list.iterator();

        AdvanceMeasurement next = iterator.next();
        assertThat(next.getDate(), equalTo(one));
        assertThat(next.getValue(), equalTo(new BigDecimal(200)));

        next = iterator.next();
        assertThat(next.getDate(), equalTo(two));
        assertThat(next.getValue(), equalTo(new BigDecimal(300)));

        next = iterator.next();
        assertThat(next.getDate(), equalTo(three));
        assertThat(next.getValue(), equalTo(new BigDecimal(750)));

        next = iterator.next();
        assertThat(next.getDate(), equalTo(four));
        assertThat(next.getValue(), equalTo(new BigDecimal(800)));

        next = iterator.next();
        assertThat(next.getDate(), equalTo(five));
        assertThat(next.getValue(), equalTo(new BigDecimal(900)));

    }

    private void addAvanceAssigmentWithMeasurements(OrderElement orderElement,
            AdvanceType advanceType, BigDecimal maxValue, LocalDate date1,
            BigDecimal value1, LocalDate date2, BigDecimal value2,
            LocalDate five,
            BigDecimal date3)
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
        AdvanceAssigment advanceAssigment = givenAdvanceAssigement(maxValue,
                advanceType);

        AdvanceMeasurement advanceMeasurement1 = AdvanceMeasurement.create();
        advanceMeasurement1.setDate(date1);
        advanceMeasurement1.setValue(value1);
        advanceAssigment.getAdvanceMeasurements().add(advanceMeasurement1);

        AdvanceMeasurement advanceMeasurement2 = AdvanceMeasurement.create();
        advanceMeasurement2.setDate(date2);
        advanceMeasurement2.setValue(value2);
        advanceAssigment.getAdvanceMeasurements().add(advanceMeasurement2);

        AdvanceMeasurement advanceMeasurement3 = AdvanceMeasurement.create();
        advanceMeasurement3.setDate(five);
        advanceMeasurement3.setValue(date3);
        advanceAssigment.getAdvanceMeasurements().add(advanceMeasurement3);

        orderElement.addAdvanceAssigment(advanceAssigment);
    }

    @Test
    public void checkGetAdvanceAssigmentsIdempotenet()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();

        AdvanceType advanceType = PredefinedAdvancedTypes.UNITS.getType();

        addAvanceAssigmentWithMeasurement(children.get(0), advanceType,
                new BigDecimal(1000), new BigDecimal(200));

        addAvanceAssigmentWithMeasurement(children.get(1), advanceType,
                new BigDecimal(2000), new BigDecimal(400));

        Set<AdvanceAssigment> advanceAssigments = orderElement
                .getAdvanceAssigments();
        assertThat(advanceAssigments.size(), equalTo(1));
        AdvanceAssigment advanceAssigment = advanceAssigments.iterator().next();
        assertThat(advanceAssigment.getMaxValue(),
                equalTo(new BigDecimal(3000)));

        assertThat(advanceAssigment.getAdvanceMeasurements().size(), equalTo(1));
        assertThat(advanceAssigment.getAdvanceMeasurements().iterator().next()
                .getValue(), equalTo(new BigDecimal(600)));


        advanceAssigments = orderElement.getAdvanceAssigments();
        assertThat(advanceAssigments.size(), equalTo(1));
        advanceAssigment = advanceAssigments.iterator().next();
        assertThat(advanceAssigment.getMaxValue(),
                equalTo(new BigDecimal(3000)));

        assertThat(advanceAssigment.getAdvanceMeasurements().size(), equalTo(1));
        assertThat(advanceAssigment.getAdvanceMeasurements().iterator().next()
                .getValue(), equalTo(new BigDecimal(600)));
    }

}
