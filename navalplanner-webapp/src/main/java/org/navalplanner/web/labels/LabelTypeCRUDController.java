package org.navalplanner.web.labels;

import java.util.List;

import org.navalplanner.business.labels.entities.LabelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Window;

/**
 * CRUD Controller for {@link LabelType}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class LabelTypeCRUDController extends GenericForwardComposer {

    Window listWindow;

    @Autowired
    ILabelTypeModel labelTypeModel;

    public LabelTypeCRUDController() {

    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
    }

    @Transactional
    public List<LabelType> getLabelTypes() {
        return labelTypeModel.getLabelTypes();
    }
}
