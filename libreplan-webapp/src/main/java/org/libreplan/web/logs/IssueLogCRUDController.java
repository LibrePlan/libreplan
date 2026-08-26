/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

import static org.libreplan.web.I18nHelper._t;


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

        // AnnotateBinderInit's own page-level pass (<?init class="AnnotateBinderInit"?>) only
        // calls Util.createBindingsFor - it never loads the bindings it registers, so without an
        // explicit initial load here listIssueLog stays empty until some other action happens to
        // trigger a reload. Only listWindow is reloaded - editWindow's bindings reference
        // controller.issueLog, which is still null the first time this runs (only set up by
        // initCreate()/initEdit(), called later); showEditWindow() reloads editWindow's own
        // bindings once that has happened.
        Util.createBindingsFor(comp);
        Util.reloadBindings(listWindow);
    }

    /**
     * Initializes order component.
     */
    private void initializeOrderComponent() {
        bdProjectIssueLog = (BandboxSearch) editWindow.getFellow("bdProjectIssueLog");
        // No separate Util.createBindingsFor(bdProjectIssueLog) call here - that would give this
        // component its own binder, rooted separately from the rest of the page. The comp-wide
        // Util.createBindingsFor(comp) call at the end of doAfterCompose() already covers it as
        // part of the single, whole-page binder that showEditWindow() actually reloads; a second,
        // narrower binder registered here first would never receive that reload and would show
        // this bandbox permanently empty (confirmed live - this was the actual bug).

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
        // See the comment in initializeOrderComponent() - no separate createBindingsFor call here.

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

    // Plain public static fields, as declared above, are never recognized as EL bean properties -
    // java.beans.Introspector (which BeanELResolver relies on) only considers getter/setter method
    // pairs, so "controller.issueTypeRenderer"/"controller.lowMediumHighEnumRenderer" could never
    // resolve, independent of the @{...} binder issue. These getters are purely for that binding.
    public ListitemRenderer getIssueTypeRenderer() {
        return issueTypeRenderer;
    }

    public ListitemRenderer getLowMediumHighEnumRenderer() {
        return lowMediumHighEnumRenderer;
    }

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
    public org.zkoss.zul.ListModel getLowMediumHighEnum() {
        // The listbox's "model" property is declared as org.zkoss.zul.ListModel - AnnotateBinder
        // has no automatic array->ListModel coercion, so wrap it explicitly.
        return new org.zkoss.zul.ListModelArray<>(LowMediumHighEnum.values());
    }

    /**
     * Returns {@link IssueTypeEnum} values.
     */
    public org.zkoss.zul.ListModel getIssueTypeEnum() {
        return new org.zkoss.zul.ListModelArray<>(IssueTypeEnum.values());
    }

    /**
     * Returns {@link ArrayList} values.
     */
    public ArrayList<String> getIssueStatusEnum() {
        ArrayList<String> result = new ArrayList<>();
	// Request for change
        if (getIssueLog().getType() == IssueTypeEnum.REQUEST_FOR_CHANGE){
            result.add(_t("Must have"));
            result.add(_t("Should have"));
            result.add(_t("Could have"));
            result.add(_t("Won't have"));

            return result;
        }
	// Problem or concern
        if (getIssueLog().getType() == IssueTypeEnum.PROBLEM_OR_CONCERN) {
            result.add(_t("Minor"));
            result.add(_t("Significant"));
            result.add(_t("Major"));
            result.add(_t("Critical"));

            return result;
        }
	// Off specification
	result.add(_t("Identified"));
	result.add(_t("Under Analysis"));
	result.add(_t("Accepted Deviation"));
	result.add(_t("Change Requested"));
	result.add(_t("Correction Planned"));
	result.add(_t("In Correction"));
	result.add(_t("Verification Pending"));
	result.add(_t("Resolved (Compliant)"));
	result.add(_t("Closed (Accepted as-is)"));
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
        List<IssueLog> result;
        if (LogsController.getProjectNameVisibility())
            result = issueLogModel.getIssueLogs();
        else {
            result = new ArrayList<>();
            Order order = LogsController.getOrder();
            for (IssueLog issueLog : issueLogModel.getIssueLogs()) {
                if (issueLog.getOrder().equals(order))
                    result.add(issueLog);
            }
        }
        // The grid's "model" property is declared as org.zkoss.zul.ListModel - AnnotateBinder has
        // no automatic List->ListModel coercion, so returning a plain List here throws a
        // ClassCastException when the binder tries to load it.
        return new ListModelList<>(result);
    }

    public Order getOrder() {
        IssueLog issueLog = getIssueLog();
        if (issueLog == null) {
            return null;
        }
        if (!LogsController.getProjectNameVisibility()) {
            issueLog.setOrder(LogsController.getOrder());
        }
        return issueLog.getOrder();
    }

    @Override
    protected String getEntityType() {
        return _t("issuelog-number");
    }

    @Override
    protected String getPluralEntityType() {
        return _t("Issue logs");
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
            throw new WrongValueException(bdProjectIssueLog, _t("please select a project"));
        }

        if (getIssueLog().getCreatedBy() == null) {
            throw new WrongValueException(bdUserIssueLog, _t("please select an author"));
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
