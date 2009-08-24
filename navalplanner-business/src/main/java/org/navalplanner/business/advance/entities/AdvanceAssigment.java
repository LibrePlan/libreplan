package org.navalplanner.business.advance.entities;

import java.util.SortedSet;
import java.util.TreeSet;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.OrderElement;

public class AdvanceAssigment extends BaseEntity {

    public static AdvanceAssigment create() {
        AdvanceAssigment advanceAssigment = new AdvanceAssigment();
        advanceAssigment.setNewObject(true);
        return advanceAssigment;
    }

    public static AdvanceAssigment create(boolean reportGlobalAdvance) {
        AdvanceAssigment advanceAssigment = new AdvanceAssigment(reportGlobalAdvance);
        advanceAssigment.setNewObject(true);
        return advanceAssigment;
    }

    private boolean reportGlobalAdvance;

    private OrderElement orderElement;

    private AdvanceType advanceType;

    private SortedSet<AdvanceMeasurement> advanceMeasurements =
            new TreeSet<AdvanceMeasurement>(new AdvanceMeasurementComparator());

    public AdvanceAssigment() {
        this.reportGlobalAdvance = false;
    }

    private AdvanceAssigment(boolean reportGlobalAdvance) {
        this.reportGlobalAdvance = reportGlobalAdvance;
    }

    public void setReportGlobalAdvance(boolean reportGlobalAdvance) {
        this.reportGlobalAdvance = reportGlobalAdvance;
    }

    public boolean getReportGlobalAdvance() {
        return this.reportGlobalAdvance;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    public OrderElement getOrderElement() {
        return this.orderElement;
    }

    public void setAdvanceType(AdvanceType advanceType) {
        this.advanceType = advanceType;
    }

    public AdvanceType getAdvanceType() {
        return this.advanceType;
    }

    public void setAdvanceMeasurements(SortedSet<AdvanceMeasurement> advanceMeasurements) {
        this.advanceMeasurements = advanceMeasurements;
    }

    public SortedSet<AdvanceMeasurement> getAdvanceMeasurements() {
        return this.advanceMeasurements;
    }
}
