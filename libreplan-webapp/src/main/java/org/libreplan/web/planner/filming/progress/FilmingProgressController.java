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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.LogFactory;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.filmingprogress.entities.FilmingProgress;
import org.libreplan.business.filmingprogress.entities.FilmingProgressTypeEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.OnlyOneVisible;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.order.ISaveCommand;
import org.libreplan.web.planner.order.ISaveCommand.IAfterSaveListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
import org.zkforge.timeplot.data.PlotData;
import org.zkforge.timeplot.geometry.DefaultTimeGeometry;
import org.zkforge.timeplot.geometry.DefaultValueGeometry;
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;

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
    private Grid gridValuesPerDay;
    private Grid gridTotals;
    private Button unitMeasureButton;

    private Datebox endDate;
    private Datebox startDate;

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(FilmingProgressController.class);

    private ProgressValuesPerDayRenderer valuesPerDayRenderer = new ProgressValuesPerDayRenderer();

    private ProgressTotalRenderer progressTotalRenderer = new ProgressTotalRenderer();

    private SortedMap<ProgressValue, Plotinfo> mapCharts = new TreeMap<ProgressValue, Plotinfo>();

    private Date endDateFilming;

    private Date startDateFilming;

    private Div filmingProgressChartScenes;

    private Div filmingProgressChartMinutes;

    private Div filmingProgressChartPages;

    private final static String MOLD = "paging";

    private final static int PAGING = 10;

    public FilmingProgressController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.mainComponent = comp;
        self.setAttribute("controller", this);
        Util.createBindingsFor(this.mainComponent);

        normalLayout = (org.zkoss.zk.ui.Component) comp
                .getFellow("normalLayout");
        noDataLayout = (Div) comp.getFellow("noDataLayout");
        onlyOneVisible = new OnlyOneVisible(normalLayout, noDataLayout);
        onlyOneVisible.showOnly(noDataLayout);
    }

    private void createTimePlots() {

        clearPlots();
        resetMapCharts();

        Timeplot timeplotScenes = createTimeplot();
        Timeplot timeplotPages = createTimeplot();
        Timeplot timeplotMinutes = createTimeplot();

        ValueGeometry vg = new DefaultValueGeometry();
        vg.setGridColor("#000000");
        vg.setMin(0);
        vg.setGridSpacing(20);
        TimeGeometry tg = new DefaultTimeGeometry();
        tg.setAxisLabelsPlacement("bottom");

        int i = 0;
        for(ProgressValue progressValue : this.getProgressValues()){

            Plotinfo plotinfo = new Plotinfo();
            plotinfo.setDataModel(createListModel(progressValue));
            plotinfo.setShowValues(true);
            switch (i) {
            case 0:
                plotinfo.setFillColor("rgba(150, 10, 10, 0.2)"); // progress
                break;
            case 1:
                plotinfo.setFillColor("rgba(0, 150, 10, 0.2)"); // progress
                break;
            case 2:
                plotinfo.setFillColor("rgba(0, 10, 200, 0.2)"); // progress
                break;
            }
            if (i == 2) {
                i = 0;
            } else {
                i = i + 1;
            }
            plotinfo.setValueGeometry(vg);
            plotinfo.setTimeGeometry(tg);

            this.mapCharts.put(progressValue, plotinfo);

            switch(progressValue.getProgressType()){
            case SCENES :
                timeplotScenes.appendChild(plotinfo);
                break;
            case MINUTES:
                timeplotMinutes.appendChild(plotinfo);
                break;
            case PAGES :
                timeplotPages.appendChild(plotinfo);
            }
        }

        filmingProgressChartScenes.appendChild(timeplotScenes);
        filmingProgressChartMinutes.appendChild(timeplotMinutes);
        filmingProgressChartPages.appendChild(timeplotPages);
    }

    private Timeplot createTimeplot() {
		Timeplot timeplot = new Timeplot();
		timeplot.setTimeFlagFormat("yyyy-MM-dd");
		return timeplot;
	}

	private ListModel createListModel(ProgressValue progressValue) {
        ListModelList lm = new ListModelList();
        BigDecimal sumProgressValues = BigDecimal.ZERO;

        for (Entry<LocalDate, BigDecimal> entry : progressValue.getValues()
                .entrySet()) {
            // create the point to represent it in the chart
            if (entry.getValue() != null) {
                sumProgressValues = sumProgressValues.add(entry.getValue());
            }
            final PlotData pd = createPlotData(entry.getKey(),
                    sumProgressValues);
            lm.add(pd);
        }
        return lm;
    }

    private void updateChart(ProgressValue pg, LocalDate date, BigDecimal oldValue,
            BigDecimal value) {
        // Calculate the difference to update
        BigDecimal diff = BigDecimal.ZERO;
        if (value != null) {
            diff = value;
            if (oldValue != null) {
                diff = value.subtract(oldValue);
            }
        }

        // Update from the changed point until the end
        Plotinfo plotinfo = mapCharts.get(pg);
        if(plotinfo != null){
            ListModelList lm = (ListModelList) plotinfo.getDataModel();
            ListModelList lmToList = new ListModelList(lm);

            int index = 0;
            Date time = date.toDateTimeAtStartOfDay().toDate();

            while (index < lmToList.getSize()) {
                PlotData point = (PlotData) lmToList.get(index);
                if (point.getTime().compareTo(time) >= 0) {
                    float newValue = point.getValue() + diff.floatValue();
                    point.setValue(newValue);
                    lm.remove(point);
                    lm.add(point);
                }
                index++;
            }
        }
    }

    private PlotData createPlotData(LocalDate date, BigDecimal value) {
        PlotData pd = new PlotData();
        float newValue = 0;
        if (value != null) {
            newValue = value.floatValue();
        }
        pd.setValue(newValue);

        if (date != null) {
            pd.setTime(date.toDateTimeAtStartOfDay().toDate());
        }
        return pd;
    }

    private void clearPlots() {
        clearPlot(filmingProgressChartScenes);
        clearPlot(filmingProgressChartMinutes);
        clearPlot(filmingProgressChartPages);
    }

    private void clearPlot(Div divChart) {
        List<AbstractComponent> children = new ArrayList<AbstractComponent>(
                divChart.getChildren());
        for (AbstractComponent child : children) {
            divChart.removeChild(child);
        }
    }

    private void reloadNormalLayout() {
        Util.createBindingsFor(normalLayout);
    }

    public void init(final Order order, ISaveCommand saveCommand) {
        filmingProgressModel.setSaveCommand(saveCommand);

        IAfterSaveListener afterSaveListener = new IAfterSaveListener() {
            @Override
            public void onAfterSave() {
                filmingProgressModel.dontPoseAsTransientObjectAnymore();
                loadAndRefressAllData();
            }
        };

        filmingProgressModel.hookIntoSaveCommand(afterSaveListener);
        filmingProgressModel.setCurrentOrder(order);
        if (this.mainComponent != null) {
            loadAndInitializeComponents();
        }
    }

    private void loadAndRefressAllData() {
        filmingProgressModel.loadDataFromOrder();
        prepareFilmingProgressList();
        createTimePlots();
        gridTotals.setModel(new SimpleListModel(getRowTotals()));
        gridTotals.renderAll();
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
        prepareFilmingProgressList();
        refreshTotalPanel();
        updatesDates();
        createTimePlots();
        reloadNormalLayout();
    }

    private FilmingProgress getFilmingProgress() {
        return this.filmingProgressModel.getFirstFilmingProgress();
    }

    private Set<FilmingProgress> getFilmingProgressSet() {
        return this.filmingProgressModel.getFilmingProgressSet();
    }

    /*
     * functions to manage the datebox
     */
    public Date getStartDate() {
        return this.startDateFilming;
    }

    public void setStartDate(Date date) {
        startDateFilming = date;
    }

    public Date getEndDate() {
        return this.endDateFilming;
    }

    public void setEndDate(Date date) {
        endDateFilming = date;
    }

    private void updatesDates() {
        this.endDateFilming = null;
        if (getFilmingProgress() != null
                && getFilmingProgress().getEndDate() != null) {
            this.endDateFilming = getFilmingProgress().getEndDate()
                    .toDateTimeAtStartOfDay().toDate();
        }
        this.startDateFilming = null;
        if (getFilmingProgress() != null
                && getFilmingProgress().getEndDate() != null) {
            this.startDateFilming = getFilmingProgress().getStartDate()
                    .toDateTimeAtStartOfDay().toDate();
        }
        Util.createBindingsFor(endDate);
        Util.createBindingsFor(startDate);
    }

    public boolean confirmChangeDate() {
        try {
            int status = Messagebox
                    .show(_("Confirm changing end date in all filming progress. Are you sure?"),
                            _("Question"), Messagebox.OK | Messagebox.CANCEL,
                            Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                return true;
            }
        } catch (InterruptedException e) {
            messages.showMessage(Level.ERROR, e.getMessage());
            LOG.error(_("Error on updating dates in filming progress"), e);
        }
        return false;
    }

    public void changeEndDate(Datebox datebox) {
        if (getFilmingProgress() != null
                && datebox.getValue() != null
                && getFilmingProgress().getEndDate().compareTo(
                        new LocalDate(datebox.getValue())) != 0) {
            if (confirmChangeDate()) {
                filmingProgressModel.updateEndDate(new LocalDate(datebox
                        .getValue()));
                filmingProgressModel.removeDays();
                loadAndRefressAllData();
            } else {
                this.endDateFilming = getFilmingProgress().getEndDate()
                        .toDateTimeAtStartOfDay().toDate();
                Util.createBindingsFor(datebox);
            }
        }
    }

    public void changeStartDate(Datebox datebox) {
        if (getFilmingProgress() != null
                && datebox.getValue() != null
                && getFilmingProgress().getStartDate().compareTo(
                        new LocalDate(datebox.getValue())) != 0) {
            if (confirmChangeDate()) {
                filmingProgressModel.updateStartDate(new LocalDate(datebox
                        .getValue()));
                filmingProgressModel.removeDays();
                loadAndRefressAllData();
            } else {
                startDateFilming = getFilmingProgress().getStartDate()
                        .toDateTimeAtStartOfDay().toDate();
                Util.createBindingsFor(datebox);
            }
        }
    }

    public Constraint checkConstraintEndDate() {
        return new Constraint() {
            @Override
            public void validate(org.zkoss.zk.ui.Component comp, Object value)
                    throws WrongValueException {
                if (value == null) {
                    throw new WrongValueException(comp,
                            _("the value cannot be empty."));
                }
                if (getFilmingProgress() != null
                        && filmingProgressModel.getCurrentOrder() != null) {
                    LocalDate newEndDate = new LocalDate((Date) value);
                    LocalDate startDateFilming = getFilmingProgress()
                            .getStartDate();
                    if (isDecreasingAndOutRange(startDateFilming, newEndDate)) {
                        throw new WrongValueException(
                                comp,
                                _("if the time period is lower the end date must be previous than the initial finish date."));
                    }
                }
            }
        };
    }

    public Constraint checkConstraintStartDate() {
        return new Constraint() {
            @Override
            public void validate(org.zkoss.zk.ui.Component comp, Object value)
                    throws WrongValueException {
                if (value == null) {
                    throw new WrongValueException(comp,
                            _("the value cannot be empty."));
                }
                if (getFilmingProgress() != null
                        && filmingProgressModel.getCurrentOrder() != null) {
                    LocalDate newStartDate = new LocalDate((Date) value);
                    LocalDate endDateFilming = getFilmingProgress()
                            .getEndDate();
                    if (isDecreasingAndOutRange(newStartDate, endDateFilming)) {
                        throw new WrongValueException(
                                comp,
                                _("if the time period is lower the start date must be posterior than the initial start date."));
                    }
                }
            }
        };
    }

    private boolean isDecreasingAndOutRange(LocalDate startDate,
            LocalDate endDate) {
        LocalDate initialStartDate = new LocalDate(filmingProgressModel
                .getCurrentOrder().getInitDate());
        LocalDate initialEndDate = new LocalDate(filmingProgressModel
                .getCurrentOrder().getDeadline());
        int oldDays = Days.daysBetween(initialStartDate, initialEndDate)
                .getDays();
        int newDays = Days.daysBetween(startDate, endDate).getDays();
        return ((oldDays > newDays) && (endDate.compareTo(initialEndDate) > 0 || startDate
                .compareTo(initialStartDate) < 0));
    }

    public ListModel getUnitMeasures() {
        return new SimpleListModel(getUnitMeasuresNotAdded());
    }

    private List<FilmingProgressTypeEnum> getUnitMeasuresNotAdded() {
        List<FilmingProgressTypeEnum> measures = new ArrayList<FilmingProgressTypeEnum>(
                Arrays.asList(FilmingProgressTypeEnum.values()));
        for (ProgressValue pgValue : this.getProgressValues()) {
            measures.remove(pgValue.getProgressType());
        }
        return measures;
    }

    public boolean isDisableAddUnitMeasure() {
        return getUnitMeasuresNotAdded().size() == 0;
    }

    private void prepareFilmingProgressList() {
        /*
         * The only way to clean the listhead, is to clean all its attributes
         * and children The paging component cannot be removed manually. It is
         * removed automatically when changing the mold
         */
        gridValuesPerDay.setSclass("grid-values-per-day");
        gridValuesPerDay.setMold(null);
        gridValuesPerDay.getChildren().clear();

        // Set mold and pagesize
        gridValuesPerDay.setMold(MOLD);
        gridValuesPerDay.setPageSize(PAGING);

        appendColumns(gridValuesPerDay);
        renderAllValuesPerDay();
    }

    private void renderAllValuesPerDay() {
        resetTotalPanel();
        gridValuesPerDay.setModel(new SimpleListModel(getProgressValues()));
        gridValuesPerDay.renderAll();
    }

    private void appendColumns(Grid gridRight) {
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

        List<ProgressValue> progressValues = filmingProgressModel
                .getProgressValues();
        if (progressValues.size() > 0) {

            ProgressValue progressValue = progressValues.get(0);
            if (progressValue != null && progressValue.getValues() != null) {
                Set<LocalDate> dates = progressValue.getValues().keySet();

                // Add static headers
                appendAuxhead(gridRight, dates);

                for (LocalDate keyValue : dates) {
                    Column column = new Column();
                    column.setAlign("center");
                    column.setWidth("40px");
                    column.setHeight("15px");
                    column.setLabel(Integer.toString(keyValue.getDayOfMonth()));
                    columns.appendChild(column);
                }
                columns.setParent(gridRight);
            }
        }
    }

    private void appendAuxhead(Grid grid, Set<LocalDate> dates) {
        // it builds the auxheaders in gridRight
        List<CustomHeader> listHeaders = groupByWeek(dates);

        Auxhead auxhead = new Auxhead();
        for (CustomHeader customHeader : listHeaders) {
            Auxheader auxHeader = new Auxheader(customHeader.type);
            auxHeader.setAlign("center");
            auxHeader.setStyle("font-weight:bold;");
            auxHeader.setColspan(customHeader.colspan);
            auxhead.appendChild(auxHeader);
        }
        auxhead.setParent(grid);
    }

    private List<CustomHeader> groupByWeek(Set<LocalDate> dates) {
        List<CustomHeader> list = new ArrayList<CustomHeader>();

        // this Map represent a year and the number of month according that year
        SortedMap<Integer, List<LocalDate>> mapByWeek = new TreeMap<Integer, List<LocalDate>>();
        for (LocalDate date : dates) {
            Integer key = date.getWeekOfWeekyear();
            if (mapByWeek.get(key) == null) {
                mapByWeek.put(key, new ArrayList<LocalDate>());
            }
            mapByWeek.get(key).add(date);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("w,MMM yyyy");
        for (Integer week : mapByWeek.keySet()) {
            Date date = mapByWeek.get(week).get(0).toDateTimeAtStartOfDay()
                    .toDate();
            String type = dateFormat.format(date);
            list.add(new CustomHeader(type, mapByWeek.get(week).size()));
        }
        return list;
    }

    public List<ProgressValue> getProgressValues() {
        return filmingProgressModel.getProgressValues();
    }

    private void addNewUnitMeasure(FilmingProgressTypeEnum unitMeasure,
            BigDecimal maxValue) {
        filmingProgressModel.addNewUnitMeasure(unitMeasure, maxValue);

    }

    public ProgressValuesPerDayRenderer getProgressValuesRenderer() {
        return valuesPerDayRenderer;
    }

    /**
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    public class ProgressValuesPerDayRenderer implements RowRenderer {

        private SortedMap<RowTotal, Row> panelTotal = new TreeMap<RowTotal, Row>();

        @Override
        public void render(final Row row, Object data) {
            final ProgressValue progressValue = (ProgressValue) data;
            row.setValue(progressValue);

            final RowTotal rowTotal = calculateRowTotal(row);
            panelTotal.put(rowTotal, row);

            for (final Entry<LocalDate, BigDecimal> entry : progressValue
                    .getValues().entrySet()) {

                Decimalbox valuebox = new Decimalbox();
                valuebox.setScale(2);
                valuebox.setReadonly(isReadOnlyByDay(
                        progressValue.getForecastLevel(), entry.getKey()));

                Util.bind(valuebox, new Util.Getter<BigDecimal>() {

                    @Override
                    public BigDecimal get() {
                        return entry.getValue();
                    }

                }, new Util.Setter<BigDecimal>() {

                    @Override
                    public void set(BigDecimal newValue) {
                        updatePanelTotal(rowTotal, entry.getValue(), newValue);
                        updateChart(progressValue, entry.getKey(),
                                entry.getValue(), newValue);

                        entry.setValue(newValue);
                        if (progressValue.getForecastLevel()
                                .equals(ForecastLevelEnum.REAL)) {
                            updateValue(progressValue, entry.getKey(), newValue);
                        }

                    }
                });

                row.appendChild(valuebox);
            }
        }

        private void updateValue(ProgressValue progressValue, LocalDate date,
                BigDecimal newValue) {
            FilmingProgress filmingProgress = progressValue
                    .getFilmingProgress();
            if (filmingProgress.getProgressForecast() != null
                    && !filmingProgress.getProgressForecast().isEmpty()) {
                filmingProgress.getProgressForecast().put(date, newValue);
                renderAllValuesPerDay();
                refreshTotalPanel();
            }
        }

        private boolean isReadOnlyByDay(ForecastLevelEnum forecastLevel,
                LocalDate date) {
            return ((ForecastLevelEnum.REAL.equals(forecastLevel) && date
                    .isAfter(new LocalDate())) || (ForecastLevelEnum.FORECAST
                    .equals(forecastLevel) && date.isBefore(new LocalDate()
                    .plusDays(1))));
        }

        private void updatePanelTotal(RowTotal rowTotal, BigDecimal oldValue,
                BigDecimal newValue) {
            BigDecimal total = rowTotal.getTotal();
            if (oldValue == null) {
                oldValue = BigDecimal.ZERO;
            }
            if (newValue == null) {
                newValue = BigDecimal.ZERO;
            }
            if (oldValue.compareTo(newValue) < 0) {
                BigDecimal diff = newValue.subtract(oldValue);
                total = total.add(diff);
                rowTotal.setTotal(total);
            } else {
                BigDecimal diff = oldValue.subtract(newValue);
                total = total.subtract(diff);
                rowTotal.setTotal(total);
            }
        }

        private RowTotal calculateRowTotal(Row row) {
            ProgressValue progressValue = (ProgressValue) row.getValue();
            BigDecimal total = BigDecimal.ZERO;
            for (final Entry<LocalDate, BigDecimal> entry : progressValue
                    .getValues().entrySet()) {
                if (entry.getValue() != null) {
                    total = total.add(entry.getValue());
                }
            }
            return new RowTotal(progressValue.getProgressType(),
                    progressValue.getForecastLevel(), total);
        }
    }

    private void refreshChart(ProgressValue pg) {
        switch (pg.getProgressType()) {
        case SCENES:
            Util.reloadBindings(this.filmingProgressChartScenes);
            break;
        case MINUTES:
            Util.reloadBindings(this.filmingProgressChartMinutes);
            break;
        case PAGES:
            Util.reloadBindings(this.filmingProgressChartPages);
        }
    }

    private void refreshTotalPanel() {
        Util.reloadBindings(gridTotals);
    }

    public void resetTotalPanel() {
        getProgressValuesRenderer().panelTotal = new TreeMap<RowTotal, Row>();
    }

    private void resetMapCharts() {
        mapCharts = new TreeMap<ProgressValue, Plotinfo>();

    }

    public List<RowTotal> getRowTotals() {
        return new ArrayList(getProgressValuesRenderer().panelTotal.keySet());
    }

    private Row getRowBy(RowTotal rowTotalToFind) {
        return getProgressValuesRenderer().panelTotal.get(rowTotalToFind);
    }

    public ProgressTotalRenderer getProgressTotalRenderer() {
        return this.progressTotalRenderer;
    }

    /**
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    public class ProgressTotalRenderer implements RowRenderer {

        @Override
        public void render(final Row row, Object data) {
            final RowTotal rowTotal = (RowTotal) data;
            row.setValue(rowTotal);

            Row rowValues = getRowBy(rowTotal);
            if (rowValues != null) {
                ProgressValue progressValue = (ProgressValue) rowValues
                        .getValue();
                if (progressValue.getForecastLevel().equals(
                        ForecastLevelEnum.INITIAL_FORECAST)) {
                    Cell cell = new Cell();
                    cell.setAlign("center");
                    cell.setValign("center");
                    cell.setRowspan(calculateRowspan(progressValue
                            .getProgressType()));
                    cell.appendChild(createRemoveButton(progressValue));
                    row.appendChild(cell);
                }
            }

            row.appendChild(rowTotal.getLbType());
            row.appendChild(rowTotal.getLbTotal());
        }

        private int calculateRowspan(FilmingProgressTypeEnum type) {
            int rowspan = 0;
            for (ProgressValue progressValue : getProgressValues()) {
                if (progressValue.getProgressType().equals(type)) {
                    rowspan++;
                }
            }
            return rowspan;
        }

        private Button createRemoveButton(final ProgressValue progressValue) {
            Button delete = new Button("", "/common/img/ico_borrar1.png");
            delete.setHoverImage("/common/img/ico_borrar.png");
            delete.setSclass("icono");
            delete.setTooltiptext(_("Delete"));
            delete.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    confirmRemove(progressValue);
                }
            });
            return delete;
        }
    }

    /**
     * functions to manage the popup with what add a new unit measure
     */
    private Window windowAddUnitMeasure;

    private Decimalbox maxValueBox;

    private Listbox unitMeasureListBox;

    public void addUnitMeasure() {
        try {
            windowAddUnitMeasure = getWindowUnitMeasures();
            loadComponents();
            windowAddUnitMeasure.doModal();
            Util.reloadBindings(windowAddUnitMeasure);
            Util.reloadBindings(unitMeasureListBox);
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Window getWindowUnitMeasures() {
        if (windowAddUnitMeasure == null) {
            Map<String, Object> arguments = new HashMap<String, Object>();
            arguments.put("filmingProgressController", this);
            org.zkoss.zk.ui.Component popup = Executions.createComponents(
                    "popup_add_unit_measure.zul", normalLayout, arguments);
            return (Window) popup.getFellowIfAny("windowAddUnitMeasure");
        }
        return windowAddUnitMeasure;
    }

    public void loadComponents() {
        if (windowAddUnitMeasure != null) {
            unitMeasureListBox = (Listbox) windowAddUnitMeasure
                    .getFellowIfAny("unitMeasureListBox");
            unitMeasureListBox.setModel(getUnitMeasures());
            if (unitMeasureListBox.getItemCount() > 0) {
                unitMeasureListBox.setSelectedIndex(0);
            }
            maxValueBox = (Decimalbox) windowAddUnitMeasure
                    .getFellowIfAny("maxValueBox");
        }
    }

    public void accept() {
        if (windowAddUnitMeasure != null) {
            if (unitMeasureListBox.getSelectedItem() != null) {
                FilmingProgressTypeEnum unitMeasure = (FilmingProgressTypeEnum) unitMeasureListBox
                        .getSelectedItem().getValue();
                checkConstraintMaxValue().validate(maxValueBox,
                        maxValueBox.getValue());
                BigDecimal maxValue = maxValueBox.getValue().setScale(2);
                addNewUnitMeasure(unitMeasure, maxValue);
                close();
                createComponents();
            } else {
                throw new WrongValueException(unitMeasureListBox,
                        _("must select an unit measuare"));
            }
        }
    }

    public void cancel() {
        close();
    }

    private void close() {
        if (windowAddUnitMeasure != null) {
            windowAddUnitMeasure.setVisible(false);
        }
        Util.reloadBindings(unitMeasureButton);
    }

    public Constraint checkConstraintMaxValue() {
        return new Constraint() {
            @Override
            public void validate(org.zkoss.zk.ui.Component comp, Object value)
                    throws WrongValueException {
                BigDecimal maxValue = (BigDecimal) value;
                if (maxValue == null) {
                    throw new WrongValueException(comp, _("must be no empty"));
                } else if (maxValue.compareTo(BigDecimal.ZERO) < 0) {
                    throw new WrongValueException(comp,
                            _("must be greater or equal than 0"));
                }
            }
        };
    }

    public void confirmRemove(ProgressValue progressValue) {
        try {
            int status = Messagebox.show(
                    _("Confirm deleting filming progress {0}. Are you sure?",
                            progressValue.getFilmingProgress().getType()),
                    _("Delete"), Messagebox.OK | Messagebox.CANCEL,
                    Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                removeFilmingProgress(progressValue.getFilmingProgress());
            }
        } catch (InterruptedException e) {
            messages.showMessage(Level.ERROR, e.getMessage());
            LOG.error(
                    _("Error on showing removing element: ", progressValue
                            .getFilmingProgress().getId()), e);
        }
    }

    private void removeFilmingProgress(FilmingProgress filmingProgress) {
        filmingProgressModel.removeFilmingProgress(filmingProgress);
        filmingProgressModel.loadDataFromOrder();
        renderAllValuesPerDay();
        refreshTotalPanel();
        updatesDates();
        createTimePlots();
        reloadNormalLayout();
    }
}

class RowTotal implements Comparable<RowTotal> {

    private BigDecimal total = BigDecimal.ZERO;
    private ForecastLevelEnum forecastLevelEnum;
    private FilmingProgressTypeEnum type;

    private Label lbType = new Label();
    private Label lbTotal = new Label();

    public RowTotal(FilmingProgressTypeEnum type, ForecastLevelEnum level,
            BigDecimal total) {
        this.forecastLevelEnum = level;
        this.setType(type);
        this.setTotal(total);
        this.lbType.setValue(type + " (" + forecastLevelEnum + ")");
    }

    private void setType(FilmingProgressTypeEnum type) {
        this.type = type;
    }

    public FilmingProgressTypeEnum getType() {
        return type;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
        this.lbTotal.setValue(total.toPlainString());
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setLbType(Label lbType) {
        this.lbType = lbType;
    }

    public Label getLbType() {
        return lbType;
    }

    public void setLbTotal(Label lbTotal) {
        this.lbTotal = lbTotal;
    }

    public Label getLbTotal() {
        return lbTotal;
    }

    public ForecastLevelEnum getForecastLevelEnum() {
        return forecastLevelEnum;
    }

    @Override
    public int compareTo(RowTotal o) {
        if (this.type.equals(o.getType())) {
            return (this.forecastLevelEnum.compareTo(o.getForecastLevelEnum()));
        }
        return this.type.compareTo(o.getType());
    }

}

class CustomHeader {
    String type = "";
    int colspan = 0;

    CustomHeader(String type, int colspan) {
        this.type = type;
        this.colspan = colspan;
    }
}
