/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
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

package org.navalplanner.web.common.components.finders;

import java.util.List;

import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Bandbox finder for {@link Order}.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
public class OrderBandboxFinder extends BandboxFinder implements IBandboxFinder {

    @Autowired
    private IOrderDAO orderDAO;

    private final String headers[] = { _("Project code"), _("Project name") };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAll() {
        List<Order> orders = orderDAO.findAll();
        return orders;
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        Order order = (Order) obj;
        text = text.trim().toLowerCase();
        return (order.getCode().toLowerCase().contains(text) || order.getName()
                .toLowerCase().contains(text));
    }

    @Override
    @Transactional(readOnly = true)
    public String objectToString(Object obj) {
        Order order = (Order) obj;
        return order.getCode() + " :: " + order.getName();
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
            Order order = (Order) data;
            item.setValue(order);

            Listcell orderCode = new Listcell();
            orderCode.setLabel(order.getCode());
            orderCode.setParent(item);

            Listcell orderName = new Listcell();
            orderName.setLabel(order.getName());
            orderName.setParent(item);
        }

    };

}
