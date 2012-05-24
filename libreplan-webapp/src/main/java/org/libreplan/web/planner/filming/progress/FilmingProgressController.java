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

import static org.libreplan.web.I18nHelper._;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.libreplan.business.filmingprogress.entities.FilmingProgress;
import org.libreplan.business.filmingprogress.entities.ProgressGranularityType;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.OnlyOneVisible;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.order.ISaveCommand;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 *         Controller for filming progress per order view
 * @param <IFilmingProgressModel>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FilmingProgressController extends GenericForwardComposer {

    private IFilmingProgressModel filmingProgressModel;

    private org.zkoss.zk.ui.Component mainComponent;

    private OnlyOneVisible onlyOneVisible;
    private Div noDataLayout;
    private org.zkoss.zk.ui.Component normalLayout;

    private IMessagesForUser messages;
    private Listbox filmingProgressZoomLevel;
    private Grid gridScenes;
    private Grid gridTitles;

    private Label totalInitialProgress;
    private Label totalCurrentEstimations;
    private Label totalRealProgress;

    private ProgressGranularityType progressGranularityType = ProgressGranularityType
            .getDefault();

    private ScenePerDayRenderer scenePerDayRenderer = new ScenePerDayRenderer();

    private final static String MOLD = "paging";

    private final static int PAGING = 10;

    private Map<ProgressType, Label> panelTotalByScene = new HashMap<ProgressType, Label>();

    public FilmingProgressController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.mainComponent = comp;
        self.setAttribute("controller", this);
        Util.createBindingsFor(this.mainComponent);

        normalLayout = (org.zkoss.zk.ui.Component) comp.getFellow("normalLayout");
        noDataLayout = (Div) comp.getFellow("noDataLayout");
        onlyOneVisible = new OnlyOneVisible(normalLayout, noDataLayout);
        onlyOneVisible.showOnly(noDataLayout);

    }

    private void reloadNormalLayout() {
        Util.createBindingsFor(normalLayout);
    }

    public void init(Order order, ISaveCommand saveCommand) {
        filmingProgressModel.setSaveCommand(saveCommand);
        filmingProgressModel.hookIntoSaveCommand(this.progressGranularityType);
        filmingProgressModel.setCurrentOrder(order);
        if (this.mainComponent != null) {
            loadAndInitializeComponents();
        }
    }

    private Order getCurrentOrder() {
        return filmingProgressModel.getCurrentOrder();
    }

    private void loadAndInitializeComponents() {
        messages = new MessagesForUser(mainComponent.getFellow("messages"));
        if (getCurrentOrder().getBudget() == null) {
            onlyOneVisible.showOnly(noDataLayout);
        } else {
            onlyOneVisible.showOnly(normalLayout);
            createComponents();
        }
    }

    private void createComponents() {
        filmingProgressZoomLevel = (Listbox) mainComponent.getFellow("filmingProgressZoomLevel");
        setZoomFromFilmingProgress();
        initScenes();
        prepareFilmingProgressList();
        createMapTotals();
        reloadNormalLayout();
    }

    private void createMapTotals() {
        if (getScenes().length == 3) {
            panelTotalByScene.put(getScenes()[0], this.totalInitialProgress);
            panelTotalByScene.put(getScenes()[1], this.totalCurrentEstimations);
            panelTotalByScene.put(getScenes()[2], this.totalRealProgress);
        }
        calculatePanelTotal();
    }

    private void calculatePanelTotal() {
        for (Entry<ProgressType, Label> entry : panelTotalByScene.entrySet()) {
            calculatePanelTotalByScene(entry.getKey(), entry.getValue());
        }
    }

    private void calculatePanelTotalByScene(ProgressType scene, Label totalComp) {
        Integer total = 0;
        for (Entry<DateInChunks, GroupByScene> entry : scene.getValuesBy(
                progressGranularityType).entrySet()) {
            total = total + entry.getValue().getValue();
        }
        totalComp.setValue(total.toString());
    }

    private void updatePanelTotalByScene(ProgressType scene, Integer oldValue, Integer newValue) {
        Label totalComp = panelTotalByScene.get(scene);
        Integer total = Integer.valueOf(totalComp.getValue());
        if (oldValue >= newValue) {
            Integer diff = oldValue - newValue;
            total = total - diff;
            totalComp.setValue(total.toString());
        } else {
            Integer diff = newValue - oldValue;
            total = total + diff;
            totalComp.setValue(total.toString());
        }
    }

    public void setZoomLevel(ProgressGranularityType zoom) {
        this.getFilmingProgress().setProgressGranularity(zoom);
        updateValuesIntoInitialMap();
        this.progressGranularityType = zoom;
        prepareFilmingProgressList();
        reloadNormalLayout();
    }

    private void setZoomFromFilmingProgress() {
        int index = ProgressGranularityType.getDefault().ordinal();
        if (this.getFilmingProgress() != null) {
            this.progressGranularityType = this.getFilmingProgress().getProgressGranularity();
            index = this.progressGranularityType.ordinal();
        }
        filmingProgressZoomLevel.setSelectedIndex(index);
    }

    private ProgressGranularityType getZoomLevel() {
        return this.progressGranularityType;
    }

    private FilmingProgress getFilmingProgress() {
        return this.filmingProgressModel.getCurrentFilmingProgress();
    }

    private void updateValuesIntoInitialMap() {
        this.filmingProgressModel
                .updateValuesIntoInitialMap(this.progressGranularityType);
    }

    public void confirmUpdateProgressForecast() {
        try {
            if (Messagebox
                    .show(_("This operation overwrite all the values of forecast progress row with the values in the row of the real progress. Are you sure?"),
                            _("Confirm"), Messagebox.OK | Messagebox.CANCEL,
                            Messagebox.QUESTION) == Messagebox.CANCEL) {
                return;
            }
        } catch (InterruptedException e) {
            this.messages.showMessage(Level.ERROR, e.getMessage());
        }
        updateProgressForecast();
    }

    public void updateProgressForecast(){
        for (ProgressType scene : getScenes()) {
            scene.setAllTypeUpdates(false);
        }
        updateValuesIntoInitialMap();
        filmingProgressModel
                .updateProgressForecast(isTotalInitialProgressZero());
        prepareFilmingProgressList();
        calculatePanelTotal();
        reloadNormalLayout();
    }

    private boolean isTotalInitialProgressZero() {
        if (this.totalInitialProgress != null) {
            try {
                Integer total = Integer
                        .valueOf(totalInitialProgress.getValue());
                return (total == 0);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /*
     * functions to manage the datebox
     */
    public Date getStartDate() {
        if (getFilmingProgress() != null && getFilmingProgress().getStartDate() != null) {
            return getFilmingProgress().getStartDate().toDateTimeAtStartOfDay().toDate();
        }
        return null;
    }

    public void setStartDate(Date date) {
        if (getFilmingProgress() != null && date != null) {
            getFilmingProgress().setStartDate(new LocalDate(date));
        }
    }

    public Date getEndDate() {
        if (getFilmingProgress() != null && getFilmingProgress().getEndDate() != null) {
            return getFilmingProgress().getEndDate().toDateTimeAtStartOfDay().toDate();
        }
        return null;
    }

    public void setEndDate(Date date) {
        if (getFilmingProgress() != null && date != null) {
            getFilmingProgress().setEndDate(new LocalDate(date));
        }
    }

    public ListModel getZoomLevels() {
        return new SimpleListModel(ProgressGranularityType.values());
    }

    private void prepareFilmingProgressList() {
        /*
         * The only way to clean the listhead, is to clean all its attributes
         * and children The paging component cannot be removed manually. It is
         * removed automatically when changing the mold
         */
        gridScenes.setMold(null);
        gridScenes.getChildren().clear();

        // Set mold and pagesize
        gridScenes.setMold(MOLD);
        gridScenes.setPageSize(PAGING);

        appendColumns(gridTitles, gridScenes);

        gridScenes.setModel(new SimpleListModel(getScenes()));
    }

    private void appendColumns(Grid gridLeft, Grid gridRight) {
        // Delete the auxHeaders and columns in gridRight
        gridRight.getHeads().clear();
        Columns columns = gridRight.getColumns();

        // Create listhead first time is rendered
        if (columns == null) {
            columns = new Columns();
        }
        // Delete all headers
        columns.getChildren().clear();
        columns.setSizable(true);

        // Add static headers
        if (filmingProgressModel.getProgressTypes() != null && filmingProgressModel.getProgressTypes().length > 0) {
            Map<DateInChunks, GroupByScene> values = filmingProgressModel.getProgressTypes()[0]
                    .getValuesBy(this.getZoomLevel());

            appendAuxhead(gridLeft, gridRight);

            for (DateInChunks keyValue : values.keySet()) {
                Column column = new Column();
                column.setAlign("center");
                column.setWidth("30px");
                column.setHeight("19px");
                column.setLabel(getTitle(keyValue));
                columns.appendChild(column);
            }
            columns.setParent(gridRight);
        }
    }

    private String getTitle(DateInChunks keyValue) {
        if (this.progressGranularityType.equals(ProgressGranularityType.MONTH)) {
            return this.getMonthForInt(keyValue.granularityValue);
        }
        return keyValue.granularityValue.toString();
    }

    private void appendAuxhead(Grid gridLeft, Grid gridRight) {
        // it builds the auxheaders in gridRight
        List<CustomHeader> listHeaders = new ArrayList<CustomHeader>();

        switch (this.progressGranularityType) {
        case DAY:
            listHeaders = createFrom(
                    filmingProgressModel.getProgressTypes()[0].getValuesBy(ProgressGranularityType.WEEK),
                    new SimpleDateFormat("w,MMM yyyy"));
            break;
        case WEEK:
            listHeaders = createFrom(
                    filmingProgressModel.getProgressTypes()[0].getValuesBy(ProgressGranularityType.MONTH),
                    new SimpleDateFormat("MMMM,yyyy"));
            break;
        case MONTH:
            listHeaders = groupByYear(
                    filmingProgressModel.getProgressTypes()[0].getValuesBy(ProgressGranularityType.MONTH),
                    new SimpleDateFormat("yyyy"));
            break;
        }

        Auxhead auxhead = new Auxhead();
        for (CustomHeader customHeader : listHeaders) {
            Auxheader auxHeader = new Auxheader(customHeader.title);
            auxHeader.setAlign("center");
            auxHeader.setHeight("19px");
            auxHeader.setStyle("font-weight:bold;");
            auxHeader.setColspan(customHeader.colspan);
            auxhead.appendChild(auxHeader);
        }
        auxhead.setParent(gridRight);

    }

    private List<CustomHeader> createFrom(
            SortedMap<DateInChunks, GroupByScene> valuesBy,
            SimpleDateFormat dateConverter) {
        List<CustomHeader> list = new ArrayList<CustomHeader>();
        for (DateInChunks key : valuesBy.keySet()) {
            if (valuesBy.get(key).getDates().size() > 0) {
                Date date = valuesBy.get(key).getDates().get(0).toDateTimeAtStartOfDay().toDate();
                String title = dateConverter.format(date);
                list.add(new CustomHeader(title, calculateColspanByZoom(valuesBy.get(key)
                        .getDates())));
            }
        }
        return list;
    }

    private List<CustomHeader> groupByYear(
            SortedMap<DateInChunks, GroupByScene> valuesBy,
            SimpleDateFormat dateConverter) {

        List<CustomHeader> list = new ArrayList<CustomHeader>();

        // this Map represent a year and the number of month according that year
        SortedMap<Integer, List<LocalDate>> mapByYear = new TreeMap<Integer, List<LocalDate>>();
        for (Entry<DateInChunks, GroupByScene> entry : valuesBy.entrySet()) {
            if (mapByYear.get(entry.getKey().year) == null) {
                mapByYear.put(entry.getKey().year, new ArrayList<LocalDate>());
            }
            mapByYear.get(entry.getKey().year).addAll(entry.getValue().getDates());
        }

        for (Integer year : mapByYear.keySet()) {
            if (mapByYear.get(year).size() > 0) {
                Date date = mapByYear.get(year).get(0).toDateTimeAtStartOfDay().toDate();
                String title = dateConverter.format(date);
                list.add(new CustomHeader(title, calculateColspanByZoom(mapByYear.get(year))));
            }
        }
        return list;
    }

    private int calculateColspanByZoom(List<LocalDate> list) {
        if (list != null) {
            Set<Integer> set = new HashSet<Integer>();
            switch (this.progressGranularityType) {
            case DAY:
                return list.size();
            case WEEK:
                for (LocalDate date : list) {
                    int week = date.getWeekOfWeekyear();
                    set.add(week);
                }
                return set.size();
            case MONTH:
                for (LocalDate date : list) {
                    int month = date.getMonthOfYear();
                    set.add(month);
                }
                return set.size();
            }
        }
        return 0;
    }

    private ProgressType[] getScenes() {
        return filmingProgressModel.getProgressTypes();
    }

    private ProgressType[] initScenes() {
        return filmingProgressModel.buildProgressTypes();
    }

    public ScenePerDayRenderer getRenderer() {
        return scenePerDayRenderer;
    }

    /**
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    public class ScenePerDayRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) {
            final ProgressType scene = (ProgressType) data;
            row.setValue(scene);
            boolean readOnly = isInitialProgress(scene);

            Map<DateInChunks, GroupByScene> map = scene.getValuesBy(getZoomLevel());
            for (final GroupByScene groupedScene : map.values()) {
                Intbox intbox = new Intbox();
                Util.bind(intbox, new Util.Getter<Integer>() {

                    @Override
                    public Integer get() {
                        return groupedScene.getValue();
                    }

                }, new Util.Setter<Integer>() {

                    @Override
                    public void set(Integer newValue) {
                        updatePanelTotalByScene(scene, groupedScene.getValue(), newValue);
                        groupedScene.setValue(newValue);
                    }
                });

                intbox.addEventListener("onChange", new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        scene.setAllTypeUpdates(false);
                    }
                });

                intbox.setReadonly(readOnly);
                row.appendChild(intbox);
            }
        }
    }

    private boolean isInitialProgress(ProgressType scene) {
        return filmingProgressModel.isInitialProgress(scene);
    }

    String getMonthForInt(int m) {
        int num_month = m - 1;
        String month = "invalid";
        DateFormatSymbols dfs = new DateFormatSymbols(Locales.getCurrent());
        String[] months = dfs.getMonths();
        if (num_month >= 0 && num_month <= 11) {
            month = months[num_month];
        }
        return month;
    }
}

class CustomHeader {
    String title = "";
    int colspan = 0;

    CustomHeader(String title, int colspan) {
        this.title = title;
        this.colspan = colspan;
    }
}
