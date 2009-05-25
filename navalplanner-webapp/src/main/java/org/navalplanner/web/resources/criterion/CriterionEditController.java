package org.navalplanner.web.resources.criterion;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.resources.entities.Criterion;
import org.zkoss.zk.ui.util.GenericForwardComposer;


public class CriterionEditController extends GenericForwardComposer {

    private final ICriterionsModel criterionsModel;

    public CriterionEditController(ICriterionsModel criterionsModel) {
        Validate.notNull(criterionsModel);
        this.criterionsModel = criterionsModel;
    }

    public Criterion getCriterion() {
        return criterionsModel.getCriterion();
    }

    public boolean isEditing() {
        return criterionsModel.isEditing();
    }
}
