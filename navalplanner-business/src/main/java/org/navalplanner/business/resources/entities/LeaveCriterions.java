package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Predefined leave criterions<br />
 * @author Lorenzo Tilve <ltilve@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public enum LeaveCriterions {
    MEDICAL_LEAVE("medicalLeave"),
    PATERNITY_LEAVE("paternityLeave");

    public static List<String> getCriterionNames() {
        ArrayList<String> result = new ArrayList<String>();
        for (LeaveCriterions leaveCriterions: values()) {
            result.add(leaveCriterions.criterionName);
        }
        return result;
    }

    private final String criterionName;

    public Criterion criterion() {
        return Criterion.create(criterionName, CriterionType.asCriterionType(PredefinedCriterionTypes.LEAVE));
    }

    private LeaveCriterions(String name) {
        this.criterionName = name;
    }
}
