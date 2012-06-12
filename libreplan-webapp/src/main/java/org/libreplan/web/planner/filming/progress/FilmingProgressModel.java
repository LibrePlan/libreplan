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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.filmingprogress.entities.FilmingProgress;
import org.libreplan.business.filmingprogress.entities.FilmingProgressTypeEnum;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.planner.order.ISaveCommand;
import org.libreplan.web.planner.order.ISaveCommand.IAfterSaveListener;
import org.libreplan.web.planner.order.ISaveCommand.IBeforeSaveListener;
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

    private ISaveCommand saveCommand;

    List<ProgressValue> progressValues = new ArrayList<ProgressValue>();

    @Override
    @Transactional
    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
        if (currentOrder != null) {
            orderDAO.reattach(currentOrder);
            progressValues = new ArrayList<ProgressValue>();
            if (currentOrder.getFilmingProgressSet() != null
                    && !currentOrder.getFilmingProgressSet().isEmpty()) {
                loadDataFromFilmingProgressSet();
            }
        }
    }

    private void loadDataFromFilmingProgressSet() {
        if (this.currentOrder.getFilmingProgressSet() != null) {
            Set<FilmingProgress> filmingProgressSet = this.currentOrder
                    .getFilmingProgressSet();
            for (FilmingProgress filmingProgress : filmingProgressSet) {
                loadDataFromFilmingProgress(filmingProgress);
            }
        }
    }

    private void loadDataFromFilmingProgress(FilmingProgress filmingProgress) {
        filmingProgress.getEndDate();
        loadMaps(filmingProgress);

        if (filmingProgress.getInitialProgressForecast() != null) {
            progressValues.add(new ProgressValue(filmingProgress,
                    ForecastLevelEnum.INITIAL_FORECAST, filmingProgress
                            .getType(), filmingProgress
                            .getInitialProgressForecast()));

            if (filmingProgress.getRealProgress() == null) {
                filmingProgress.setRealProgress(createIntoIntervalWithValue(
                        filmingProgress.getStartDate(),
                        filmingProgress.getEndDate(), null));
            }

        }
        if (filmingProgress.getProgressForecast() != null) {
            progressValues.add(new ProgressValue(filmingProgress, ForecastLevelEnum.FORECAST,
                    filmingProgress.getType(), filmingProgress
                            .getProgressForecast()));
        }
        if (filmingProgress.getRealProgress() != null) {
            progressValues.add(new ProgressValue(filmingProgress, ForecastLevelEnum.REAL,
                    filmingProgress.getType(), filmingProgress
                            .getRealProgress()));
        }
    }

    private void loadMaps(FilmingProgress filmingProgress) {
        loadEmptyValues(filmingProgress.getInitialProgressForecast(),
                filmingProgress.getStartDate(), filmingProgress.getEndDate());
        loadEmptyValues(filmingProgress.getProgressForecast(),
                filmingProgress.getStartDate(), filmingProgress.getEndDate());
        loadEmptyValues(filmingProgress.getRealProgress(),
                filmingProgress.getStartDate(), filmingProgress.getEndDate());
    }

    private void loadEmptyValues(SortedMap<LocalDate, BigDecimal> map,
            LocalDate startDate, LocalDate endDate) {
        if (map != null && !map.values().isEmpty()) {
            Validate.notNull(startDate);
            Validate.notNull(endDate);
            while (startDate.compareTo(endDate) <= 0) {
                if (map.get(startDate) == null) {
                    map.put(startDate, null);
                }
                startDate = startDate.plusDays(1);
            }
        }
    }

    @Override
    public Order getCurrentOrder() {
        return this.currentOrder;
    }

    @Override
    public FilmingProgress getCurrentFilmingProgress() {
        return null;
    }

    public void setSaveCommand(ISaveCommand saveCommand) {
        this.saveCommand = saveCommand;
    }

    private ISaveCommand getSaveCommand() {
        return this.saveCommand;
    }

    public void hookIntoSaveCommand(IAfterSaveListener afterSaveListener) {
        if (getSaveCommand() != null) {
            IBeforeSaveListener beforeSaveListener = new IBeforeSaveListener() {
                @Override
                public void onBeforeSave() {
                    createMapsProgressForecast();
                }
            };
            getSaveCommand().addListener(beforeSaveListener);
            getSaveCommand().addListener(afterSaveListener);
        }
    }

    public List<ProgressValue> getProgressValues() {
        return progressValues;
    }

    @Override
    public void addNewUnitMeasure(FilmingProgressTypeEnum unitMeasure,
            BigDecimal maxValue) {
        buildProgressValueBy(unitMeasure, maxValue);
    }

    private void buildProgressValueBy(FilmingProgressTypeEnum type,
            BigDecimal maxValue) {
        // create a new filming progress
        FilmingProgress filmingProgress = FilmingProgress.create(currentOrder,
                type);
        this.currentOrder.getFilmingProgressSet().add(filmingProgress);

        // init the initial progress forecast with the max value
        createInitialProgressForecastBy(filmingProgress, maxValue);

        // init the real progress with value zero
        filmingProgress.setRealProgress(createIntoIntervalWithValue(
                filmingProgress.getStartDate(), filmingProgress.getEndDate(),
                null));

        // add the maps in the progress values list
        progressValues.add(new ProgressValue(filmingProgress,
                ForecastLevelEnum.INITIAL_FORECAST, type, filmingProgress
                        .getInitialProgressForecast()));
        progressValues
                .add(new ProgressValue(filmingProgress, ForecastLevelEnum.REAL,
                        type, filmingProgress.getRealProgress()));
    }

    private SortedMap<LocalDate, BigDecimal> createIntoIntervalWithValue(
            LocalDate startDate, LocalDate endDate, BigDecimal value) {
        SortedMap<LocalDate, BigDecimal> map = new TreeMap<LocalDate, BigDecimal>();
        Validate.notNull(startDate);
        Validate.notNull(endDate);
        while (startDate.compareTo(endDate) <= 0) {
            map.put(startDate, value);
            startDate = startDate.plusDays(1);
        }
        return map;
    }

    private void createMapsProgressForecast() {
        for (FilmingProgress filmingProgress : this.currentOrder
                .getFilmingProgressSet()) {
            if (filmingProgress.getProgressForecast() == null) {
                filmingProgress
                        .setProgressForecast(createMapProgressForecast(filmingProgress));
            }
        }
    }

    private SortedMap<LocalDate, BigDecimal> createMapProgressForecast(
            FilmingProgress filmingProgress) {
        if (hasValues(filmingProgress.getRealProgress())) {
            SortedMap<LocalDate, BigDecimal> toMap = new TreeMap<LocalDate, BigDecimal>();
            LocalDate today = new LocalDate();

            initMapIntoIntervalWithValue_(toMap,
                    filmingProgress.getStartDate(), today,
                    filmingProgress.getRealProgress());
            initMapIntoIntervalWithValue_(toMap, today.plusDays(1),
                    filmingProgress.getEndDate(),
                    filmingProgress.getInitialProgressForecast());
            return toMap;
        }
        return null;
    }

    private boolean hasValues(SortedMap<LocalDate, BigDecimal> realProgress) {
        if (realProgress != null) {
            for (BigDecimal value : realProgress.values()) {
                if (value != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private SortedMap<LocalDate, BigDecimal> initMapIntoIntervalWithValue_(
            SortedMap<LocalDate, BigDecimal> toMap, LocalDate startDate,
            LocalDate endDate, SortedMap<LocalDate, BigDecimal> fromMap) {
        Validate.notNull(startDate);
        Validate.notNull(endDate);
        while (startDate.compareTo(endDate) <= 0) {
            toMap.put(startDate, fromMap.get(startDate));
            startDate = startDate.plusDays(1);
        }
        return toMap;
    }

    private void createInitialProgressForecastBy(
            FilmingProgress filmingProgress, BigDecimal maxValue) {
        SortedMap<LocalDate, BigDecimal> map = new TreeMap<LocalDate, BigDecimal>();
        if (this.getCurrentOrder() != null) {
            createIntoInterval(map, filmingProgress.getStartDate(),
                    filmingProgress.getEndDate(), maxValue);
        }
        filmingProgress.setInitialProgressForecast(map);
    }

    private SortedMap<LocalDate, BigDecimal> createIntoInterval(
            SortedMap<LocalDate, BigDecimal> map, LocalDate initDate,
            LocalDate finishDate, BigDecimal maxValue) {
        Validate.notNull(initDate);
        Validate.notNull(finishDate);

        LocalDate date = initDate;
        int days = Days.daysBetween(date, finishDate).getDays() + 1;
        BigDecimal value = BigDecimal.ZERO.setScale(2);
        BigDecimal total = BigDecimal.ZERO.setScale(2);

        if (days > 0) {
            value = maxValue.divide(new BigDecimal(days), 2,
                    RoundingMode.HALF_DOWN);
        } else {
            return map;
        }

        for (int i = 0; i < days; i++) {
            map.put(date, value);
            total = total.add(value);
            date = date.plusDays(1);
        }
        BigDecimal rest = maxValue.subtract(total);
        map.put(finishDate, map.get(finishDate).add(rest));
        return map;
    }

}

enum ForecastLevelEnum {
    INITIAL_FORECAST, FORECAST, REAL;
}

class ProgressValue {

    ForecastLevelEnum forecastLevel;

    FilmingProgressTypeEnum progressType;

    private FilmingProgress filmingProgress;

    private SortedMap<LocalDate, BigDecimal> values = new TreeMap<LocalDate, BigDecimal>();

    ProgressValue() {

    }

    public ProgressValue(FilmingProgress filmingProgress,
            ForecastLevelEnum forecastLevel,
            FilmingProgressTypeEnum type,
            SortedMap<LocalDate, BigDecimal> values) {
        this.forecastLevel = forecastLevel;
        this.progressType = type;
        this.values = values;
        this.filmingProgress = filmingProgress;
    }

    public void setValues(SortedMap<LocalDate, BigDecimal> values) {
        this.values = values;
    }

    public SortedMap<LocalDate, BigDecimal> getValues() {
        return values;
    }

    public void setFilmingProgress(FilmingProgress filmingProgress) {
        this.filmingProgress = filmingProgress;
    }

    public FilmingProgress getFilmingProgress() {
        return filmingProgress;
    }
}