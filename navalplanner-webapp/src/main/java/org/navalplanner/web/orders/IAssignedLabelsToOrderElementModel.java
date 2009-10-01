package org.navalplanner.web.orders;

import java.util.List;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IAssignedLabelsToOrderElementModel {

    /**
     *
     * @param labelName
     * @param labelType
     * @return
     */
    boolean existsLabelByNameAndType(String labelName, LabelType labelType);

    /**
     *
     * @param labelName
     * @param labelType
     */
    void addLabel(String labelName, LabelType labelType);

    /**
     *
     * @return
     */
    OrderElement getOrderElement();

    /**
     * Set {@link OrderElement}
     *
     * @param orderElement
     */
    void setOrderElement(OrderElement orderElement);

    /**
     * Returns {@link OrderElement}
     *
     * @return
     */
    public List<Label> getLabels();

    /**
     *
     * @param label
     */
    void deleteLabel(Label label);

}
