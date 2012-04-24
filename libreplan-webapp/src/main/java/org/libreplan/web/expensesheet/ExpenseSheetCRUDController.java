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

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.web.common.ConstraintChecker;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.OnlyOneVisible;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link ExpenseSheet}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class ExpenseSheetCRUDController extends GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(ExpenseSheetCRUDController.class);

    private Window editWindow;

    private Window listWindow;

    @Autowired
    private IExpenseSheetModel expenseSheetModel;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private static final String ITEM = "item";

    /*
     * components listWindow
     */

    private Grid listing;

    /*
     * components editWindow
     */

    private Textbox txtExpenseCode;

    private Checkbox generateCode;

    private BandboxSearch bandboxSelectOrder;

    private Datebox dateboxExpenseDate;

    private Grid gridExpenseLines;

    private BandboxSearch bandboxTasks;

    private Order orderEmpty;

    private Decimalbox dboxValue;

    private BandboxSearch bandboxResource;

    private Textbox tbConcept;

    private ExpenseSheetLineRenderer expenseSheetLineRenderer = new ExpenseSheetLineRenderer();

    private EventListener eventListenerUpdateResource = new EventListener() {
        @Override
        public void onEvent(Event event) {
            expenseSheetModel.getExpenseSheetLineDTO().setResource(
                    (Resource) bandboxResource.getSelectedElement());
        }
    };

    private EventListener eventListenerUpdateTask = new EventListener() {
        @Override
        public void onEvent(Event event) {
            OrderElement orderElement = (OrderElement) bandboxTasks.getSelectedElement();
            expenseSheetModel.getExpenseSheetLineDTO().setOrderElement(orderElement);
        }
    };

    private EventListener eventListenerUpdateProject = new EventListener() {
        @Override
        public void onEvent(Event event) {
            setProject((Order) bandboxSelectOrder.getSelectedElement());
        }
    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        goToList();
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(editWindow, listWindow) : visibility;
    }

    public void saveAndExit() {
        if (save()) {
            goToList();
        }
    }

    public void saveAndContinue() {
        if (save()) {
            goToEditForm(getExpenseSheet());
        }
    }

    public boolean save() {
        ConstraintChecker.isValid(editWindow);
        expenseSheetModel.generateExpenseSheetLineCodesIfIsNecessary();

        if (this.getExpenseSheet() != null
                && (getExpenseSheet().getExpenseSheetLines() == null || getExpenseSheet()
                        .getExpenseSheetLines().isEmpty())) {
            messagesForUser.showMessage(Level.ERROR,
                    "The expense sheet line collection cannot be empty.");
            LOG.error(_("Error on saving element: ", getExpenseSheet().getId()));
            return false;
        }

        try {
            expenseSheetModel.confirmSave();
            messagesForUser.showMessage(Level.INFO, _("Expense sheet saved"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void cancel() {
        goToList();
    }

    public void goToList() {
        expenseSheetModel.prepareToList();
        goToListWindow();
    }

    private void goToListWindow() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
        loadComponentsListWindow();
    }

    private void loadComponentsListWindow() {
        listing = (Grid) this.listWindow.getFellowIfAny("listing");
    }

    public void goToCreateForm() {
        expenseSheetModel.initCreate();
        goToEditWindow();
    }

    public void goToEditForm(ExpenseSheet expenseSheet) {
        expenseSheetModel.prepareToEdit(expenseSheet);
        goToEditWindow();
    }

    private void goToEditWindow() {
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
        loadComponentsEditWindow();
    }

    private void loadComponentsEditWindow() {
        tbConcept = (Textbox) editWindow.getFellowIfAny("tbConcept");
        dateboxExpenseDate = (Datebox) editWindow.getFellowIfAny("dateboxExpenseDate");
        dboxValue = (Decimalbox) editWindow.getFellowIfAny("dboxValue");
        gridExpenseLines = (Grid) editWindow.getFellowIfAny("gridExpenseLines");
        bandboxResource = (BandboxSearch) editWindow.getFellowIfAny("bandboxResource");
        bandboxTasks = (BandboxSearch) editWindow.getFellowIfAny("bandboxTasks");
        bandboxSelectOrder = (BandboxSearch) editWindow.getFellowIfAny("bandboxSelectOrder");

        if (bandboxSelectOrder != null) {
            bandboxSelectOrder
                    .setListboxEventListener(Events.ON_SELECT, eventListenerUpdateProject);
            bandboxSelectOrder.setListboxEventListener(Events.ON_OK, eventListenerUpdateProject);
            bandboxSelectOrder.setBandboxEventListener(Events.ON_CHANGING,
                    eventListenerUpdateProject);
        }

        if (bandboxTasks != null) {
            bandboxTasks.setListboxEventListener(Events.ON_SELECT, eventListenerUpdateTask);
            bandboxTasks.setListboxEventListener(Events.ON_OK, eventListenerUpdateTask);
            bandboxTasks.setBandboxEventListener(Events.ON_CHANGING, eventListenerUpdateTask);
        }

        if (bandboxResource != null) {
            bandboxResource.setListboxEventListener(Events.ON_SELECT, eventListenerUpdateResource);
            bandboxResource.setListboxEventListener(Events.ON_OK, eventListenerUpdateResource);
            bandboxResource
                    .setBandboxEventListener(Events.ON_CHANGING, eventListenerUpdateResource);
        }
    }

    /*
     * Operations in the list window
     */

    public void onCreateNewExpenseSheet() {
        goToCreateForm();
    }

    public List<ExpenseSheet> getExpenseSheets() {
        return expenseSheetModel.getExpenseSheets();
    }

    public void confirmRemove(ExpenseSheet expenseSheet) {
        try {
            int status = Messagebox.show(
                    _("Confirm deleting {0}. Are you sure?", getExpenseSheetName(expenseSheet)),
                    _("Delete"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                removeExpenseSheet(expenseSheet);
            }
        } catch (InterruptedException e) {
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
            LOG.error(_("Error on showing removing element: ", expenseSheet.getId()), e);
        }
    }

    private void removeExpenseSheet(ExpenseSheet expenseSheet) {
        expenseSheetModel.removeExpenseSheet(expenseSheet);
        reloadExpenseSheetList();
    }

    private String getExpenseSheetName(ExpenseSheet expenseSheet) {
        if (expenseSheet != null) {
            String code = expenseSheet.getCode();
            String description = expenseSheet.getDescription();
            if (code != null && description != null) {
                return _("expense sheet ") + code + " - " + description;
            }
        }
        return ITEM;
    }

    /*
     * Operations in the create window
     */

    public ExpenseSheet getExpenseSheet() {
        return expenseSheetModel.getExpenseSheet();
    }

    public SortedSet<ExpenseSheetLine> getExpenseSheetLines() {
        return expenseSheetModel.getExpenseSheetLines();
    }

    private void reloadExpenseSheetList() {
        Util.reloadBindings(this.listing);
    }

    /**
     * Adds a new {@link ExpenseSheetLine} to the list of rows
     *
     * @param rows
     */
    public void addExpenseSheetLine() {
        if (validateLineDTO()) {
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

    private boolean validateLineDTO() {
        boolean result = true;
        if (expenseSheetModel.getExpenseSheetLineDTO().getDate() == null) {
            result = false;
            throw new WrongValueException(this.dateboxExpenseDate, _("must be not empty"));
        }
        if (expenseSheetModel.getExpenseSheetLineDTO().getOrderElement() == null) {
            result = false;
            throw new WrongValueException(this.bandboxTasks, _("must be not empty"));
        }
        BigDecimal value = expenseSheetModel.getExpenseSheetLineDTO().getValue();
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            result = false;
            throw new WrongValueException(this.dboxValue,
                    _("must be not empty and greater or equal than zero"));
        }
        return result;
    }

    public void confirmRemove(ExpenseSheetLine expenseSheetLine) {
        try {
            int status = Messagebox.show(
                    _("Confirm deleting {0}. Are you sure?",
                            getExpenseSheetLineName(expenseSheetLine)), _("Delete"), Messagebox.OK
                            | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                removeExpenseSheetLine(expenseSheetLine);
            }
        } catch (InterruptedException e) {
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
            LOG.error(_("Error on showing removing element: ", expenseSheetLine.getId()), e);
        }
    }

    private void removeExpenseSheetLine(ExpenseSheetLine expenseSheetLine) {
        expenseSheetModel.removeExpenseSheetLine(expenseSheetLine);
        reloadExpenseSheetLines();
    }

    private String getExpenseSheetLineName(ExpenseSheetLine expenseSheetLine) {
        if (expenseSheetLine != null) {
            Date date = expenseSheetLine.getDate();
            OrderElement task = expenseSheetLine.getOrderElement();
            if (date != null && task != null) {
                return _("expense line of the ") + task.getName() + " - " + date;
            }
        }
        return ITEM;
    }

    private void reloadExpenseSheetLines() {
        if (gridExpenseLines != null) {
            Util.reloadBindings(gridExpenseLines);
        }
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            // we have to auto-generate the code for new objects
            try {
                expenseSheetModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
        }
        Util.reloadBindings(editWindow);
        reloadExpenseSheetLines();
    }

    public ExpenseSheetLine getExpenseSheetLineDTO() {
        return expenseSheetModel.getExpenseSheetLineDTO();
    }

    public List<Order> getOrders() {
        List<Order> orders = expenseSheetModel.getOrders();
        orders.add(getProjectDefault());
        return orders;
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

    public Order getProjectDefault() {
        return orderEmpty;
    }

    public ExpenseSheetLineRenderer getExpenseSheetLineRenderer() {
        return expenseSheetLineRenderer;
    }

    /**
     * RowRenderer for a @{ExpenseSheetLine} element
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    public class ExpenseSheetLineRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) {
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
            final ExpenseSheetLine expenseSheetLine = (ExpenseSheetLine) row.getValue();
            final Textbox txtConcept = new Textbox();
            txtConcept.setWidth("160px");
            Util.bind(txtConcept, new Util.Getter<String>() {

                @Override
                public String get() {
                    if (expenseSheetLine != null) {
                        return expenseSheetLine.getConcept();
                    }
                    return "";
                }

            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    if (expenseSheetLine != null) {
                        expenseSheetLine.setConcept(value);
                    }
                }
            });
            row.appendChild(txtConcept);
        }

        private void appendDateInLines(Row row) {
            final ExpenseSheetLine expenseSheetLine = (ExpenseSheetLine) row.getValue();
            final Datebox dateboxExpense = new Datebox();
            Util.bind(dateboxExpense, new Util.Getter<Date>() {

                @Override
                public Date get() {
                    if (expenseSheetLine != null) {
                        return expenseSheetLine.getDate();
                    }
                    return null;
                }

            }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    if (expenseSheetLine != null) {
                        expenseSheetLine.setDate(value);
                    }
                }
            });
            dateboxExpense.setConstraint("no empty:" + _("cannot be null or empty"));
            row.appendChild(dateboxExpense);
        }

        private void appendResourceInLines(Row row) {
            final ExpenseSheetLine expenseSheetLine = (ExpenseSheetLine) row.getValue();
            final BandboxSearch bandboxSearch = BandboxSearch.create(
                    "ResourceInExpenseSheetBandboxFinder");

            bandboxSearch.setSelectedElement(expenseSheetLine.getResource());
            bandboxSearch.setSclass("bandbox-workreport-task");
            bandboxSearch.setListboxWidth("450px");

            EventListener eventListenerUpdateResource = new EventListener() {
                @Override
                public void onEvent(Event event) {
                    Listitem selectedItem = bandboxSearch.getSelectedItem();
                    setResourceInESL(selectedItem, expenseSheetLine);
                }
            };
            bandboxSearch.setListboxEventListener(Events.ON_SELECT, eventListenerUpdateResource);
            bandboxSearch.setListboxEventListener(Events.ON_OK, eventListenerUpdateResource);
            bandboxSearch.setBandboxEventListener(Events.ON_CHANGING, eventListenerUpdateResource);
            row.appendChild(bandboxSearch);
        }

        private void appendCode(final Row row) {
            final ExpenseSheetLine line = (ExpenseSheetLine) row.getValue();
            final Textbox code = new Textbox();
            code.setWidth("170px");
            code.setDisabled(getExpenseSheet().isCodeAutogenerated());
            code.applyProperties();

             if (line.getCode() != null) {
                 code.setValue(line.getCode());
             }

            code.addEventListener("onChange", new EventListener() {
                @Override
                public void onEvent(Event event) {
                    final ExpenseSheetLine line = (ExpenseSheetLine) row.getValue();
                    line.setCode(code.getValue());
                }
            });
            code.setConstraint(checkConstraintLineCodes(line));
            row.appendChild(code);
        }

        private void appendDeleteButton(final Row row) {
            Button delete = new Button("", "/common/img/ico_borrar1.png");
            delete.setHoverImage("/common/img/ico_borrar.png");
            delete.setSclass("icono");
            delete.setTooltiptext(_("Delete"));
            delete.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    confirmRemove((ExpenseSheetLine) row.getValue());
                }
            });
            row.appendChild(delete);
        }

        private void appendValueInLines(Row row) {
            final ExpenseSheetLine expenseSheetLine = (ExpenseSheetLine) row.getValue();
            final Decimalbox dbValue = new Decimalbox();
            dbValue.setScale(2);

            Util.bind(dbValue, new Util.Getter<BigDecimal>() {

                @Override
                public BigDecimal get() {
                    if (expenseSheetLine != null) {
                        return expenseSheetLine.getValue();
                    }
                    return BigDecimal.ZERO.setScale(2);
                }

            }, new Util.Setter<BigDecimal>() {

                @Override
                public void set(BigDecimal value) {
                    if (expenseSheetLine != null) {
                        expenseSheetLine.setValue(value);
                    }
                }
            });

            dbValue.setConstraint(checkConstraintExpenseValue());
            row.appendChild(dbValue);
        }

        /**
         * Append a Bandbox @{link OrderElement} to row
         *
         * @param row
         */
        private void appendOrderElementInLines(Row row) {
            final ExpenseSheetLine expenseSheetLine = (ExpenseSheetLine) row.getValue();
            final BandboxSearch bandboxSearch = BandboxSearch
                    .create("OrderElementInExpenseSheetBandboxFinder");

            bandboxSearch.setSelectedElement(expenseSheetLine.getOrderElement());
            bandboxSearch.setSclass("bandbox-workreport-task");
            bandboxSearch.setListboxWidth("450px");

            EventListener eventListenerUpdateOrderElement = new EventListener() {
                @Override
                public void onEvent(Event event) {
                    Listitem selectedItem = bandboxSearch.getSelectedItem();
                    setOrderElementInESL(selectedItem, expenseSheetLine);
                }
            };
            bandboxSearch
                    .setListboxEventListener(Events.ON_SELECT, eventListenerUpdateOrderElement);
            bandboxSearch.setListboxEventListener(Events.ON_OK, eventListenerUpdateOrderElement);
            bandboxSearch.setBandboxEventListener(Events.ON_CHANGING,
                    eventListenerUpdateOrderElement);
            bandboxSearch.setBandboxConstraint("no empty:" + _("cannot be null or empty"));
            row.appendChild(bandboxSearch);
        }

        private void setOrderElementInESL(Listitem selectedItem, ExpenseSheetLine line) {
            OrderElement orderElement = (selectedItem == null ? null : (OrderElement) selectedItem
                    .getValue());
            line.setOrderElement(orderElement);
        }

        private void setResourceInESL(Listitem selectedItem,
                ExpenseSheetLine expenseSheetLine) {
            Resource resource = (selectedItem == null ? null : (Resource) selectedItem.getValue());
            expenseSheetLine.setResource(resource);
        }
    }

    public Constraint checkConstraintExpenseValue() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value) throws WrongValueException {
                BigDecimal expenseValue = (BigDecimal) value;
                if (expenseValue == null || expenseValue.compareTo(BigDecimal.ZERO) < 0) {
                    throw new WrongValueException(comp, _("must be greater or equal than 0"));
                }
            }
        };
    }

    public Constraint checkConstraintLineCodes(final ExpenseSheetLine line) {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value) throws WrongValueException {
                if (!getExpenseSheet().isCodeAutogenerated()) {
                    String code = (String) value;
                    if (code == null || code.isEmpty()){
                        throw new WrongValueException(comp,
                                _("The code cannot be empty."));
                    }else{
                        String oldCode = line.getCode();
                        line.setCode(code);
                        if(!getExpenseSheet()
                            .checkConstraintNonRepeatedExpenseSheetLinesCodes()) {
                                line.setCode(oldCode);
                                throw new WrongValueException(comp,
                                        _("The code must be unique."));
                        }
                    }
                }
            }
        };
    }

    public Constraint checkConstraintExpendeCode() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value) throws WrongValueException {
                if (!getExpenseSheet().isCodeAutogenerated()) {
                    String code = (String) value;
                    if (code == null || code.isEmpty()) {
                        throw new WrongValueException(comp,
                                _("The code cannot be empty and it must be unique."));
                    } else if (!getExpenseSheet().checkConstraintUniqueCode()) {
                        throw new WrongValueException(comp,
                                _("it already exists another expense sheet with the same code."));
                    }
                }
            }
        };
    }

}
