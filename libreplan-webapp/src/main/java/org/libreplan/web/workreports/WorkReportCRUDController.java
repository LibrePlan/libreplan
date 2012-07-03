/*
 * This file is part of LibrePlan
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

package org.libreplan.web.workreports;

import static org.libreplan.web.I18nHelper._;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.joda.time.LocalDate;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.entities.HoursManagementEnum;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLabelTypeAssigment;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.libreplan.business.workreports.valueobjects.DescriptionValue;
import org.libreplan.web.common.ConstraintChecker;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.OnlyOneVisible;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.Autocomplete;
import org.libreplan.web.common.components.NewDataSortableColumn;
import org.libreplan.web.common.components.NewDataSortableGrid;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.common.entrypoints.EntryPointsHandler;
import org.libreplan.web.common.entrypoints.IURLHandlerRegistry;
import org.libreplan.web.users.dashboard.IMonthlyTimesheetController;
import org.zkoss.ganttz.IPredicate;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.api.Window;
/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 *         Controller for CRUD actions over a {@link WorkReport}
 *
 */
public class WorkReportCRUDController extends GenericForwardComposer implements
        IWorkReportCRUDControllerEntryPoints {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(WorkReportCRUDController.class);

    private boolean cameBackList = false;

    private Window createWindow;

    private Window listWindow;

    private IWorkReportModel workReportModel;

    private IURLHandlerRegistry URLHandlerRegistry;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private IWorkReportTypeCRUDControllerEntryPoints workReportTypeCRUD;

    private WorkReportListRenderer workReportListRenderer = new WorkReportListRenderer();

    private OrderedFieldsAndLabelsRowRenderer orderedFieldsAndLabelsRowRenderer = new OrderedFieldsAndLabelsRowRenderer();

    private NewDataSortableGrid listWorkReportLines;

    private Grid headingFieldsAndLabels;

    private Autocomplete autocompleteResource;

    private BandboxSearch bandboxSelectOrderElementInHead;

    private ListModel allHoursType;

    private final static String MOLD = "paging";

    private final static int PAGING = 10;

    private static final String ITEM = "item";

    private static final int EXTRA_FIELD_MIN_WIDTH = 70;
    private static final int EXTRA_FIELD_MAX_WIDTH = 150;
    private static final int EXTRA_FIELD_PX_PER_CHAR = 5;

    private transient IPredicate predicate;

    private Grid listing;

    private Listbox listType;

    private Listbox listTypeToAssign;

    private Datebox filterStartDate;

    private Datebox filterFinishDate;

    @javax.annotation.Resource
    private IMonthlyTimesheetController monthlyTimesheetController;

    private Popup monthlyTimesheetsPopup;

    private Datebox monthlyTimesheetsDatebox;

    private BandboxSearch monthlyTimesheetsBandboxSearch;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        listWorkReportLines = (NewDataSortableGrid) createWindow
                .getFellowIfAny("listWorkReportLines");
        messagesForUser = new MessagesForUser(messagesContainer);
        showMessageIfMonthlyTimesheetWasSaved();

        comp.setAttribute("controller", this);
        goToList();
        if(listType != null) {
            //listType is null in reports -> work report lines
            listType.setSelectedIndex(0);
        }
        initializeHoursType();
        final EntryPointsHandler<IWorkReportCRUDControllerEntryPoints> handler = URLHandlerRegistry
                .getRedirectorFor(IWorkReportCRUDControllerEntryPoints.class);
        handler.register(this, page);
    }

    private void showMessageIfMonthlyTimesheetWasSaved() {
        String timesheetSave = Executions.getCurrent().getParameter(
                "timesheet_saved");
        if (!StringUtils.isBlank(timesheetSave)) {
            messagesForUser.showMessage(Level.INFO,
                    _("Monthly timesheet saved"));
        }
    }

    private void initializeHoursType() {
        allHoursType = new SimpleListModel(workReportModel.getAllHoursType());
    }

    /**
     * Show confirm window for deleting {@link WorkReport}
     *
     * @param workReport
     */
    public void showConfirmDelete(WorkReportDTO workReportDTO) {
        WorkReport workReport = workReportDTO.getWorkReport();
        try {

            final String workReportName = formatWorkReportName(workReport);
            int status = Messagebox.show(_("Confirm deleting {0}. Are you sure?", workReportName), "Delete",
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                workReportModel.remove(workReport);
                messagesForUser.showMessage(Level.INFO,
                        _("Timesheet removed successfully"));
                loadComponentslist(listWindow);
                Util.reloadBindings(listWindow);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatWorkReportName(WorkReport workReport) {
        return workReport.getWorkReportType().getName();
    }

    public List<WorkReportDTO> getWorkReportDTOs() {
        return workReportModel.getWorkReportDTOs();
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(createWindow,
                listWindow) : visibility;
    }

    public void saveAndExit() {
        if (save()) {
            goToList();
        }
    }

    public void saveAndContinue() {
        if (save()) {
            goToEditForm(getWorkReport());
        }
    }

    public boolean save() {
        ConstraintChecker.isValid(createWindow);
        workReportModel.generateWorkReportLinesIfIsNecessary();
        try {
            workReportModel.confirmSave();
            messagesForUser.showMessage(Level.INFO, _("Timesheet saved"));
            return true;
        } catch (ValidationException e) {
            showInvalidValues(e);
        } catch (Exception e) {
            if(!showInvalidProperty()) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * Shows invalid values for {@link WorkReport} and {@link WorkReportLine}
     * entities
     * @param e
     */
    private void showInvalidValues(ValidationException e) {
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            Object value = invalidValue.getBean();
            if (value instanceof WorkReport) {
                validateWorkReport();
            }
            if (value instanceof WorkReportLine) {
                WorkReportLine workReportLine = (WorkReportLine) invalidValue.getBean();
                Row row = ComponentsFinder.findRowByValue(listWorkReportLines, workReportLine);
                if (row == null) {
                    messagesForUser.showInvalidValues(e);
                } else {
                    validateWorkReportLine(row, workReportLine);
                }
            }
        }
    }

    private boolean showInvalidProperty() {
        WorkReport workReport = getWorkReport();
        if (workReport != null) {
            if (!validateWorkReport()) {
                return true;
            }
            for (WorkReportLine each : workReport.getWorkReportLines()) {
                if (!validateWorkReportLine(each)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Validates {@link WorkReport} data constraints
     * @param invalidValue
     */
    private boolean validateWorkReport() {

        if (!getWorkReport()
                .checkConstraintDateMustBeNotNullIfIsSharedByLines()) {
            Datebox datebox = (Datebox) createWindow.getFellowIfAny("date");
            showInvalidMessage(datebox, _("cannot be empty"));
            return false;
        }

        if (!getWorkReport()
                .checkConstraintResourceMustBeNotNullIfIsSharedByLines()) {
            showInvalidMessage(autocompleteResource,
                    _("cannot be empty"));
            return false;
        }

        if (!getWorkReport()
                .checkConstraintOrderElementMustBeNotNullIfIsSharedByLines()) {
            showInvalidMessage(bandboxSelectOrderElementInHead,
                    _("cannot be empty"));
            return false;
        }
        return true;
    }

    private boolean validateWorkReportLine(WorkReportLine workReportLine) {
        Row row = ComponentsFinder.findRowByValue(listWorkReportLines,
                workReportLine);
        return row != null ? validateWorkReportLine(row, workReportLine)
                : false;
    }

    /**
     * Validates {@link WorkReportLine} data constraints
     *
     * @param invalidValue
     */
    @SuppressWarnings("unchecked")
    private boolean validateWorkReportLine(Row row,
            WorkReportLine workReportLine) {

        if (getWorkReportType().getDateIsSharedByLines()) {
            if (!validateWorkReport()) {
                return false;
            }
        } else if (workReportLine.getDate() == null) {
            Datebox date = getDateboxDate(row);
            if (date != null) {
                String message = _("cannot be empty");
                showInvalidMessage(date, message);
            }
            return false;
        }

        if (getWorkReportType().getResourceIsSharedInLines()) {
            if (!validateWorkReport()) {
                return false;
            }
        } else if (workReportLine.getResource() == null) {
            Autocomplete autoResource = getTextboxResource(row);
            if (autoResource != null) {
                String message = _("cannot be empty");
                showInvalidMessage(autoResource, message);
            }
            return false;
        }

        if (getWorkReportType().getOrderElementIsSharedInLines()) {
            if (!validateWorkReport()) {
                return false;
            }
        } else if (workReportLine.getOrderElement() == null) {
            BandboxSearch bandboxOrder = getTextboxOrder(row);
            if (bandboxOrder != null) {
                String message = _("cannot be empty");
                bandboxOrder.clear();
                showInvalidMessage(bandboxOrder, message);
            }
            return false;
        }

        if (!workReportLine
                .checkConstraintClockStartMustBeNotNullIfIsCalculatedByClock()) {
            Timebox timeStart = getTimeboxStart(row);
            if (timeStart != null) {
                String message = _("cannot be empty");
                showInvalidMessage(timeStart, message);
            }
            return false;
        }

        if (!workReportLine
                .checkConstraintClockFinishMustBeNotNullIfIsCalculatedByClock()) {
            Timebox timeFinish = getTimeboxFinish(row);
            if (timeFinish != null) {
                String message = _("cannot be empty");
                showInvalidMessage(timeFinish, message);
            }
            return false;
        }

        if (workReportLine.getEffort() == null) {
            Textbox effort = getEffort(row);
            if (effort == null) {
                String message = _("cannot be empty");
                showInvalidMessage(effort, message);
            }
            if (EffortDuration.parseFromFormattedString(effort.getValue())
                    .compareTo(EffortDuration.zero()) <= 0) {
                String message = _("Effort must be greater than zero");
                showInvalidMessage(effort, message);
            }
            return false;
        }

        if (!workReportLine.checkConstraintHoursCalculatedByClock()) {
            Textbox effort = getEffort(row);
            if (effort != null) {
                String message = _("effort is not properly calculated based on clock");
                showInvalidMessage(effort, message);
            }
            return false;
        }

        if (workReportLine.getTypeOfWorkHours() == null) {
            // Locate TextboxOrder
            Listbox autoTypeOfHours = getTypeOfHours(row);
            if (autoTypeOfHours != null) {
                String message = autoTypeOfHours.getItems().isEmpty() ? _("Hours types are empty. Please, create some hours types before proceeding")
                        : _("cannot be empty");
                showInvalidMessage(autoTypeOfHours, message);
            }
            return false;
        }

        if ((!getWorkReport().isCodeAutogenerated())
                && (workReportLine.getCode() == null || workReportLine
                        .getCode().isEmpty())) {
            // Locate TextboxCode
            Textbox txtCode = getCode(row);
            if (txtCode != null) {
                String message = _("cannot be empty.");
                showInvalidMessage(txtCode, message);
            }
            return false;
        }
        return true;
    }

    private void showInvalidMessage(Component comp, String message) {
        throw new WrongValueException(comp, message);
    }

    /**
     * Locates {@link Timebox} time finish in {@link Row}
     * @param row
     * @return
     */
    private Timebox getTimeboxFinish(Row row) {
        try {
            int position = row.getChildren().size() - 5;
            return (Timebox) row.getChildren().get(position);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Locates {@link Timebox} time start in {@link Row}
     * @param row
     * @return
     */
    private Timebox getTimeboxStart(Row row) {
        try {
            int position = row.getChildren().size() - 6;
            return (Timebox) row.getChildren().get(position);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Locates {@link Autocomplete} type of work hours in {@link Row}
     * @param row
     * @return
     */
    private Listbox getTypeOfHours(Row row) {
        try {
            int position = row.getChildren().size() - 3;
            return (Listbox) row.getChildren().get(position);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Locates {@link Texbox} code in {@link Row}
     * @param row
     * @return
     */
    private Textbox getCode(Row row) {
        try {
            int position = row.getChildren().size() - 2;
            return (Textbox) row.getChildren().get(position);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Locates {@link Textbox} effort in {@link Row}
     *
     * @param row
     * @return
     */
    private Textbox getEffort(Row row) {
        try {
            int position = row.getChildren().size() - 4;
            return (Textbox) row.getChildren().get(position);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Locates {@link Datebox} date in {@link Row}
     * @param row
     * @return
     */
    private Datebox getDateboxDate(Row row) {
        try {
            return (Datebox) row.getChildren().get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Locates {@link Textbox} Resource in {@link Row}
     * @param row
     * @return
     */
    private Autocomplete getTextboxResource(Row row) {
        int position = 0;
        if (!getWorkReportType().getDateIsSharedByLines()) {
            position++;
        }
        try {
            return (Autocomplete) row.getChildren().get(position);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Locates {@link Textbox} Order in {@link Row}
     *
     * @param row
     * @return
     */
    private BandboxSearch getTextboxOrder(Row row) {
        int position = 0;
        if (!getWorkReportType().getDateIsSharedByLines()) {
            position++;
        }
        if (!getWorkReportType().getResourceIsSharedInLines()) {
            position++;
        }
        try {
            return (BandboxSearch) row.getChildren().get(position);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void goToList() {
        getVisibility().showOnly(listWindow);
        loadComponentslist(listWindow);
        Util.reloadBindings(listWindow);
    }

    public void cancel() {
        if (cameBackList || workReportModel.isEditing()) {
            goToList();
        } else {
            workReportTypeCRUD.goToList();
        }
    }

    @Override
    public void goToCreateForm(WorkReportType workReportType) {
        if (workReportType.isMonthlyTimesheetsType()) {
            monthlyTimesheetsPopup.open(listTypeToAssign);
        } else {
            cameBackList = false;
            workReportModel.initCreate(workReportType);
            prepareWorkReportList();
            createWindow.setTitle(_("Create Timesheet"));
            getVisibility().showOnly(createWindow);
            loadComponents(createWindow);
            Util.reloadBindings(createWindow);
        }
    }

    public void goToEditForm(WorkReportDTO workReportDTO) {
        goToEditForm(workReportDTO.getWorkReport());
    }

    @Override
    public void goToEditForm(WorkReport workReport) {
        if (workReportModel.isMonthlyTimesheet(workReport)) {
            goToEditMonthlyTimeSheet(workReport);
        } else {
            workReportModel.initEdit(workReport);
            createWindow.setTitle(_("Edit Timesheet"));
            loadComponents(createWindow);
            prepareWorkReportList();
            getVisibility().showOnly(createWindow);
            Util.reloadBindings(createWindow);
        }
    }

    private void goToEditMonthlyTimeSheet(WorkReport workReport) {
        Date date = workReport.getWorkReportLines().iterator().next().getDate();
        Resource resource = workReport.getResource();
        monthlyTimesheetController.goToCreateOrEditFormForResource(
                LocalDate.fromDateFields(date), resource);
    }

    private void loadComponents(Component window) {
        listWorkReportLines = (NewDataSortableGrid) window
                .getFellow("listWorkReportLines");
        headingFieldsAndLabels = (Grid) window
                .getFellow("headingFieldsAndLabels");
        autocompleteResource = (Autocomplete) window
                .getFellow("autocompleteResource");
        bandboxSelectOrderElementInHead = (BandboxSearch) window
                .getFellow("bandboxSelectOrderElementInHead");
        bandboxSelectOrderElementInHead.setListboxWidth("750px");
        bandboxSelectOrderElementInHead.setListboxEventListener(Events.ON_SELECT,
                new EventListener() {
            @Override
            public void onEvent(Event event) {
                Listitem selectedItem = (Listitem) ((SelectEvent) event)
                        .getSelectedItems().iterator().next();
                OrderElement orderElement = (OrderElement) selectedItem
                        .getValue();
                getWorkReport().setOrderElement(orderElement);
            }
        });

        bandboxSelectOrderElementInHead.setListboxEventListener(Events.ON_OK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        Listitem selectedItem = bandboxSelectOrderElementInHead
                                .getSelectedItem();
                        if ((selectedItem != null) && (getWorkReport() != null)) {
                            getWorkReport().setOrderElement(
                                    (OrderElement) selectedItem.getValue());
                        }
                        bandboxSelectOrderElementInHead.close();
                    }
                });

    }

    private void loadComponentslist(Component window) {
        // components work report list
        listing = (Grid) window.getFellow("listing");
        listType = (Listbox) window.getFellow("listType");
        listTypeToAssign = (Listbox) window.getFellow("listTypeToAssign");
        filterStartDate = (Datebox) window.getFellow("filterStartDate");
        filterFinishDate = (Datebox) window.getFellow("filterFinishDate");
        monthlyTimesheetsPopup = (Popup) window
                .getFellow("monthlyTimesheetsPopup");
        monthlyTimesheetsDatebox = (Datebox) window
                .getFellow("monthlyTimesheetsDatebox");
        monthlyTimesheetsBandboxSearch = (BandboxSearch) window
                .getFellow("monthlyTimesheetsBandboxSearch");
        clearFilterDates();
    }

    /**
     * {@link WorkReportLine} list is finally constructed dynamically
     *
     * It seems there are some problems when a list of data is rendered,
     * modified (the data model changes), and it's rendered again. Deleting
     * previous settings and re-establishing the settings again each time the
     * list is rendered, solve those problems.
     *
     */
    private void prepareWorkReportList() {
        /*
         * The only way to clean the listhead, is to clean all its attributes
         * and children The paging component cannot be removed manually. It is
         * removed automatically when changing the mold
         */
         listWorkReportLines.setMold(null);
         listWorkReportLines.getChildren().clear();

         // Set mold and pagesize
         listWorkReportLines.setMold(MOLD);
         listWorkReportLines.setPageSize(PAGING);

        appendColumns(listWorkReportLines);
        listWorkReportLines
                .setSortedColumn((NewDataSortableColumn) listWorkReportLines
                        .getColumns().getFirstChild());

        listWorkReportLines.setModel(new SimpleListModel(getWorkReportLines()
                .toArray()));
    }

    /**
     * Appends list headers to {@link WorkReportLine} list
     *
     * @param listBox
     */
    private void appendColumns(Grid grid) {

        Columns columns = grid.getColumns();
        // Create listhead first time is rendered
        if (columns == null) {
            columns = new Columns();
        }
        // Delete all headers
        columns.getChildren().clear();
        columns.setSizable(true);

        // Add static headers
        if (getWorkReport() != null) {
            if (!getWorkReport().getWorkReportType().getDateIsSharedByLines()) {
                NewDataSortableColumn columnDate = new NewDataSortableColumn();
                columnDate.setLabel(_("Date"));
                columnDate.setSclass("date-column");
                Util.setSort(columnDate, "auto=(date)");
                columnDate.setSortDirection("ascending");

                columnDate.addEventListener("onSort", new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        sortWorkReportLines();
                    }
                });
                columns.appendChild(columnDate);
            }
            if (!getWorkReport().getWorkReportType()
                    .getResourceIsSharedInLines()) {
                NewDataSortableColumn columnResource = new NewDataSortableColumn();
                columnResource.setLabel(_("Resource"));
                columnResource.setSclass("resource-column");
                // columnResource.setWidth("75px");
                columns.appendChild(columnResource);
            }
            if (!getWorkReport().getWorkReportType()
                    .getOrderElementIsSharedInLines()) {
                NewDataSortableColumn columnCode = new NewDataSortableColumn();
                columnCode.setLabel(_("Task Code"));
                columnCode.setSclass("order-code-column");
                columnCode.setHflex("min");
                columns.appendChild(columnCode);
            }

            for (Object fieldOrLabel : workReportModel
                    .getFieldsAndLabelsLineByDefault()) {
                String columnName;
                int width = EXTRA_FIELD_MIN_WIDTH;
                if (fieldOrLabel instanceof DescriptionField) {
                    columnName = ((DescriptionField) fieldOrLabel)
                            .getFieldName();
                    width = Math.max(((DescriptionField) fieldOrLabel)
                            .getLength()
                            * EXTRA_FIELD_PX_PER_CHAR, EXTRA_FIELD_MIN_WIDTH);
                    width = Math.min(width, EXTRA_FIELD_MAX_WIDTH);

                } else {
                    columnName = ((WorkReportLabelTypeAssigment) fieldOrLabel)
                            .getLabelType().getName();
                }
                NewDataSortableColumn columnFieldOrLabel = new NewDataSortableColumn();
                columnFieldOrLabel.setLabel(_(columnName));
                columnFieldOrLabel.setSclass("columnFieldOrLabel");
                columnFieldOrLabel.setWidth(width + "px");
                columns.appendChild(columnFieldOrLabel);
            }

            if (!getWorkReport().getWorkReportType().getHoursManagement()
                    .equals(HoursManagementEnum.NUMBER_OF_HOURS)) {
                NewDataSortableColumn columnHourStart = new NewDataSortableColumn();
                columnHourStart.setLabel(_("Start hour"));
                columnHourStart.setSclass("column-hour-start");
                columns.appendChild(columnHourStart);
                NewDataSortableColumn columnHourFinish = new NewDataSortableColumn();
                columnHourFinish.setLabel(_("Finish Hour"));
                columnHourStart.setSclass("column-hour-finish");
                columns.appendChild(columnHourFinish);
            }
        }
        NewDataSortableColumn columnNumHours = new NewDataSortableColumn();
        columnNumHours.setLabel(_("Hours"));
        columnNumHours.setSclass("hours-column");
        columns.appendChild(columnNumHours);
        NewDataSortableColumn columnHoursType = new NewDataSortableColumn();
        columnHoursType.setLabel(_("Hours type"));
        columnHoursType.setSclass("hours-type-column");
        columns.appendChild(columnHoursType);
        NewDataSortableColumn columnCode = new NewDataSortableColumn();
        columnCode.setLabel(_("Code"));
        columnCode.setSclass("code-column");
        columns.appendChild(columnCode);
        NewDataSortableColumn columnOperations = new NewDataSortableColumn();
        columnOperations.setLabel(_("Op."));
        columnOperations.setSclass("operations-column");
        columnOperations.setTooltiptext(_("Operations"));
        columns.appendChild(columnOperations);

        columns.setParent(grid);

    }

    private WorkReportType getWorkReportType() {
        return getWorkReport().getWorkReportType();
    }

    public WorkReport getWorkReport() {
        return workReportModel.getWorkReport();
    }

    /**
     * Adds a new {@link WorkReportLine} to the list of rows
     *
     * @param rows
     */
    public void addWorkReportLine() {
        workReportModel.addWorkReportLine();
        reloadWorkReportLines();
    }

    private void removeWorkReportLine(WorkReportLine workReportLine) {
        workReportModel.removeWorkReportLine(workReportLine);
        reloadWorkReportLines();
    }

    public List<WorkReportLine> getWorkReportLines() {
        return workReportModel.getWorkReportLines();
    }

    private void appendDateInLines(final Row row) {
        final Datebox date = new Datebox();
        final WorkReportLine line = (WorkReportLine) row.getValue();
        Util.bind(date, new Util.Getter<Date>() {

            @Override
            public Date get() {
                if (line != null) {
                    return line.getDate();
                }
                return null;
            }

        }, new Util.Setter<Date>() {

            @Override
            public void set(Date value) {
                if (line != null) {
                    line.setDate(value);
                }
            }
        });
        row.appendChild(date);
    }

    /**
     * Append a Autocomplete @{link Resource} to row
     *
     * @param row
     */
    private void appendResourceInLines(final Row row) {
        final Autocomplete autocomplete = new Autocomplete();
        autocomplete.setWidth("200px");
        autocomplete.setAutodrop(true);
        autocomplete.applyProperties();
        autocomplete.setFinder("ResourceFinder");

        // Getter, show worker selected
        if (getResource(row) != null) {
            autocomplete.setSelectedItem(getResource(row));
        }

        autocomplete.addEventListener("onChange", new EventListener() {
            @Override
            public void onEvent(Event event) {
                changeResourceInLines(autocomplete, row);
            }
        });
        row.appendChild(autocomplete);
    }

    private void changeResourceInLines(final Autocomplete autocomplete, Row row) {
        final WorkReportLine workReportLine = (WorkReportLine) row.getValue();
        final Comboitem comboitem = autocomplete.getSelectedItem();
        if ((comboitem == null) || ((Resource) comboitem.getValue() == null)) {
            workReportLine.setResource(null);
            throw new WrongValueException(autocomplete,
                    _("Please, select an item"));
        } else {
            workReportLine.setResource((Resource) comboitem.getValue());
        }
    }

    private Resource getResource(Row listitem) {
        WorkReportLine workReportLine = (WorkReportLine) listitem.getValue();
        return workReportLine.getResource();
    }

    /**
     * Append a Textbox @{link Order} to row
     *
     * @param row
     */
    private void appendOrderElementInLines(Row row) {
        final WorkReportLine workReportLine = (WorkReportLine) row.getValue();

        final BandboxSearch bandboxSearch = BandboxSearch.create(
                "OrderElementBandboxFinder", getOrderElements());

        bandboxSearch.setSelectedElement(workReportLine.getOrderElement());
        bandboxSearch.setSclass("bandbox-workreport-task");
        bandboxSearch.setListboxWidth("750px");

        bandboxSearch.setListboxEventListener(Events.ON_SELECT,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        Listitem selectedItem = bandboxSearch.getSelectedItem();
                        setOrderElementInWRL(selectedItem, workReportLine);
                    }
                });

        bandboxSearch.setListboxEventListener(Events.ON_OK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        Listitem selectedItem = bandboxSearch.getSelectedItem();
                        setOrderElementInWRL(selectedItem, workReportLine);
                        bandboxSearch.close();
                    }
                });

        row.appendChild(bandboxSearch);
    }

    private void setOrderElementInWRL(Listitem selectedItem, WorkReportLine line) {
        OrderElement orderElement = (OrderElement) selectedItem.getValue();
        line.setOrderElement(orderElement);
    }

    private void appendFieldsAndLabelsInLines(final Row row){
        final WorkReportLine line = (WorkReportLine)row.getValue();
        for(Object fieldOrLabel : getFieldsAndLabelsLine(line)){
            if(fieldOrLabel instanceof DescriptionValue){
                appendNewTextbox(row, (DescriptionValue) fieldOrLabel);
            } else if (fieldOrLabel instanceof Label) {
                appendAutocompleteLabelsByTypeInLine(row,
                        ((Label) fieldOrLabel));
            }
        }
    }

    private void appendAutocompleteLabelsByTypeInLine(Row row,
            final Label currentLabel) {
        final LabelType labelType = currentLabel.getType();
        final WorkReportLine line = (WorkReportLine) row.getValue();
        final Autocomplete comboLabels = createAutocompleteLabels(labelType,
                currentLabel);
        comboLabels.setParent(row);

        comboLabels.addEventListener(Events.ON_CHANGE, new EventListener() {
            @Override
            public void onEvent(Event event) {
                if (comboLabels.getSelectedItem() != null) {
                    Label newLabel = (Label) comboLabels.getSelectedItem()
                            .getValue();
                    workReportModel.changeLabelInWorkReportLine(currentLabel,
                            newLabel, line);
                }
                reloadWorkReportLines();
            }
        });
    }

    private void appendHoursStartAndFinish(final Row row) {
        final WorkReportLine line = (WorkReportLine) row.getValue();

        final Timebox timeStart = getNewTimebox();
        final Timebox timeFinish = getNewTimebox();

        row.appendChild(timeStart);
        row.appendChild(timeFinish);

        Util.bind(timeStart, new Util.Getter<Date>() {

            @Override
            public Date get() {
                if ((line != null) && (line.getClockStart() != null)) {
                    return line.getClockStart().toDateTimeToday().toDate();
                }
                return null;
            }

        }, new Util.Setter<Date>() {

            @Override
            public void set(Date value) {
                if (line != null) {
                    checkCannotBeHigher(timeStart, timeFinish);
                    setClock(line, timeStart, timeFinish);
                    updateEffort(row);
                }
            }

        });

        Util.bind(timeFinish, new Util.Getter<Date>() {

            @Override
            public Date get() {
                if ((line != null) && (line.getClockStart() != null)) {
                    return line.getClockFinish().toDateTimeToday().toDate();
                }
                return null;
            }

        }, new Util.Setter<Date>() {

            @Override
            public void set(Date value) {
                if (line != null) {
                    checkCannotBeHigher(timeStart, timeFinish);
                    setClock(line, timeStart, timeFinish);
                    updateEffort(row);
                }
            }
        });
    }

    protected void setClock(WorkReportLine line, Timebox timeStart,
            Timebox timeFinish) {
        line.setClockStart(timeStart.getValue());
        line.setClockFinish(timeFinish.getValue());
    }

    private Timebox getNewTimebox() {
        final Timebox timeStart = new Timebox();
        timeStart.setWidth("60px");
        timeStart.setFormat("short");
        timeStart.setButtonVisible(true);
        return timeStart;
    }

    private void updateEffort(final Row row) {
        WorkReportLine line = (WorkReportLine) row.getValue();
        Textbox effort = getEffort(row);
        if (effort != null && line.getEffort() != null) {
            effort.setValue(line.getEffort().toFormattedString());
            effort.invalidate();
        }
    }

    public void checkCannotBeHigher(Timebox starting, Timebox ending) {
        starting.clearErrorMessage(true);
        ending.clearErrorMessage(true);

        final Date startingDate = starting.getValue();
        final Date endingDate = ending.getValue();

        if (endingDate == null || startingDate == null
                || startingDate.compareTo(endingDate) > 0) {
            throw new WrongValueException(starting,
                    _("Cannot be higher than finish hour"));
        }
    }

    /**
     * Append a {@link Textbox} effort to {@link Row}
     *
     * @param row
     */
    private void appendEffortDuration(Row row) {
        WorkReportLine workReportLine = (WorkReportLine) row.getValue();
        Textbox effort = new Textbox();
        effort.setConstraint(new Constraint() {

            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if (!Pattern.matches("(\\d+)(\\s*:\\s*\\d+\\s*)*",
                        (String) value))
                    throw new WrongValueException(comp,
                            _("Please, enter a valid effort"));
            }
        });
        bindEffort(effort, workReportLine);

        if (getWorkReportType().getHoursManagement().equals(
                HoursManagementEnum.HOURS_CALCULATED_BY_CLOCK)) {
            effort.setDisabled(true);
        }
        row.appendChild(effort);
    }

    /**
     * Append Selectbox of @{link TypeOfWorkHours} to row
     *
     * @param row
     */
    private void appendHoursType(final Row row) {
        final WorkReportLine workReportLine = (WorkReportLine) row.getValue();
        final Listbox lbHoursType = new Listbox();
        lbHoursType.setMold("select");
        lbHoursType.setModel(allHoursType);
        lbHoursType.renderAll();
        lbHoursType.applyProperties();

        if (lbHoursType.getItems().isEmpty()) {
            row.appendChild(lbHoursType);
            return;
        }

        // First time is rendered, select first item
        TypeOfWorkHours type = workReportLine.getTypeOfWorkHours();
        if (workReportLine.isNewObject() && type == null) {
            Listitem item = lbHoursType.getItemAtIndex(0);
            item.setSelected(true);
            setHoursType(workReportLine, item);
        } else {
            // If workReportLine has a type, select item with that type
            Listitem item = ComponentsFinder.findItemByValue(lbHoursType, type);
            if (item != null) {
                lbHoursType.selectItem(item);
            }
        }

        lbHoursType.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) {
                Listitem item = lbHoursType.getSelectedItem();
                if (item != null) {
                    setHoursType((WorkReportLine) row.getValue(), item);
                }
            }

        });

        row.appendChild(lbHoursType);
    }

    private void setHoursType(WorkReportLine workReportLine, Listitem item) {
        TypeOfWorkHours value = item != null ? (TypeOfWorkHours) item
                .getValue() : null;
        workReportLine.setTypeOfWorkHours(value);
        if (value == null) {
            throw new WrongValueException(item.getParent(),
                    _("Please, select an item"));
        }
    }

    private void appendCode(final Row row) {
        final WorkReportLine line = (WorkReportLine) row.getValue();
        final Textbox code = new Textbox();
        code.setDisabled(getWorkReport().isCodeAutogenerated());
        code.applyProperties();

         if (line.getCode() != null) {
             code.setValue(line.getCode());
         }

        code.addEventListener("onChange", new EventListener() {
            @Override
            public void onEvent(Event event) {
                final WorkReportLine line = (WorkReportLine) row.getValue();
                line.setCode(code.getValue());
            }
        });
        row.appendChild(code);
    }

    /**
     * Append a delete {@link Button} to {@link Row}
     *
     * @param row
     */
    private void appendDeleteButton(final Row row) {
        Button delete = new Button("", "/common/img/ico_borrar1.png");
        delete.setHoverImage("/common/img/ico_borrar.png");
        delete.setSclass("icono");
        delete.setTooltiptext(_("Delete"));
        delete.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) {
                confirmRemove((WorkReportLine) row.getValue());
            }
        });
        row.appendChild(delete);
    }

    public void confirmRemove(WorkReportLine workReportLine) {
        try {
            int status = Messagebox.show(_("Confirm deleting {0}. Are you sure?", getWorkReportLineName(workReportLine)), _("Delete"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                removeWorkReportLine(workReportLine);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getWorkReportLineName(WorkReportLine workReportLine) {
        final Resource resource = workReportLine.getResource();
        final OrderElement orderElement = workReportLine.getOrderElement();

        if (resource == null || orderElement == null) {
            return ITEM;
        }
        return resource.getShortDescription() + " - " + orderElement.getCode();
    }

    /**
     * Binds Textbox effort to a {@link WorkReportLine} numHours
     *
     * @param effort
     * @param workReportLine
     */
    private void bindEffort(final Textbox box,
         final WorkReportLine workReportLine) {
        Util.bind(box, new Util.Getter<String>() {

            @Override
            public String get() {
                if (workReportLine.getEffort() != null)
                    return workReportLine.getEffort().toFormattedString();
                else
                    return EffortDuration.zero().toFormattedString();
            }

        }, new Util.Setter<String>() {

            @Override
            public void set(String value) {
                workReportLine.setEffort(EffortDuration
                        .parseFromFormattedString(value));
            }
        });
     }

    public WorkReportListRenderer getRenderer() {
        return workReportListRenderer;
    }

    /**
     * RowRenderer for a @{WorkReportLine} element
     * @author Diego Pino García <dpino@igalia.com>
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    public class WorkReportListRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) {
            WorkReportLine workReportLine = (WorkReportLine) data;

            row.setValue(workReportLine);

            // Create textboxes
            if (!getWorkReport().getWorkReportType().getDateIsSharedByLines()) {
                appendDateInLines(row);
            }
            if (!getWorkReport().getWorkReportType()
                    .getResourceIsSharedInLines()) {
                appendResourceInLines(row);
            }
            if (!getWorkReport().getWorkReportType()
                    .getOrderElementIsSharedInLines()) {
                appendOrderElementInLines(row);
            }

            // Create the fields and labels
            appendFieldsAndLabelsInLines(row);

            if (!getWorkReport().getWorkReportType().getHoursManagement()
                    .equals(HoursManagementEnum.NUMBER_OF_HOURS)) {
                appendHoursStartAndFinish(row);
            }

            appendEffortDuration(row);
            appendHoursType(row);
            appendCode(row);
            appendDeleteButton(row);
        }
    }

    public OrderedFieldsAndLabelsRowRenderer getOrderedFieldsAndLabelsRowRenderer() {
        return orderedFieldsAndLabelsRowRenderer;
    }

    public class OrderedFieldsAndLabelsRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) {
            row.setValue(data);

            if (data instanceof DescriptionValue) {
                appendNewLabel(row, ((DescriptionValue) data).getFieldName());
                appendNewTextbox(row, ((DescriptionValue) data));
            } else {
                appendNewLabel(row, ((Label) data).getType().getName());
                appendAutocompleteLabelsByType(row, ((Label) data));
            }
        }
    }

    private void appendNewLabel(Row row, String label) {
        org.zkoss.zul.Label labelName = new org.zkoss.zul.Label();
        labelName.setParent(row);
        labelName.setValue(label);
    }

    private void appendNewTextbox(Row row,
            final DescriptionValue descriptionValue) {
        Textbox textbox = new Textbox();
        Integer length = workReportModel.getLength(descriptionValue);
        textbox.setCols(length);
        textbox.setParent(row);
        textbox.setTooltiptext(descriptionValue.getValue());

        Util.bind(textbox, new Util.Getter<String>() {

            @Override
            public String get() {
                if (descriptionValue != null) {
                    return descriptionValue.getValue();
                }
                return "";
            }

        }, new Util.Setter<String>() {

            @Override
            public void set(String value) {
                if (descriptionValue != null) {
                    descriptionValue.setValue(value);
                }
            }
        });
    }

    private void appendAutocompleteLabelsByType(Row row,
            final Label currentLabel) {
        final LabelType labelType = currentLabel.getType();
        final Autocomplete comboLabels = createAutocompleteLabels(labelType,
                currentLabel);
        comboLabels.setParent(row);

        comboLabels.addEventListener(Events.ON_CHANGE, new EventListener() {
            @Override
            public void onEvent(Event event) {
                if(comboLabels.getSelectedItem() != null){
                    Label newLabel = (Label) comboLabels.getSelectedItem()
                            .getValue();
                    workReportModel.changeLabelInWorkReport(currentLabel,
                            newLabel);
                }
                Util.reloadBindings(headingFieldsAndLabels);
            }
        });
    }

    private Autocomplete createAutocompleteLabels(LabelType labelType,Label selectedLabel) {
        Autocomplete comboLabels = new Autocomplete();
        comboLabels.setButtonVisible(true);
        comboLabels.setWidth("100px");

        if (labelType != null) {
            final List<Label> listLabel = getMapLabelTypes()
                    .get(labelType);

            for (Label label : listLabel) {
                Comboitem comboItem = new Comboitem();
                comboItem.setValue(label);
                comboItem.setLabel(label.getName());
                comboItem.setParent(comboLabels);

                if ((selectedLabel != null)
                        && (selectedLabel.equals(label))) {
                    comboLabels.setSelectedItem(comboItem);
                }
            }
        }
        return comboLabels;
    }

    public List<Object> getFieldsAndLabelsHeading() {
        return workReportModel.getFieldsAndLabelsHeading();
    }

    public List<Object> getFieldsAndLabelsLine(WorkReportLine workReportLine) {
        return workReportModel.getFieldsAndLabelsLine(workReportLine);
    }

    private Map<LabelType, List<Label>> getMapLabelTypes() {
        return workReportModel.getMapAssignedLabelTypes();
    }

    public void changeResource(Comboitem selectedItem) {
        if (selectedItem != null) {
            getWorkReport().setResource((Resource) selectedItem.getValue());
        } else {
            getWorkReport().setResource(null);
        }
    }

    private void reloadWorkReportLines() {
        this.prepareWorkReportList();
        Util.reloadBindings(listWorkReportLines);
    }

    private void sortWorkReportLines() {
        listWorkReportLines.setModel(new SimpleListModel(getWorkReportLines()
                .toArray()));
    }

    public void sortWorkReports() {
        Column columnDateStart = (Column) listWindow
                .getFellow("columnDateStart");
        if (columnDateStart != null) {
            if (columnDateStart.getSortDirection().equals("ascending")) {
                columnDateStart.sort(false, false);
                columnDateStart.setSortDirection("ascending");
            } else if (columnDateStart.getSortDirection().equals("descending")) {
                columnDateStart.sort(true, false);
                columnDateStart.setSortDirection("descending");
            }
        }
    }

    public List<WorkReportType> getFilterWorkReportTypes() {
        List<WorkReportType> result = workReportModel.getWorkReportTypes();
        if (result.isEmpty()) {
            result.add(getDefaultWorkReportType());
        } else {
            result.add(0, getDefaultWorkReportType());
        }
        return result;
    }

    public List<WorkReportType> getWorkReportTypes() {
        List<WorkReportType> result = workReportModel.getWorkReportTypes();
        if (!result.isEmpty()) {
            this.firstType = result.get(0);
        }
        return result;
    }

    public WorkReportType getDefaultWorkReportType() {
        return workReportModel.getDefaultType();
    }

    public void setDefaultWorkReportType(WorkReportType type) {

    }

    /**
     * Apply filter to work reports
     * @param event
     */
    public void onApplyFilter(Event event) {
        createPredicate();
        filterByPredicate();
    }

    public Constraint checkConstraintFinishDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date finishDate = (Date) value;
                if ((finishDate != null)
                        && (filterStartDate.getValue() != null)
                        && (finishDate.compareTo(filterStartDate.getValue()) < 0)) {
                    filterFinishDate.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be later than start date"));
                }
            }
        };
    }

    public Constraint checkConstraintStartDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date startDate = (Date) value;
                if ((startDate != null)
                        && (filterFinishDate.getValue() != null)
                        && (startDate.compareTo(filterFinishDate.getValue()) > 0)) {
                    filterStartDate.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be before finish date"));
                }
            }
        };
    }

    private void createPredicate() {
        WorkReportType type = getSelectedType();
        Date startDate = filterStartDate.getValue();
        Date finishDate = filterFinishDate.getValue();
        predicate = new WorkReportPredicate(type, startDate, finishDate);
    }

    private WorkReportType getSelectedType() {
        Listitem itemSelected = listType.getSelectedItem();
        if ((itemSelected != null)
                && (!((WorkReportType) itemSelected.getValue())
                        .equals(getDefaultWorkReportType()))) {
            return (WorkReportType) itemSelected.getValue();
        }
        return null;
    }

    private void filterByPredicate() {
        List<WorkReportDTO> filterWorkReports = workReportModel
                .getFilterWorkReportDTOs(predicate);
        listing.setModel(new SimpleListModel(filterWorkReports.toArray()));
        listing.invalidate();
    }

    private void clearFilterDates() {
        filterStartDate.setValue(null);
        filterFinishDate.setValue(null);
    }

    public List<OrderElement> getOrderElements() {
        return workReportModel.getOrderElements();
    }

    /**
     * Methods improved the work report edition and creation.Executed on
     * pressing New work report button Creates a new work report for a type, and
     * added it to the work report list
     */

    public void onCreateNewWorkReport() {
        Listitem selectedItem = listTypeToAssign.getSelectedItem();
        if (selectedItem == null) {
            throw new WrongValueException(listTypeToAssign,
                    _("please, select a timesheet template type"));
        }

        WorkReportType type = (WorkReportType) selectedItem.getValue();
        if (type == null) {
            throw new WrongValueException(listTypeToAssign,
                    _("please, select a timesheet template type"));
        }

        goToCreateForm(type);
        listTypeToAssign.clearSelection();
        cameBackList = true;
    }

    private WorkReportType firstType;

    public WorkReportType getFirstType() {
        return firstType;
    }

    public void setFirstType(WorkReportType firstType) {
        this.firstType = firstType;
    }

    public void newWorkReportWithSameType() {
        if (save()) {
            goToCreateForm(workReportModel.getWorkReportType());
            cameBackList = true;
        }
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if(ce.isChecked()) {
            //we have to auto-generate the code for new objects
            try {
                workReportModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
        }
        Util.reloadBindings(createWindow);
        reloadWorkReportLines();
    }

    public List<Worker> getBoundWorkers() {
        return workReportModel.getBoundWorkers();
    }

    public void createOrEditMonthlyTimesheet() {
        Date date = monthlyTimesheetsDatebox.getValue();
        if (date == null) {
            throw new WrongValueException(monthlyTimesheetsDatebox,
                    _("Please set a date"));
        }
        Resource resource = (Resource) monthlyTimesheetsBandboxSearch
                .getSelectedElement();
        if (resource == null) {
            throw new WrongValueException(monthlyTimesheetsBandboxSearch,
                    _("Please select a worker"));
        }

        monthlyTimesheetController.goToCreateOrEditFormForResource(
                LocalDate.fromDateFields(date), resource);
    }

}
