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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

    private FilmingProgress currentFilming;

    private ISaveCommand saveCommand;

    private Map<FilmingProgressTypeEnum, SortedMap<LocalDate, BigDecimal>> progressForecast = new HashMap<FilmingProgressTypeEnum, SortedMap<LocalDate, BigDecimal>>();
    private Map<FilmingProgressTypeEnum, SortedMap<LocalDate, BigDecimal>> realProgress = new HashMap<FilmingProgressTypeEnum, SortedMap<LocalDate, BigDecimal>>();

    List<ProgressValue> progressValues = new ArrayList<ProgressValue>();

    @Override
    @Transactional
    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
        if (currentOrder != null) {
            orderDAO.reattach(currentOrder);
            this.currentFilming = this.currentOrder.getFilmingProgress();

            if (currentFilming == null) {
                this.currentFilming = FilmingProgress.create(order,
                        FilmingProgressTypeEnum.SCENES);
                order.setFilmingProgress(currentFilming);
                progressValues = new ArrayList<ProgressValue>();
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

    private void checkOutDontSaveEmptyFilmingProgress() {
        if (this.getCurrentFilmingProgress().isNewObject()
                && isEmptyFilmingProgress()) {
            this.getCurrentOrder().setFilmingProgress(null);
        }
    }

    private boolean isEmptyFilmingProgress() {
        return (calculateTotal(this.getCurrentFilmingProgress()
                .getRealProgress()) == 0)
                && (calculateTotal(this.getCurrentFilmingProgress()
                        .getProgressForecast()) == 0);
    }

    private Integer calculateTotal(Map<LocalDate, Integer> progressValues) {
        Integer total = 0;
        for (Integer value : progressValues.values()) {
            total = total + value;
        }
        return total;
    }

    public void setSaveCommand(ISaveCommand saveCommand) {
        this.saveCommand = saveCommand;
    }

    private ISaveCommand getSaveCommand() {
        return this.saveCommand;
    }

    public void hookIntoSaveCommand() {
        if (getSaveCommand() != null) {
            IBeforeSaveListener beforeSaveListener = new IBeforeSaveListener() {
                @Override
                public void onBeforeSave() {
                    checkOutDontSaveEmptyFilmingProgress();
                }
            };
            getSaveCommand().addListener(beforeSaveListener);
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

    private void buildProgressValueBy(FilmingProgressTypeEnum unitMeasure,
            BigDecimal maxValue) {
        for (ProgressType type : ProgressType.values()) {
            getProgressMapByType(type).put(unitMeasure,
                    new TreeMap<LocalDate, BigDecimal>());
            initSortedMap(getProgressMapByType(type).get(unitMeasure), maxValue);
            progressValues.add(new ProgressValue(type, unitMeasure,
                    getProgressMapByType(type).get(unitMeasure)));
        }
    }

    private void initSortedMap(SortedMap<LocalDate, BigDecimal> map,
            BigDecimal maxValue) {
        if (this.getCurrentOrder() != null) {
            createIntoInterval(map, getCurrentOrder().getInitDate(),
                    getCurrentOrder().getDeadline(), maxValue);
        }
    }

    private SortedMap<LocalDate, BigDecimal> createIntoInterval(
            SortedMap<LocalDate, BigDecimal> map, Date initDate,
            Date deadline, BigDecimal maxValue) {
        Validate.notNull(initDate);
        Validate.notNull(deadline);
        LocalDate finishDate = new LocalDate(deadline);
        LocalDate date = new LocalDate(initDate);

        int days = Days.daysBetween(date, finishDate).getDays();
        BigDecimal value = BigDecimal.ZERO.setScale(2);
        if (days > 0) {
            value = maxValue.divide(new BigDecimal(days), 2,
                    RoundingMode.HALF_UP);
        }

        for (int i = 0; i < days; i++) {
            map.put(date, value);
            date = date.plusDays(1);
        }
        return map;
    }

    private Map<FilmingProgressTypeEnum, SortedMap<LocalDate, BigDecimal>> getProgressMapByType(
            ProgressType type) {
        switch (type) {
        case FORECAST:
            return progressForecast;
        case REAL:
        default:
            return realProgress;
        }
    }
}

enum ProgressType {
    FORECAST, REAL;
}

class ProgressValue {

    ProgressType progressType;

    FilmingProgressTypeEnum unitMeasure;

    private SortedMap<LocalDate, BigDecimal> values = new TreeMap<LocalDate, BigDecimal>();

    ProgressValue() {

    }

    public ProgressValue(ProgressType type,
            FilmingProgressTypeEnum unitMeasure,
            SortedMap<LocalDate, BigDecimal> values) {
        this.progressType = type;
        this.unitMeasure = unitMeasure;
        this.values = values;
    }

    public void setValues(SortedMap<LocalDate, BigDecimal> values) {
        this.values = values;
    }

    public SortedMap<LocalDate, BigDecimal> getValues() {
        return values;
    }
}