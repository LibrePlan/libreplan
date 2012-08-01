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

package org.libreplan.web.templates.historicalAssignment;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.web.planner.tabs.IGlobalViewEntryPoints;
import org.libreplan.web.security.SecurityUtils;
import org.libreplan.web.templates.ITemplatesModel;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.Messagebox;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 */
@SuppressWarnings("serial")
public class OrderElementHistoricalAssignmentComponent extends HtmlMacroComponent {

    private OrderElementTemplate template;

    private ITemplatesModel model;

    private List<OrderElement> orderElements;

    private IAdHocTransactionService adHocTransactionService;

    private IOrderElementDAO orderElementDAO;

    private IGlobalViewEntryPoints globalView;

    private IOrderDAO orderDAO;

    @Transactional(readOnly = true)
    public void afterCompose() {
        super.afterCompose();
        this.adHocTransactionService = (IAdHocTransactionService) getBean("adHocTransactionService");
        this.orderElementDAO = (IOrderElementDAO) getBean("orderElementDAO");
        this.orderDAO = (IOrderDAO) getBean("orderDAO");
    }

    public void useModel(ITemplatesModel model,
            IGlobalViewEntryPoints globalView) {
        template = model.getTemplate();
        this.model = model;
        this.orderElements = model.getOrderElementsOnConversation()
                .getOrderElements();
        this.globalView = globalView;
    }

    public List<OrderElementHistoricAssignmentDTO> getOrderElementsWithThisAssignedTemplate() {
        if ((model == null) || (template == null) || template.isNewObject()) {
            return Collections.emptyList();
        } else {
            return this.adHocTransactionService.runOnReadOnlyTransaction(new IOnTransaction<List<OrderElementHistoricAssignmentDTO>>() {
                @Override
                public List<OrderElementHistoricAssignmentDTO> execute() {
                            model.getOrderElementsOnConversation().reattach();
                            return createOrderElementHistoricAssignmentDTOs(orderElements);
                        }
                    });
       }
    }

    private List<OrderElementHistoricAssignmentDTO> createOrderElementHistoricAssignmentDTOs(
            List<OrderElement> orderElements) {
        List<OrderElementHistoricAssignmentDTO> dtos = new ArrayList<OrderElementHistoricAssignmentDTO>();
        for (OrderElement orderElement : orderElements) {
            dtos.add(new OrderElementHistoricAssignmentDTO(orderElement,
                    getOrder(orderElement),
                    getEstimatedHours(orderElement),
                    getWorkedHours(orderElement)));
        }
        return dtos;
    }

    private Order getOrder(OrderElement orderElement) {
        Order order = orderDAO.loadOrderAvoidingProxyFor(orderElement);
        return order;
    }

    public String getEstimatedHours(OrderElement orderElement) {
        return (orderElement.getWorkHours()).toString();
    }

    private String getWorkedHours(OrderElement orderElement){
        EffortDuration asignedDirectHours = orderElementDAO
                .getAssignedDirectEffort(orderElement);
        return asignedDirectHours.toFormattedString();
    }

    public void view(final OrderElementHistoricAssignmentDTO dto) {
        OrderElement orderElement = dto.getOrderElement();
        Order order = dto.getOrder();
        try {
            if (model.getCurrentScenario().contains(order)) {
                if (SecurityUtils.isSuperuserOrUserInRoles(
                        UserRole.ROLE_PLANNING,
                        UserRole.ROLE_READ_ALL_PROJECTS,
                        UserRole.ROLE_EDIT_ALL_PROJECTS)
                        || curerntUserHasAnyPermissionOverOrder(order)) {
                    globalView.goToOrderElementDetails(order, orderElement);
                } else {
                    Messagebox
                            .show(_("Not enough permissions to edit this project"),
                                    _("Warning"), Messagebox.OK,
                                    Messagebox.EXCLAMATION);
                }
            } else {
                String scenarios = "";
                for (Scenario scene : getScenarios(order)) {
                    scenarios = scenarios.concat(scene.getName() + "\n");
                }
                Messagebox
                        .show(_("The planning of this task is not in the current scenenario.\nYou should change to any of the following scenarios: {0}",
                                scenarios), _("Information"), Messagebox.OK,
                                Messagebox.INFORMATION);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean curerntUserHasAnyPermissionOverOrder(final Order order) {
        return this.adHocTransactionService
                .runOnReadOnlyTransaction(new IOnTransaction<Boolean>() {
                    @Override
                    public Boolean execute() {
                        try {
                            User user = Registry.getUserDAO()
                                    .findByLoginName(
                                            SecurityUtils.getLoggedUser()
                                                    .getUsername());
                            return !Registry.getOrderAuthorizationDAO()
                                    .listByOrderUserAndItsProfiles(order, user)
                                    .isEmpty();
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private Set<Scenario> getScenarios(final Order order) {
        return this.adHocTransactionService
                .runOnReadOnlyTransaction(new IOnTransaction<Set<Scenario>>() {
            @Override
            public Set<Scenario> execute() {
                        orderDAO.reattachUnmodifiedEntity(order);
                        order.getScenarios().keySet().size();
                        return order.getScenarios().keySet();
                    }
                });
    }

    private Object getBean(String classname) {
        HttpServletRequest servletRequest = (HttpServletRequest) Executions
                .getCurrent().getNativeRequest();
        ServletContext servletContext = servletRequest.getSession()
                .getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getWebApplicationContext(servletContext);
        return webApplicationContext.getBean(classname);
    }

}
