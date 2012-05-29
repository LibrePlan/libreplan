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

import org.joda.time.LocalDate;
import org.libreplan.business.filmingprogress.entities.FilmingProgress;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.OnlyOneVisible;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.order.ISaveCommand;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
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

    private ProgressValuesPerDayRenderer valuesPerDayRenderer = new ProgressValuesPerDayRenderer();

    private ProgressTotalRenderer progressTotalRenderer = new ProgressTotalRenderer();

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
        filmingProgressModel.hookIntoSaveCommand();
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
        prepareFilmingProgressList();
        refreshTotalPanel();
        reloadNormalLayout();
    }

    private FilmingProgress getFilmingProgress() {
        return this.filmingProgressModel.getCurrentFilmingProgress();
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

    public ListModel getUnitMeasures() {
        return new SimpleListModel(getUnitMeasuresNotAdded());
    }

    private List<UnitMeasureFilmingProgress> getUnitMeasuresNotAdded() {
        List<UnitMeasureFilmingProgress> measures = new ArrayList<UnitMeasureFilmingProgress>(
                Arrays.asList(UnitMeasureFilmingProgress.values()));
        for(ProgressValue pgValue :this.getProgressValues()){
            measures.remove(pgValue.unitMeasure);
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
        gridValuesPerDay.setMold(null);
        gridValuesPerDay.getChildren().clear();

        // Set mold and pagesize
        gridValuesPerDay.setMold(MOLD);
        gridValuesPerDay.setPageSize(PAGING);

        appendColumns(gridValuesPerDay);

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
            Auxheader auxHeader = new Auxheader(customHeader.title);
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
            String title = dateFormat.format(date);
            list.add(new CustomHeader(title, mapByWeek.get(week).size()));
            }
        return list;
    }

    public List<ProgressValue> getProgressValues() {
        return filmingProgressModel.getProgressValues();
    }

    private void addNewUnitMeasure(UnitMeasureFilmingProgress unitMeasure,
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

        private Map<Row, RowTotal> panelTotal = new HashMap<Row, RowTotal>();

        @Override
        public void render(final Row row, Object data) {
            final ProgressValue progressValue = (ProgressValue) data;
            row.setValue(progressValue);

            panelTotal.put(row, calculateRowTotal(row));

            for (final Entry<LocalDate, BigDecimal> entry : progressValue
                    .getValues().entrySet()) {

                Decimalbox valuebox = new Decimalbox();

                Util.bind(valuebox, new Util.Getter<BigDecimal>() {

                    @Override
                    public BigDecimal get() {
                        return entry.getValue();
                    }

                }, new Util.Setter<BigDecimal>() {

                    @Override
                    public void set(BigDecimal newValue) {
                        updatePanelTotal(row, entry.getValue(), newValue);
                        entry.setValue(newValue);
                    }
                });

                row.appendChild(valuebox);
            }
        }

        private void updatePanelTotal(Row row, BigDecimal oldValue,
                BigDecimal newValue) {
            RowTotal rowTotal = panelTotal.get(row);

            BigDecimal total = rowTotal.getTotal();
             if (oldValue.compareTo(newValue)  >= 0) {
                 BigDecimal diff = oldValue.subtract(newValue);
                total = total.subtract(diff);
                rowTotal.setTotal(total);
             } else {
                BigDecimal diff = newValue.subtract(oldValue);
                total = total.add(diff);
                rowTotal.setTotal(total);
            }
        }

        private RowTotal calculateRowTotal(Row row) {
            ProgressValue progressValue = (ProgressValue) row.getValue();
            BigDecimal total = BigDecimal.ZERO;
            for (final Entry<LocalDate, BigDecimal> entry : progressValue
                    .getValues().entrySet()) {
                total = total.add(entry.getValue());
            }
            return new RowTotal(progressValue.unitMeasure,
                    progressValue.progressType.toString(), total);
        }
    }

    private void refreshTotalPanel() {
        Util.reloadBindings(gridTotals);
    }

    public void resetTotalPanel() {
        getProgressValuesRenderer().panelTotal = new HashMap<Row, RowTotal>();
    }

    public List<RowTotal> getRowTotals(){
        Map<Row, RowTotal> panelTotal = getProgressValuesRenderer().panelTotal;
        List<RowTotal> list = new ArrayList<RowTotal>(panelTotal.values());
        return list;
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

            String unitMeasureLabel = "";
            if (rowTotal.getUnitMeasure() != null) {
                unitMeasureLabel = rowTotal.getUnitMeasure().toString();
            }

            row.appendChild(new Label(unitMeasureLabel));
            row.appendChild(rowTotal.getLbTitle());
            row.appendChild(rowTotal.getLbTotal());
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
                UnitMeasureFilmingProgress unitMeasure = (UnitMeasureFilmingProgress) unitMeasureListBox
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

}

class RowTotal{
    private UnitMeasureFilmingProgress unitMeasure;
    private String title = "";
    private BigDecimal total = BigDecimal.ZERO;

    private Label lbTitle = new Label();
    private Label lbTotal = new Label();

    public RowTotal(UnitMeasureFilmingProgress unitMeasure, String title,
            BigDecimal total) {
        this.setTitle(title);
        this.setTotal(total);
        this.setUnitMeasure(unitMeasure);
    }

    public void setTitle(String title) {
        this.title = title;
        this.lbTitle.setValue(title);
    }

    public String getTitle() {
        return title;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
        this.lbTotal.setValue(total.toPlainString());
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setLbTitle(Label lbTitle) {
        this.lbTitle = lbTitle;
    }

    public Label getLbTitle() {
        return lbTitle;
    }

    public void setLbTotal(Label lbTotal) {
        this.lbTotal = lbTotal;
    }

    public Label getLbTotal() {
        return lbTotal;
    }

    public void setUnitMeasure(UnitMeasureFilmingProgress unitMeasure) {
        this.unitMeasure = unitMeasure;
    }

    public UnitMeasureFilmingProgress getUnitMeasure() {
        return unitMeasure;
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
