/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.orders;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.orders.labels.AssignedLabelsToOrderElementController;
import org.navalplanner.web.orders.labels.LabelsAssignmentToOrderElementComponent;
import org.navalplanner.web.orders.materials.AssignedMaterialsToOrderElementController;
import org.navalplanner.web.orders.materials.OrderElementMaterialAssignmentsComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Window;

/**
 * Controller for {@link OrderElement} view of {@link Order} entities <br />
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class OrderElementController extends GenericForwardComposer {

    /**
     * {@link IOrderElementModel} with the data needed for this controller
     */
    private IOrderElementModel orderElementModel;

    private Component orderElementDetails;

    private DetailsOrderElementController detailsController;

    private Component orderElementHours;

    private AssignedHoursToOrderElementController assignedHoursToOrderElementController;

    private Component orderElementAdvances;

    private ManageOrderElementAdvancesController manageOrderElementAdvancesController;

    private LabelsAssignmentToOrderElementComponent orderElementLabels;

    private AssignedLabelsToOrderElementController assignedLabelsController;

    private Component orderElementTaskQualityForms;

    private AssignedTaskQualityFormsToOrderElementController assignedTaskQualityFormsController;

    private Component orderElementCriterionRequirements;

    private AssignedCriterionRequirementToOrderElementController assignedCriterionRequirementController;

    private AssignedMaterialsToOrderElementController assignedMaterialsController;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("orderElementController", this, true);
        setupDetailsOrderElementController();

        setupAssignedLabelsToOrderElementController(comp);
        setupAssignedCriterionRequirementToOrderElementController(comp);
        setupAssignedMaterialsToOrderElementController(comp);
        setupAssignedTaskQualityFormsToOrderElementController(comp);
    }

    private void setupDetailsOrderElementController() throws Exception {
        detailsController = (DetailsOrderElementController)
        orderElementDetails.getVariable("detailsController", true);
    }

    public void setupAssignedHoursToOrderElementController() throws Exception {
        if (assignedHoursToOrderElementController == null) {
            assignedHoursToOrderElementController = (AssignedHoursToOrderElementController) orderElementHours
                    .getVariable("assignedHoursToOrderElementController", true);
            assignedHoursToOrderElementController.openWindow(orderElementModel);
        } else {
            Util.createBindingsFor(orderElementHours);
            Util.reloadBindings(orderElementHours);
        }
    }

    public void setupManageOrderElementAdvancesController()
            throws Exception {
        if (manageOrderElementAdvancesController == null) {
            manageOrderElementAdvancesController = (ManageOrderElementAdvancesController) orderElementAdvances
                    .getVariable("manageOrderElementAdvancesController", true);
            manageOrderElementAdvancesController.openWindow(orderElementModel);
        } else {
            manageOrderElementAdvancesController
                    .refreshChangesFromOrderElement();
            manageOrderElementAdvancesController.createAndLoadBindings();
        }
    }

    private void setupAssignedLabelsToOrderElementController(Component comp)
            throws Exception {
        assignedLabelsController = orderElementLabels.getController();
    }

    private void setupAssignedCriterionRequirementToOrderElementController(
            Component comp) throws Exception {
        assignedCriterionRequirementController = (AssignedCriterionRequirementToOrderElementController) orderElementCriterionRequirements
                .getVariable("assignedCriterionRequirementController", true);
    }

    private void setupAssignedMaterialsToOrderElementController(Component comp)
            throws Exception {
        OrderElementMaterialAssignmentsComponent assignedMaterialsComponent = (OrderElementMaterialAssignmentsComponent) comp
                .getFellowIfAny("orderElementMaterials");
        assignedMaterialsController = assignedMaterialsComponent
                .getController();
    }

    private void setupAssignedTaskQualityFormsToOrderElementController(
            Component comp) throws Exception {
        assignedTaskQualityFormsController = (AssignedTaskQualityFormsToOrderElementController) orderElementTaskQualityForms
                .getVariable("assignedTaskQualityFormsController", true);
    }

    public OrderElement getOrderElement() {
        return (orderElementModel == null) ? OrderLine.create() : orderElementModel.getOrderElement();
    }

    /**
     * Open the window to edit a {@link OrderElement}. If it's a
     * {@link OrderLineGroup} less fields will be enabled.
     * @param orderElement
     *            The {@link OrderElement} to be edited
     */
    public void openWindow(IOrderElementModel model){
        clearAll();
        setOrderElementModel(model);
        detailsController.openWindow(model);

        // initialize the controllers
        manageOrderElementAdvancesController = null;
        assignedHoursToOrderElementController = null;

        assignedLabelsController.openWindow(model);
        assignedCriterionRequirementController.openWindow(model);
        assignedMaterialsController.openWindow(model.getOrderElement());
        assignedTaskQualityFormsController.openWindow(model);

        try {
            ((Window) self).doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setOrderElementModel(IOrderElementModel orderElementModel) {
        this.orderElementModel = orderElementModel;
    }

    private void clearAll() {
        clear();
        assignedLabelsController.clear();
    }

    private void clear() {
        selectDefaultTab();
    }

    private void selectDefaultTab() {
        selectTab("tabDetails");
    }

    private void selectTab(String str) {
        Tab tab = (Tab) self.getFellowIfAny(str);
        if (tab != null) {
            tab.setSelected(true);
        }
    }

    public void back() {
        closeAll();
    }

    private void closeAll() {
        if ((manageOrderElementAdvancesController != null)
                && (!manageOrderElementAdvancesController.close())) {
            selectTab("tabAdvances");
            return;
        }
        if (!assignedCriterionRequirementController.close()) {
            selectTab("tabRequirements");
            return;
        }
        selectTab("tabTaskQualityForm");
        if (!assignedTaskQualityFormsController.confirm()) {
            return;
        }
        close();
    }

    private void close() {
        self.setVisible(false);
        Util.reloadBindings(self.getParent());
    }

    public void onClose(Event event) {
        closeAll();
        event.stopPropagation();
    }

}
