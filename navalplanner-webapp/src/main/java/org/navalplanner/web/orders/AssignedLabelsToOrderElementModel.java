package org.navalplanner.web.orders;

import org.navalplanner.business.orders.daos.OrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
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

    OrderElementDAO orderDAO;

    OrderElement orderElement;

    @Transactional(readOnly = true)
    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    private void reattachLabels() {

    }

}
