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

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.IOrderLineGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.planner.order.IOrderPlanningGate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderCRUDController extends GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(OrderCRUDController.class);

    @Autowired
    private IOrderModel orderModel;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Component editWindow;

    private Component createWindow;

    private Component listWindow;

    private OnlyOneVisible cachedOnlyOneVisible;

    private Window confirmSchedule;

    private boolean confirmingSchedule;

    private IOrderPlanningGate planningControllerEntryPoints;

    public List<Order> getOrders() {
        return orderModel.getOrders();
    }

    private OnlyOneVisible getVisibility() {
        if (cachedOnlyOneVisible == null) {
            cachedOnlyOneVisible = new OnlyOneVisible(listWindow, editWindow,
                    createWindow);
        }
        return cachedOnlyOneVisible;
    }

    public IOrderLineGroup getOrder() {
        return orderModel.getOrder();
    }

    public void save() {
        try {
            orderModel.save();
            messagesForUser.showMessage(Level.INFO, _("Order saved"));
            goToList();
        } catch (ValidationException e) {
            if (e.getInvalidValues().length == 0) {
                messagesForUser.showMessage(Level.INFO, e.getMessage());
            } else {
                messagesForUser.showInvalidValues(e,
                        new IMessagesForUser.ICustomLabelCreator() {

                            @Override
                            public Component createLabelFor(
                                    InvalidValue invalidValue) {
                                if (invalidValue.getBean() instanceof OrderElement) {
                                    Label result = new Label();

                                    String orderElementName;
                                    if (invalidValue.getBean() instanceof Order) {
                                        orderElementName = _("Order");
                                    } else {
                                        orderElementName = ((OrderElement) invalidValue
                                                .getBean()).getName();
                                    }

                                    result.setValue(orderElementName + " "
                                            + invalidValue.getPropertyName()
                                            + ": " + invalidValue.getMessage());
                                    return result;
                                } else {
                                    return MessagesForUser
                                            .createLabelFor(invalidValue);
                                }
                    }
                });
            }
        }
    }

    public void goToList() {
        Util.reloadBindings(listWindow);
        getVisibility().showOnly(listWindow);
    }

    public void cancel() {
        goToList();
    }

    public void up() {
        if (onUp == null) {
            throw new IllegalStateException(
                    "in order to call up onUp action should have been set");
        }
        onUp.run();
    }

    public void confirmRemove(Order order) {
        try {
            int status = Messagebox.show(_("Confirm deleting {0}. Are you sure?", order.getName()), "Delete",
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                remove(order);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void remove(Order order) {
        orderModel.remove(order);
        Util.reloadBindings(self);
        messagesForUser.showMessage(Level.INFO, _("Removed {0}", order.getName()));
    }

    public void confirmSchedule(Order order) {
        if (!orderModel.isAlreadyScheduled(order)) {
            orderModel.prepareForSchedule(order);
            showScheduleConfirmingWindow();
        } else {
            goToShedulingView(order);
        }
    }

    private void goToShedulingView(Order order) {
        planningControllerEntryPoints.goToScheduleOf(order);
    }

    private void showScheduleConfirmingWindow() {
        confirmingSchedule = true;
        try {
            Util.reloadBindings(confirmSchedule);
            confirmSchedule.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void schedule() {
        try {
            orderModel.schedule();
            goToShedulingView((Order) orderModel.getOrder());
        } finally {
            hideScheduleConfirmingWindow();
        }
    }

    public void cancelSchedule() {
        hideScheduleConfirmingWindow();
    }

    private void hideScheduleConfirmingWindow() {
        confirmingSchedule = false;
        Util.reloadBindings(confirmSchedule);
    }

    private Runnable onUp;

    public void goToEditForm(Order order) {
        orderModel.prepareEditFor(order);
        clearOrderElementTreeController(editWindow);
        selectDefaultTab(editWindow);
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    private void selectDefaultTab(Component comp) {
        Tab tabGeneralData = (Tab) comp.getFellowIfAny("tabGeneralData");
        tabGeneralData.setSelected(true);
    }

    private void clearOrderElementTreeController(Component comp) {
        OrderElementTreeController controller = (OrderElementTreeController) comp
                .getVariable("orderElementTreeController", true);
        controller.clear();
    }

    public void goToCreateForm() {
        orderModel.prepareForCreate();
        clearOrderElementTreeController(createWindow);
        selectDefaultTab(createWindow);
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        getVisibility().showOnly(listWindow);

        OrderElementController orderElementController = new OrderElementController();
        orderElementController.doAfterCompose(comp
                .getFellow("editOrderElement"));

        setupOrderElementTreeController(comp, "editWindow",
                orderElementController);
        setupOrderElementTreeController(comp, "createWindow",
                orderElementController);
    }

    private void setupOrderElementTreeController(Component comp, String window,
            OrderElementController orderElementController) throws Exception {
        OrderElementTreeController controller = new OrderElementTreeController(
                orderModel, orderElementController);
        controller.doAfterCompose(comp.getFellow(window).getFellow(
                "orderElementTree"));
    }

    public boolean isConfirmingSchedule() {
        return confirmingSchedule;
    }

    public void setPlanningControllerEntryPoints(
            IOrderPlanningGate planningControllerEntryPoints) {
        this.planningControllerEntryPoints = planningControllerEntryPoints;
    }

    public void setActionOnUp(Runnable onUp) {
        this.onUp = onUp;
    }

}
