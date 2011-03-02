/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.navalplanner.web.common.components.finders;

import java.util.List;

import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Bandbox finder for {@link OrderElement}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
public class OrderElementBandboxFinder extends BandboxFinder implements IBandboxFinder {

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IOrderDAO orderDAO;

    private final String headers[] = { _("Project"), _("Project code"),
            _("Task"), _("Task code") };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderElement> getAll() {
        List<OrderElement> orderElements = orderElementDAO.getAll();
        forLoadOrderElements(orderElements);
        return orderElements;
    }

    private void forLoadOrderElements(List<OrderElement> orderElements) {
        for (OrderElement orderElement : orderElements) {
            orderElement.getName();
            orderElement.getOrder().getName();
        }
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        OrderElement orderElement = (OrderElement) obj;
        text = text.trim().toLowerCase();
        return (orderElement.getCode().toLowerCase().contains(text)
                || orderElement.getName().toLowerCase().contains(text)
                || orderElement.getOrder().getCode().toLowerCase().contains(
                        text) || orderElement.getOrder().getName()
                .toLowerCase().contains(text));
    }

    @Override
    @Transactional(readOnly = true)
    public String objectToString(Object obj) {
        OrderElement orderElement = (OrderElement) obj;
        Order order = orderDAO.loadOrderAvoidingProxyFor(orderElement);
        return orderElement.getName() + " :: " + order.getName();
    }

    @Override
    public String[] getHeaders() {
        return headers.clone();
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return orderElementRenderer;
    }

    private final ListitemRenderer orderElementRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            OrderElement orderElement = (OrderElement) data;
            item.setValue(orderElement);

            Listcell orderCode = new Listcell();
            orderCode.setLabel(orderElement.getOrder().getCode());
            orderCode.setParent(item);

            Listcell orderName = new Listcell();
            orderName.setLabel(orderElement.getOrder().getName());
            orderName.setParent(item);

            Listcell orderElementCode = new Listcell();
            orderElementCode.setLabel(orderElement.getCode());
            orderElementCode.setParent(item);

            Listcell orderElementName = new Listcell();
            orderElementName.setLabel(orderElement.getName());
            orderElementName.setParent(item);
        }

    };

}
