package org.navalplanner.business.workreports.entities;

import java.util.Set;

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;

/**
 * @author Diego Pino García <dpino@igalia.com>
 */
public class WorkReportLine {

	private Long id;

	@SuppressWarnings("unused")
	private long version;

	Integer numHours;

	Set<Resource> resources;

	Set<OrderElement> orderElements;

	Set<Criterion> criterions;

	public WorkReportLine() {

	}

	public WorkReportLine(Integer numHours, Set<Resource> resources,
	        Set<OrderElement> orderElements, Set<Criterion> criterions) {
		this.numHours = numHours;
		this.resources = resources;
		this.orderElements = orderElements;
		this.criterions = criterions;
	}

    public Integer getNumHours() {
        return numHours;
    }

    public void setNumHours(Integer numHours) {
        this.numHours = numHours;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    public Set<OrderElement> getOrderElements() {
        return orderElements;
    }

    public void setOrderElements(Set<OrderElement> orderElements) {
        this.orderElements = orderElements;
    }

    public Set<Criterion> getCriterions() {
        return criterions;
    }

    public void setCriterions(Set<Criterion> criterions) {
        this.criterions = criterions;
    }
}
