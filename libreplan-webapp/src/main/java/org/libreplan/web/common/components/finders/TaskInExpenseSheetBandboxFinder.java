/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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

package org.libreplan.web.common.components.finders;

import java.util.List;

import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Bandbox finder for {@link OrderElement} in ExpenseSheet.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
public class TaskInExpenseSheetBandboxFinder extends BandboxFinder implements IBandboxFinder {

    @Autowired
    private IOrderElementDAO orderElementDAO;

    private final String headers[] = { _("Task name (Task code)"), _("Project name (Project code)") };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderElement> getAll() {
        List<OrderElement> orders = orderElementDAO.findAll();
        return orders;
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        OrderElement order = (OrderElement) obj;
        text = text.trim().toLowerCase();
        return (order.getCode().toLowerCase().contains(text) || order.getName().toLowerCase()
                .contains(text));
    }

    @Override
    @Transactional(readOnly = true)
    public String objectToString(Object obj) {
        OrderElement order = (OrderElement) obj;
        return order.getName() + " :: " + order.getCode();
    }

    @Override
    public String[] getHeaders() {
        return headers.clone();
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return orderRenderer;
    }

    private final ListitemRenderer orderRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) {
            OrderElement orderElement = (OrderElement) data;
            item.setValue(orderElement);

            Order order = (Order) orderElement.getOrder();

            Listcell infoTask = new Listcell();
            infoTask.setLabel(orderElement.getName() + " (" + orderElement.getCode() + ")");
            infoTask.setParent(item);

            Listcell infoProject = new Listcell();
            infoProject.setLabel(order.getName() + " (" + order.getCode() + ")");
            infoProject.setParent(item);

        }
    };

}