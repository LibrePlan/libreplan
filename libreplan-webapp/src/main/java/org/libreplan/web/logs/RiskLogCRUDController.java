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
import org.libreplan.business.logs.entities.LowMediumHighEnum;
import org.libreplan.business.logs.entities.RiskLog;
import org.libreplan.business.logs.entities.RiskScoreStatesEnum;
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
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Cell;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Label;
import org.zkoss.zul.RowRenderer;

import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.libreplan.web.I18nHelper._;

/**
 * Controller for RiskLog CRUD actions.
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RiskLogCRUDController extends BaseCRUDController<RiskLog> {

    private IRiskLogModel riskLogModel;

    private BandboxSearch bdProjectRiskLog;

    private BandboxSearch bdUserRiskLog;

    private Textbox riskScore;

    private boolean saved;

    /**
     * Renders LOW, MEDIUM, HIGH enums.
     *
     * Should be public!
     * Used in _editRiskLog.zul
     */
    public static ListitemRenderer lowMediumHighEnumRenderer = (item, data, i) -> {
        LowMediumHighEnum lowMediumHighEnum = (LowMediumHighEnum) data;
        String displayName = lowMediumHighEnum.getDisplayName();
        item.setLabel(displayName);
    };

    /**
     * Renders riskScoreState enums.
     *
     * Should be public!
     * Used in _editRiskLog.zul
     */
    public static ListitemRenderer riskScoreStatesEnumRenderer = (item, data, i) -> {
        RiskScoreStatesEnum riskScoreStatesEnum = (RiskScoreStatesEnum) data;
        String displayName = riskScoreStatesEnum.getDisplayName();
        item.setLabel(displayName);
    };

    public RiskLogCRUDController() {
        riskLogModel = (IRiskLogModel) SpringUtil.getBean("riskLogModel");
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        riskScore = (Textbox)comp.getFellow("editWindow").getFellow("riskScore");
        comp.setAttribute("riskLogController", this, true);
        showListWindow();
        initializeOrderComponent();
        initializeUserComponent();
        bdProjectRiskLog.setDisabled(!LogsController.getProjectNameVisibility());
        bdUserRiskLog.setDisabled(true);
    }

    /**
     * Initializes order component.
     */
    private void initializeOrderComponent() {
        bdProjectRiskLog = (BandboxSearch) editWindow.getFellow("bdProjectRiskLog");
        Util.createBindingsFor(bdProjectRiskLog);

        bdProjectRiskLog.setListboxEventListener(Events.ON_SELECT, event -> {
            final Object object = bdProjectRiskLog.getSelectedElement();
            riskLogModel.setOrder((Order) object);
        });

        bdProjectRiskLog.setListboxEventListener(Events.ON_OK, event -> {
            final Object object = bdProjectRiskLog.getSelectedElement();
            riskLogModel.setOrder((Order) object);
            bdProjectRiskLog.close();
        });
    }

    /**
     * Initializes user component.
     */
    private void initializeUserComponent() {
        bdUserRiskLog = (BandboxSearch) editWindow.getFellow("bdUserRiskLog");
        Util.createBindingsFor(bdUserRiskLog);

        bdUserRiskLog.setListboxEventListener(Events.ON_SELECT, event -> {
            final Object object = bdUserRiskLog.getSelectedElement();
            riskLogModel.setCreatedBy((User) object);
        });

        bdUserRiskLog.setListboxEventListener(Events.ON_OK, event -> {
            final Object object = bdUserRiskLog.getSelectedElement();
            riskLogModel.setCreatedBy((User) object);
            bdUserRiskLog.close();
        });
    }

    /**
     * Renders risk logs.
     * Should be public!
     * Used in _listRiskLog.zul
     *
     * @return {@link RowRenderer}
     */
    public RowRenderer getRiskLogsRowRenderer() {
        return (row, data, i) -> {
            final RiskLog riskLog = (RiskLog) data;
            row.setValue(riskLog);
            appendObject(row, riskLog.getCode());
            appendLabel(row, riskLog.getOrder().getName());
            appendObject(row, riskLog.getProbability());
            appendObject(row, riskLog.getImpact());
            appendObject(row, riskLog.getRiskScore());
            appendLabel(row, riskLog.getStatus());
            appendLabel(row, riskLog.getDescription());
            appendDate(row, riskLog.getDateCreated());
            appendLabel(row, riskLog.getCreatedBy().getFullName() + riskLog.getCreatedBy().getLoginName());
            appendLabel(row, riskLog.getCounterMeasures());
            appendLabel(row, riskLog.getScoreAfterCM().getDisplayName());
            appendLabel(row, riskLog.getContingency());
            appendLabel(row, riskLog.getResponsible());
            appendDate(row, riskLog.getActionWhen());
            appendLabel(row, riskLog.getNotes());
            appendOperations(row, riskLog);
            setScoreCellColor(row, riskLog.getRiskScore());
        };
    }

    private void setScoreCellColor(Row row, int riskScore) {
        Cell cell = (Cell) row.getChildren().get(4);

        switch ( riskScore ) {
            case 1:
                cell.setClass("riskLog-score-color-1");
                break;

            case 2:
                cell.setClass("riskLog-score-color-2");
                break;

            case 3:
                cell.setClass("riskLog-score-color-3");
                break;

            case 4:
                cell.setClass("riskLog-score-color-4");
                break;

            case 6:
                cell.setClass("riskLog-score-color-6");
                break;

            case 9:
                cell.setClass("riskLog-score-color-9");
                break;

            default: throw new UnsupportedCharsetException("Unsupported risk score");
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
     * @param riskLog
     */
    private void appendOperations(final Row row, final RiskLog riskLog) {
        Hbox hbox = new Hbox();
        hbox.appendChild(Util.createEditButton(event -> goToEditForm(riskLog)));
        hbox.appendChild(Util.createRemoveButton(event -> confirmDelete(riskLog)));
        row.appendChild(hbox);
    }

    /**
     * Should be public!
     * Used in _editRiskLog.zul
     *
     * @return  {@link LowMediumHighEnum} values
     */
    public LowMediumHighEnum[] getLowMediumHighEnums() {
        return LowMediumHighEnum.values();
    }
    /**
     * Should be public!
     * Used in _editRiskLog.zul
     *
     * @return  {@link RiskScoreStatesEnum} values
     */
    public RiskScoreStatesEnum[] getRiskScoreStatesEnums() {
        return RiskScoreStatesEnum.values();
    }

    /**
     * @return {@link List<Order>}
     */
    public List<Order> getOrders() {
        return riskLogModel.getOrders();
    }

    /**
     * @return {@link List<User>}
     */
    public List<User> getUsers() {
        return riskLogModel.getUsers();
    }

    /**
     * @return  {@link Date}
     */
    public Date getDateCreated() {
        if (riskLogModel.getRiskLog() == null) {
            return null;
        }
        return (riskLogModel.getRiskLog().getDateCreated() != null) ? riskLogModel.getRiskLog().getDateCreated() : null;
    }

    /**
     * Sets the date created
     *
     * @param date
     *            date created
     */
    public void setDateCreated(Date date) {
        riskLogModel.getRiskLog().setDateCreated(date);
    }

    /**
     * Sets the Action When
     *
     * @param date
     *            date created
     */
    public void setActionWhen(Date date) {
        riskLogModel.getRiskLog().setActionWhen(date);
    }

    /**
     * @return  {@link Date}
     */
    public Date getActionWhen() {
        if (riskLogModel.getRiskLog() == null) {
            return null;
        }
        return (riskLogModel.getRiskLog().getActionWhen() != null) ? riskLogModel.getRiskLog().getActionWhen() : null;
    }

    /**
     * Sets the Score for risk.
     */
    public void setUpdateScore() {
        riskScore.setValue(String.valueOf(getRiskLog().getRiskScore()));
    }

    /**
     * Should be public!
     * Used in _editRiskLog.zul
     * @return {@link RiskLog}
     */
    public RiskLog getRiskLog() {
        return riskLogModel.getRiskLog();
    }

    /**
     * Returns a list of {@link RiskLog} objects
     */
    public List<RiskLog> getRiskLogs() {
        if (LogsController.getProjectNameVisibility()) {
            return riskLogModel.getRiskLogs();

        } else {
            List<RiskLog> riskLogs = new ArrayList<>();
            Order order = LogsController.getOrder();

            for (RiskLog issueLog : riskLogModel.getRiskLogs()) {
                if (issueLog.getOrder().equals(order)) {
                    riskLogs.add(issueLog);
                }

            }

            return riskLogs;
        }
    }

    public Order getOrder() {
        if (!LogsController.getProjectNameVisibility()) {
            getRiskLog().setOrder(LogsController.getOrder());

            return getRiskLog().getOrder();

        } else {
            return riskLogModel.getRiskLog().getOrder();
        }

    }

    @Override
    protected String getEntityType() {
        return _("Issue log");
    }

    @Override
    protected String getPluralEntityType() {
        return _("Issue logs");
    }

    @Override
    protected void initCreate() {
        riskLogModel.initCreate();
    }

    @Override
    protected void initEdit(RiskLog entity) {
        riskLogModel.initEdit(entity);
    }

    @Override
    protected void save() throws ValidationException {
        if (getRiskLog().getOrder() == null) {
            throw new WrongValueException(bdProjectRiskLog, _("please select a project"));
        }

        if (getRiskLog().getCreatedBy() == null) {
            throw new WrongValueException(bdUserRiskLog, _("please select an author"));
        }

        riskLogModel.confirmSave();
        saved = true;
    }

    @Override
    protected RiskLog getEntityBeingEdited() {
        return riskLogModel.getRiskLog();
    }

    @Override
    protected void delete(RiskLog entity) throws InstanceNotFoundException {
        riskLogModel.remove(entity);
    }

    public Boolean isRiskLogSaved () {
        return saved;
    }

    public void setRiskLogToModel (RiskLog log) {
        this.riskLogModel.setRisklog(log);
    }

}