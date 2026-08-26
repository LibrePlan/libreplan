/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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
package org.libreplan.web.reports;

import java.util.function.Consumer;

import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.web.common.Util;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.ListitemRenderer;

/**
 * Shared {@link ListitemRenderer}s for the Label/Criterion/Order multi-select "assigned items"
 * listboxes that are near-identical (copy-pasted) across most of the reports/* controllers - each
 * had its own broken {@code <listitem self="@{each=...}">} template under this app's
 * AnnotateBinder shim.
 */
public final class ReportListitemRenderers {

    private ReportListitemRenderers() {
    }

    public static ListitemRenderer labelRenderer(Consumer<Label> onRemove) {
        return (item, data, index) -> {
            final Label label = (Label) data;
            item.setValue(label);

            item.appendChild(new Listcell(label.getType().getName()));
            item.appendChild(new Listcell(label.getName()));

            Listcell operations = new Listcell();
            operations.appendChild(Util.createRemoveButton(event -> onRemove.accept(label)));
            item.appendChild(operations);
        };
    }

    public static ListitemRenderer criterionRenderer(Consumer<Criterion> onRemove) {
        return (item, data, index) -> {
            final Criterion criterion = (Criterion) data;
            item.setValue(criterion);

            item.appendChild(new Listcell(criterion.getType().getName()));
            item.appendChild(new Listcell(criterion.getName()));

            Listcell operations = new Listcell();
            operations.appendChild(Util.createRemoveButton(event -> onRemove.accept(criterion)));
            item.appendChild(operations);
        };
    }

    public static ListitemRenderer orderRenderer(Consumer<Order> onRemove) {
        return (item, data, index) -> {
            final Order order = (Order) data;
            item.setValue(order);

            item.appendChild(new Listcell(order.getName()));
            item.appendChild(new Listcell(order.getCode()));
            item.appendChild(new Listcell(Util.formatDate(order.getInitDate())));

            Listcell operations = new Listcell();
            operations.appendChild(Util.createRemoveButton(event -> onRemove.accept(order)));
            item.appendChild(operations);
        };
    }

}
