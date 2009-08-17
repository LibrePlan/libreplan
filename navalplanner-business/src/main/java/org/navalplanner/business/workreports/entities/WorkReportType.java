package org.navalplanner.business.workreports.entities;

import java.util.HashSet;
import java.util.Set;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.CriterionType;

/**
 * @author Diego Pino García <dpino@igalia.com>
 */

public class WorkReportType extends BaseEntity {

    public static WorkReportType create() {
        WorkReportType workReportType = new WorkReportType();
        workReportType.setNewObject(true);
        return workReportType;
    }

    public static WorkReportType create(String name,
            Set<CriterionType> criterionTypes) {
        WorkReportType workReportType = new WorkReportType(name, criterionTypes);
        workReportType.setNewObject(true);
        return workReportType;
    }

    private String name;

    private Set<CriterionType> criterionTypes = new HashSet<CriterionType>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public WorkReportType() {

    }

    private WorkReportType(String name, Set<CriterionType> criterionTypes) {
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
        return new HashSet<CriterionType>(criterionTypes);
    }

    public void setCriterionTypes(Set<CriterionType> criterionTypes) {
        this.criterionTypes = criterionTypes;
    }
}
