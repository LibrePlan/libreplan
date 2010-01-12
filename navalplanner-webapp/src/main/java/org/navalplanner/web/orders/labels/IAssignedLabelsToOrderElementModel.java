/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.orders.labels;

import java.util.List;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.web.orders.IOrderModel;

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

    void init(OrderElement orderElement);

    /**
     * Check whether {@link Label} has been already assigned to
     * {@link OrderElement} or not
     *
     * @param label
     */
    boolean isAssigned(Label label);

    /**
     *
     * @param orderModel
     */
    void setOrderModel(IOrderModel orderModel);

}
