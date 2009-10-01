package org.navalplanner.web.orders;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedLabelsToOrderElementModel implements
        IAssignedLabelsToOrderElementModel {

    @Autowired
    IOrderElementDAO orderDAO;

    OrderElement orderElement;

    @Autowired
    ILabelTypeDAO labelTypeDAO;

    @Autowired
    ILabelDAO labelDAO;

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    private void reattachLabels() {

    }

    public List<Label> getLabels() {
        List<Label> result = new ArrayList<Label>();
        if (orderElement.getLabels() != null) {
            result.addAll(orderElement.getLabels());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public boolean existsLabelByNameAndType(String labelName,
            LabelType labelType) {
        return (labelDAO.findByNameAndType(labelName, labelType) != null);
    }

    public void addLabel(String labelName, LabelType labelType) {
        Label label = Label.create(labelName);
        label.setType(labelType);
        orderElement.addLabel(label);
    }
}
