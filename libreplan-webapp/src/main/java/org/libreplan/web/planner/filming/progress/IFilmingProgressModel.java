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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.filmingprogress.entities.FilmingProgress;
import org.libreplan.business.filmingprogress.entities.FilmingProgressTypeEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.planner.order.ISaveCommand;
import org.libreplan.web.planner.order.ISaveCommand.IAfterSaveListener;

interface IFilmingProgressModel {

    void setCurrentOrder(Order order);

    Order getCurrentOrder();

    FilmingProgress getFirstFilmingProgress();

    void setSaveCommand(ISaveCommand saveCommand);

    void hookIntoSaveCommand(IAfterSaveListener afterSaveListener);

    List<ProgressValue> getProgressValues();

    void addNewUnitMeasure(FilmingProgressTypeEnum unitMeasure,
            BigDecimal maxValue);

    void removeFilmingProgress(FilmingProgress filmingProgress);

    void loadDataFromOrder();

    void dontPoseAsTransientObjectAnymore();

    void updateStartDate(LocalDate localDate);

    void updateEndDate(LocalDate localDate);

    void removeDays();

    Set<FilmingProgress> getFilmingProgressSet();

}
