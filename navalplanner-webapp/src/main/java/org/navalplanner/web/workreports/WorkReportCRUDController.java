package org.navalplanner.web.workreports;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.InvalidValue;
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
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
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

    private Window listWindow;

    private IWorkReportModel workReportModel;

    private IURLHandlerRegistry URLHandlerRegistry;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private IWorkReportTypeCRUDControllerEntryPoints workReportTypeCRUD;

    private WorkReportListRenderer workReportListRenderer = new WorkReportListRenderer();

    public final static String ID_WORK_REPORT_LINES = "workReportLines";

    public List<WorkReport> getWorkReports() {
        return workReportModel.getWorkReports();
    }

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
        return (visibility == null) ? new OnlyOneVisible(createWindow,
                listWindow)
                : visibility;
    }

    public void save() {
        try {
            workReportModel.save();
            messagesForUser.showMessage(Level.ERROR,
                    "Parte de traballo gardado");
        } catch (ValidationException e) {
            showInvalidValues(e);
        }
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
            throw new WrongValueException(datebox, "Data non pode ser nulo");
        }
        if (WorkReport.RESPONSIBLE.equals(propertyName)) {
            Textbox textbox = (Textbox) createWindow.getFellowIfAny(propertyName);
            throw new WrongValueException(textbox,
                    "Responsable non poder ser nulo");
        }
    }

    /**
     * Validates {@link WorkReportLine} data constraints
     *
     * @param invalidValue
     */
    private void validateWorkReportLine(InvalidValue invalidValue) {
        Listbox listBox = (Listbox) createWindow
                .getFellowIfAny(ID_WORK_REPORT_LINES);
        if (listBox != null) {
            // Find which listItem contains workReportLine inside listBox
            Listitem listItem = findWorkReportLine(listBox.getItems(),
                    (WorkReportLine) invalidValue.getBean());

            if (listItem != null) {
                String propertyName = invalidValue.getPropertyName();

                if (WorkReportLine.RESOURCE.equals(propertyName)) {
                    // Locate TextboxResource
                    Textbox txtResource = getTextboxResource(listItem);
                    // Value is incorrect, clear
                    txtResource.setValue("");
                    throw new WrongValueException(txtResource,
                            "Recurso non pode ser nulo");
                }
                if (WorkReportLine.ORDER_ELEMENT.equals(propertyName)) {
                    // Locate TextboxOrder
                    Textbox txtOrder = getTextboxOrder(listItem);
                    // Value is incorrect, clear
                    txtOrder.setValue("");
                    throw new WrongValueException(txtOrder,
                            "Código non pode ser nulo");
                }
            }
        }
    }

    /**
     * Locates which {@link Listitem} is bound to {@link WorkReportLine} in
     * listItems
     *
     * @param listItems
     * @param workReportLine
     * @return
     */
    private Listitem findWorkReportLine(List<Listitem> listItems,
            WorkReportLine workReportLine) {
        for (Listitem listItem : listItems) {
            if (workReportLine.equals(listItem.getValue())) {
                return listItem;
            }
        }
        return null;
    }

    /**
     * Locates {@link Textbox} Resource in {@link Listitem}
     *
     * @param listItem
     * @return
     */
    private Textbox getTextboxResource(Listitem listItem) {
        return (Textbox) ((Listcell) listItem.getChildren().get(0))
                .getChildren().get(0);
    }

    /**
     * Locates {@link Textbox} Order in {@link Listitem}
     *
     * @param listItem
     * @return
     */
    private Textbox getTextboxOrder(Listitem listItem) {
        return (Textbox) ((Listcell) listItem.getChildren().get(1))
                .getChildren().get(0);
    }

    @Override
    public void goToList() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
    }

    public void cancel() {
        if (workReportModel.isEditing()) {

        } else {
            workReportTypeCRUD.goToList();
        }
    }

    public void goToCreateForm(WorkReportType workReportType) {
        workReportModel.prepareForCreate(workReportType);
        appendCriterionTypesToHeader(getCriterionTypes());
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
    private void appendCriterionTypesToHeader(Set<CriterionType> criterionTypes) {
        Listbox grid = (Listbox) createWindow.getFellow(ID_WORK_REPORT_LINES);
        for (CriterionType criterionType : criterionTypes) {
            appendCriterionTypeToColumns(criterionType, grid.getListhead());
        }
    }

    /**
     * Appends a new {@link CriterionType} to columns header
     */
    private void appendCriterionTypeToColumns(CriterionType criterionType,
            Listhead columns) {
        Listheader listHeader = new Listheader(criterionType.getName());
        listHeader.setParent(columns);
    }

    /**
     * Adds a new {@link WorkReportLine} to the list of rows
     *
     * @param rows
     */
    public void addWorkReportLine(Listbox listBox) {
        WorkReportLine workReportLine = new WorkReportLine();
        getWorkReportLines().add(workReportLine);
        listBox.appendChild(createListItem(workReportLine));
    }

    /**
     * Delete @{link WorkReportLine} from listbox
     *
     * @param listBox
     */
    public void deleteWorkReportLine(Listbox listBox) {
        Listitem listItem = listBox.getSelectedItem();
        WorkReportLine workReportLine = (WorkReportLine) listItem.getValue();
        getWorkReportLines().remove(workReportLine);
        listBox.removeItemAt(listItem.getIndex());
    }

    public Set<WorkReportLine> getWorkReportLines() {
        return (getWorkReport() != null) ? getWorkReport().getWorkReportLines()
                : new HashSet<WorkReportLine>();
    }

    /**
     * Returns a new listItem bound to to a {@link WorkReportLine}
     *
     * A listItem consists of a several textboxes plus several listboxes, one
     * for every {@link CriterionType} associated with current @{link
     * WorkReport}
     *
     * @param workReportLine
     * @return
     */
    private Listitem createListItem(WorkReportLine workReportLine) {
        Listitem listItem = new Listitem();

        // Bind workReportLine to listItem
        listItem.setValue(workReportLine);

        appendTextboxResource(listItem);
        appendTextboxOrder(listItem);
        appendIntboxNumHours(listItem);

        for (CriterionType criterionType : getCriterionTypes()) {
            appendListboxCriterionType(criterionType, listItem);
        }

        return listItem;
    }

    /**
     * Append a Textbox @{link Resource} to listItem
     *
     * @param listItem
     */
    private void appendTextboxResource(Listitem listItem) {
        Textbox txtResource = new Textbox();
        bindTextboxResource(txtResource, (WorkReportLine) listItem.getValue());

        // Insert textbox in listcell and append to listItem
        Listcell listCell = new Listcell();
        listCell.appendChild(txtResource);
        listItem.appendChild(listCell);
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
     * Append a Textbox @{link Order} to listItem
     *
     * @param listItem
     */
    private void appendTextboxOrder(Listitem listItem) {
        Textbox txtOrder = new Textbox();
        bindTextboxOrder(txtOrder, (WorkReportLine) listItem.getValue());

        // Insert textbox in listcell and append to listItem
        Listcell listCell = new Listcell();
        listCell.appendChild(txtOrder);
        listItem.appendChild(listCell);
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
     * Append a Intbox numHours to listItem
     *
     * @param listItem
     */
    private void appendIntboxNumHours(Listitem listItem) {
        Intbox intNumHours = new Intbox();
        bindIntboxNumHours(intNumHours, (WorkReportLine) listItem.getValue());

        // Insert intbox in listcell and append to listItem
        Listcell listCell = new Listcell();
        listCell.appendChild(intNumHours);
        listItem.appendChild(listCell);
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
     * Appends a {@link CriterionType} listbox to listItem
     *
     * @param criterionType
     * @param listItem
     */
    private void appendListboxCriterionType(final CriterionType criterionType,
            Listitem listItem) {
        WorkReportLine workReportLine = (WorkReportLine) listItem.getValue();
        Listbox listBox = createListboxCriterionType(criterionType,
                getSelectedCriterion(workReportLine, criterionType));
        bindListboxCriterionType(criterionType, listBox, workReportLine);

        // Insert listbox in listcell and append to listItem
        Listcell listCell = new Listcell();
        listCell.appendChild(listBox);
        listItem.appendChild(listCell);
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
    public class WorkReportListRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem listItem, Object data) throws Exception {
            WorkReportLine workReportLine = (WorkReportLine) data;

            // Convert Resource to Worker
            if (workReportLine.getResource() instanceof Resource) {
                workReportLine.setResource(workReportModel
                        .asWorker(workReportLine.getResource()));
            }

            listItem.setValue(workReportLine);

            // Create textboxes
            appendTextboxResource(listItem);
            appendTextboxOrder(listItem);
            appendIntboxNumHours(listItem);

            // Get criterion types for each listItem and append to it
            // CriterionTypes
            for (CriterionType criterionType : getCriterionTypes()) {
                appendListboxCriterionType(criterionType, listItem);
            }
        }
    }
}
