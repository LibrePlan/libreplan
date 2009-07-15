package org.navalplanner.business.workreports.entities;

import java.util.Set;

import org.navalplanner.business.resources.entities.CriterionType;

/**
 * @author Diego Pino García <dpino@igalia.com>
 */

public class WorkReportType {
	private Long id;

	@SuppressWarnings("unused")
	private long version;

	String name;

	Set<CriterionType> criterionTypes;

	public WorkReportType() {

	}

	public WorkReportType(String name, Set<CriterionType> criterionTypes) {
		this.name = name;
		this.criterionTypes = criterionTypes;
	}

	public Long getId() {
		return id;
	}

	public long getVersion() {
		return version;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<CriterionType> getCriterionTypes() {
        return criterionTypes;
    }

    public void setCriterionTypes(Set<CriterionType> criterionTypes) {
        this.criterionTypes = criterionTypes;
    }
}
