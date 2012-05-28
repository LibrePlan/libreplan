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

import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.entrypoints.IURLHandlerRegistry;
import org.libreplan.web.users.services.CustomTargetUrlResolver;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Grid;

/**
 * Controller for creation/edition of a monthly timesheet
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@SuppressWarnings("serial")
public class MonthlyTimesheetController extends GenericForwardComposer
        implements IMonthlyTimesheetController {

    private IMonthlyTimesheetModel monthlyTimesheetModel;

    private IURLHandlerRegistry URLHandlerRegistry;

    private Grid timesheet;

    private Columns columns;

    private RowRenderer orderElementsRenderer = new RowRenderer() {

        @Override
        public void render(Row row, Object data) throws Exception {
            OrderElement orderElement = (OrderElement) data;
            row.setValue(orderElement);

            Util.appendLabel(row, orderElement.getOrder().getName());
            Util.appendLabel(row, orderElement.getName());

            appendInputsForDays(row, orderElement);

            appendTotalColumn(row, orderElement);
        }

        private void appendInputsForDays(Row row,
                final OrderElement orderElement) {
            LocalDate date = monthlyTimesheetModel.getDate();

            LocalDate start = date.dayOfMonth().withMinimumValue();
            LocalDate end = date.dayOfMonth().withMaximumValue();

            for (LocalDate day = start; day.compareTo(end) <= 0; day = day
                    .plusDays(1)) {
                final LocalDate textboxDate = day;

                final Textbox textbox = new Textbox();
                textbox.setWidth("30px");

                Util.bind(textbox, new Util.Getter<String>() {
                    @Override
                    public String get() {
                        EffortDuration effortDuration = monthlyTimesheetModel
                                .getEffortDuration(orderElement, textboxDate);
                        return effortDuration != null ? effortDuration
                                .toFormattedString() : "";
                    }
                }, new Util.Setter<String>() {
                    @Override
                    public void set(String value) {
                        EffortDuration effortDuration = EffortDuration
                                .parseFromFormattedString(value);
                        if (effortDuration == null) {
                            throw new WrongValueException(textbox,
                                    _("Not a valid effort duration"));
                        }
                        monthlyTimesheetModel.setEffortDuration(orderElement,
                                textboxDate, effortDuration);
                        updateTotalColumn(orderElement);
                    }

                });

                row.appendChild(textbox);
            }

        }

        private void appendTotalColumn(Row row, final OrderElement orderElement) {
            Textbox textbox = new Textbox();
            textbox.setId(getTotalColumnTextboxId(orderElement));
            textbox.setDisabled(true);
            row.appendChild(textbox);

            updateTotalColumn(orderElement);
        }

        private String getTotalColumnTextboxId(final OrderElement orderElement) {
            return "textbox-total-" + orderElement.getId();
        }

        private void updateTotalColumn(OrderElement orderElement) {
            Textbox textbox = (Textbox) timesheet.getFellow(getTotalColumnTextboxId(orderElement));
            textbox.setValue(monthlyTimesheetModel.getEffortDuration(
                    orderElement).toFormattedString());
        }

    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);

        setBreadcrumbs(comp);

        URLHandlerRegistry.getRedirectorFor(IMonthlyTimesheetController.class)
                .register(this, page);
    }

    private void setBreadcrumbs(Component comp) {
        Component breadcrumbs = comp.getPage().getFellow("breadcrumbs");
        if (breadcrumbs.getChildren() != null) {
            breadcrumbs.getChildren().clear();
        }
        breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
        breadcrumbs.appendChild(new Label(_("My account")));
        breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
        breadcrumbs.appendChild(new Label(_("My dashboard")));
        breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
        breadcrumbs.appendChild(new Label(_("Monthly timesheet")));
    }

    @Override
    public void goToCreateOrEditForm(LocalDate date) {
        monthlyTimesheetModel.initCreateOrEdit(date);
        initTimesheet(date);
    }

    private void initTimesheet(LocalDate date) {
        columns = new Columns();
        timesheet.appendChild(columns);
        createColumns(date);
    }

    private void createColumns(LocalDate date) {
        createProjectAndTaskColumns();
        createColumnsForDays(date);
        createTotalColumn();
    }

    private void createProjectAndTaskColumns() {
        columns.appendChild(new Column(_("Project")));
        columns.appendChild(new Column(_("Task")));
    }

    private void createColumnsForDays(LocalDate date) {
        LocalDate start = date.dayOfMonth().withMinimumValue();
        LocalDate end = date.dayOfMonth().withMaximumValue();

        for (LocalDate day = start; day.compareTo(end) <= 0; day = day
                .plusDays(1)) {
            Column column = new Column(day.getDayOfMonth() + "");
            column.setAlign("center");
            columns.appendChild(column);
        }
    }

    private void createTotalColumn() {
        columns.appendChild(new Column(_("Total")));
    }

    public String getDate() {
        return monthlyTimesheetModel.getDate().toString("MMMM y");
    }

    public String getResource() {
        return monthlyTimesheetModel.getWorker().getShortDescription();
    }

    public List<OrderElement> getOrderElements() {
        return monthlyTimesheetModel.getOrderElements();
    }

    public RowRenderer getOrderElementsRenderer() {
        return orderElementsRenderer;
    }

    public void save() {
        monthlyTimesheetModel.save();
        Executions.getCurrent().sendRedirect(
                CustomTargetUrlResolver.USER_DASHBOARD_URL + "?timesheet_save="
                        + monthlyTimesheetModel.getDate());
    }

    public void cancel() {
        monthlyTimesheetModel.cancel();
        Executions.getCurrent().sendRedirect(
                CustomTargetUrlResolver.USER_DASHBOARD_URL);
    }

}
