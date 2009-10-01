package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.Autocomplete;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Textbox;

/**
 * Controller for showing OrderElement assigned labels
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class AssignedLabelsToOrderElementController extends
        GenericForwardComposer {

    private IAssignedLabelsToOrderElementModel assignedLabelsToOrderElementModel;

    private Autocomplete cbLabelType;

    private Grid directLabels;

    private Textbox txtLabelName;

    public OrderElement getOrderElement() {
        return assignedLabelsToOrderElementModel.getOrderElement();
    }

    public void setOrderElement(OrderElement orderElement) {
        assignedLabelsToOrderElementModel.setOrderElement(orderElement);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp.getFellow("listOrderElementLabels"));
        comp.setVariable("assignedLabelsController", this, true);
    }

    public void createAndAssign() {

        // Check LabelType is not null
        final Comboitem comboitem = cbLabelType.getSelectedItem();
        if (comboitem == null || comboitem.getValue() == null) {
            throw new WrongValueException(cbLabelType, _("cannot be null"));
        }

        final String labelName = txtLabelName.getValue();
        final LabelType labelType = (LabelType) comboitem.getValue();

        // Label does not exist, create
        Label label = assignedLabelsToOrderElementModel
                .findLabelByNameAndType(labelName, labelType);
        if (label == null) {
            label = createLabel(labelName, labelType);
        } else {
            // Label is already assigned?
            if (isAssigned(label)) {
                throw new WrongValueException(txtLabelName,
                        _("already assigned"));
            }
        }

        // Assign label
        assignLabel(label);
    }

    private boolean isAssigned(Label label) {
        return assignedLabelsToOrderElementModel.isAssigned(label);
    }

    private void assignLabel(Label label) {
        assignedLabelsToOrderElementModel.assignLabel(label);
        Util.reloadBindings(directLabels);
    }

    private Label createLabel(String labelName, LabelType labelType) {
        return assignedLabelsToOrderElementModel.createLabel(labelName,
                labelType);
    }

    public List<Label> getLabels() {
        return assignedLabelsToOrderElementModel.getLabels();
    }

    public List<Label> getInheritedLabels() {
        return assignedLabelsToOrderElementModel.getInheritedLabels();
    }

    public void deleteLabel(Label label) {
        assignedLabelsToOrderElementModel.deleteLabel(label);
        Util.reloadBindings(directLabels);
    }

    public void openWindow(IOrderElementModel orderElementModel) {
        assignedLabelsToOrderElementModel.init(orderElementModel
                .getOrderElement());
        Util.reloadBindings(self);
    }

}
