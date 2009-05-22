package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.List;

public enum LeaveCriterions {
    MEDICAL_LEAVE("medicalLeaveWorkingRelationship"), PATERNITY_LEAVE(
            "paternityLeaveWorkingRelationship");

    public static List<Criterion> getCriterions() {
        ArrayList<Criterion> result = new ArrayList<Criterion>();
        for (LeaveCriterions leaveCriterions : values()) {
            result.add(leaveCriterions.criterion());
        }
        return result;
    }

    private final String criterionName;

    private LeaveCriterions(String name) {
        this.criterionName = name;
    }

    public Criterion criterion() {
        return PredefinedCriterionTypes.LEAVE
                .createCriterion(criterionName);
    }

}