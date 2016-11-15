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

package org.libreplan.web.expensesheet;

import org.joda.time.LocalDate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.IndexController;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.common.entrypoints.IURLHandlerRegistry;
import org.libreplan.web.common.entrypoints.MatrixParameters;
import org.libreplan.web.security.SecurityUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static org.libreplan.web.I18nHelper._;

/**
 * Controller for CRUD actions over a {@link ExpenseSheet}.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class ExpenseSheetCRUDController
        extends BaseCRUDController<ExpenseSheet>
        implements IExpenseSheetCRUDController {

    private static final String NOT_EMPTY = "cannot be empty";

    private IExpenseSheetModel expenseSheetModel;

    /**
     * Components editWindow
     */

    private Datebox dateboxExpenseDate;

    private Grid gridExpenseLines;

    private BandboxSearch bandboxTasks;

    private Decimalbox dboxValue;

    private BandboxSearch bandboxResource;

    private Textbox tbConcept;

    private ExpenseSheetLineRenderer expenseSheetLineRenderer = new ExpenseSheetLineRenderer();

    private IURLHandlerRegistry URLHandlerRegistry;

    private boolean fromUserDashboard = false;

    private boolean cancel = false;

    public ExpenseSheetCRUDController() {
        if ( expenseSheetModel == null ) {
            expenseSheetModel = (IExpenseSheetModel) SpringUtil.getBean("expenseSheetModel");
        }

        if ( URLHandlerRegistry == null ) {
            URLHandlerRegistry = (IURLHandlerRegistry) SpringUtil.getBean("URLHandlerRegistry");
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        checkUserHasProperRoleOrSendForbiddenCode();
        URLHandlerRegistry.getRedirectorFor(IExpenseSheetCRUDController.class).register(this, page);
    }

    private void checkUserHasProperRoleOrSendForbiddenCode() {
        HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
        Map<String, String> matrixParams = MatrixParameters.extract(request);

        // If it doesn't come from a entry point
        if (matrixParams.isEmpty()) {
            if (!SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_EXPENSES)) {
                Util.sendForbiddenStatusCodeInHttpServletResponse();
            }
        }
    }

    @Override
    public void save() throws ValidationException {
        expenseSheetModel.confirmSave();
    }

    @Override
    protected void beforeSaving() throws ValidationException {
        super.beforeSaving();
        expenseSheetModel.generateExpenseSheetLineCodesIfIsNecessary();
    }

    private void loadComponentsEditWindow() {
        tbConcept = (Textbox) editWindow.getFellowIfAny("tbConcept");
        dateboxExpenseDate = (Datebox) editWindow.getFellowIfAny("dateboxExpenseDate");
        dboxValue = (Decimalbox) editWindow.getFellowIfAny("dboxValue");
        gridExpenseLines = (Grid) editWindow.getFellowIfAny("gridExpenseLines");
        bandboxResource = (BandboxSearch) editWindow.getFellowIfAny("bandboxResource");
        bandboxTasks = (BandboxSearch) editWindow.getFellowIfAny("bandboxTasks");
    }

    /**
     * Operations in the list window.
     */

    public List<ExpenseSheet> getExpenseSheets() {
        return expenseSheetModel.getExpenseSheets();
    }

    /**
     * Operations in the create window.
     */

    public ExpenseSheet getExpenseSheet() {
        return expenseSheetModel.getExpenseSheet();
    }

    public SortedSet<ExpenseSheetLine> getExpenseSheetLines() {
        return expenseSheetModel.getExpenseSheetLines();
    }

    /**
     * Adds a new {@link ExpenseSheetLine} to the list of rows.
     */
    public void addExpenseSheetLine() {
        if (validateNewLine()) {
            expenseSheetModel.addExpenseSheetLine();
            reloadExpenseSheetLines();
            reloadComponentsNewLine();
        }
    }

    private void reloadComponentsNewLine() {
        Util.reloadBindings(bandboxTasks);
        Util.reloadBindings(bandboxResource);
        Util.reloadBindings(tbConcept);
        Util.reloadBindings(dboxValue);
    }

    private boolean validateNewLine() {
        if (expenseSheetModel.getNewExpenseSheetLine().getDate() == null) {
            throw new WrongValueException(this.dateboxExpenseDate, _(NOT_EMPTY));
        }

        if (expenseSheetModel.getNewExpenseSheetLine().getOrderElement() == null) {
            throw new WrongValueException(this.bandboxTasks, _(NOT_EMPTY));
        }

        BigDecimal value = expenseSheetModel.getNewExpenseSheetLine().getValue();

        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new WrongValueException(this.dboxValue, _("cannot be empty or less than zero"));
        }
        return true;
    }

    public void confirmRemove(ExpenseSheetLine expenseSheetLine) {
        int status = Messagebox.show(
                _("Confirm deleting {0}. Are you sure?", getExpenseSheetLineName(expenseSheetLine)),
                _("Delete"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);

        if (Messagebox.OK == status)
            removeExpenseSheetLine(expenseSheetLine);

    }

    private void removeExpenseSheetLine(ExpenseSheetLine expenseSheetLine) {
        expenseSheetModel.removeExpenseSheetLine(expenseSheetLine);
        reloadExpenseSheetLines();
    }

    private String getExpenseSheetLineName(ExpenseSheetLine expenseSheetLine) {
        if (expenseSheetLine != null) {
            LocalDate date = expenseSheetLine.getDate();
            OrderElement task = expenseSheetLine.getOrderElement();

            if (date != null && task != null) {
                return _("expense line of the ") + task.getName() + " - " + date;
            }
        }
        return _("item");
    }

    private void reloadExpenseSheetLines() {
        if (gridExpenseLines != null) {
            Util.reloadBindings(gridExpenseLines);
        }
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            // We have to auto-generate the code for new objects
            try {
                expenseSheetModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
        }
        Util.reloadBindings(editWindow);
        reloadExpenseSheetLines();
    }

    public ExpenseSheetLine getNewExpenseSheetLine() {
        return expenseSheetModel.getNewExpenseSheetLine();
    }

    public List<Order> getOrders() {
        return expenseSheetModel.getOrders();
    }

    public List<OrderElement> getTasks() {
        return expenseSheetModel.getTasks();
    }

    public void setProject(Order project) {
        expenseSheetModel.setSelectedProject(project);
        Util.reloadBindings(bandboxTasks);
    }

    public Order getProject() {
        return expenseSheetModel.getSelectedProject();
    }

    public ExpenseSheetLineRenderer getExpenseSheetLineRenderer() {
        return expenseSheetLineRenderer;
    }

    /**
     * RowRenderer for a @{ExpenseSheetLine} element.
     */
    private class ExpenseSheetLineRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data, int i) {
            ExpenseSheetLine expenseSheetLine = (ExpenseSheetLine) data;
            row.setValue(expenseSheetLine);

            appendOrderElementInLines(row);
            appendValueInLines(row);
            appendConceptInLines(row);
            appendDateInLines(row);
            appendResourceInLines(row);
            appendCode(row);
            appendDeleteButton(row);
        }

        private void appendConceptInLines(Row row) {
            final ExpenseSheetLine expenseSheetLine = row.getValue();
            final Textbox txtConcept = new Textbox();
            txtConcept.setWidth("160px");

            Util.bind(
                    txtConcept,
                    () -> expenseSheetLine != null ? expenseSheetLine.getConcept() : "",
                    value -> {
                        if (expenseSheetLine != null)
                            expenseSheetLine.setConcept(value);
                    });

            row.appendChild(txtConcept);
        }

        private void appendDateInLines(Row row) {
            final ExpenseSheetLine expenseSheetLine = row.getValue();
            final Datebox dateboxExpense = new Datebox();

            Util.bind(
                    dateboxExpense,
                    () -> expenseSheetLine != null && expenseSheetLine.getDate() != null
                            ? expenseSheetLine.getDate().toDateTimeAtStartOfDay().toDate()
                            : null,
                    value -> {
                        if (expenseSheetLine != null) {
                            LocalDate newDate = null;

                            if (value != null)
                                newDate = LocalDate.fromDateFields(value);

                            expenseSheetModel.keepSortedExpenseSheetLines(expenseSheetLine, newDate);
                            reloadExpenseSheetLines();
                        }
                    });

            dateboxExpense.setConstraint("no empty:" + _(NOT_EMPTY));
            row.appendChild(dateboxExpense);
        }

        private void appendResourceInLines(Row row) {
            final ExpenseSheetLine expenseSheetLine = row.getValue();
            final BandboxSearch bandboxSearch = BandboxSearch.create("ResourceInExpenseSheetBandboxFinder");

            bandboxSearch.setSelectedElement(expenseSheetLine.getResource());
            bandboxSearch.setSclass("bandbox-workreport-task");
            bandboxSearch.setListboxWidth("450px");

            EventListener eventListenerUpdateResource = event -> {
                Listitem selectedItem = bandboxSearch.getSelectedItem();
                setResourceInESL(selectedItem, expenseSheetLine);
            };

            bandboxSearch.setListboxEventListener(Events.ON_SELECT, eventListenerUpdateResource);
            bandboxSearch.setListboxEventListener(Events.ON_OK, eventListenerUpdateResource);
            bandboxSearch.setBandboxEventListener(Events.ON_CHANGING, eventListenerUpdateResource);
            row.appendChild(bandboxSearch);
        }

        private void appendCode(final Row row) {
            final ExpenseSheetLine line = row.getValue();
            final Textbox code = new Textbox();
            code.setWidth("90%");
            code.setDisabled(getExpenseSheet().isCodeAutogenerated());
            code.applyProperties();

            if (line.getCode() != null) {
                code.setValue(line.getCode());
            }

            code.addEventListener("onChange", event -> {
                final ExpenseSheetLine line1 = row.getValue();
                line1.setCode(code.getValue());
            });

            code.setConstraint(checkConstraintLineCodes(line));
            row.appendChild(code);
        }

        private void appendDeleteButton(final Row row) {
            Button delete = new Button("", "/common/img/ico_borrar1.png");
            delete.setHoverImage("/common/img/ico_borrar.png");
            delete.setSclass("icono");
            delete.setTooltiptext(_("Delete"));
            delete.addEventListener(Events.ON_CLICK, event -> confirmRemove(row.getValue()));
            row.appendChild(delete);
        }

        private void appendValueInLines(Row row) {
            final ExpenseSheetLine expenseSheetLine = row.getValue();
            final Decimalbox dbValue = new Decimalbox();
            dbValue.setScale(2);

            Util.bind(
                    dbValue,
                    () -> expenseSheetLine != null ? expenseSheetLine.getValue() : BigDecimal.ZERO.setScale(2),
                    value -> {
                        if (expenseSheetLine != null)
                            expenseSheetLine.setValue(value);
                    });

            dbValue.setConstraint(checkConstraintExpenseValue());
            dbValue.setFormat(Util.getMoneyFormat());
            row.appendChild(dbValue);
        }

        /**
         * Append a Bandbox @{link OrderElement} to row.
         *
         * @param row
         */
        private void appendOrderElementInLines(Row row) {
            final ExpenseSheetLine expenseSheetLine = row.getValue();
            final BandboxSearch bandboxSearch = BandboxSearch.create("OrderElementInExpenseSheetBandboxFinder");

            bandboxSearch.setSelectedElement(expenseSheetLine.getOrderElement());
            bandboxSearch.setSclass("bandbox-workreport-task");
            bandboxSearch.setListboxWidth("450px");

            EventListener eventListenerUpdateOrderElement = event -> {
                Listitem selectedItem = bandboxSearch.getSelectedItem();
                setOrderElementInESL(selectedItem, expenseSheetLine);
            };

            bandboxSearch.setListboxEventListener(Events.ON_SELECT, eventListenerUpdateOrderElement);
            bandboxSearch.setListboxEventListener(Events.ON_OK, eventListenerUpdateOrderElement);
            bandboxSearch.setBandboxEventListener(Events.ON_CHANGING, eventListenerUpdateOrderElement);
            bandboxSearch.setBandboxConstraint("no empty:" + _(NOT_EMPTY));
            row.appendChild(bandboxSearch);
        }

        private Constraint checkConstraintLineCodes(final ExpenseSheetLine line) {
            return (comp, value) -> {
                if (!getExpenseSheet().isCodeAutogenerated()) {
                    String code = (String) value;

                    if (code == null || code.isEmpty()) {
                        throw new WrongValueException(comp, _("Code cannot be empty."));
                    } else {
                        String oldCode = line.getCode();
                        line.setCode(code);

                        if (!getExpenseSheet().isNonRepeatedExpenseSheetLinesCodesConstraint()) {
                            line.setCode(oldCode);
                            throw new WrongValueException(comp, _("The code must be unique."));
                        }
                    }
                }
            };
        }

        private void setOrderElementInESL(Listitem selectedItem, ExpenseSheetLine line) {
            OrderElement orderElement = selectedItem == null ? null : (OrderElement) selectedItem.getValue();
            line.setOrderElement(orderElement);
        }

        private void setResourceInESL(Listitem selectedItem, ExpenseSheetLine expenseSheetLine) {
            Resource resource = selectedItem == null ? null : (Resource) selectedItem.getValue();
            expenseSheetLine.setResource(resource);
        }
    }

    public Constraint checkConstraintExpenseValue() {
        return (comp, value) -> {
            BigDecimal expenseValue = (BigDecimal) value;
            if (expenseValue == null || expenseValue.compareTo(BigDecimal.ZERO) < 0) {
                throw new WrongValueException(comp, _("must be greater or equal than 0"));
            }
        };
    }

    public Constraint checkConstraintExpendeCode() {
        return (comp, value) -> {
            if (!getExpenseSheet().isCodeAutogenerated()) {
                String code = (String) value;

                if (code == null || code.isEmpty()) {
                    throw new WrongValueException(comp, _("The code cannot be empty and it must be unique."));
                } else if (!getExpenseSheet().isUniqueCodeConstraint()) {

                    throw new WrongValueException(
                            comp, _("it already exists another expense sheet with the same code."));
                }
            }
        };
    }

    public Date getExpenseSheetLineDate() {
        if (expenseSheetModel.getNewExpenseSheetLine() != null) {

            return (expenseSheetModel.getNewExpenseSheetLine().getDate() != null)
                    ? expenseSheetModel.getNewExpenseSheetLine().getDate().toDateTimeAtStartOfDay().toDate()
                    : null;
        }

        return null;
    }

    public void setExpenseSheetLineDate(Date date) {
        if (expenseSheetModel.getNewExpenseSheetLine() != null) {
            LocalDate localDate = null;
            if (date != null) {
                localDate = LocalDate.fromDateFields(date);
            }
            expenseSheetModel.getNewExpenseSheetLine().setDate(localDate);
        }
    }

    @Override
    protected void initCreate() {
        initCreate(false);
    }

    @Override
    protected void initEdit(ExpenseSheet expenseSheet) {
        expenseSheetModel.prepareToEdit(expenseSheet);
        loadComponentsEditWindow();
    }

    @Override
    protected ExpenseSheet getEntityBeingEdited() {
        return expenseSheetModel.getExpenseSheet();
    }

    @Override
    public void delete(ExpenseSheet expenseSheet) throws InstanceNotFoundException {
        expenseSheetModel.removeExpenseSheet(expenseSheet);
    }

    @Override
    protected String getEntityType() {
        return _("Expense Sheet");
    }

    @Override
    protected String getPluralEntityType() {
        return _("Expense Sheets");
    }

    public String getCurrencySymbol() {
        return Util.getCurrencySymbol();
    }

    public String getMoneyFormat() {
        return Util.getMoneyFormat();
    }

    @Override
    public void goToCreatePersonalExpenseSheet() {
        if (!SecurityUtils.isUserInRole(UserRole.ROLE_BOUND_USER)) {
            Util.sendForbiddenStatusCodeInHttpServletResponse();
        }

        state = CRUDControllerState.CREATE;
        initCreate(true);
        showEditWindow();
        fromUserDashboard = true;
    }

    private void initCreate(boolean personal) {
        expenseSheetModel.initCreate(personal);
        loadComponentsEditWindow();
    }

    public String getResource() {
        Resource resource = expenseSheetModel.getResource();
        return resource == null ? "" : resource.getShortDescription();
    }

    @Override
    public void goToEditPersonalExpenseSheet(ExpenseSheet expenseSheet) {
        if ( !SecurityUtils.isUserInRole(UserRole.ROLE_BOUND_USER) ||
                !expenseSheetModel.isPersonalAndBelongsToCurrentUser(expenseSheet) )

            Util.sendForbiddenStatusCodeInHttpServletResponse();

        goToEditForm(expenseSheet);
        fromUserDashboard = true;
    }

    @Override
    protected void showListWindow() {
        if (fromUserDashboard) {
            String url = IndexController.USER_DASHBOARD_URL;

            if (!cancel)
                url += "?expense_sheet_saved=" + expenseSheetModel.getExpenseSheet().getCode();

            Executions.getCurrent().sendRedirect(url);
        } else {
            super.showListWindow();
        }
    }

    @Override
    protected void cancel() {
        cancel = true;
    }

    public String getType() {
        return getType(expenseSheetModel.getExpenseSheet());
    }

    private String getType(ExpenseSheet expenseSheet) {
        return expenseSheet != null && expenseSheet.isPersonal() ? _("Personal") : _("Regular");
    }

    public RowRenderer getExpenseSheetsRenderer() {
        return (row, data, i) -> {
            final ExpenseSheet expenseSheet = (ExpenseSheet) data;
            row.setValue(expenseSheet);

            Util.appendLabel(row, expenseSheet.getFirstExpense().toString());
            Util.appendLabel(row, expenseSheet.getLastExpense().toString());
            Util.appendLabel(row, Util.addCurrencySymbol(expenseSheet.getTotal()));
            Util.appendLabel(row, expenseSheet.getCode());
            Util.appendLabel(row, expenseSheet.getDescription());
            Util.appendLabel(row, getType(expenseSheet));

            Util.appendOperationsAndOnClickEvent(
                    row, event -> goToEditForm(expenseSheet), event -> confirmDelete(expenseSheet));

        };
    }

}
