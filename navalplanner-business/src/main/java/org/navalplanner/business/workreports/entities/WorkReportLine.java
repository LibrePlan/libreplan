package org.navalplanner.business.workreports.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;

/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkReportLine extends BaseEntity {

    public static final String RESOURCE = "resource";

    public static final String ORDER_ELEMENT = "orderElement";

    public static WorkReportLine create() {
        WorkReportLine workReportLine = new WorkReportLine();
        workReportLine.setNewObject(true);
        return workReportLine;
    }

    public static WorkReportLine create(Integer numHours, Resource resource,
            OrderElement orderElement, Set<Criterion> criterions) {
        WorkReportLine workReportLine = new WorkReportLine(numHours, resource,
                orderElement, criterions);
        workReportLine.setNewObject(true);
        return workReportLine;
    }

    private Integer numHours;

    @NotNull
    private Resource resource;

    @NotNull
    private OrderElement orderElement;

    private WorkReport workReport;

    private Set<Criterion> criterions = new HashSet<Criterion>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public WorkReportLine() {

    }

    private WorkReportLine(Integer numHours, Resource resource,
            OrderElement orderElement, Set<Criterion> criterions) {
        this.numHours = numHours;
        this.resource = resource;
        this.orderElement = orderElement;
        this.criterions = criterions;
    }

    public Integer getNumHours() {
        return numHours;
    }

    public void setNumHours(Integer numHours) {
        this.numHours = numHours;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    public Set<Criterion> getCriterions() {
        return new HashSet<Criterion>(criterions);
    }

    public void setCriterions(Set<Criterion> criterions) {
        this.criterions = criterions;
    }

    public WorkReport getWorkReport() {
        return workReport;
    }

    public void setWorkReport(WorkReport workReport) {
        this.workReport = workReport;
    }
}
