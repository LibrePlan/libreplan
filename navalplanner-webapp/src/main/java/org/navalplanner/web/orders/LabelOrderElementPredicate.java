package org.navalplanner.web.orders;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.OrderElement;

/**
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
    public boolean complays(Object object) {
        final OrderElement orderElement = (OrderElement) object;
        for (Label label : orderElement.getLabels()) {
            if (this.label != null && equalsLabel(label)) {
                return true;
            }
        }
        return false;
    }

    private boolean equalsLabel(Label label) {
        return (this.label.getName().equals(label.getName()) && this.label
                .getType().getName().equals(label.getType().getName()));
    }

    @Override
    public boolean isEmpty() {
        return (label == null);
    }

}
