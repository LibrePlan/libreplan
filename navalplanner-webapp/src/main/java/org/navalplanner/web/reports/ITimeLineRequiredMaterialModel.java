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

package org.navalplanner.web.reports;

import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.MaterialStatusEnum;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.reports.dtos.TimeLineRequiredMaterialDTO;
import org.zkoss.zul.TreeModel;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface ITimeLineRequiredMaterialModel {

    JRDataSource getTimeLineRequiredMaterial(Date startingDate,
            Date endingDate, MaterialStatusEnum lbStatus,
            List<Order> listOrders, List<MaterialCategory> categories,
            List<Material> materials);

    void init();

    List<Order> getOrders();

    List<TimeLineRequiredMaterialDTO> filterConsult(Date startingDate,
            Date endingDate,
 MaterialStatusEnum status, List<Order> listOrders,
            List<MaterialCategory> categories, List<Material> materials);

    List<TimeLineRequiredMaterialDTO> sort(
            List<TimeLineRequiredMaterialDTO> list);

    TreeModel getAllMaterialCategories();

    void removeSelectedOrder(Order order);

    boolean addSelectedOrder(Order order);

    List<Order> getSelectedOrders();

}
