/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.workreports;

import static org.navalplanner.web.I18nHelper._;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.Autocomplete;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link WorkReport}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
public class WorkReportCRUDController extends GenericForwardComposer implements
        IWorkReportCRUDControllerEntryPoints {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(WorkReportCRUDController.class);

    private Window createWindow;

    private Window listWindow;

    private IWorkReportModel workReportModel;

    private IURLHandlerRegistry URLHandlerRegistry;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private IWorkReportTypeCRUDControllerEntryPoints workReportTypeCRUD;

    private WorkReportListRenderer workReportListRenderer = new WorkReportListRenderer();

    private Grid listWorkReportLines;

    private final static String MOLD = "paging";

    private final static int PAGING = 10;

    private static final String ITEM = "item";

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        listWorkReportLines = (Grid) createWindow.getFellowIfAny("listWorkReportLines");
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        final URLHandler<IWorkReportCRUDControllerEntryPoints> handler = URLHandlerRegistry
                .getRedirectorFor(IWorkReportCRUDControllerEntryPoints.class);
        handler.registerListener(this, page);
        getVisibility().showOnly(listWindow);
    }

    /**
     * Show confirm window for deleting {@link WorkReport}
     *
     * @param workReport
     */
    public void showConfirmDelete(WorkReport workReport) {
        try {
            final String workReportName = formatWorkReportName(workReport);
            int status = Messagebox.show(_("Confirm deleting {0}. Are you sure?", workReportName), "Delete",
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                workReportModel.remove(workReport);
            }
        } catch (InterruptedException e) {
            messagesForUser.showMessage(
                    Level.ERROR, e.getMessage());
            LOG.error(_("Error on removing element: ", workReport.getId()), e);
        }
    }

    private String formatWorkReportName(WorkReport workReport) {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
        return workReport.getWorkReportType().getName() + " - " + sdf.format(workReport.getDate());
    }

    public List<WorkReport> getWorkReports() {
        return workReportModel.getWorkReports();
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(createWindow,
                listWindow)
                : visibility;
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
        try {
            workReportModel.confirmSave();
            messagesForUser.showMessage(Level.INFO,
                    _("Work report saved"));
            return true;
        } catch (ValidationException e) {
            showInvalidValues(e);
        }
        return false;
    }

    /**
     * Shows invalid values for {@link WorkReport} and {@link WorkReportLine}
     * entities
     *
     * @param e
     */
    private void showInvalidValues(ValidationException e) {
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            Object value = invalidValue.getBean();
            if (value instanceof WorkReport) {
                validateWorkReport(invalidValue);
            }
            if (value instanceof WorkReportLine) {
                validateWorkReportLine(invalidValue);
            }
        }
    }

    /**
     * Validates {@link WorkReport} data constraints
     *
     * @param invalidValue
     */
    private void validateWorkReport(InvalidValue invalidValue) {
        String propertyName = invalidValue.getPropertyName();

        if (WorkReport.DATE.equals(propertyName)) {
            Datebox datebox = (Datebox) createWindow.getFellowIfAny(propertyName);
            throw new WrongValueException(datebox, _("Date cannot be null"));
        }
        if (WorkReport.RESPONSIBLE.equals(propertyName)) {
            Textbox textbox = (Textbox) createWindow.getFellowIfAny(propertyName);
            throw new WrongValueException(textbox,
                    _("Responsible cannot be null"));
        }
    }

    /**
     * Validates {@link WorkReportLine} data constraints
     *
     * @param invalidValue
     */
    @SuppressWarnings("unchecked")
    private void validateWorkReportLine(InvalidValue invalidValue) {
        if (listWorkReportLines != null) {
            // Find which row contains workReportLine inside listBox
            Row row = findWorkReportLine(listWorkReportLines.getRows().getChildren(),
                    (WorkReportLine) invalidValue.getBean());

            if (row != null) {
                String propertyName = invalidValue.getPropertyName();

                if (WorkReportLine.RESOURCE.equals(propertyName)) {
                    // Locate TextboxResource
                    Textbox txtResource = getTextboxResource(row);
                    // Value is incorrect, clear
                    txtResource.setValue("");
                    throw new WrongValueException(txtResource,
                            _("Resource cannot be null"));
                }
                if (WorkReportLine.ORDER_ELEMENT.equals(propertyName)) {
                    // Locate TextboxOrder
                    Textbox txtOrder = getTextboxOrder(row);
                    // Value is incorrect, clear
                    txtOrder.setValue("");
                    throw new WrongValueException(txtOrder,
                            _("Código cannot be null"));
                }
            }
        }
    }

    /**
     * Locates which {@link Row} is bound to {@link WorkReportLine} in
     * rows
     *
     * @param rows
     * @param workReportLine
     * @return
     */
    private Row findWorkReportLine(List<Row> rows,
            WorkReportLine workReportLine) {
        for (Row row : rows) {
            if (workReportLine.equals(row.getValue())) {
                return row;
            }
        }
        return null;
    }

    /**
     * Locates {@link Textbox} Resource in {@link Row}
     *
     * @param row
     * @return
     */
    private Textbox getTextboxResource(Row row) {
        return (Textbox) row.getChildren().get(0);
    }

    /**
     * Locates {@link Textbox} Order in {@link Row}
     *
     * @param row
     * @return
     */
    private Textbox getTextboxOrder(Row row) {
        return (Textbox) row.getChildren().get(1);
    }

    @Override
    public void goToList() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
    }

    public void cancel() {
        if (workReportModel.isEditing()) {
            goToList();
        } else {
            workReportTypeCRUD.goToList();
        }
    }

    public void goToCreateForm(WorkReportType workReportType) {
        workReportModel.initCreate(workReportType);
        prepareWorkReportList();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    public void goToEditForm(WorkReport workReport) {
        workReportModel.initEdit(workReport);
        prepareWorkReportList();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
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
        // The only way to clean the listhead, is to clean all its attributes
        // and children
        // The paging component cannot be removed manually. It is removed automatically when changing the mold
        listWorkReportLines.setMold(null);
        listWorkReportLines.getChildren().clear();

        // Set mold and pagesize
        listWorkReportLines.setMold(MOLD);
        listWorkReportLines.setPageSize(PAGING);

        appendColumns(listWorkReportLines);
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
        Column columnResource = new Column(_("Resource"));
        columns.appendChild(columnResource);
        Column columnCode = new Column(_("Code"));
        columns.appendChild(columnCode);
        Column columnNumHours = new Column(_("Hours"));
        columns.appendChild(columnNumHours);

        // Add dynamic headers
        appendCriterionTypesToColumns(getCriterionTypes(), columns);

        Column columnOperations = new Column(_("Operations"));
        columns.appendChild(columnOperations);

        columns.setParent(grid);
    }

    /**
     * Appends a set of {@link CriterionType} to {@link Listhead}
     */
    private void appendCriterionTypesToColumns(
            Set<CriterionType> criterionTypes, Columns columns) {
        for (CriterionType criterionType : criterionTypes) {
            appendCriterionTypeToListHead(criterionType, columns);
        }
    }

    /**
     * Appends a {@link CriterionType} to {@link Listhead}
     */
    private void appendCriterionTypeToListHead(CriterionType criterionType,
            Columns columns) {
        Column column= new Column(StringUtils
                .capitalize(criterionType.getName().toLowerCase()));
        column.setParent(columns);
    }

    private Set<CriterionType> getCriterionTypes() {
        return getWorkReportType().getCriterionTypes();
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
        WorkReportLine workReportLine = workReportModel.addWorkReportLine();
        listWorkReportLines.getRows().appendChild(createWorkReportLine(workReportLine));
    }

    private void removeWorkReportLine(WorkReportLine workReportLine) {
        workReportModel.removeWorkReportLine(workReportLine);
        Util.reloadBindings(listWorkReportLines);
    }

    public List<WorkReportLine> getWorkReportLines() {
        return workReportModel.getWorkReportLines();
    }

    /**
     * Returns a new row bound to to a {@link WorkReportLine}
     *
     * A row consists of a several textboxes plus several listboxes, one
     * for every {@link CriterionType} associated with current @{link
     * WorkReport}
     *
     * @param workReportLine
     * @return
     */
    private Row createWorkReportLine(WorkReportLine workReportLine) {
        Row row = new Row();

        // Bind workReportLine to row
        row.setValue(workReportLine);

        appendAutocompleteResource(row);
        appendTextboxOrder(row);
        appendIntboxNumHours(row);

        for (CriterionType criterionType : getCriterionTypes()) {
            appendListboxCriterionType(criterionType, row);
        }

        appendDeleteButton(row);

        return row;
    }

    /**
     * Append a Autocomplete @{link Resource} to row
     *
     * @param row
     */
    private void appendAutocompleteResource(final Row row) {
        final Autocomplete autocomplete = new Autocomplete();
        autocomplete.setAutodrop(true);
        autocomplete.applyProperties();
        autocomplete.setFinder("WorkerFinder");

        // Getter, show worker selected
        if (getWorker(row) != null) {
            autocomplete.setSelectedItem(getWorker(row));
        }

        // Setter, set worker selected to WorkReportLine.resource
        autocomplete.addEventListener("onSelect", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                final Comboitem comboitem = autocomplete.getSelectedItem();
                if (comboitem == null) {
                    throw new WrongValueException(autocomplete,
                            _("Please, select an item"));
                }
                // Update worker
                WorkReportLine workReportLine = (WorkReportLine) row
                        .getValue();
                workReportLine.setResource((Worker) comboitem.getValue());
                row.setValue(workReportLine);
            }
        });
        row.appendChild(autocomplete);
    }

    private Worker getWorker(Row listitem) {
        WorkReportLine workReportLine = (WorkReportLine) listitem.getValue();
        return (Worker) workReportLine.getResource();
    }

    /**
     * Append a Textbox @{link Order} to row
     *
     * @param row
     */
    private void appendTextboxOrder(Row row) {
        Textbox txtOrder = new Textbox();
        bindTextboxOrder(txtOrder, (WorkReportLine) row.getValue());
        row.appendChild(txtOrder);
    }

    /**
     * Binds Textbox @{link Order} to a {@link WorkReportLine} {@link Order}
     *
     * @param txtOrder
     * @param workReportLine
     */
    private void bindTextboxOrder(final Textbox txtOrder,
            final WorkReportLine workReportLine) {
        Util.bind(txtOrder, new Util.Getter<String>() {

            @Override
            public String get() {
                if (workReportLine.getOrderElement() != null) {
                    try {
                        return workReportModel
                                .getDistinguishedCode(workReportLine
                                        .getOrderElement());
                    } catch (InstanceNotFoundException e) {
                    }
                }
                return "";
            }

        }, new Util.Setter<String>() {

            @Override
            public void set(String value) {
                if (value.length() > 0) {
                    try {
                        workReportLine.setOrderElement(workReportModel
                            .findOrderElement(value));
                    } catch (InstanceNotFoundException e) {
                        throw new WrongValueException(txtOrder,
                            _("OrderElement not found"));
                    }
                }
            }
        });
    }

    /**
     * Append a {@link Intbox} numHours to {@link Row}
     *
     * @param row
     */
    private void appendIntboxNumHours(Row row) {
        Intbox intNumHours = new Intbox();
        bindIntboxNumHours(intNumHours, (WorkReportLine) row.getValue());
        row.appendChild(intNumHours);
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
            public void onEvent(Event event) throws Exception {
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
            messagesForUser.showMessage(
                    Level.ERROR, e.getMessage());
            LOG.error(_("Error on showing removing element: ", workReportLine.getId()), e);
        }
    }

    private String getWorkReportLineName(WorkReportLine workReportLine) {
        final Worker resource = (Worker) workReportLine.getResource();
        final OrderElement orderElement = workReportLine.getOrderElement();

        if (resource == null || orderElement == null) {
            return ITEM;
        }
        return resource.getName() + " - " + orderElement.getCode();
    }

    /**
     * Binds Intbox numHours to a {@link WorkReportLine} numHours
     * @param intNumHours
     * @param workReportLine
     */
    private void bindIntboxNumHours(final Intbox intNumHours,
            final WorkReportLine workReportLine) {
        Util.bind(intNumHours, new Util.Getter<Integer>() {

            @Override
            public Integer get() {
                return workReportLine.getNumHours();
            }

        }, new Util.Setter<Integer>() {

            @Override
            public void set(Integer value) {
                workReportLine.setNumHours(value);
            }
        });
    }

    /**
     * Appends a {@link CriterionType} listbox to row
     *
     * @param criterionType
     * @param row
     */
    private void appendListboxCriterionType(final CriterionType criterionType,
            Row row) {
        WorkReportLine workReportLine = (WorkReportLine) row.getValue();
        Listbox listBox = createListboxCriterionType(criterionType,
                getSelectedCriterion(workReportLine, criterionType));
        bindGridCriterionType(criterionType, listBox, workReportLine);
        row.appendChild(listBox);
    }

    /**
     * Determines which {@link Criterion} of @{link CriterionType} is selected
     * in a @{link WorkReportLine}
     *
     * Notice that in a list of {@link Criterion} belonging to a @{link
     * WorkReportLine}, only one {@link Criterion} for each
     * {@link CriterionType} is possible
     *
     * @param workReportLine
     * @param criterionType
     */
    private Criterion getSelectedCriterion(WorkReportLine workReportLine,
            CriterionType criterionType) {
        for (Criterion criterion : workReportLine.getCriterions()) {
            if (criterionType.equals(criterion.getType())) {
                return criterion;
            }
        }

        return null;
    }

    /**
     * Create a listbox of {@link Criterion} for a {@link CriterionType}
     *
     * @param criterionType
     * @param workReportLine
     *            needed to determine which {@link Criterion} should be set to
     *            selected
     * @return
     */
    private Listbox createListboxCriterionType(CriterionType criterionType,
            Criterion selectedCriterion) {
        Listbox listBox = new Listbox();
        listBox.setRows(1);
        listBox.setMold("select");

        // Add empty option to list
        List<Criterion> criterions = new ArrayList<Criterion>(criterionType
                .getCriterions());
        criterions.add(0, Criterion.create(" ", criterionType));

        // Adds a new item to list for each criterion
        for (Criterion criterion : criterions) {
            Listitem listitem = new Listitem();
            listitem.setLabel(criterion.getName());
            listitem.setValue(criterion);
            listitem.setParent(listBox);

            if (criterion.equals(selectedCriterion)) {
                listBox.setSelectedItem(listitem);
            }
        }

        return listBox;
    }

    /**
     * Updates the list of {@link Criterion} of a {@link WorkReportLine} when a
     * new @{link Criterion} is selected
     *
     * @param criterionType
     *            needed to determine which {@link Criterion} inside the list
     *            should be updated
     * @param listBox
     * @param workReportLine
     */
    private void bindGridCriterionType(final CriterionType criterionType,
            final Listbox listBox, final WorkReportLine workReportLine) {
        listBox.addEventListener("onSelect", new EventListener() {

            @Override
            public void onEvent(Event arg0) throws Exception {
                Listitem listitem = listBox.getSelectedItem();

                // There only can be one criterion for each criterion type
                for (Criterion criterion : workReportLine.getCriterions()) {
                    if (criterionType.equals(criterion.getType())) {
                        workReportLine.removeCriterion(criterion);
                    }
                }
                workReportLine.addCriterion((Criterion) listitem.getValue());
            }
        });
    }

    public WorkReportListRenderer getRenderer() {
        return workReportListRenderer;
    }

    /**
     * RowRenderer for a @{WorkReportLine} element
     *
     * @author Diego Pino García <dpino@igalia.com>
     *
     */
    public class WorkReportListRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            WorkReportLine workReportLine = (WorkReportLine) data;

            workReportLine.setResource(workReportModel.asWorker(workReportLine
                    .getResource()));

            row.setValue(workReportLine);

            // Create textboxes
            appendAutocompleteResource(row);
            appendTextboxOrder(row);
            appendIntboxNumHours(row);

            // Get criterion types for each row and append to it
            // CriterionTypes
            for (CriterionType criterionType : getCriterionTypes()) {
                appendListboxCriterionType(criterionType, row);
            }

            appendDeleteButton(row);
        }
    }
}
