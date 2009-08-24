package org.navalplanner.business.advance.entities;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Map;
import java.util.Set;
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

    //private Set<AdvanceMeasurement> advanceMeasurements = new HashSet<AdvanceMeasurement>();
    //private Map<AdvanceMeasurement,> advanceMeasurements = new HashMap<AdvanceMeasurement,>();
    private SortedSet<AdvanceMeasurement> advanceMeasurements = new TreeSet<AdvanceMeasurement>();

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

   /* public void setAdvanceMeasurements(
            List<AdvanceMeasurement> advanceMeasurements) {
        this.advanceMeasurements = advanceMeasurements;
    }

    public List<AdvanceMeasurement> getAdvanceMeasurements() {
        return this.advanceMeasurements;
    }*/

    public void setAdvanceMeasurements(SortedSet<AdvanceMeasurement> advanceMeasurements) {
        this.advanceMeasurements = advanceMeasurements;
    }

    public SortedSet<AdvanceMeasurement> getAdvanceMeasurements() {
        return this.advanceMeasurements;
    }
}
