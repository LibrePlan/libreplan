package org.navalplanner.web.resources.criterion;

import java.util.List;

import org.navalplanner.business.resources.entities.Worker;
import org.zkoss.zk.ui.util.GenericForwardComposer;

public class CriterionWorkersController extends GenericForwardComposer {

    private final ICriterionsModel criterionsModel;

    public CriterionWorkersController(ICriterionsModel criterionsModel) {
        this.criterionsModel = criterionsModel;
    }

    public List<Worker> getWorkersForCurrentCriterion() {
        return criterionsModel
                .getResourcesSatisfyingCurrentCriterionOfType(Worker.class);
    }

}
