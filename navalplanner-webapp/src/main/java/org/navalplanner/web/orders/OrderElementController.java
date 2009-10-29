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

package org.navalplanner.web.orders;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.web.common.Util;
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

    private ManageOrderElementAdvancesController manageOrderElementAdvancesController;

    private Component orderElementDetails;

    private DetailsOrderElementController detailsController;

    private Component orderElementHours;

    private AsignedHoursToOrderElementController assignedHoursController;

    private Component orderElementLabels;

    private AssignedLabelsToOrderElementController assignedLabelsController;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("orderElementController", this, true);
        setupDetailsOrderElementController(comp);
        setupAsignedHoursToOrderElementController(comp);
        setupManageOrderElementAdvancesController(comp);
        setupAssignedLabelsToOrderElementController(comp);
    }

    private void setupDetailsOrderElementController(Component comp)throws Exception{
        detailsController = (DetailsOrderElementController)
            orderElementDetails.getVariable("detailsController", true);
    }

    private void setupAsignedHoursToOrderElementController(Component comp)throws Exception{
        assignedHoursController = (AsignedHoursToOrderElementController)
            orderElementHours.getVariable("asignedHoursToOrderElementController", true);
    }

    private void setupAssignedLabelsToOrderElementController(Component comp)
            throws Exception {
        assignedLabelsController = (AssignedLabelsToOrderElementController)
            orderElementLabels.getVariable("assignedLabelsController", true);
    }

    private void setupManageOrderElementAdvancesController(Component comp)
            throws Exception {
        manageOrderElementAdvancesController = new ManageOrderElementAdvancesController();
        manageOrderElementAdvancesController.doAfterCompose(comp);
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
        assignedHoursController.openWindow(model);
        manageOrderElementAdvancesController.openWindow(model);
        assignedLabelsController.openWindow(model);

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
        detailsController.clear();
        assignedLabelsController.clear();
    }

    private void clear() {
        selectTab();
    }

    Tab tabDetails;

    private void selectTab() {
        if (tabDetails != null) {
            tabDetails.setSelected(true);
        }
    }

    public void back() {
        closeAll();
    }

    private void closeAll() {
        detailsController.close();
        manageOrderElementAdvancesController.close();
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
