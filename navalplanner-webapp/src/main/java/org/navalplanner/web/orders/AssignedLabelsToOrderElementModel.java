package org.navalplanner.web.orders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLineGroup;
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

    @Autowired
    ILabelTypeDAO labelTypeDAO;

    @Autowired
    ILabelDAO labelDAO;

    OrderElement orderElement;

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @Override
    @Transactional(readOnly = true)
    public void init(OrderElement orderElement) {
        this.orderElement = orderElement;
        reattachOrderElement(this.orderElement);
    }

    private void reattachOrderElement(OrderElement orderElement) {
        orderDAO.save(orderElement);
        orderElement.getName();
        if (orderElement.getParent() != null) {
            orderElement.getParent().getName();
        }
        reattachLabels(orderElement.getLabels());
    }

    private void reattachLabels(Collection<Label> labels) {
        for (Label label : labels) {
            reattachLabel(label);
        }
    }

    public void reattachLabel(Label label) {
        label.getName();
        label.getType().getName();
    }

    @Transactional(readOnly = true)
    public List<Label> getLabels() {
        List<Label> result = new ArrayList<Label>();
        if (orderElement != null && orderElement.getLabels() != null) {
            result.addAll(orderElement.getLabels());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Label> getInheritedLabels() {
        List<Label> result = new ArrayList<Label>();

        if (orderElement != null) {
            OrderLineGroup parent = orderElement.getParent();
            while (parent != null) {
                reattachOrderElement(parent);
                result.addAll(parent.getLabels());
                parent = parent.getParent();
            }
        }
        return result;
    }

    public Label createLabel(String labelName, LabelType labelType) {
        Label label = Label.create(labelName);
        label.setType(labelType);
        return label;
    }

    public void assignLabel(Label label) {
        orderElement.addLabel(label);
    }

    @Override
    public void deleteLabel(Label label) {
        orderElement.removeLabel(label);
    }

    @Override
    @Transactional(readOnly = true)
    public Label findLabelByNameAndType(String labelName, LabelType labelType) {
        final Label label = labelDAO.findByNameAndType(labelName, labelType);
        if (label != null) {
            reattachLabel(label);
        }
        return label;
    }

    @Override
    public boolean isAssigned(Label label) {
        final Set<Label> labels = orderElement.getLabels();

        for (Label element : labels) {
            if (element.getId().equals(label.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Label> getAllLabels() {
        final List<Label> labels = labelDAO.getAll();
        reattachLabels(labels);
        return labels;
    }
}
