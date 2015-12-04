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

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.logs.entities.IssueLog;
import org.libreplan.business.logs.entities.IssueStatusEnum;
import org.libreplan.business.logs.entities.IssueTypeEnum;
import org.libreplan.business.logs.entities.LowMediumHighEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import org.zkoss.zk.ui.Executions;

import javax.swing.*;

/**
 * Controller for IssueLog CRUD actions
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
@SuppressWarnings("serial")
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class IssueLogCRUDController extends BaseCRUDController<IssueLog> {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(IssueLogCRUDController.class);

    @Autowired
    private IIssueLogModel issueLogModel;

    private BandboxSearch bdProjectIssueLog;

    private BandboxSearch bdUserIssueLog;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("issueLogController", this, true);
        showListWindow();
        initializeOrderComponent();
        initializeUserComponent();

    }

    /**
     * Initializes order component
     */
    private void initializeOrderComponent() {
        bdProjectIssueLog = (BandboxSearch) editWindow
                .getFellow("bdProjectIssueLog");
        Util.createBindingsFor(bdProjectIssueLog);
        bdProjectIssueLog.setListboxEventListener(Events.ON_SELECT,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        final Object object = bdProjectIssueLog
                                .getSelectedElement();
                        issueLogModel.setOrder((Order) object);
                    }
                });
        bdProjectIssueLog.setListboxEventListener(Events.ON_OK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        final Object object = bdProjectIssueLog
                                .getSelectedElement();
                        issueLogModel.setOrder((Order) object);
                        bdProjectIssueLog.close();
                    }
                });
    }

    /**
     * Initializes user component
     */
    private void initializeUserComponent() {
        bdUserIssueLog = (BandboxSearch) editWindow.getFellow("bdUserIssueLog");
        Util.createBindingsFor(bdUserIssueLog);

        bdUserIssueLog.setListboxEventListener(Events.ON_SELECT, new EventListener() {
            @Override
            public void onEvent(Event event) {
                final Object object = bdUserIssueLog.getSelectedElement();
                issueLogModel.setCreatedBy((User) object);
            }
        });
        bdUserIssueLog.setListboxEventListener(Events.ON_OK, new EventListener() {
            @Override
            public void onEvent(Event event) {
                final Object object = bdUserIssueLog.getSelectedElement();
                issueLogModel.setCreatedBy((User) object);
                bdUserIssueLog.close();
            }
        });
    }

    /**
     * Enumerations rendering
     */
    public static ListitemRenderer issueTypeRenderer = new ListitemRenderer() {
        @Override
        public void render(org.zkoss.zul.Listitem item, Object data)
                throws Exception {
            IssueTypeEnum issueTypeEnum = (IssueTypeEnum) data;
            String displayName = issueTypeEnum.getDisplayName();
            item.setLabel(displayName);
        }
    };
    public static ListitemRenderer issueStatusRenderer = new ListitemRenderer() {
        @Override
        public void render(org.zkoss.zul.Listitem item, Object data)
                throws Exception {
            IssueStatusEnum issueStatusEnum = (IssueStatusEnum) data;
            String displayName = issueStatusEnum.getDisplayName();
            item.setLabel(displayName);
        }
    };

    public static ListitemRenderer lowMediumHighEnumRenderer = new ListitemRenderer() {
        @Override
        public void render(org.zkoss.zul.Listitem item, Object data)
                throws Exception {
            LowMediumHighEnum lowMediumHighEnum = (LowMediumHighEnum) data;
            String displayName = lowMediumHighEnum.getDisplayName();
            item.setLabel(displayName);
        }
    };

    /**
     * Renders issue logs
     *
     * @return {@link RowRenderer}
     */
    public RowRenderer getIssueLogsRowRenderer() {
        return new RowRenderer() {

            @Override
            public void render(Row row, Object data) throws Exception {
                final IssueLog issueLog = (IssueLog) data;
                row.setValue(issueLog);
                appendObject(row, issueLog.getCode());
                appendLabel(row, issueLog.getOrder().getName());
                appendObject(row, issueLog.getType());
                appendObject(row, issueLog.getStatus());
                appendLabel(row, issueLog.getDescription());
                appendObject(row, issueLog.getPriority());
                appendObject(row, issueLog.getSeverity());
                appendDate(row, issueLog.getDateRaised());
                appendLabel(row, issueLog.getCreatedBy().getLoginName());
                appendLabel(row, issueLog.getAssignedTo());
                appendDate(row, issueLog.getDeadline());
                appendDate(row, issueLog.getDateResolved());
                appendLabel(row, issueLog.getNotes());
                appendOperations(row, issueLog);
            }
        };
    }

    /**
     * Appends the specified <code>object</code> to the specified
     * <code>row</code>
     *
     * @param row
     * @param object
     */
    private void appendObject(final Row row, Object object) {
        String text = new String("");
        if (object != null) {
            text = object.toString();
        }
        appendLabel(row, text);
    }

    /**
     * Creates {@link Label} bases on the specified <code>value</code> and
     * appends to the specified <code>row</code>
     *
     * @param row
     * @param value
     */
    private void appendLabel(final Row row, String value) {
        Label label = new Label(value);
        row.appendChild(label);
    }

    /**
     * Appends the specified <code>date</code> to the specified <code>row</code>
     *  @param row
     * @param date*/
    private void appendDate(final Row row, Date date) {
        String labelDate = new String("");
        if (date != null) {
            labelDate = Util.formatDate(date);
        }
        appendLabel(row, labelDate);
    }

    /**
     * Appends operation(edit and remove) to the specified <code>row</code>
     *
     * @param row
     * @param issueLog
     */
    private void appendOperations(final Row row, final IssueLog issueLog) {
        Hbox hbox = new Hbox();
        hbox.appendChild(Util.createEditButton(new EventListener() {
            @Override
            public void onEvent(Event event) {
                goToEditForm(issueLog);
            }
        }));
        hbox.appendChild(Util.createRemoveButton(new EventListener() {
            @Override
            public void onEvent(Event event) {
                confirmDelete(issueLog);
            }
        }));
        row.appendChild(hbox);
    }

    /**
     * Returns {@link LowMediumHighEnum} values
     */
    public LowMediumHighEnum[] getLowMediumHighEnum() {
        return LowMediumHighEnum.values();
    }

    /**
     * Returns {@link IssueTypeEnum} values
     */
    public IssueTypeEnum[] getIssueTypeEnum() {
        return IssueTypeEnum.values();
    }

    /**
     * Returns {@link IssueStatusEnum} values
     */
    public IssueStatusEnum[] getIssueStatusEnum() {
        return IssueStatusEnum.values();
    }

    /**
     * Returns a list of {@link Order} objects
     */
    public List<Order> getOrders() {
        return issueLogModel.getOrders();
    }


    /**
     * Returns a list of {@link User} objects
     */
    public List<User> getUsers() {
        return issueLogModel.getUsers();
    }

    /**
     * Returns date entered
     */
    public Date getDateRaised() {
        if (issueLogModel.getIssueLog() == null) {
            return null;
        }
        return (issueLogModel.getIssueLog().getDateRaised() != null) ? issueLogModel
                .getIssueLog().getDateRaised()
                : null;
    }

    /**
     * Sets the date entered
     *
     * @param date
     *            date eneted
     */
    public void setDateRaised(Date date) {
        issueLogModel.getIssueLog().setDateRaised(date);
    }

    /**
     * Returns date resolved
     */
    public Date getDateResolved() {
        if (issueLogModel.getIssueLog() == null) {
            return null;
        }
        return (issueLogModel.getIssueLog().getDateResolved() != null) ? issueLogModel
                .getIssueLog().getDateResolved()
                : null;
    }
    /**
     * Sets the date resolved
     *
     * @param date
     *            the date resolved
     */
    public void setDateResolved(Date date) {
        issueLogModel.getIssueLog().setDateResolved(date);
    }

    public Date getDeadline() {
        if (issueLogModel.getIssueLog() == null) {
            return null;
        }
        return (issueLogModel.getIssueLog().getDeadline() != null) ? issueLogModel
                .getIssueLog().getDeadline()    // this is a getIntegrationEntityDAO method
                : null;
    }

    public void setDeadline(Date date) {
        issueLogModel.getIssueLog().setDeadline(date);
    }

    /**
     * Returns the {@link IssueLog} object
     */
    public IssueLog getIssueLog() {
        return issueLogModel.getIssueLog();
    }

    /**
     * Returns a list of {@link IssueLog} objects
     */
    public List<IssueLog> getIssueLogs() {
        if (LogsController.getProjectNameVisibility() == true)
            return issueLogModel.getIssueLogs();
        else{
            List<IssueLog> issueLogs = new ArrayList<IssueLog>();
            Order order = LogsController.getOrder();
            for (IssueLog issueLog : issueLogModel.getIssueLogs()) {
                if (issueLog.getOrder().equals(order))
                    issueLogs.add(issueLog);
            }
            return issueLogs;
        }
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
    }

    @Override
    protected void initEdit(IssueLog entity) {
        issueLogModel.initEdit(entity);
    }

    @Override
    protected void save() throws ValidationException {
        if (getIssueLog().getOrder() == null) {
            throw new WrongValueException(bdProjectIssueLog,
                    _("please select a project"));
        }

        if (getIssueLog().getCreatedBy() == null) {
            throw new WrongValueException(bdUserIssueLog,
                    _("please select an author"));
        }
        issueLogModel.confirmSave();
    }

    @Override
    protected IssueLog getEntityBeingEdited() {
        return issueLogModel.getIssueLog();
    }

    @Override
    protected void delete(IssueLog entity) throws InstanceNotFoundException {
        issueLogModel.remove(entity);
    }


}
