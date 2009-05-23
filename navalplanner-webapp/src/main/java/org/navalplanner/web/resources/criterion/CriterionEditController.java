package org.navalplanner.web.resources.criterion;

import org.apache.commons.lang.Validate;
import org.zkoss.zk.ui.util.GenericForwardComposer;

public class CriterionEditController extends GenericForwardComposer {

    private final ICriterionsModel criterionsModel;

    public CriterionEditController(ICriterionsModel criterionsModel) {
        Validate.notNull(criterionsModel);
        this.criterionsModel = criterionsModel;
    }

    public void setCriterionName(String name) {
        criterionsModel.setNameForCriterion(name);
    }

    public String getCriterionName() {
        return criterionsModel.getNameForCriterion();
    }

    public boolean isEditing() {
        return criterionsModel.isEditing();
    }

    public boolean isCriterionActive() {
        return criterionsModel.isCriterionActive();
    }

    public void setCriterionActive(boolean active) {
        criterionsModel.setCriterionActive(active);
    }


}
