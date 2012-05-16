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

package org.libreplan.web.planner.filming.progress;

import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.libreplan.business.filmingprogress.entities.FilmingProgress;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to Filming Progress View
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FilmingProgressModel implements IFilmingProgressModel {

    @Autowired
    private IOrderDAO orderDAO;

    private Order currentOrder;

    private FilmingProgress currentFilming;

    @Override
    @Transactional
    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
        if (currentOrder != null) {
            orderDAO.reattach(currentOrder);
            this.currentFilming = this.currentOrder.getFilmingProgress();

            if (currentFilming == null) {
                this.currentFilming = FilmingProgress.create(order);
                order.setFilmingProgress(currentFilming);
            } else {
                loadDataFromFilmingProgess();
            }
        }
    }

    private void loadDataFromFilmingProgess() {
        this.currentFilming.getEndDate();
        loadDataInMaps(this.currentFilming.getInitialProgressForecast());
        loadDataInMaps(this.currentFilming.getProgressForecast());
        loadDataInMaps(this.currentFilming.getRealProgress());
    }

    private void loadDataInMaps(Map<LocalDate, Integer> map) {
        for (Entry<LocalDate, Integer> entry : map.entrySet()) {
            entry.getKey();
        }
    }

    @Override
    public Order getCurrentOrder() {
        return this.currentOrder;
    }

    @Override
    public FilmingProgress getCurrentFilmingProgress() {
        return this.currentFilming;
    }
}