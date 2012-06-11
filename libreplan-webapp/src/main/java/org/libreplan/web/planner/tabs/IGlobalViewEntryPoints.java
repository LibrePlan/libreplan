/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2011 WirelessGalicia, S.L.
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
package org.libreplan.web.planner.tabs;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.templates.entities.OrderTemplate;
import org.libreplan.web.common.entrypoints.EntryPoint;
import org.libreplan.web.common.entrypoints.EntryPoints;

/**
 * Entry points for {@link MultipleTabsPlannerController} <br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
@EntryPoints(page = "/planner/index.zul", registerAs = "globalView")
public interface IGlobalViewEntryPoints {

    @EntryPoint("company_scheduling")
    public void goToCompanyScheduling();

    @EntryPoint("company_load")
    public void goToCompanyLoad();

    @EntryPoint("limiting_resources")
    public void goToLimitingResources();

    @EntryPoint("orders_list")
    public void goToOrdersList();

    @EntryPoint("order")
    public void goToOrder(Order order);

    @EntryPoint({ "order", "orderElement" })
    public void goToOrderElementDetails(Order order, OrderElement orderElement);

    @EntryPoint("limiting_resources")
    void goToCompanyLimitingResources();

    @EntryPoint("order_details")
    void goToOrderDetails(Order order);

    @EntryPoint("order_load")
    void goToResourcesLoad(Order order);

    @EntryPoint("order_advanced_allocation")
    void goToAdvancedAllocation(Order order);

    @EntryPoint("create_order_from_template")
    void goToCreateotherOrderFromTemplate(OrderTemplate template);

    @EntryPoint({"order","task"})
    void goToAdvanceTask(Order order,TaskElement task);

}
