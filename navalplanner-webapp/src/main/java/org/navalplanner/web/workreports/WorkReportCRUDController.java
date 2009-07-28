package org.navalplanner.web.workreports;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link WorkReport}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
public class WorkReportCRUDController extends GenericForwardComposer implements
        IWorkReportCRUDControllerEntryPoints {

    private Window createWindow;

    private IWorkReportModel workReportModel;

    private IURLHandlerRegistry URLHandlerRegistry;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private IWorkReportTypeCRUDControllerEntryPoints workReportTypeCRUD;

    private WorkReportListRenderer workReportListRenderer = new WorkReportListRenderer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        final URLHandler<IWorkReportCRUDControllerEntryPoints> handler = URLHandlerRegistry
                .getRedirectorFor(IWorkReportCRUDControllerEntryPoints.class);
        handler.registerListener(this, page);
        // Shows a blank page until createWindow is completely rendered
        getVisibility().showOnly(null);
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(createWindow)
                : visibility;
    }

    public void save() {
        try {
            workReportModel.save();
            messagesForUser.showMessage(Level.ERROR,
                    "Parte de traballo gardado");
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    public void cancel() {
        System.out.println("### Cancel");
        workReportTypeCRUD.goToList();
    }

    public void goToCreateForm(WorkReportType workReportType) {
        workReportModel.prepareForCreate(workReportType);
        appendCriterionTypesToColumns(getCriterionTypes());
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
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
     * Appends a set of {@link CriterionType} to columns header
     */
    private void appendCriterionTypesToColumns(Set<CriterionType> criterionTypes) {
        Grid grid = (Grid) createWindow.getFellow("listWorkReportLines");
        for (CriterionType criterionType : criterionTypes) {
            appendCriterionTypeToColumns(criterionType, grid.getColumns());
        }
    }

    /**
     * Appends a new {@link CriterionType} to columns header
     */
    private void appendCriterionTypeToColumns(CriterionType criterionType,
            Columns columns) {
        Column column = new Column(criterionType.getName());
        column.setParent(columns);
    }

    /**
     * Adds a new {@link WorkReportLine} to the list of rows
     *
     * @param rows
     */
    public void addWorkReportLine(Rows rows) {
        WorkReportLine workReportLine = new WorkReportLine();
        getWorkReportLines().add(workReportLine);
        rows.appendChild(createRow(workReportLine));
    }

    public Set<WorkReportLine> getWorkReportLines() {
        return (getWorkReport() != null) ? getWorkReport().getWorkReportLines()
                : new HashSet<WorkReportLine>();
    }

    /**
     * Returns a new row bound to to a {@link WorkReportLine}
     *
     * A row consists of a several textboxes plus several listboxes, one for
     * every {@link CriterionType} associated with current @{link WorkReport}
     *
     * @param workReportLine
     * @return
     */
    private Row createRow(WorkReportLine workReportLine) {
        Row row = new Row();

        // Bind workReportLine to row
        row.setValue(workReportLine);

        appendTextboxResource(row);
        appendTextboxOrder(row);
        appendIntboxNumHours(row);

        for (CriterionType criterionType : getCriterionTypes()) {
            appendListboxCriterionType(criterionType, row);
        }

        return row;
    }

    /**
     * Append a Textbox @{link Resource} to row
     *
     * @param row
     */
    private void appendTextboxResource(Row row) {
        Textbox txtResource = new Textbox();
        bindTextboxResource(txtResource, (WorkReportLine) row.getValue());
        row.appendChild(txtResource);
    }

    /**
     * Binds Textbox @{link Resource} to a {@link WorkReportLine}
     * {@link Resource}
     *
     * @param txtResource
     * @param workReportLine
     */
    private void bindTextboxResource(final Textbox txtResource,
            final WorkReportLine workReportLine) {
        Util.bind(txtResource, new Util.Getter<String>() {

            @Override
            public String get() {
                return (workReportLine.getResource() != null) ? ((Worker) workReportLine
                        .getResource()).getNif()
                        : "";
            }

        }, new Util.Setter<String>() {

            @Override
            public void set(String value) {
                if (value.length() > 0) {
                    Worker worker;
                    try {
                        worker = workReportModel.findWorker(value);
                    } catch (InstanceNotFoundException e) {
                        throw new WrongValueException(txtResource,
                                "Worker not found");
                    }
                    workReportLine.setResource(worker);
                }
            }
        });
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
                return (workReportLine.getOrderElement() != null) ? workReportLine
                        .getOrderElement().getCode()
                        : "";
            }

        }, new Util.Setter<String>() {

            @Override
            public void set(String value) {
                if (value.length() > 0) {
                    OrderElement orderElement = workReportModel
                            .findOrderElement(value);
                    if (orderElement == null) {
                        throw new WrongValueException(txtOrder,
                                "OrderElement not found");
                    }
                    workReportLine.setOrderElement(orderElement);
                }
            }
        });
    }

    /**
     * Append a Intbox numHours to row
     *
     * @param row
     */
    private void appendIntboxNumHours(Row row) {
        Intbox intNumHours = new Intbox();
        bindIntboxNumHours(intNumHours, (WorkReportLine) row.getValue());
        row.appendChild(intNumHours);
    }

    /**
     * Binds Intbox numHours to a {@link WorkReportLine} numHours
     *
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
        bindListboxCriterionType(criterionType, listBox, workReportLine);
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
        criterions.add(0, new Criterion(" ", criterionType));

        // Adds a new item to list for each criterion
        for (Criterion criterion : criterions) {
            Listitem listItem = new Listitem();
            listItem.setLabel(criterion.getName());
            listItem.setValue(criterion);
            listItem.setParent(listBox);

            if (criterion.equals(selectedCriterion)) {
                listBox.setSelectedItem(listItem);
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
    private void bindListboxCriterionType(final CriterionType criterionType,
            final Listbox listBox, final WorkReportLine workReportLine) {
        listBox.addEventListener("onSelect", new EventListener() {

            @Override
            public void onEvent(Event arg0) throws Exception {
                Listitem listItem = listBox.getSelectedItem();

                // There only can be one criterion for each criterion type
                for (Criterion criterion : workReportLine.getCriterions()) {
                    if (criterionType.equals(criterion.getType())) {
                        workReportLine.getCriterions().remove(criterion);
                    }
                }
                workReportLine.getCriterions().add(
                        (Criterion) listItem.getValue());
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

            // Convert Resource to Worker
            if (workReportLine.getResource() instanceof Resource) {
                workReportLine.setResource(workReportModel
                        .asWorker(workReportLine.getResource()));
            }

            row.setValue(workReportLine);

            // Create textboxes
            appendTextboxResource(row);
            appendTextboxOrder(row);
            appendIntboxNumHours(row);

            // Get criterion types for each row and append to it
            // CriterionTypes
            for (CriterionType criterionType : getCriterionTypes()) {
                appendListboxCriterionType(criterionType, row);
            }
        }
    }
}
