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

package org.libreplan.web.orders;

import static org.libreplan.web.I18nHelper._;

import java.util.HashMap;
import java.util.List;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for move {@link OrderElement} to another {@link Order}
 * 
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class MoveOrderElementToAnotherOrderController extends
        GenericForwardComposer {

    private IOrderElementModel orderElementModel;

    private Component moveOrderElement;

    private OrderCRUDController orderController;

    private BandboxSearch bdOrders;

    private OrderElementTreeController treeController;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        moveOrderElement = Executions.createComponents("/orders/_moveTask.zul",
                null, new HashMap<String, String>());
        moveOrderElement.setVariable("moveOrderElementController", this, true);

    }

    /**
     * Returns all orders
     */
    public List<Order> getAllOrders() {
        return orderElementModel.getOrderModel().getOrders();
    }

    /**
     * Sets the {@link OrderElementModel}
     * 
     * @param orderElementModel
     */
    private void setOrderElementModel(IOrderElementModel orderElementModel) {
        this.orderElementModel = orderElementModel;
    }

    /**
     * Returns the {@link OrderElement}
     */
    private OrderElement getOrderElement() {
        return orderElementModel.getOrderElement();
    }

    /**
     * Opens the MoveOrderElement Window, to select the task to be moved and the
     * destination order
     * 
     * @param model
     *            the {@link OrderElementModel}
     */
    public void openMoveOrderElementWindow(IOrderElementModel model) {
        setOrderElementModel(model);
        Textbox txtBox = (Textbox) moveOrderElement
                .getFellow("selectedOrderElementId");
        txtBox.setValue(getOrderElement().getName());
        try {
            ((Window) moveOrderElement).setTitle("Move Task");
            ((Window) moveOrderElement).doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hides the MoveOrderElement window
     */
    public void cancelMoveOrderElement() {
        ((Window) moveOrderElement).setVisible(false);
    }

    /**
     * Moves the selected {@link OrderElement} to the selected destination
     * {@link Order} and refresh the screen
     * 
     * The
     * {@link OrderElementModel#moveOrderElement(OrderElement, Order, org.zkoss.zk.ui.Desktop)}
     * method saves the destination order. This method then saves the source
     * order after its order element is moved to the destination order
     */
    public void moveOrderElement() {
        bdOrders = (BandboxSearch) moveOrderElement.getFellow("bdOrders");
        Order destinationOrder = (Order) bdOrders.getSelectedElement();
        if (getOrderElement() == null) {
            throw new ValidationException(_("Please select a task"));
        }
        if (destinationOrder == null) {
            throw new WrongValueException(bdOrders,
                    _("Pleas select destination order"));
        }

        Order sourceOrder = getOrderElement().getOrder();

        if (sourceOrder.getId().equals(destinationOrder.getId())) {
            throw new ValidationException(
                    _("Source and destination order are the same"));
        }

        orderElementModel.moveOrderElement(getOrderElement(), destinationOrder,
                moveOrderElement.getDesktop());


        orderController.getOrderModel().initEdit(sourceOrder,
                moveOrderElement.getDesktop());
        orderController.saveAndContinue(false);


        orderController.initEdit(sourceOrder);
        ((Window) moveOrderElement).setVisible(false);

    }

}
