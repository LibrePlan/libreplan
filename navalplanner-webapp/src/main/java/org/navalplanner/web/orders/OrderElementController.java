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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.api.Listhead;

/**
 * Controller for {@link OrderElement} view of {@link Order} entities <br />
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class OrderElementController extends GenericForwardComposer {

    /**
     * {@link IOrderElementModel} with the data needed for this controller
     */
    private IOrderElementModel orderElementModel;

    /**
     * {@link Window} where {@link OrderElement} edition form is showed
     */
    private Window window;

    private AsignedHoursToOrderElementController asignedHoursController;

    private ManageOrderElementAdvancesController manageOrderElementAdvancesController;

    private AssignedLabelsToOrderElementController assignedLabelsController;

    private Component orderElementDetails;

    private DetailsOrderElementController detailsController;


    public IOrderElementModel getModel() {
        return orderElementModel;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        System.out.println("### orderElement.doAfterCompose: " + orderElementModel);
        comp.setVariable("orderElementController", this, true);
        window = (Window) comp;
        setupDetailsOrderElementController(comp);
        setupAsignedHoursToOrderElementController(comp);
        setupManageOrderElementAdvancesController(comp);
        setupAssignedLabelsToOrderElementController(comp);
    }

    private void setupDetailsOrderElementController(Component comp)throws Exception{
        detailsController = (DetailsOrderElementController)
            orderElementDetails.getVariable("detailsController", true);
        detailsController.setOrderElementModel(orderElementModel);
    }

    private void setupAsignedHoursToOrderElementController(Component comp)throws Exception{
        asignedHoursController = new AsignedHoursToOrderElementController();
        asignedHoursController.doAfterCompose(comp);
    }

    private void setupAssignedLabelsToOrderElementController(Component comp)
            throws Exception {
        assignedLabelsController = new AssignedLabelsToOrderElementController();
        assignedLabelsController.doAfterCompose(comp);
    }

    private void setupManageOrderElementAdvancesController(Component comp)
            throws Exception {
        manageOrderElementAdvancesController = new ManageOrderElementAdvancesController();
        manageOrderElementAdvancesController.doAfterCompose(comp);
    }

    private void clearAll() {
        detailsController.clear();
    }

    public OrderElement getOrderElement() {
        if (orderElementModel == null) {
            return OrderLine.create();
        }

        return orderElementModel.getOrderElement();
    }


    /**
     * Open the window to edit a {@link OrderElement}. If it's a
     * {@link OrderLineGroup} less fields will be enabled.
     * @param orderElement
     *            The {@link OrderElement} to be edited
     */
    public void initEdit(IOrderElementModel model){
        clearAll();

        this.orderElementModel = model;
        final OrderElement orderElement = model.getOrderElement();

        detailsController.openWindow(model);
        asignedHoursController.openWindow(model);
        manageOrderElementAdvancesController.openWindow(model);
        assignedLabelsController.openWindow(model);

        try {
            window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // reloadSelectedCriterionTypes();
    }

    /**
     * Just close the {@link Window} and refresh parent status. Save actions are
     * managed by "save-when" at .zul file.
     */
    public void back() {
//        if (!getOrderElement().checkAtLeastOneHoursGroup()) {
//            throw new WrongValueException(window
//                    .getFellow("hoursGroupsListbox"),
//                    _("At least one HoursGroup is needed"));
//        }
//
//        for (CriterionType criterionType : getCriterionTypes()) {
//            removeCriterionsFromHoursGroup(criterionType);
//        }
//
//        Clients.closeErrorBox(window.getFellow("hoursGroupsListbox"));
//        window.setVisible(false);
//        Util.reloadBindings(window.getParent());
    }

    public void onClose(Event event) {
        self.setVisible(false);
        event.stopPropagation();
    }

}
