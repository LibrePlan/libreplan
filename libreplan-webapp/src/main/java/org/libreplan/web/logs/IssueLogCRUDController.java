/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.web.logs;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.logs.entities.IssueLog;
import org.libreplan.business.logs.entities.IssueTypeEnum;
import org.libreplan.business.logs.entities.LowMediumHighEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Label;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zkplus.spring.SpringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.libreplan.web.I18nHelper._;


/**
 * Controller for IssueLog CRUD actions.
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
@SuppressWarnings("serial")
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class IssueLogCRUDController extends BaseCRUDController<IssueLog> {

    private IIssueLogModel issueLogModel;

    private BandboxSearch bdProjectIssueLog;

    private BandboxSearch bdUserIssueLog;

    private Listbox status;

    private boolean saved;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        issueLogModel = (IIssueLogModel) SpringUtil.getBean("issueLogModel");
        status = (Listbox)comp.getFellow("editWindow").getFellow("listIssueLogStatus");
        comp.setAttribute("issueLogController", this, true);
        showListWindow();
        initializeOrderComponent();
        initializeUserComponent();
        bdProjectIssueLog.setDisabled(!LogsController.getProjectNameVisibility());
        bdUserIssueLog.setDisabled(true);
    }

    /**
     * Initializes order component.
     */
    private void initializeOrderComponent() {
        bdProjectIssueLog = (BandboxSearch) editWindow.getFellow("bdProjectIssueLog");
        Util.createBindingsFor(bdProjectIssueLog);

        bdProjectIssueLog.setListboxEventListener(Events.ON_SELECT,
                event -> {
                    final Object object = bdProjectIssueLog.getSelectedElement();
                    issueLogModel.setOrder((Order) object);
                });

        bdProjectIssueLog.setListboxEventListener(Events.ON_OK,
                event -> {
                    final Object object = bdProjectIssueLog.getSelectedElement();
                    issueLogModel.setOrder((Order) object);
                    bdProjectIssueLog.close();
                });
    }

    /**
     * Initializes user component.
     */
    private void initializeUserComponent() {
        bdUserIssueLog = (BandboxSearch) editWindow.getFellow("bdUserIssueLog");
        Util.createBindingsFor(bdUserIssueLog);

        bdUserIssueLog.setListboxEventListener(Events.ON_SELECT, event -> {
            final Object object = bdUserIssueLog.getSelectedElement();
            issueLogModel.setCreatedBy((User) object);
        });

        bdUserIssueLog.setListboxEventListener(Events.ON_OK, event -> {
            final Object object = bdUserIssueLog.getSelectedElement();
            issueLogModel.setCreatedBy((User) object);
            bdUserIssueLog.close();
        });
    }

    /**
     * Enumerations rendering.
     */
    public static ListitemRenderer issueTypeRenderer = (item, data, i) -> {
        IssueTypeEnum issueTypeEnum = (IssueTypeEnum) data;
        String displayName = issueTypeEnum.getDisplayName();
        item.setLabel(displayName);
    };



    public static ListitemRenderer lowMediumHighEnumRenderer = (item, data, i) -> {
        LowMediumHighEnum lowMediumHighEnum = (LowMediumHighEnum) data;
        String displayName = lowMediumHighEnum.getDisplayName();
        item.setLabel(displayName);
    };

    /**
     * Renders issue logs.
     *
     * @return {@link RowRenderer}
     */
    public RowRenderer getIssueLogsRowRenderer() {
        return (row, data, i) -> {
            final IssueLog issueLog = (IssueLog) data;
            row.setValue(issueLog);
            appendObject(row, issueLog.getCode());
            appendLabel(row, issueLog.getOrder().getName());
            appendObject(row, issueLog.getType());
            appendObject(row, issueLog.getStatus());
            appendLabel(row, issueLog.getDescription());
            appendLabel(row, issueLog.getPriority().getDisplayName());
            appendLabel(row, issueLog.getSeverity().getDisplayName());
            appendDate(row, issueLog.getDateRaised());
            appendLabel(row, issueLog.getCreatedBy().getLoginName());
            appendLabel(row, issueLog.getAssignedTo());
            appendDate(row, issueLog.getDeadline());
            appendDate(row, issueLog.getDateResolved());
            appendLabel(row, issueLog.getNotes());
            appendOperations(row, issueLog);
            setPriorityCellColor(row, issueLog.getPriority());
        };
    }

    private void setPriorityCellColor(Row row, LowMediumHighEnum priority) {
        Cell cell = (Cell) row.getChildren().get(5);
        if (priority == LowMediumHighEnum.LOW) {
            cell.setClass("issueLog-priority-color-green");
        }

        if (priority == LowMediumHighEnum.MEDIUM) {
            cell.setClass("issueLog-priority-color-yellow");
        }

        if (priority == LowMediumHighEnum.HIGH) {
            cell.setClass("issueLog-priority-color-red");
        }
    }

    /**
     * Appends the specified <code>object</code> to the specified <code>row</code>.
     *
     * @param row
     * @param object
     */
    private void appendObject(final Row row, Object object) {
        String text = "";
        if (object != null) {
            text = object.toString();
        }
        appendLabel(row, text);
    }

    /**
     * Creates {@link Label} bases on the specified <code>value</code> and appends to the specified <code>row</code>.
     *
     * @param row
     * @param value
     */
    private void appendLabel(final Row row, String value) {
        Label label = new Label(value);
        Cell cell = new Cell();
        cell.appendChild(label);
        row.appendChild(cell);
    }

    /**
     * Appends the specified <code>date</code> to the specified <code>row</code>.
     *
     * @param row
     * @param date
     */
    private void appendDate(final Row row, Date date) {
        String labelDate = "";
        if (date != null) {
            labelDate = Util.formatDate(date);
        }
        appendLabel(row, labelDate);
    }

    /**
     * Appends operation(edit and remove) to the specified <code>row</code>.
     *
     * @param row
     * @param issueLog
     */
    private void appendOperations(final Row row, final IssueLog issueLog) {
        Hbox hbox = new Hbox();
        hbox.appendChild(Util.createEditButton(event -> goToEditForm(issueLog)));
        hbox.appendChild(Util.createRemoveButton(event -> confirmDelete(issueLog)));
        row.appendChild(hbox);
    }

    /**
     * Returns {@link LowMediumHighEnum} values.
     */
    public LowMediumHighEnum[] getLowMediumHighEnum() {
        return LowMediumHighEnum.values();
    }

    /**
     * Returns {@link IssueTypeEnum} values.
     */
    public IssueTypeEnum[] getIssueTypeEnum() {
        return IssueTypeEnum.values();
    }

    /**
     * Returns {@link ArrayList} values.
     */
    public ArrayList<String> getIssueStatusEnum() {
        ArrayList<String> result = new ArrayList<>();
        if (getIssueLog().getType() == IssueTypeEnum.REQUEST_FOR_CHANGE){
            result.add(_("Must have"));
            result.add(_("Should have"));
            result.add(_("Could have"));
            result.add(_("Won't have"));

            return result;
        }
        if (getIssueLog().getType() == IssueTypeEnum.PROBLEM_OR_CONCERN) {
            result.add(_("Minor"));
            result.add(_("Significant"));
            result.add(_("Major"));
            result.add(_("Critical"));

            return result;
        }

        result.add(_("Low"));
        result.add(_("Medium"));
        result.add(_("High"));
        return result;
    }

    public void updateStatusList(boolean ifNew) {
        ListModelList model = new ListModelList<>(getIssueStatusEnum());
        status.setModel(model);
        if (ifNew)
            status.setSelectedItem(status.getItemAtIndex(0));
        else {
            for(int i = 0; i < status.getItems().size(); i++) {
                if (status.getModel().getElementAt(i).toString().equals(getIssueLog().getStatus())) {
                    status.setSelectedItem(status.getItemAtIndex(i));
                    break;
                }
            }
        }
    }

    /**
     * Returns a list of {@link Order} objects.
     */
    public List<Order> getOrders() {
        return issueLogModel.getOrders();
    }


    /**
     * Returns a list of {@link User} objects.
     */
    public List<User> getUsers() {
        return issueLogModel.getUsers();
    }

    /**
     * Returns {@link Date}.
     */
    public Date getDateRaised() {
        if (issueLogModel.getIssueLog() == null) {
            return null;
        }

        return (issueLogModel.getIssueLog().getDateRaised() != null)
                ? issueLogModel.getIssueLog().getDateRaised()
                : null;
    }

    /**
     * Sets the date raised.
     *
     * @param date
     *            date raised
     */
    public void setDateRaised(Date date) {
        issueLogModel.getIssueLog().setDateRaised(date);
    }

    /**
     * Returns {@link Date}.
     */
    public Date getDateResolved() {
        if (issueLogModel.getIssueLog() == null) {
            return null;
        }

        return (issueLogModel.getIssueLog().getDateResolved() != null)
                ? issueLogModel.getIssueLog().getDateResolved()
                : null;
    }
    /**
     * Sets the date resolved.
     *
     * @param date
     *            the date resolved
     */
    public void setDateResolved(Date date) {
        issueLogModel.getIssueLog().setDateResolved(date);
    }

    /**
     * Returns {@link Date}.
     */
    public Date getDeadline() {
        if (issueLogModel.getIssueLog() == null) {
            return null;
        }

        return (issueLogModel.getIssueLog().getDeadline() != null)
                ? issueLogModel.getIssueLog().getDeadline()    // this is a getIntegrationEntityDAO method
                : null;
    }

    public void setDeadline(Date date) {
        issueLogModel.getIssueLog().setDeadline(date);
    }

    /**
     * Returns the {@link IssueLog} object.
     */
    public IssueLog getIssueLog() {
        return issueLogModel.getIssueLog();
    }

    /**
     * Returns a list of {@link IssueLog} objects.
     */
    public List<IssueLog> getIssueLogs() {
        if (LogsController.getProjectNameVisibility())
            return issueLogModel.getIssueLogs();
        else {
            List<IssueLog> issueLogs = new ArrayList<>();
            Order order = LogsController.getOrder();
            for (IssueLog issueLog : issueLogModel.getIssueLogs()) {
                if (issueLog.getOrder().equals(order))
                    issueLogs.add(issueLog);
            }

            return issueLogs;
        }
    }

    public Order getOrder() {
        if (!LogsController.getProjectNameVisibility()){
            this.getIssueLog().setOrder(LogsController.getOrder());
            return getIssueLog().getOrder();
        }
        else
            return getIssueLog().getOrder();
    }

    @Override
    protected String getEntityType() {
        return _("issuelog-number");
    }

    @Override
    protected String getPluralEntityType() {
        return _("Issue logs");
    }

    @Override
    protected void initCreate() {
        issueLogModel.initCreate();
        updateStatusList(true);
    }

    @Override
    protected void initEdit(IssueLog entity) {
        issueLogModel.initEdit(entity);
        updateStatusList(false);
    }

    @Override
    protected void save() throws ValidationException {
        if (getIssueLog().getOrder() == null) {
            throw new WrongValueException(bdProjectIssueLog, _("please select a project"));
        }

        if (getIssueLog().getCreatedBy() == null) {
            throw new WrongValueException(bdUserIssueLog, _("please select an author"));
        }
        getIssueLog().setStatus(status.getSelectedItem().getLabel());
        issueLogModel.confirmSave();
        saved = true;
    }

    @Override
    protected IssueLog getEntityBeingEdited() {
        return issueLogModel.getIssueLog();
    }

    @Override
    protected void delete(IssueLog entity) throws InstanceNotFoundException {
        issueLogModel.remove(entity);
    }

    public void setIssueLogToModel (IssueLog log) {
        this.issueLogModel.setIssueLog(log);
    }

    public Boolean isIssueLogSaved () {
        return saved;
    }

    public void setDefaultStatus() {
        status.setSelectedIndex(0);
    }
}
