/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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
package org.libreplan.web.resources.machine;

import static org.libreplan.web.I18nHelper._t;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionWithItsType;
import org.libreplan.business.resources.entities.MachineWorkerAssignment;
import org.libreplan.business.resources.entities.MachineWorkersConfigurationUnit;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.Autocomplete;
import org.libreplan.web.resources.worker.CriterionsMachineController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;


/**
 * @author Lorenzo Tilve <ltilve@igalia.com>
 */
public class MachineConfigurationController extends GenericForwardComposer {

    private IMachineModel machineModel;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private Vbox configurationUnitsContainer;

    public MachineConfigurationController() {
        machineModel = (IMachineModel) SpringUtil.getBean("machineModel");
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("configurationController", this, true);
        messages = new MessagesForUser(messagesContainer);
    }

    public void addConfigurationUnit() {

        MachineWorkersConfigurationUnit unit =
                MachineWorkersConfigurationUnit.create(machineModel.getMachine(), "New configuration unit", new BigDecimal(1));

        machineModel.getMachine().addMachineWorkersConfigurationUnit(unit);
        renderConfigurationUnits();
    }

    public void reload() {
        renderConfigurationUnits();
    }

    public IMachineModel getMachineModel() {
        return machineModel;
    }

    public void setMachineModel(IMachineModel machineModel) {
        this.machineModel = machineModel;
    }

    void initConfigurationController(IMachineModel machineModel) {
        this.machineModel = machineModel;
        renderConfigurationUnits();
    }

    public List<MachineWorkersConfigurationUnit> getConfigurationUnits() {
        return this.machineModel.getConfigurationUnitsOfMachine();
    }

    public ListModelList<Worker> getAllWorkers() {
        ListModelList <Worker> modelList = new ListModelList<>();
        modelList.addAll(machineModel.getWorkers());
        return modelList;
    }

    /**
     * The criterion picker shares its data with the sibling "Assigned criteria" tab, whose
     * controller (CriterionsMachineController) attaches itself as a component attribute on
     * "criterionsContainer" - a sibling subtree, not an ancestor of this one, so it has to be
     * looked up by fellow-id rather than through the normal attribute-inheritance/ancestor walk.
     * Looked up lazily (not cached) since this controller's doAfterCompose runs before
     * MachineCRUDController wires up CriterionsMachineController.
     */
    private List<CriterionWithItsType> getCriterionWorkersWithItsTypes() {
        Component criterionsContainer = self.getFellowIfAny("criterionsContainer");
        if ( criterionsContainer == null ) {
            return Collections.emptyList();
        }

        CriterionsMachineController assignedCriterionsController =
                (CriterionsMachineController) criterionsContainer.getAttribute("assignedCriterionsController");

        return assignedCriterionsController != null
                ? assignedCriterionsController.getCriterionWorkersWithItsTypes()
                : Collections.emptyList();
    }

    /**
     * This whole subtree used to be a ZUML "each" template (self="@{each=...}") over a Grid whose
     * rows used ZK's <detailrow> for the two nested panels - under this app's AnnotateBinder/ZK 10
     * stack "each" only ever clones the row's FIRST child, so this is rebuilt as plain Java
     * component construction instead (same fix pattern as elsewhere in this sweep, e.g.
     * WorkerCRUDController.getWorkersRenderer()).
     *
     * <detailrow> itself turned out to be a SEPARATE, structural problem, not a binding-syntax one:
     * its client widget (zkex.grid.Detail) lives in ZK's "zkex" extended-components module, which
     * this project has never depended on (confirmed: no zkex jar anywhere in the Maven tree, no
     * zkex.grid.Detail widget bundled in the app's JS) - so <detailrow> could only ever throw
     * "Widget class required for <Detail...>" once actually rendered. Replaced the collapsible
     * detail-row with an always-visible card layout (Vbox per configuration unit) - functionally
     * equivalent, minus the collapse/expand toggle, and needs no missing dependency.
     */
    private void renderConfigurationUnits() {
        configurationUnitsContainer.getChildren().clear();
        for (MachineWorkersConfigurationUnit unit : getConfigurationUnits()) {
            configurationUnitsContainer.appendChild(buildConfigurationUnitCard(unit));
        }
    }

    private Component buildConfigurationUnitCard(MachineWorkersConfigurationUnit unit) {
        Vbox card = new Vbox();
        card.setWidth("100%");
        card.setStyle("border: 1px solid #cccccc; padding: 10px; margin-bottom: 10px;");

        Hbox header = new Hbox();
        header.setStyle("margin-bottom: 5px;");

        header.appendChild(new Label(_t("Name") + ":"));

        Textbox name = new Textbox(unit.getName());
        name.setWidth("400px");
        name.setConstraint("no empty:" + _t("cannot be empty"));
        name.addEventListener(Events.ON_CHANGE, event -> unit.setName(name.getValue()));
        header.appendChild(name);

        header.appendChild(new Label(_t("Alpha") + ":"));

        Textbox alpha = new Textbox(unit.getAlpha() == null ? "" : unit.getAlpha().toString());
        alpha.setWidth("100px");
        alpha.setConstraint(
                "no zero,no empty,/[0-9][0-9]*(\\.[0-9][0-9]?)?/:" + _t("must be a real positive number"));
        alpha.addEventListener(Events.ON_CHANGE, event -> unit.setAlpha(new BigDecimal(alpha.getValue())));
        header.appendChild(alpha);

        Button delete = new Button();
        delete.setSclass("icono");
        delete.setImage("/common/img/ico_borrar1.png");
        delete.setHoverImage("/common/img/ico_borrar.png");
        delete.setTooltiptext(_t("Delete"));
        delete.addEventListener(Events.ON_CLICK, event -> {
            machineModel.removeConfigurationUnit(unit);
            renderConfigurationUnits();
        });
        header.appendChild(delete);

        card.appendChild(header);
        card.appendChild(buildWorkerAssignmentsPanel(unit));
        card.appendChild(buildCriterionRequirementsPanel(unit));

        return card;
    }

    private Panel buildWorkerAssignmentsPanel(MachineWorkersConfigurationUnit unit) {
        Panel panel = new Panel();
        panel.setTitle(_t("Worker assignments"));
        panel.setBorder("normal");
        panel.setStyle("margin-top:10px;");

        Panelchildren panelchildren = new Panelchildren();
        panelchildren.setStyle("padding:10px;");
        panel.appendChild(panelchildren);

        // Model was added because when autocomplete value was changing, then all autocomplete's
        // values where changed too.
        final Autocomplete autocomplete = new Autocomplete();
        autocomplete.setFinder("WorkerFinder");
        autocomplete.setModel(getAllWorkers());
        autocomplete.setButtonVisible(true);
        autocomplete.setWidth("300px");
        autocomplete.setStyle("padding-bottom:10px;margin-top:10px;");
        panelchildren.appendChild(autocomplete);

        Button addButton = new Button();
        addButton.setLabel(_t("Add new worker assignment"));
        addButton.setSclass("add-button");
        panelchildren.appendChild(addButton);

        final Grid assignmentsGrid = new Grid();
        assignmentsGrid.setSpan(true);
        Columns columns = new Columns();
        columns.appendChild(new Column(_t("Name")));
        columns.appendChild(new Column(_t("Start date")));
        columns.appendChild(new Column(_t("End date")));
        columns.appendChild(new Column(_t("Operations")));
        assignmentsGrid.appendChild(columns);
        assignmentsGrid.setModel(new ListModelList<>(unit.getWorkerAssignments()));
        assignmentsGrid.setRowRenderer(getWorkerAssignmentRenderer(assignmentsGrid, unit));
        panelchildren.appendChild(assignmentsGrid);

        addButton.addEventListener(Events.ON_CLICK, event -> {
            Worker worker = (Worker) autocomplete.getItemByText(autocomplete.getValue());
            if (worker == null) {
                messages.showMessage(Level.ERROR, _t("No worker selected"));
            } else {
                machineModel.addWorkerAssignmentToConfigurationUnit(unit, worker);
                assignmentsGrid.setModel(new ListModelList<>(unit.getWorkerAssignments()));
            }
        });

        return panel;
    }

    private RowRenderer getWorkerAssignmentRenderer(Grid assignmentsGrid, MachineWorkersConfigurationUnit unit) {
        return (row, data, i) -> {
            final MachineWorkerAssignment assignment = (MachineWorkerAssignment) data;
            row.setValue(assignment);

            row.appendChild(new Label(assignment.getWorker().getName()));

            Datebox startDate = new Datebox();
            startDate.setValue(assignment.getStartDate());
            startDate.setConstraint("no empty");
            startDate.setStyle("margin-bottom:3px;");
            startDate.addEventListener(Events.ON_CHANGE, event -> assignment.setStartDate(startDate.getValue()));
            row.appendChild(startDate);

            Datebox finishDate = new Datebox();
            finishDate.setValue(assignment.getFinishDate());
            finishDate.setConstraint(validateEndDate());
            finishDate.setStyle("margin-bottom:3px;");
            finishDate.addEventListener(Events.ON_CHANGE, event -> assignment.setFinishDate(finishDate.getValue()));
            row.appendChild(finishDate);

            Button delete = new Button();
            delete.setSclass("icono");
            delete.setImage("/common/img/ico_borrar1.png");
            delete.setHoverImage("/common/img/ico_borrar.png");
            delete.setTooltiptext(_t("Delete"));
            delete.addEventListener(Events.ON_CLICK, event -> {
                unit.removeMachineWorkersConfigurationUnit(assignment);
                assignmentsGrid.setModel(new ListModelList<>(unit.getWorkerAssignments()));
            });
            row.appendChild(delete);
        };
    }

    private Panel buildCriterionRequirementsPanel(MachineWorkersConfigurationUnit unit) {
        Panel panel = new Panel();
        panel.setTitle(_t("Criterion requirements"));
        panel.setBorder("normal");
        panel.setStyle("margin-top:10px; margin-bottom:15px;");

        Panelchildren panelchildren = new Panelchildren();
        panelchildren.setStyle("padding:10px;");
        panel.appendChild(panelchildren);

        final Bandbox bandbox = new Bandbox();
        bandbox.setWidth("300px");

        final Listbox listbox = new Listbox();
        listbox.setWidth("500px");
        listbox.setHeight("150px");
        listbox.setModel(new ListModelList<>(getCriterionWorkersWithItsTypes()));
        listbox.setItemRenderer(getCriterionWithItsTypeRenderer());
        listbox.addEventListener(Events.ON_SELECT, event -> {
            Listitem selected = listbox.getSelectedItem();
            selectCriterionRequirement(selected, bandbox);
        });

        Listhead listhead = new Listhead();
        listhead.appendChild(new Listheader("Type"));
        listhead.appendChild(new Listheader("Criterion"));
        listbox.appendChild(listhead);

        Bandpopup bandpopup = new Bandpopup();
        bandpopup.appendChild(listbox);
        bandbox.appendChild(bandpopup);
        panelchildren.appendChild(bandbox);

        Button addButton = new Button();
        addButton.setLabel(_t("Add new criterion requirement"));
        addButton.setSclass("add-button");
        panelchildren.appendChild(addButton);

        final Grid requirementsGrid = new Grid();
        requirementsGrid.setStyle("margin-top:10px;");
        Columns columns = new Columns();
        columns.appendChild(new Column(_t("Name")));
        columns.appendChild(new Column(_t("Operations")));
        requirementsGrid.appendChild(columns);
        requirementsGrid.setModel(new ListModelList<>(unit.getRequiredCriterions()));
        requirementsGrid.setRowRenderer(getRequiredCriterionRenderer(requirementsGrid, unit));
        panelchildren.appendChild(requirementsGrid);

        addButton.addEventListener(Events.ON_CLICK, event -> {
            Listitem item = listbox.getSelectedItem();
            if (item != null) {
                CriterionWithItsType criterionAndType = item.getValue();
                bandbox.setValue(criterionAndType.getNameAndType());
                if (checkExistingCriterion(unit, criterionAndType.getCriterion())) {
                    messages.showMessage(Level.ERROR, _t("Criterion previously selected"));
                } else {
                    machineModel.addCriterionRequirementToConfigurationUnit(unit, criterionAndType.getCriterion());
                    bandbox.setValue("");
                    requirementsGrid.setModel(new ListModelList<>(unit.getRequiredCriterions()));
                }
            }
        });

        return panel;
    }

    private void selectCriterionRequirement(Listitem item, Bandbox bandbox) {
        if (item != null) {
            CriterionWithItsType criterionAndType = item.getValue();
            bandbox.setValue(criterionAndType.getNameAndType());
        } else {
            bandbox.setValue("");
        }
        bandbox.close();
    }

    private ListitemRenderer getCriterionWithItsTypeRenderer() {
        return (item, data, i) -> {
            final CriterionWithItsType criterionWithItsType = (CriterionWithItsType) data;
            item.setValue(criterionWithItsType);
            item.appendChild(new Listcell(criterionWithItsType.getType().getName()));
            item.appendChild(new Listcell(criterionWithItsType.getNameHierarchy()));
        };
    }

    private RowRenderer getRequiredCriterionRenderer(Grid requirementsGrid, MachineWorkersConfigurationUnit unit) {
        return (row, data, i) -> {
            final Criterion requirement = (Criterion) data;
            row.setValue(requirement);

            row.appendChild(new Label(requirement.getCompleteName()));

            Button delete = new Button();
            delete.setSclass("icono");
            delete.setImage("/common/img/ico_borrar1.png");
            delete.setHoverImage("/common/img/ico_borrar.png");
            delete.setTooltiptext(_t("Delete"));
            delete.addEventListener(Events.ON_CLICK, event -> {
                unit.removeRequiredCriterion(requirement);
                requirementsGrid.setModel(new ListModelList<>(unit.getRequiredCriterions()));
            });
            row.appendChild(delete);
        };
    }

    private boolean checkExistingCriterion(MachineWorkersConfigurationUnit unit, Criterion criterion) {
        boolean repeated = false;
        for (Criterion each : unit.getRequiredCriterions()) {
            if (each.getId().equals(criterion.getId())) {
                repeated = true;
            }
        }
        return repeated;
    }

    public Constraint validateEndDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value) throws WrongValueException {
                validateEndDate(comp, value);
            }
        };
    }

    private void validateEndDate(Component comp, Object value) {
        if (value == null) {
            throw new WrongValueException(comp, _t("End date is not valid, the date field can not be blank"));
        }
        else {
            Datebox startDateBox = (Datebox) comp.getPreviousSibling();
            if (startDateBox != null) {
                if (startDateBox.getValue() != null) {
                    if (startDateBox.getValue().compareTo((Date) value) > 0) {

                        throw new WrongValueException(
                                comp, _t("End date is not valid, the new end date must be after start date"));
                    }
                }
            }
        }
    }

}
