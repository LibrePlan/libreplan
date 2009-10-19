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

package org.navalplanner.web.orders;

import java.util.List;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 * Checks if {@link Label} from {@link OrderElement} matches attribute label
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class LabelOrderElementPredicate implements IPredicate {

    Label label;

    public LabelOrderElementPredicate(Label label) {
        this.label = label;
    }

    @Override
    public boolean accepts(Object object) {
        final OrderElement orderElement = (OrderElement) object;
        return accepts(orderElement) || accepts(orderElement.getChildren());
    }

    /**
     * Returns true if at least there's one {@link Label} in
     * {@link OrderElement} that holds predicate or orderElement is a new object
     *
     * @param orderElement
     * @return
     */
    private boolean accepts(OrderElement orderElement) {
        if (orderElement.isNewObject()) {
            return true;
        }
        for (Label label : orderElement.getLabels()) {
            if (this.label != null && equalsLabel(label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if there's at least on element in orderElements which any of
     * its labels holds predicate
     *
     * @param orderElements
     * @return
     */
    private boolean accepts(List<OrderElement> orderElements) {
        boolean result = false;
        for (OrderElement orderElement : orderElements) {
            result |= accepts(orderElement);
        }
        return result;
    }

    private boolean equalsLabel(Label label) {
        return (this.label.getName().equals(label.getName()) && this.label
                .getType().getName().equals(label.getType().getName()));
    }

}
