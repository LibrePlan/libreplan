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
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceMeasurementComparator;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
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

    private static OrderLine givenOrderLine(String name, String code,
            Integer hours) {
        OrderLine orderLine = OrderLine
                .createOrderLineWithUnfixedPercentage(hours);
        orderLine.setName(name);
        orderLine.setCode(code);

        return orderLine;
    }

    private static OrderLineGroup givenOrderLineGroupWithOneOrderLine(
            Integer hours) {
        OrderLineGroup orderLineGroup = OrderLineGroup.create();
        orderLineGroup.setName("OrderLineGroup1");
        orderLineGroup.setCode("1");

        OrderLine orderLine = givenOrderLine("OrderLine1", "1.1", hours);
        orderLineGroup.add(orderLine);

        return orderLineGroup;
    }

    public static OrderLineGroup givenOrderLineGroupWithTwoOrderLines(
            Integer hours1,
            Integer hours2) {
        OrderLineGroup orderLineGroup = givenOrderLineGroupWithOneOrderLine(hours1);

        OrderLine orderLine = givenOrderLine("OrderLine2", "1.2", hours2);
        orderLineGroup.add(orderLine);

        return orderLineGroup;
    }

    private static DirectAdvanceAssignment givenAdvanceAssigement(
            BigDecimal maxValue,
            AdvanceType advanceType) {
        DirectAdvanceAssignment advanceAssignment = DirectAdvanceAssignment
                .create();
        advanceAssignment.setMaxValue(maxValue);
        advanceAssignment.setAdvanceType(advanceType);
        advanceAssignment.setReportGlobalAdvance(false);

        return advanceAssignment;
    }

    public static void addAvanceAssignmentWithMeasurement(
            OrderElement orderElement,
            AdvanceType advanceType, BigDecimal maxValue,
            BigDecimal currentValue, boolean reportGlobalAdvance)
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create();
        advanceMeasurement.setDate(new LocalDate());
        advanceMeasurement.setValue(currentValue);

        DirectAdvanceAssignment advanceAssignment = givenAdvanceAssigement(
                maxValue,
                advanceType);
        advanceAssignment.getAdvanceMeasurements().add(advanceMeasurement);
        advanceAssignment.setReportGlobalAdvance(reportGlobalAdvance);

        advanceMeasurement.setAdvanceAssignment(advanceAssignment);

        orderElement.addAdvanceAssignment(advanceAssignment);
    }

    private static AdvanceType givenAdvanceType(String name) {
        BigDecimal value = new BigDecimal(5000).setScale(2);
        BigDecimal precision = new BigDecimal(10).setScale(2);
        AdvanceType advanceType = AdvanceType.create(name, value, true,
                precision, true);
        return advanceType;
    }

    @Test
    public void checkAdvancePercentageEmptyOrderLine() {
        OrderLine orderLine = givenOrderLine("name", "code", 1000);
        assertThat(orderLine.getAdvancePercentage(), equalTo(BigDecimal.ZERO));
    }

    @Test
    public void checkAdvancePercentageOrderLineWithAdvanceAssignmentWithoutMesaurement()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderLine orderLine = givenOrderLine("name", "code", 1000);

        DirectAdvanceAssignment advanceAssignment = givenAdvanceAssigement(
                new BigDecimal(5000), PredefinedAdvancedTypes.UNITS.getType());

        orderLine.addAdvanceAssignment(advanceAssignment);

        assertThat(orderLine.getAdvancePercentage(), equalTo(BigDecimal.ZERO));
    }

    @Test
    public void checkAdvancePercentageOrderLineWithTwoAssignments1()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderLine orderLine = givenOrderLine("name", "code", 1000);

        addAvanceAssignmentWithMeasurement(orderLine,
                givenAdvanceType("test1"), new BigDecimal(2000),
                new BigDecimal(200), true);

        addAvanceAssignmentWithMeasurement(orderLine,
                givenAdvanceType("test2"), new BigDecimal(1000),
                new BigDecimal(600), false);

        assertThat(orderLine.getAdvancePercentage(), equalTo(new BigDecimal(10)
                .divide(new BigDecimal(100))));
    }

    @Test
    public void checkAdvancePercentageOrderLineWithTwoAssignments2()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderLine orderLine = givenOrderLine("name", "code", 1000);

        addAvanceAssignmentWithMeasurement(orderLine, givenAdvanceType("test1"),
                new BigDecimal(2000),
                new BigDecimal(200), false);

        addAvanceAssignmentWithMeasurement(orderLine, givenAdvanceType("test2"),
                new BigDecimal(1000),
                new BigDecimal(600), true);

        assertThat(orderLine.getAdvancePercentage(), equalTo(new BigDecimal(60)
                .divide(new BigDecimal(100))));
    }

    @Test
    public void checkAdvancePercentageOrderLineWithThreeAssignments()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderLine orderLine = givenOrderLine("name", "code", 1000);

        addAvanceAssignmentWithMeasurement(orderLine, givenAdvanceType("test1"),
                new BigDecimal(2000),
                new BigDecimal(200), false);

        addAvanceAssignmentWithMeasurement(orderLine,
                givenAdvanceType("test3"), new BigDecimal(4000),
                new BigDecimal(800), true);

        addAvanceAssignmentWithMeasurement(orderLine, givenAdvanceType("test2"),
                new BigDecimal(1000),
                new BigDecimal(600), false);

        assertThat(orderLine.getAdvancePercentage(), equalTo(new BigDecimal(20)
                .divide(new BigDecimal(100))));
    }

    @Test
    public void checkAdvancePercentageOrderGroupLine1()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();

        AdvanceType advanceType1 = AdvanceType.create("test1", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurement(children.get(0), advanceType1,
                new BigDecimal(1000), new BigDecimal(400), true);

        AdvanceType advanceType2 = AdvanceType.create("test2", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurement(children.get(1), advanceType2,
                new BigDecimal(2000), new BigDecimal(200), true);

        Set<IndirectAdvanceAssignment> indirectAdvanceAssignments = ((OrderLineGroup) orderElement)
                .getIndirectAdvanceAssignments();
        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (indirectAdvanceAssignment.getAdvanceType().getUnitName()
                    .equals("test1")) {
                indirectAdvanceAssignment.setReportGlobalAdvance(true);
            } else {
                indirectAdvanceAssignment.setReportGlobalAdvance(false);
            }
        }

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                40).divide(new BigDecimal(100))));
    }

    @Test
    public void checkAdvancePercentageOrderGroupLine2()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();

        AdvanceType advanceType1 = AdvanceType.create("test1", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurement(children.get(0), advanceType1,
                new BigDecimal(1000), new BigDecimal(400), true);

        AdvanceType advanceType2 = AdvanceType.create("test2", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurement(children.get(1), advanceType2,
                new BigDecimal(2000), new BigDecimal(200), true);

        Set<IndirectAdvanceAssignment> indirectAdvanceAssignments = ((OrderLineGroup) orderElement)
                .getIndirectAdvanceAssignments();
        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (indirectAdvanceAssignment.getAdvanceType().getUnitName()
                    .equals("test2")) {
                indirectAdvanceAssignment.setReportGlobalAdvance(true);
            } else {
                indirectAdvanceAssignment.setReportGlobalAdvance(false);
            }
        }

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                10).divide(new BigDecimal(100))));
    }

    @Test
    public void checkAdvancePercentageOrderGroupLine3()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();

        AdvanceType advanceType1 = AdvanceType.create("test1", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurement(children.get(0), advanceType1,
                new BigDecimal(1000), new BigDecimal(400), true);

        AdvanceType advanceType2 = AdvanceType.create("test2", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurement(children.get(1), advanceType2,
                new BigDecimal(2000), new BigDecimal(200), true);

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                20).divide(new BigDecimal(100)).setScale(2)));
    }

    @Test
    public void checkAdvancePercentageOrderGroupLineSameAdvanceType()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(2000,
                3000);

        List<OrderElement> children = orderElement.getChildren();

        AdvanceType advanceType = PredefinedAdvancedTypes.UNITS.getType();

        addAvanceAssignmentWithMeasurement(children.get(0), advanceType,
                new BigDecimal(1000), new BigDecimal(100), true);

        addAvanceAssignmentWithMeasurement(children.get(1), advanceType,
                new BigDecimal(1000), new BigDecimal(300), true);

        Set<IndirectAdvanceAssignment> indirectAdvanceAssignments = ((OrderLineGroup) orderElement)
                .getIndirectAdvanceAssignments();
        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (indirectAdvanceAssignment.getAdvanceType().equals(advanceType)) {
                indirectAdvanceAssignment.setReportGlobalAdvance(true);
            } else {
                indirectAdvanceAssignment.setReportGlobalAdvance(false);
            }
        }

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                20).divide(new BigDecimal(100))));
    }

    @Test
    public void checkAdvancePercentageOrderGroupLineSameAdvanceTypeChildren()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(2000,
                3000);

        List<OrderElement> children = orderElement.getChildren();

        AdvanceType advanceType = PredefinedAdvancedTypes.UNITS.getType();

        addAvanceAssignmentWithMeasurement(children.get(0), advanceType,
                new BigDecimal(1000), new BigDecimal(100), true);

        addAvanceAssignmentWithMeasurement(children.get(1), advanceType,
                new BigDecimal(1000), new BigDecimal(300), true);

        Set<IndirectAdvanceAssignment> indirectAdvanceAssignments = ((OrderLineGroup) orderElement)
                .getIndirectAdvanceAssignments();
        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (indirectAdvanceAssignment.getAdvanceType().equals(
                    PredefinedAdvancedTypes.CHILDREN.getType())) {
                indirectAdvanceAssignment.setReportGlobalAdvance(true);
            } else {
                indirectAdvanceAssignment.setReportGlobalAdvance(false);
            }
        }

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                22).divide(new BigDecimal(100))));
    }

    @Test
    public void checkAdvancePercentageOrderGroupLineWithAssignments1()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();
        addAvanceAssignmentWithMeasurement(children.get(0),
                PredefinedAdvancedTypes.UNITS.getType(), new BigDecimal(1000),
                new BigDecimal(400), true);
        addAvanceAssignmentWithMeasurement(children.get(1),
                PredefinedAdvancedTypes.UNITS.getType(), new BigDecimal(2000),
                new BigDecimal(200), true);

        addAvanceAssignmentWithMeasurement(orderElement,
                PredefinedAdvancedTypes.PERCENTAGE.getType(), new BigDecimal(
                        100), new BigDecimal(90), true);

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                90).divide(new BigDecimal(100))));
    }

    @Test
    public void checkAdvancePercentageOrderGroupLineWithAssignments2()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();
        addAvanceAssignmentWithMeasurement(children.get(0),
                PredefinedAdvancedTypes.UNITS.getType(), new BigDecimal(1000),
                new BigDecimal(400), true);
        addAvanceAssignmentWithMeasurement(children.get(1),
                PredefinedAdvancedTypes.UNITS.getType(), new BigDecimal(2000),
                new BigDecimal(200), true);

        addAvanceAssignmentWithMeasurement(orderElement,
                PredefinedAdvancedTypes.PERCENTAGE.getType(), new BigDecimal(
                        100), new BigDecimal(90), false);

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                20).divide(new BigDecimal(100)).setScale(2)));
    }

    @Test
    public void checkAdvanceMeasurementMerge()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();

        LocalDate one = new LocalDate(2009, 9, 1);
        LocalDate two = new LocalDate(2009, 9, 2);
        LocalDate three = new LocalDate(2009, 9, 3);
        LocalDate four = new LocalDate(2009, 9, 4);
        LocalDate five = new LocalDate(2009, 9, 5);

        AdvanceType advanceType = PredefinedAdvancedTypes.UNITS.getType();

        addAvanceAssignmentWithMeasurements(children.get(0), advanceType, true,
                new BigDecimal(1000), one, new BigDecimal(200), three,
                new BigDecimal(400), five, new BigDecimal(500));

        addAvanceAssignmentWithMeasurements(children.get(1), advanceType, true,
                new BigDecimal(1000), two, new BigDecimal(100), three,
                new BigDecimal(350), four, new BigDecimal(400));

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                43).divide(new BigDecimal(100)).setScale(2)));

        Set<DirectAdvanceAssignment> directAdvanceAssignments = orderElement
                .getDirectAdvanceAssignments();
        assertThat(directAdvanceAssignments.size(), equalTo(0));

        Set<IndirectAdvanceAssignment> indirectAdvanceAssignments = ((OrderLineGroup) orderElement)
                .getIndirectAdvanceAssignments();
        assertThat(indirectAdvanceAssignments.size(), equalTo(2));

        DirectAdvanceAssignment advanceAssignment = null;
        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (indirectAdvanceAssignment.getAdvanceType().equals(advanceType)) {
                advanceAssignment = ((OrderLineGroup) orderElement)
                        .calculateFakeDirectAdvanceAssigment(indirectAdvanceAssignment);
                break;
            }
        }
        assertThat(advanceAssignment.getMaxValue(),
                equalTo(new BigDecimal(2000)));

        SortedSet<AdvanceMeasurement> advanceMeasurements = advanceAssignment
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

    private static void addAvanceAssignmentWithMeasurements(
            OrderElement orderElement,
            AdvanceType advanceType, boolean reportGlobalAdvance,
            BigDecimal maxValue, LocalDate date1,
            BigDecimal value1, LocalDate date2, BigDecimal value2,
            LocalDate five, BigDecimal date3)
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        DirectAdvanceAssignment advanceAssignment = givenAdvanceAssigement(
                maxValue,
                advanceType);
        advanceAssignment.setReportGlobalAdvance(reportGlobalAdvance);

        AdvanceMeasurement advanceMeasurement1 = AdvanceMeasurement.create();
        advanceMeasurement1.setDate(date1);
        advanceMeasurement1.setValue(value1);
        advanceAssignment.getAdvanceMeasurements().add(advanceMeasurement1);
        advanceMeasurement1.setAdvanceAssignment(advanceAssignment);

        AdvanceMeasurement advanceMeasurement2 = AdvanceMeasurement.create();
        advanceMeasurement2.setDate(date2);
        advanceMeasurement2.setValue(value2);
        advanceAssignment.getAdvanceMeasurements().add(advanceMeasurement2);
        advanceMeasurement2.setAdvanceAssignment(advanceAssignment);

        AdvanceMeasurement advanceMeasurement3 = AdvanceMeasurement.create();
        advanceMeasurement3.setDate(five);
        advanceMeasurement3.setValue(date3);
        advanceAssignment.getAdvanceMeasurements().add(advanceMeasurement3);
        advanceMeasurement3.setAdvanceAssignment(advanceAssignment);

        orderElement.addAdvanceAssignment(advanceAssignment);
    }

    @Test
    public void checkGetAdvanceAssignmentsIdempotenet()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();

        AdvanceType advanceType = PredefinedAdvancedTypes.UNITS.getType();

        addAvanceAssignmentWithMeasurement(children.get(0), advanceType,
                new BigDecimal(1000), new BigDecimal(200), true);

        addAvanceAssignmentWithMeasurement(children.get(1), advanceType,
                new BigDecimal(2000), new BigDecimal(400), true);

        Set<DirectAdvanceAssignment> directAdvanceAssignments = orderElement
                .getDirectAdvanceAssignments();
        assertThat(directAdvanceAssignments.size(), equalTo(0));

        Set<IndirectAdvanceAssignment> indirectAdvanceAssignments = ((OrderLineGroup) orderElement)
                .getIndirectAdvanceAssignments();
        assertThat(indirectAdvanceAssignments.size(), equalTo(2));

        DirectAdvanceAssignment advanceAssignment = null;
        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (indirectAdvanceAssignment.getAdvanceType().equals(advanceType)) {
                advanceAssignment = ((OrderLineGroup) orderElement)
                        .calculateFakeDirectAdvanceAssigment(indirectAdvanceAssignment);
                break;
            }
        }
        assertThat(advanceAssignment.getMaxValue(),
                equalTo(new BigDecimal(3000)));

        assertThat(advanceAssignment.getAdvanceMeasurements().size(),
                equalTo(1));
        assertThat(advanceAssignment.getAdvanceMeasurements().iterator().next()
                .getValue(), equalTo(new BigDecimal(600)));
    }

    @Test
    public void checkAdvanceMeasurementMergeWithDifferentAdvanceTypes()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderElement orderElement = givenOrderLineGroupWithTwoOrderLines(1000,
                2000);

        List<OrderElement> children = orderElement.getChildren();

        LocalDate one = new LocalDate(2009, 9, 1);
        LocalDate two = new LocalDate(2009, 9, 2);
        LocalDate three = new LocalDate(2009, 9, 3);
        LocalDate four = new LocalDate(2009, 9, 4);
        LocalDate five = new LocalDate(2009, 9, 5);

        AdvanceType advanceType1 = AdvanceType.create("test1", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurements(children.get(0), advanceType1,
                true,
                new BigDecimal(1000), one, new BigDecimal(200), three,
                new BigDecimal(400), five, new BigDecimal(500));

        AdvanceType advanceType2 = AdvanceType.create("test2", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurements(children.get(1), advanceType2,
                true,
                new BigDecimal(1000), two, new BigDecimal(100), three,
                new BigDecimal(350), four, new BigDecimal(400));

        assertThat(orderElement.getAdvancePercentage(), equalTo(new BigDecimal(
                43).divide(new BigDecimal(100)).setScale(2)));

        Set<DirectAdvanceAssignment> directAdvanceAssignments = orderElement
                .getDirectAdvanceAssignments();
        assertThat(directAdvanceAssignments.size(), equalTo(0));

        Set<IndirectAdvanceAssignment> indirectAdvanceAssignments = ((OrderLineGroup) orderElement)
                .getIndirectAdvanceAssignments();
        assertThat(indirectAdvanceAssignments.size(), equalTo(3));

        DirectAdvanceAssignment advanceAssignment = null;
        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (indirectAdvanceAssignment.getAdvanceType().getUnitName()
                    .equals(PredefinedAdvancedTypes.CHILDREN.getTypeName())) {
                advanceAssignment = ((OrderLineGroup) orderElement)
                        .calculateFakeDirectAdvanceAssigment(indirectAdvanceAssignment);
                break;
            }
        }
        assertThat(advanceAssignment.getMaxValue(),
                equalTo(new BigDecimal(100)));

        SortedSet<AdvanceMeasurement> advanceMeasurements = advanceAssignment
                .getAdvanceMeasurements();
        assertThat(advanceMeasurements.size(), equalTo(5));

        ArrayList<AdvanceMeasurement> list = new ArrayList<AdvanceMeasurement>(
                advanceMeasurements);
        Collections.sort(list, new AdvanceMeasurementComparator());
        Collections.reverse(list);
        Iterator<AdvanceMeasurement> iterator = list.iterator();

        AdvanceMeasurement next = iterator.next();
        assertThat(next.getDate(), equalTo(one));
        assertThat(next.getValue(), equalTo(new BigDecimal(6).setScale(2)));

        next = iterator.next();
        assertThat(next.getDate(), equalTo(two));
        assertThat(next.getValue(), equalTo(new BigDecimal(13).setScale(2)));

        next = iterator.next();
        assertThat(next.getDate(), equalTo(three));
        assertThat(next.getValue(), equalTo(new BigDecimal(36).setScale(2)));

        next = iterator.next();
        assertThat(next.getDate(), equalTo(four));
        assertThat(next.getValue(), equalTo(new BigDecimal(40).setScale(2)));

        next = iterator.next();
        assertThat(next.getDate(), equalTo(five));
        assertThat(next.getValue(), equalTo(new BigDecimal(43).setScale(2)));
    }

    @Test
    public void checkGetAdvancePercentageTwoLevelOfDepth1()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderLineGroup orderLineGroup_1 = OrderLineGroup.create();
        orderLineGroup_1.setName("OrderLineGroup 1");
        orderLineGroup_1.setCode("1");

        OrderLineGroup orderLineGroup_1_1 = OrderLineGroup.create();
        orderLineGroup_1_1.setName("OrderLineGroup 1.1");
        orderLineGroup_1_1.setCode("1.1");

        OrderLine orderLine_1_1_1 = givenOrderLine("OrderLine 1.1.1", "1.1.1",
                1000);

        orderLineGroup_1_1.add(orderLine_1_1_1);
        orderLineGroup_1.add(orderLineGroup_1_1);

        AdvanceType advanceType1 = AdvanceType.create("test1", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurement(orderLine_1_1_1, advanceType1,
                new BigDecimal(10), new BigDecimal(2), true);

        AdvanceType advanceType2 = AdvanceType.create("test2", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurement(orderLineGroup_1_1, advanceType2,
                new BigDecimal(100), new BigDecimal(50), true);

        assertThat(orderLineGroup_1.getDirectAdvanceAssignments().size(),
                equalTo(0));
        assertThat(orderLineGroup_1.getIndirectAdvanceAssignments().size(),
                equalTo(3));
        assertThat(orderLineGroup_1.getAdvancePercentage(),
                equalTo(new BigDecimal(50).setScale(2).divide(
                        new BigDecimal(100))));
    }

    @Test
    public void checkGetAdvancePercentageTwoLevelOfDepth2()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        OrderLineGroup orderLineGroup_1 = OrderLineGroup.create();
        orderLineGroup_1.setName("OrderLineGroup 1");
        orderLineGroup_1.setCode("1");

        OrderLineGroup orderLineGroup_1_1 = OrderLineGroup.create();
        orderLineGroup_1_1.setName("OrderLineGroup 1.1");
        orderLineGroup_1_1.setCode("1.1");

        OrderLine orderLine_1_1_1 = givenOrderLine("OrderLine 1.1.1", "1.1.1",
                1000);

        orderLineGroup_1_1.add(orderLine_1_1_1);
        orderLineGroup_1.add(orderLineGroup_1_1);

        AdvanceType advanceType1 = AdvanceType.create("test1", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurement(orderLine_1_1_1, advanceType1,
                new BigDecimal(10), new BigDecimal(2), true);

        AdvanceType advanceType2 = AdvanceType.create("test2", new BigDecimal(
                10000), true, new BigDecimal(1), true);
        addAvanceAssignmentWithMeasurement(orderLineGroup_1_1, advanceType2,
                new BigDecimal(100), new BigDecimal(50), false);

        assertThat(orderLineGroup_1.getDirectAdvanceAssignments().size(),
                equalTo(0));
        assertThat(orderLineGroup_1.getIndirectAdvanceAssignments().size(),
                equalTo(3));
        assertThat(orderLineGroup_1.getAdvancePercentage(),
                equalTo(new BigDecimal(20).setScale(2).divide(
                        new BigDecimal(100))));
    }

}
