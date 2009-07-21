package org.navalplanner.web.orders;

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
import org.navalplanner.web.planner.IOrderPlanningControllerEntryPoints;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class OrderCRUDController extends GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(OrderCRUDController.class);

    private IOrderModel orderModel;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Component editWindow;

    private Component createWindow;

    private Component listWindow;

    private OnlyOneVisible cachedOnlyOneVisible;

    private Window confirmRemove;

    private Window confirmSchedule;

    private boolean confirmingSchedule;

    private IOrderPlanningControllerEntryPoints planningControllerEntryPoints;

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
            messagesForUser.showMessage(Level.INFO, "order saved");
            goToList();
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e, new IMessagesForUser.ICustomLabelCreator() {

                @Override
                public Component createLabelFor(InvalidValue invalidValue) {
                    Label result= new Label();
                    String orderElementName = ((OrderElement)invalidValue.getBean()).getName();
                    result.setValue(orderElementName+" "+invalidValue.getPropertyName() + ": "
                    + invalidValue.getMessage());
                    return result;
                }
            });
        }
    }

    public void goToList() {
        Util.reloadBindings(listWindow);
        getVisibility().showOnly(listWindow);
    }

    public void cancel() {
        goToList();
    }

    public void confirmRemove(Order order) {
        orderModel.prepareForRemove(order);
        showConfirmingWindow();
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
        planningControllerEntryPoints.showSchedule(order);
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

    public void cancelRemove() {
        confirmingRemove = false;
        confirmRemove.setVisible(false);
        Util.reloadBindings(confirmRemove);
    }

    private boolean confirmingRemove = false;

    public boolean isConfirmingRemove() {
        return confirmingRemove;
    }

    private void hideConfirmingWindow() {
        confirmingRemove = false;
        Util.reloadBindings(confirmRemove);
    }

    private void showConfirmingWindow() {
        confirmingRemove = true;
        try {
            Util.reloadBindings(confirmRemove);
            confirmRemove.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void goToEditForm(Order order) {
        orderModel.prepareEditFor(order);
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void remove(Order order) {
        orderModel.remove(order);
        hideConfirmingWindow();
        Util.reloadBindings(listWindow);
        messagesForUser.showMessage(Level.INFO, "removed " + order.getName());
    }

    public void goToCreateForm() {
        orderModel.prepareForCreate();
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

}
