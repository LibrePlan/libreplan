package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Predefined working relationships<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public enum WorkingRelationship {
    HIRED("hiredResourceWorkingRelationship"),
    FIRED("firedResourceWorkingRelationship");

    public static List<String> getCriterionNames() {
        ArrayList<String> result = new ArrayList<String>();
        for (WorkingRelationship workingRelationship : values()) {
            result.add(workingRelationship.criterionName);
        }
        return result;
    }

    private final String criterionName;

    public Criterion criterion() {
        return new Criterion(criterionName,
            CriterionType.asCriterionType(PredefinedCriterionTypes.WORK_RELATIONSHIP));
    }

    public String getCriterionName() {
        return criterionName;
    }

    private WorkingRelationship(String name) {
        this.criterionName = name;
    }
}
