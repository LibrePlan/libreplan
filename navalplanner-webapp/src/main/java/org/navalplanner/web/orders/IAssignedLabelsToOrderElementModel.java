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
     * Assigns {@link Label} to {@link OrderElement}
     *
     * @param label
     */
    void assignLabel(Label label);

    /**
     * Undo changes
     */
    void cancel();

    /**
     * Creates new {@link Label}
     *
     * @param labelName
     * @param labelType
     * @return
     */
    Label createLabel(String labelName, LabelType labelType);

    /**
     * Delete {@link Label}
     *
     * @param label
     */
    void deleteLabel(Label label);

    /**
     * Returns {@link Label} by name and type
     *
     * @param labelName
     * @param labelType
     * @return
     */
    Label findLabelByNameAndType(String labelName, LabelType labelType);

    /**
     * Gets all {@link Label} from any {@link LabelType}
     *
     * @return
     */
    List<Label> getAllLabels();

    /**
     * Returns all {@link Label} from {@link OrderElement} ancestors
     *
     * @return
     */
    List<Label> getInheritedLabels();

    /**
     * Returns {@link OrderElement}
     *
     * @return
     */
    public List<Label> getLabels();

    /**
     *
     * @return
     */
    OrderElement getOrderElement();

    void init(OrderElement orderElement);

    /**
     * Check whether {@link Label} has been already assigned to
     * {@link OrderElement} or not
     *
     * @param label
     */
    boolean isAssigned(Label label);

    /**
     * Set {@link OrderElement}
     *
     * @param orderElement
     */
    void setOrderElement(OrderElement orderElement);

    /**
     * Save {@link OrderElement}
     *
     */
    void confirm();

}
