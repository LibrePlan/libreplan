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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.libreplan.business.filmingprogress.entities.FilmingProgress;
import org.libreplan.business.filmingprogress.entities.ProgressGranularityType;
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

    private ProgressType[] progressTypes = new ProgressType[3];

    private ISaveCommand saveCommand;

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

    @Override
    public ProgressType[] getProgressTypes() {
        return progressTypes;
    }

    @Override
    public ProgressType[] buildProgressTypes() {
        this.progressTypes = new ProgressType[3];
        if (this.getCurrentFilmingProgress() != null) {
            this.progressTypes[0] = ProgressType.create(getCurrentFilmingProgress().getInitialProgressForecast());
            this.progressTypes[1] = ProgressType.create(getCurrentFilmingProgress().getProgressForecast());
            this.progressTypes[2] = ProgressType.create(getCurrentFilmingProgress().getRealProgress());
        }
        return progressTypes;
    }

    public boolean isInitialProgress(ProgressType progressType) {
        if (this.getCurrentFilmingProgress() != null && progressType.getInitialMap() != null) {
            return (progressType.getInitialMap() ==
                    getCurrentFilmingProgress().getInitialProgressForecast());
        }
        return false;
    }

    @Override
    public void updateProgressForecast(boolean updateInitialProgress) {
        if (this.getCurrentFilmingProgress() != null) {
            if (updateInitialProgress) {
                overwriteProgress(
                        getCurrentFilmingProgress().getRealProgress(),
                        getCurrentFilmingProgress()
                                .getInitialProgressForecast());
            }
            overwriteProgress(getCurrentFilmingProgress().getRealProgress(),
                    getCurrentFilmingProgress().getProgressForecast());
        }
    }

    private void overwriteProgress(Map<LocalDate, Integer> fromMap,
            Map<LocalDate, Integer> toMap) {
        for (Entry<LocalDate, Integer> entry : fromMap.entrySet()) {
            if (entry.getValue() != 0) {
                toMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void checkOutDontSaveEmptyFilmingProgress(){
        if(this.getCurrentFilmingProgress().isNewObject() && isEmptyFilmingProgress()){
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

    public void hookIntoSaveCommand(final ProgressGranularityType type) {
        if (getSaveCommand() != null) {
            IBeforeSaveListener beforeSaveListener = new IBeforeSaveListener() {
                @Override
                public void onBeforeSave() {
                    updateValuesIntoInitialMap(type);
                    checkOutDontSaveEmptyFilmingProgress();
                }
            };
            getSaveCommand().addListener(beforeSaveListener);
        }
    }

    @Override
    public void updateValuesIntoInitialMap(
            ProgressGranularityType progressGranularityType) {
        for (ProgressType progressType : getProgressTypes()) {
            progressType.changeValuesByGranularity(progressGranularityType);
        }
    }
}

class ProgressType {

    private Map<ProgressGranularityType, Boolean> allTypesUpdates = new HashMap<ProgressGranularityType, Boolean>();

    private final Map<LocalDate, Integer> mapInitial;

    private Map<ProgressGranularityType, SortedMap<DateInChunks, GroupByScene>> mapValues = new HashMap<ProgressGranularityType, SortedMap<DateInChunks, GroupByScene>>();

    private ProgressType(Map<LocalDate, Integer> map) {
        this.mapInitial = map;
        setAllTypeUpdates(false);
    }

    public void setAllTypeUpdates(boolean updated) {
        for (ProgressGranularityType type : ProgressGranularityType.values()) {
            allTypesUpdates.put(type, updated);
        }
    }

    public static ProgressType create(Map<LocalDate, Integer> map) {
        return new ProgressType(map);
    }

    public Map<LocalDate, Integer> getInitialMap() {
        return mapInitial;
    }

    public void setMapValues(
            Map<ProgressGranularityType, SortedMap<DateInChunks, GroupByScene>> mapValues) {
        this.mapValues = mapValues;
    }

    public Map<ProgressGranularityType, SortedMap<DateInChunks, GroupByScene>> getMapValues() {
        return mapValues;
    }

    public SortedMap<DateInChunks, GroupByScene> getValuesBy(
            ProgressGranularityType granularity) {
        if (mapValues.get(granularity) == null) {
            buildValuesByGranularity(granularity);
        }
        return mapValues.get(granularity);
    }

    public void buildValuesByGranularity(ProgressGranularityType granularity) {
        SortedMap<DateInChunks, GroupByScene> map = new TreeMap<DateInChunks, GroupByScene>();
        for (Entry<LocalDate, Integer> entry : mapInitial.entrySet()) {
            DateInChunks key = getGranularityAndYearValue(granularity, entry.getKey());
            if (map.get(key) != null) {
                map.get(key).add(entry.getKey(), entry.getValue());
            } else {
                map.put(key, new GroupByScene(entry.getKey(), entry.getValue()));
            }
        }
        mapValues.put(granularity, map);
    }

    public void changeValuesByGranularity(ProgressGranularityType previousGranularity) {
        // update the mapInitial with the values if it's not updated
        if (allTypesUpdates.get(previousGranularity)) {
            return;
        }

        SortedMap<DateInChunks, GroupByScene> mapGroupedValues = getValuesBy(previousGranularity);
        for (GroupByScene groupedScene : mapGroupedValues.values()) {
            Integer total = groupedScene.getValue();
            Integer numDays = groupedScene.getDates().size();
            Integer newValue = total / numDays;
            Integer resto = total % numDays;

            Collections.sort(groupedScene.getDates());

            for (LocalDate date : groupedScene.getDates()) {
                mapInitial.put(date, newValue);
            }

            Iterator<LocalDate> iter = groupedScene.getDates().iterator();
            for (int i = 0; i < resto; i++) {
                LocalDate date = iter.next();
                mapInitial.put(date, mapInitial.get(date) + 1);
            }
        }
        resetMapValues();
        allTypesUpdates.put(previousGranularity, true);
    }

    private void resetMapValues() {
        for (ProgressGranularityType type : ProgressGranularityType.values()) {
            this.mapValues.put(type, null);
        }
    }

    public static DateInChunks getGranularityAndYearValue(
            ProgressGranularityType granularity, LocalDate key) {
        return createDateInChunksByGranularity(granularity, key);
    }

    public static DateInChunks createDateInChunksByGranularity(
            ProgressGranularityType granularity, LocalDate key) {
        switch (granularity) {
        case MONTH:
            return new DateInChunks(key.getYear(), key.getMonthOfYear(),
                    key.getMonthOfYear());
        case WEEK:
            return new DateInChunks(key.getYear(), key.getWeekOfWeekyear(),
                    key.getWeekOfWeekyear());
        case DAY:
        default:
            return new DateInChunks(key.getYear(), key.getDayOfYear(),
                    key.getDayOfMonth());
        }
    }

}

class DateInChunks implements Comparable<DateInChunks> {
    Integer granularityValue;
    Integer year;
    Integer month;

    DateInChunks(Integer year, Integer month, Integer granularityValue) {
        this.year = year;
        this.month = month;
        this.granularityValue = granularityValue;
    }

    @Override
    public int compareTo(DateInChunks arg0) {
        if (year.compareTo(arg0.year) == 0) {
            if (month.compareTo(arg0.month) == 0) {
                return granularityValue.compareTo(arg0.granularityValue);
            }
            return month.compareTo(arg0.month);
        }
        return year.compareTo(arg0.year);
    }
}

class GroupByScene {

    private List<LocalDate> dates = new ArrayList<LocalDate>();

    private Integer value = 0;

    public GroupByScene(LocalDate date, Integer value) {
        add(date, value);
    }

    public void add(LocalDate date, Integer value) {
        add(value);
        add(date);
    }

    public List<LocalDate> getDates() {
        return dates;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public void add(Integer value) {
        this.value += value;
    }

    public void add(LocalDate date) {
        this.dates.add(date);
    }
}