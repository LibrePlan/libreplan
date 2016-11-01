/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.web.users.dashboard;

import static org.libreplan.web.I18nHelper._;
import static org.libreplan.web.planner.tabs.MultipleTabsPlannerController.BREADCRUMBS_SEPARATOR;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.IndexController;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.common.entrypoints.EntryPointsHandler;
import org.libreplan.web.common.entrypoints.IURLHandlerRegistry;
import org.libreplan.web.common.entrypoints.MatrixParameters;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.util.Assert;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Frozen;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Popup;

/**
 * Controller for creation/edition of a personal timesheet.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@SuppressWarnings("serial")
public class PersonalTimesheetController extends GenericForwardComposer implements IPersonalTimesheetController {

    private static final String EFFORT_DURATION_TEXTBOX_WIDTH = "30px";

    private static final String TOTAL_DURATION_TEXTBOX_WIDTH = "50px";

    private static final String WORK_REPORTS_URL = "/workreports/workReport.zul";

    private static final String ALIGN_CENTER = "center";

    private IPersonalTimesheetModel personalTimesheetModel;

    private IURLHandlerRegistry URLHandlerRegistry;

    private IPersonalTimesheetController personalTimesheetController;

    private Grid timesheet;

    private Columns columns;

    private BandboxSearch orderElementBandboxSearch;

    private Button previousPeriod;

    private Button nextPeriod;

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    private Label summaryTotalPersonalTimesheet;

    private Label summaryTotalOther;

    private Label summaryTotal;

    private Label summaryTotalCapacity;

    private Label summaryTotalExtraPerDay;

    private Label summaryTotalExtra;

    private Popup personalTimesheetPopup;

    private Label personalTimesheetPopupTask;

    private Label personalTimesheetPopupDate;

    private Div personalTimesheetPopupEffort;

    private Div personalTimesheetPopupFinished;

    private RowRenderer rowRenderer = new RowRenderer() {

        private LocalDate first;

        private LocalDate last;

        @Override
        public void render(Row row, Object data, int i) throws Exception {
            PersonalTimesheetRow personalTimesheetRow = (PersonalTimesheetRow) data;

            initPersonalTimesheetDates();

            switch (personalTimesheetRow.getType()) {
                case ORDER_ELEMENT:
                    renderOrderElementRow(row, personalTimesheetRow.getOrderElemement());
                    break;

                case OTHER:
                    renderOtherRow(row);
                    break;

                case CAPACITY:
                    renderCapacityRow(row);
                    break;

                case TOTAL:
                    renderTotalRow(row);
                    break;

                case EXTRA:
                    renderExtraRow(row);
                    // This is the last row so we can load the info in the summary
                    updateSummary();
                    break;

                default:
                    throw new IllegalStateException(
                            "Unknown PersonalTimesheetRow type: " + personalTimesheetRow.getType());
            }
        }

        private void initPersonalTimesheetDates() {
            first = personalTimesheetModel.getFirstDay();
            last = personalTimesheetModel.getLastDate();
        }

        private void renderOrderElementRow(Row row, OrderElement orderElement) {
            /* Flattening of orderElement row by height */
            row.setHeight("27px");

            row.setClass("row-timetracking");

            Util.appendLabel(row, personalTimesheetModel.getOrder(orderElement).getName());
            Util.appendLabel(row, orderElement.getName());

            appendInputsForDays(row, orderElement);

            if ( personalTimesheetModel.hasOtherReports() ) {
                appendOtherColumn(row, orderElement);
            }

            appendTotalColumn(row, orderElement);
        }

        private void appendInputsForDays(Row row, final OrderElement orderElement) {
            for (LocalDate day = first; day.compareTo(last) <= 0; day = day.plusDays(1)) {
                final LocalDate textboxDate = day;

                final Textbox textbox = new Textbox();
                textbox.setHflex("true");

                Util.bind(
                        textbox,
                        () -> effortDurationToString(
                                personalTimesheetModel.getEffortDuration(orderElement, textboxDate)),
                        new Util.Setter<String>() {
                            @Override
                            public void set(String value) {
                                EffortDuration effortDuration = effortDurationFromString(value);

                                if ( effortDuration == null ) {
                                    throw new WrongValueException(textbox, _("Invalid Effort Duration"));
                                }

                                personalTimesheetModel.setEffortDuration(orderElement, textboxDate, effortDuration);
                                markAsModified(textbox);
                                updateTotals(orderElement, textboxDate);
                            }

                            private void updateTotals(OrderElement orderElement, LocalDate date) {
                                updateTotalColumn(orderElement);
                                updateTotalRow(date);
                                updateExtraRow(date);
                                updateTotalColumn();
                                updateTotalExtraColumn();
                                updateSummary();
                            }
                        });

                EventListener openPersonalTimesheetPopup =
                        event -> openPersonalTimesheetPopup(textbox, orderElement, textboxDate);

                textbox.addEventListener(Events.ON_DOUBLE_CLICK, openPersonalTimesheetPopup);
                textbox.addEventListener(Events.ON_OK, openPersonalTimesheetPopup);

                if ( personalTimesheetModel.wasModified(orderElement, textboxDate) ) {
                    markAsModified(textbox);
                }

                Cell cell = getCenteredCell(textbox);
                if ( personalTimesheetModel.getResourceCapacity(day).isZero() ) {
                    setBackgroundNonCapacityCell(cell);
                }
                row.appendChild(cell);
            }

        }

        private void openPersonalTimesheetPopup(Textbox textbox, OrderElement orderElement, LocalDate textboxDate) {
            Textbox toFocus = setupPersonalTimesheetPopup(textbox, orderElement, textboxDate);

            personalTimesheetPopup.open(textbox, "after_start");
            ((Column) personalTimesheetPopup.getChildren().get(0).getChildren().get(0).getChildren().get(0)).setWidth("60px");

            toFocus.setFocus(true);
        }

        private Textbox setupPersonalTimesheetPopup(final Textbox textbox,
                                                    final OrderElement orderElement,
                                                    final LocalDate textboxDate) {

            personalTimesheetPopupTask.setValue(orderElement.getName());
            personalTimesheetPopupDate.setValue(textboxDate.toString());

            personalTimesheetPopupEffort.getChildren().clear();

            Textbox effortTextbox = Util.bind(
                    new Textbox(),
                    () -> {
                        EffortDuration effortDuration =
                                personalTimesheetModel.getEffortDuration(orderElement, textboxDate);

                        return effortDurationToString(effortDuration);
                    },
                    value -> {
                        EffortDuration effortDuration = effortDurationFromString(value);

                        if ( effortDuration == null ) {
                            throw new WrongValueException(
                                    personalTimesheetPopupEffort, _("Invalid Effort Duration"));
                        }

                        Events.sendEvent(new InputEvent(Events.ON_CHANGE, textbox, value, textbox.getValue()));
                    });

            addOnOkEventToClosePopup(effortTextbox);
            personalTimesheetPopupEffort.appendChild(effortTextbox);

            personalTimesheetPopupFinished.getChildren().clear();

            Checkbox finishedCheckbox = Util.bind(
                    new Checkbox(),
                    () -> personalTimesheetModel.isFinished(orderElement, textboxDate),
                    value -> {
                        personalTimesheetModel.setFinished(orderElement, textboxDate, value);
                        markAsModified(textbox);
                    });

            if ( !finishedCheckbox.isChecked() ) {
                finishedCheckbox.setDisabled(personalTimesheetModel.isFinished(orderElement));
            }

            addOnOkEventToClosePopup(finishedCheckbox);
            personalTimesheetPopupFinished.appendChild(finishedCheckbox);

            return effortTextbox;
        }

        private boolean addOnOkEventToClosePopup(Component component) {
            return component.addEventListener(Events.ON_OK, event -> closePersonalTimesheetPopup());
        }

        private void markAsModified(final Textbox textbox) {
            textbox.setStyle("font-weight: bold");
        }

        private void appendOtherColumn(Row row, final OrderElement orderElement) {
            Textbox other = getDisabledTextbox(getOtherRowTextboxId(orderElement));
            other.setValue(effortDurationToString(personalTimesheetModel.getOtherEffortDuration(orderElement)));
            row.appendChild(getCenteredCell(other));
        }

        private void appendTotalColumn(Row row, final OrderElement orderElement) {
            row.appendChild(getCenteredCell(getDisabledTextbox(getTotalRowTextboxId(orderElement))));
            updateTotalColumn(orderElement);
        }

        private void updateTotalColumn(OrderElement orderElement) {
            EffortDuration effort = personalTimesheetModel.getEffortDuration(orderElement);
            effort = effort.plus(personalTimesheetModel.getOtherEffortDuration(orderElement));

            Textbox textbox = (Textbox) timesheet.getFellow(getTotalRowTextboxId(orderElement));
            textbox.setValue(effortDurationToString(effort));
        }

        private void renderTotalRow(Row row) {
            appendLabelSpaningTwoColumns(row, _("Total"));
            appendTotalForDays(row);
            row.setSclass("total-row");
            appendTotalColumn(row);
        }

        private void appendLabelSpaningTwoColumns(Row row, String text) {
            Cell cell = new Cell();
            cell.setColspan(2);
            Label label = new Label(text);
            label.setStyle("font-weight: bold;");
            cell.appendChild(label);
            row.appendChild(cell);
        }

        private void appendTotalForDays(Row row) {
            for (LocalDate day = first; day.compareTo(last) <= 0; day = day.plusDays(1)) {
                Cell cell = getCenteredCell(getDisabledTextbox(getTotalColumnTextboxId(day)));

                if ( personalTimesheetModel.getResourceCapacity(day).isZero() ) {
                    setBackgroundNonCapacityCell(cell);
                }

                row.appendChild(cell);

                updateTotalRow(day);
            }
        }

        private void updateTotalRow(LocalDate date) {
            EffortDuration effort = personalTimesheetModel.getEffortDuration(date);
            effort = effort.plus(personalTimesheetModel.getOtherEffortDuration(date));

            Textbox textbox = (Textbox) timesheet.getFellow(getTotalColumnTextboxId(date));
            textbox.setValue(effortDurationToString(effort));
        }

        private void appendTotalColumn(Row row) {
            Cell totalCell = getCenteredCell(getDisabledTextbox(getTotalTextboxId()));

            if ( personalTimesheetModel.hasOtherReports() ) {
                totalCell.setColspan(2);
            }

            row.appendChild(totalCell);
            updateTotalColumn();
        }

        private void updateTotalColumn() {
            EffortDuration effort = personalTimesheetModel.getTotalEffortDuration();
            effort = effort.plus(personalTimesheetModel.getTotalOtherEffortDuration());

            Textbox textbox = (Textbox) timesheet.getFellow(getTotalTextboxId());
            textbox.setValue(effortDurationToString(effort));
        }

        private void renderOtherRow(Row row) {
            appendLabelSpaningTwoColumns(row, _("Other"));
            appendOtherForDaysAndTotal(row);
        }

        private void appendOtherForDaysAndTotal(Row row) {
            EffortDuration totalOther = EffortDuration.zero();

            for (LocalDate day = first; day.compareTo(last) <= 0; day = day.plusDays(1)) {
                EffortDuration other = personalTimesheetModel.getOtherEffortDuration(day);

                Cell cell = getCenteredCell(getDisabledTextbox(getOtherColumnTextboxId(day), other));

                if ( personalTimesheetModel.getResourceCapacity(day).isZero() ) {
                    setBackgroundNonCapacityCell(cell);
                }

                row.appendChild(cell);

                totalOther = totalOther.plus(other);
            }

            Cell totalOtherCell = getCenteredCell(getDisabledTextbox(getTotalOtherTextboxId(), totalOther));
            totalOtherCell.setColspan(2);
            row.appendChild(totalOtherCell);
        }

        private void renderCapacityRow(Row row) {
            appendLabelSpaningTwoColumns(row, _("Capacity"));
            appendCapacityForDaysAndTotal(row);
        }

        private void appendCapacityForDaysAndTotal(Row row) {
            EffortDuration totalCapacity = EffortDuration.zero();

            for (LocalDate day = first; day.compareTo(last) <= 0; day = day.plusDays(1)) {
                EffortDuration capacity = personalTimesheetModel.getResourceCapacity(day);

                Cell cell = getCenteredCell(getDisabledTextbox(getCapacityColumnTextboxId(day), capacity));

                if ( personalTimesheetModel.getResourceCapacity(day).isZero() ) {
                    setBackgroundNonCapacityCell(cell);
                }

                row.appendChild(cell);

                totalCapacity = totalCapacity.plus(capacity);
            }

            Cell totalCapacityCell = getCenteredCell(getDisabledTextbox(getTotalCapacityTextboxId(), totalCapacity));

            if ( personalTimesheetModel.hasOtherReports() ) {
                totalCapacityCell.setColspan(2);
            }

            row.appendChild(totalCapacityCell);
        }

        private void renderExtraRow(Row row) {
            appendLabelSpaningTwoColumns(row, _("Extra"));
            appendExtraForDays(row);
            appendTotalExtra(row);
        }

        private void appendExtraForDays(Row row) {
            for (LocalDate day = first; day.compareTo(last) <= 0; day = day.plusDays(1)) {
                Cell cell = getCenteredCell(getDisabledTextbox(getExtraColumnTextboxId(day)));

                if ( personalTimesheetModel.getResourceCapacity(day).isZero() ) {
                    setBackgroundNonCapacityCell(cell);
                }

                row.appendChild(cell);

                updateExtraRow(day);
            }

        }

        private void updateExtraRow(LocalDate date) {
            EffortDuration total = getEffortDuration(getTotalColumnTextboxId(date));
            EffortDuration capacity = getEffortDuration(getCapacityColumnTextboxId(date));

            EffortDuration extra = EffortDuration.zero();
            if ( total.compareTo(capacity) > 0 ) {
                extra = total.minus(capacity);
            }

            Textbox textbox = (Textbox) timesheet.getFellow(getExtraColumnTextboxId(date));
            textbox.setValue(effortDurationToString(extra));
        }

        private EffortDuration getEffortDuration(String textboxId) {
            String value = ((Textbox) timesheet.getFellow(textboxId)).getValue();

            return effortDurationFromString(value);
        }

        private void appendTotalExtra(Row row) {
            Cell totalExtraCell = getCenteredCell(getDisabledTextbox(getTotalExtraTextboxId()));

            if ( personalTimesheetModel.hasOtherReports() ) {
                totalExtraCell.setColspan(2);
            }

            row.appendChild(totalExtraCell);
            updateTotalExtraColumn();
        }

        private void updateTotalExtraColumn() {
            EffortDuration totalExtra = EffortDuration.zero();
            for (LocalDate day = first; day.compareTo(last) <= 0; day = day.plusDays(1)) {
                EffortDuration extra = getEffortDuration(getExtraColumnTextboxId(day));
                totalExtra = totalExtra.plus(extra);
            }

            Textbox textbox = (Textbox) timesheet.getFellow(getTotalExtraTextboxId());
            textbox.setValue(effortDurationToString(totalExtra));
        }

        private Textbox getDisabledTextbox(String id) {
            Textbox textbox = new Textbox();
            textbox.setHflex("true");
            textbox.setId(id);
            textbox.setDisabled(true);

            return textbox;
        }

        private Textbox getDisabledTextbox(String id, EffortDuration effort) {
            Textbox textbox = getDisabledTextbox(id);
            textbox.setValue(effortDurationToString(effort));

            return textbox;
        }

        private Cell getCenteredCell(Component component) {
            Cell cell = new Cell();
            cell.setAlign(ALIGN_CENTER);
            cell.appendChild(component);

            return cell;
        }

        private void setBackgroundNonCapacityCell(Cell cell) {
            cell.setStyle("background-color: #FFEEEE");
        }

    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);

        setBreadcrumbs(comp);
        messagesForUser = new MessagesForUser(messagesContainer);

        checkUserComesFromEntryPointsOrSendForbiddenCode();

        injectObjects();

        URLHandlerRegistry.getRedirectorFor(IPersonalTimesheetController.class).register(this, page);
    }

    /**
     * After migration from ZK 5 to ZK 8 ZK Spring works not as before.
     * Now you should manually inject objects.
     */
    private void injectObjects() {
        if ( personalTimesheetModel == null )
            personalTimesheetModel = (IPersonalTimesheetModel) SpringUtil.getBean("personalTimesheetModel");

        if ( URLHandlerRegistry == null )
            URLHandlerRegistry = (IURLHandlerRegistry) SpringUtil.getBean("URLHandlerRegistry");

        if ( personalTimesheetController == null ) {

            personalTimesheetController =
                    (IPersonalTimesheetController) SpringUtil.getBean("personalTimesheetController");
        }
    }
    /**
     * Hack to reduce frozen scroll area.
     * Timeout needed because of ZK 8 timings.
     */
    private void adjustFrozenWidth() {
        Clients.evalJavaScript(
                "setTimeout(function(){jq('.z-frozen-inner div').width(jq('.totals-column').offset().left);}, 1);");
    }

    private void checkUserComesFromEntryPointsOrSendForbiddenCode() {
        HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
        Map<String, String> matrixParams = MatrixParameters.extract(request);

        // If it does not come from a entry point
        if ( matrixParams.isEmpty() ) {
            Util.sendForbiddenStatusCodeInHttpServletResponse();
        }
    }

    private void setBreadcrumbs(Component comp) {
        Component breadcrumbs = comp.getPage().getFellow("breadcrumbs");

        if ( breadcrumbs.getChildren() != null ) {
            breadcrumbs.getChildren().clear();
        }

        breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
        breadcrumbs.appendChild(new Label(_("My account")));

        breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
        breadcrumbs.appendChild(new Label(_("My dashboard")));

        breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
        breadcrumbs.appendChild(new Label(_("Personal timesheet")));
    }

    @Override
    public void goToCreateOrEditForm(LocalDate date) {
        if ( !SecurityUtils.isUserInRole(UserRole.ROLE_BOUND_USER) ) {
            Util.sendForbiddenStatusCodeInHttpServletResponse();
        }

        personalTimesheetModel.initCreateOrEdit(date);
        initTimesheet(date);
    }

    @Override
    public void goToCreateOrEditFormForResource(LocalDate date, Resource resource) {

        if ( !SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_TIMESHEETS) ) {
            Util.sendForbiddenStatusCodeInHttpServletResponse();
        }

        personalTimesheetModel.initCreateOrEdit(date, resource);
        initTimesheet(date);
    }

    private void initTimesheet(LocalDate date) {
        columns = new Columns();
        columns.setSizable(true);
        timesheet.getChildren().clear();
        timesheet.appendChild(columns);
        createColumns(date);

        Frozen frozen = new Frozen();
        frozen.setColumns(2);
        timesheet.appendChild(frozen);
        timesheet.invalidate();

        adjustFrozenWidth();
    }

    private void createColumns(LocalDate date) {
        createProjectAndTaskColumns();
        createColumnsForDays(date);

        if ( personalTimesheetModel.hasOtherReports() ) {
            createOtherColumn();
        }

        createTotalColumn();
    }

    private void createProjectAndTaskColumns() {
        /* setWidth() was used because setStyle(min-width) was not working */

        Column project = new Column(_("Project"));
        project.setWidth("150px");

        columns.appendChild(project);

        Column task = new Column(_("Task"));
        task.setWidth("150px");

        columns.appendChild(project);
        columns.appendChild(task);
    }

    private void createColumnsForDays(LocalDate date) {
        LocalDate start = personalTimesheetModel.getPersonalTimesheetsPeriodicity().getStart(date);
        LocalDate end = personalTimesheetModel.getPersonalTimesheetsPeriodicity().getEnd(date);

        for (LocalDate day = start; day.compareTo(end) <= 0; day = day.plusDays(1)) {
            Column column = new Column(Integer.toString(day.getDayOfMonth()));
            column.setAlign(ALIGN_CENTER);
            column.setWidth(EFFORT_DURATION_TEXTBOX_WIDTH);
            columns.appendChild(column);
        }
    }

    private void createOtherColumn() {
        Column other = new Column(_("Other"));
        other.setWidth(TOTAL_DURATION_TEXTBOX_WIDTH);
        other.setSclass("totals-column");
        other.setAlign(ALIGN_CENTER);
        columns.appendChild(other);
    }

    private void createTotalColumn() {
        Column total = new Column(_("Total"));
        total.setWidth(TOTAL_DURATION_TEXTBOX_WIDTH);
        total.setSclass("totals-column");
        total.setAlign(ALIGN_CENTER);
        columns.appendChild(total);
    }

    public String getTimesheetString() {
        return personalTimesheetModel.getTimesheetString();
    }

    public String getResource() {
        return personalTimesheetModel.getWorker().getShortDescription();
    }

    public List<PersonalTimesheetRow> getRows() {
        List<PersonalTimesheetRow> result = PersonalTimesheetRow.wrap(personalTimesheetModel.getOrderElements());
        if ( personalTimesheetModel.hasOtherReports() ) {
            result.add(PersonalTimesheetRow.createOtherRow());
        }

        result.add(PersonalTimesheetRow.createTotalRow());
        result.add(PersonalTimesheetRow.createCapacityRow());
        result.add(PersonalTimesheetRow.createExtraRow());

        return result;
    }

    public RowRenderer getRowRenderer() {
        return rowRenderer;
    }

    public void save() {
        personalTimesheetModel.save();
        String url = IndexController.USER_DASHBOARD_URL + "?timesheet_saved=" + personalTimesheetModel.getDate();

        if ( !personalTimesheetModel.isCurrentUser() ) {
            url = WORK_REPORTS_URL + "?timesheet_saved=true";
        }

        Executions.getCurrent().sendRedirect(url);
    }

    public void saveAndContinue() {
        personalTimesheetModel.save();

        if ( personalTimesheetModel.isCurrentUser() ) {
            goToCreateOrEditForm(personalTimesheetModel.getDate());
        } else {
            goToCreateOrEditFormForResource(personalTimesheetModel.getDate(), personalTimesheetModel.getWorker());
        }

        messagesForUser.showMessage(Level.INFO, _("Personal timesheet saved"));
        Util.reloadBindings(timesheet);
    }

    public void cancel() {
        personalTimesheetModel.cancel();
        String url = IndexController.USER_DASHBOARD_URL;

        if ( !personalTimesheetModel.isCurrentUser() ) {
            url = WORK_REPORTS_URL;
        }

        Executions.getCurrent().sendRedirect(url);
    }

    /**
     * Should be public!
     * Used in personalTimesheet.zul
     */
    public void addOrderElement() {
        OrderElement orderElement = (OrderElement) orderElementBandboxSearch.getSelectedElement();
        if ( orderElement != null ) {
            personalTimesheetModel.addOrderElement(orderElement);
            orderElementBandboxSearch.setSelectedElement(null);
            Util.reloadBindings(timesheet);
            adjustFrozenWidth();
        }
    }

    public boolean isFirstPeriod() {
        return personalTimesheetModel.isFirstPeriod();
    }

    public boolean isLastPeriod() {
        return personalTimesheetModel.isLastPeriod();
    }

    public void previousPeriod() {
        if ( personalTimesheetModel.isModified() ) {
            throw new WrongValueException(previousPeriod,
                    _("There are unsaved changes in the current personal timesheet, please save before moving"));
        }

        sendToPersonalTimesheet(personalTimesheetModel.getPrevious());
    }

    public void nextPeriod() {
        if ( personalTimesheetModel.isModified() ) {
            throw new WrongValueException(nextPeriod,
                    _("There are unsaved changes in the current personal timesheet, please save before moving"));
        }

        sendToPersonalTimesheet(personalTimesheetModel.getNext());
    }

    private void sendToPersonalTimesheet(final LocalDate date) {
        String capturePath =
                EntryPointsHandler.capturePath(() -> personalTimesheetController.goToCreateOrEditForm(date));

        Executions.getCurrent().sendRedirect(capturePath);
    }

    public boolean isCurrentUser() {
        return personalTimesheetModel.isCurrentUser();
    }

    public boolean isNotCurrentUser() {
        return !personalTimesheetModel.isCurrentUser();
    }

    private static String getTotalRowTextboxId(final OrderElement orderElement) {
        return "textbox-total-row" + orderElement.getId();
    }

    private static String getTotalColumnTextboxId(LocalDate date) {
        return "textbox-total-column-" + date;
    }

    private static String getTotalTextboxId() {
        return "textbox-total";
    }

    private static String getOtherRowTextboxId(final OrderElement orderElement) {
        return "textbox-other-row" + orderElement.getId();
    }

    private static String getOtherColumnTextboxId(LocalDate date) {
        return "textbox-other-column-" + date;
    }

    private static String getTotalOtherTextboxId() {
        return "textbox-other-capacity";
    }

    private static String getCapacityColumnTextboxId(LocalDate date) {
        return "textbox-capacity-column-" + date;
    }

    private static String getTotalCapacityTextboxId() {
        return "textbox-total-capacity";
    }

    private static String getExtraColumnTextboxId(LocalDate date) {
        return "textbox-extra-column-" + date;
    }

    private static String getTotalExtraTextboxId() {
        return "textbox-total-extra";
    }

    private static String effortDurationToString(EffortDuration effort) {
        return effort == null || effort.isZero() ? "" : effort.toFormattedString();
    }

    private static EffortDuration effortDurationFromString(String effort) {
        if ( StringUtils.isBlank(effort) ) {
            return EffortDuration.zero();
        }

        String decimalSeparator = Character.toString(
                ((DecimalFormat) DecimalFormat.getInstance(Locales.getCurrent()))
                        .getDecimalFormatSymbols().getDecimalSeparator());

        if ( effort.contains(decimalSeparator) || effort.contains(".") ) {
            try {
                effort = effort.replace(decimalSeparator, ".");
                double hours = Double.parseDouble(effort);

                return EffortDuration.fromHoursAsBigDecimal(BigDecimal.valueOf(hours));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return EffortDuration.parseFromFormattedString(effort);
    }

    public void updateSummary() {
        EffortDuration total = getEffortDurationFromTextbox(getTotalTextboxId());
        EffortDuration other = EffortDuration.zero();

        if ( personalTimesheetModel.hasOtherReports() ) {
            other = getEffortDurationFromTextbox(getTotalOtherTextboxId());
        }

        EffortDuration capacity = getEffortDurationFromTextbox(getTotalCapacityTextboxId());
        EffortDuration extraPerDay = getEffortDurationFromTextbox(getTotalExtraTextboxId());

        EffortDuration timesheet = total.minus(other);
        EffortDuration extra = EffortDuration.zero();

        if ( total.compareTo(capacity) > 0 ) {
            extra = total.minus(capacity);
        }

        if ( personalTimesheetModel.hasOtherReports() ) {
            summaryTotalPersonalTimesheet.setValue(timesheet.toFormattedString());
            summaryTotalOther.setValue(other.toFormattedString());
        }

        summaryTotal.setValue(total.toFormattedString());
        summaryTotalCapacity.setValue(capacity.toFormattedString());
        summaryTotalExtraPerDay.setValue(extraPerDay.toFormattedString());
        summaryTotalExtra.setValue(extra.toFormattedString());
    }

    private EffortDuration getEffortDurationFromTextbox(String id) {
        return effortDurationFromString(((Textbox) timesheet.getFellow(id)).getValue());
    }

    public boolean hasOtherReports() {
        return personalTimesheetModel.hasOtherReports();
    }

    public void closePersonalTimesheetPopup() {
        personalTimesheetPopup.close();
    }

}

/**
 * Simple class to represent the the rows in the personal timesheet grid.
 * <br />
 * This is used to mark the special rows like capacity and total.
 */
class PersonalTimesheetRow {

    enum PersonalTimesheetRowType {
        ORDER_ELEMENT,
        OTHER,
        CAPACITY,
        TOTAL,
        EXTRA
    }

    private PersonalTimesheetRowType type;

    private OrderElement orderElemement;

    private PersonalTimesheetRow(PersonalTimesheetRowType type) {
        this.type = type;
    }

    public static PersonalTimesheetRow createOrderElementRow(OrderElement orderElemement) {
        PersonalTimesheetRow row = new PersonalTimesheetRow(PersonalTimesheetRowType.ORDER_ELEMENT);
        Assert.notNull(orderElemement);
        row.orderElemement = orderElemement;

        return row;
    }

    public static PersonalTimesheetRow createOtherRow() {
        return new PersonalTimesheetRow(PersonalTimesheetRowType.OTHER);
    }

    public static PersonalTimesheetRow createCapacityRow() {
        return new PersonalTimesheetRow(PersonalTimesheetRowType.CAPACITY);
    }

    public static PersonalTimesheetRow createTotalRow() {
        return new PersonalTimesheetRow(PersonalTimesheetRowType.TOTAL);
    }

    public static PersonalTimesheetRow createExtraRow() {
        return new PersonalTimesheetRow(PersonalTimesheetRowType.EXTRA);
    }

    public static List<PersonalTimesheetRow> wrap(List<OrderElement> orderElements) {
        List<PersonalTimesheetRow> result = new ArrayList<>();
        for (OrderElement each : orderElements) {
            result.add(createOrderElementRow(each));
        }

        return result;
    }

    public PersonalTimesheetRowType getType() {
        return type;
    }

    public OrderElement getOrderElemement() {
        return orderElemement;
    }

}
