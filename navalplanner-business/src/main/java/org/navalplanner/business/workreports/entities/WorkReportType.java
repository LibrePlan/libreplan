package org.navalplanner.business.workreports.entities;

import java.util.Set;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.CriterionType;

/**
 * @author Diego Pino García <dpino@igalia.com>
 */

public class WorkReportType extends BaseEntity {

    String name;

    Set<CriterionType> criterionTypes;

    public WorkReportType() {

    }

    public WorkReportType(String name, Set<CriterionType> criterionTypes) {
        this.name = name;
        this.criterionTypes = criterionTypes;
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
