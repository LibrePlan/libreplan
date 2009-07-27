package org.navalplanner.business.workreports.entities;

import java.util.Set;

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;

/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkReportLine {

    private Long id;

    @SuppressWarnings("unused")
    private long version;

    Integer numHours;

    Resource resource;

    OrderElement orderElement;

    WorkReport workReport;

    Set<Criterion> criterions;

    public WorkReportLine() {

    }

    public WorkReportLine(Integer numHours, Resource resource,
            OrderElement orderElement, Set<Criterion> criterions) {
        this.numHours = numHours;
        this.resource = resource;
        this.orderElement = orderElement;
        this.criterions = criterions;
    }

    public Long getId() {
        return id;
    }

    public long getVersion() {
        return version;
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
        return criterions;
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
