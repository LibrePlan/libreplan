package org.navalplanner.web.orders;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Controller for showing OrderElement assigned labels
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class AssignedLabelsToOrderElementController extends
        GenericForwardComposer {

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp.getFellow("listOrderElementLabels"));
        comp.setVariable("assignedLabelsController", this, true);
    }

}
