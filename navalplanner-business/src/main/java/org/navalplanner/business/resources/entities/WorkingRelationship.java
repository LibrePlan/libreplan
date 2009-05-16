package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.List;

public enum WorkingRelationship {
    HIRED("hiredResourceWorkingRelationship"), FIRED(
            "firedResourceWorkingRelationship");

    public static List<Criterion> getCriterions() {
        ArrayList<Criterion> result = new ArrayList<Criterion>();
        for (WorkingRelationship workingRelationship : values()) {
            result.add(workingRelationship.criterion());
        }
        return result;
    }

    private final String criterionName;

    private WorkingRelationship(String name) {
        this.criterionName = name;
    }

    public Criterion criterion() {
        return PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(criterionName);
    }


}
