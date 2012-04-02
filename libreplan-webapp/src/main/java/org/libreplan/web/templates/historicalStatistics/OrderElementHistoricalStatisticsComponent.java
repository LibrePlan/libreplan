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

package org.libreplan.web.templates.historicalStatistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.web.templates.ITemplatesModel;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class OrderElementHistoricalStatisticsComponent extends
        HtmlMacroComponent {

    private OrderElementTemplate template;

    private ITemplatesModel model;

    private IAdHocTransactionService adHocTransactionService;

    private IOrderElementDAO orderElementDAO;

    private List<OrderElement> orderElements = new ArrayList<OrderElement>();

    private List<OrderElement> finishedOrderElements = new ArrayList<OrderElement>();

    public String applications = "0";
    public String finishApplications = "0";
    public String averageEstimatedHours = "0";
    public String averageWorkedHours = "0";
    public String maxEstimatedHours = "0";
    public String maxWorkedHours = "0";
    public String minEstimatedHours = "0";
    public String minWorkedHours = "0";

    @Transactional(readOnly = true)
    public void afterCompose() {
        super.afterCompose();
        this.adHocTransactionService = (IAdHocTransactionService) getBean("adHocTransactionService");
        this.orderElementDAO = (IOrderElementDAO) getBean("orderElementDAO");
    }

    public void useModel(ITemplatesModel model) {
        template = model.getTemplate();
        orderElements = model.getOrderElementsOnConversation()
                .getOrderElements();
        this.model = model;
        calculateTemplateHistoricalStatistics();
    }

    @Transactional(readOnly = true)
    public void calculateTemplateHistoricalStatistics() {
        if ((model != null) && (template != null) && (!template.isNewObject())) {
            this.adHocTransactionService
                    .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                        @Override
                        public Void execute() {
                            model.getOrderElementsOnConversation().reattach();
                            finishedOrderElements = getFinishedApplications();

                            applications = getApplicationsNumber().toString();
                            finishApplications = getFinishedApplicationsNumber()
                                    .toString();
                            averageEstimatedHours = calculateAverageEstimatedHours()
                                    .setScale(2).toString();
                            averageWorkedHours = calculateAverageWorkedHours()
                                    .toHoursAsDecimalWithScale(2).toString();
                            maxEstimatedHours = calculateMaxEstimatedHours()
                                    .setScale(2).toString();
                            maxWorkedHours = calculateMaxWorkedHours()
                                    .toHoursAsDecimalWithScale(2)
                                    .setScale(2).toString();
                            minEstimatedHours = calculateMinEstimatedHours()
                                    .setScale(2).toString();
                            minWorkedHours = calculateMinWorkedHours()
                                    .toHoursAsDecimalWithScale(2).toString();
                            return null;
                        }
                    });
        }
    }

    public String getApplications() {
        return applications;
    }

    public String getFinishApplications() {
        return finishApplications;
    }

    public String getAverageEstimatedHours() {
        return averageEstimatedHours;
    }

    public String getAverageWorkedHours() {
        return averageWorkedHours;
    }

    public String getMaxEstimatedHours() {
        return maxEstimatedHours;
    }

    public String getMaxWorkedHours() {
        return maxWorkedHours;
    }

    public String getMinEstimatedHours() {
        return minEstimatedHours;
    }

    public String getMinWorkedHours() {
        return minWorkedHours;
    }

    /**
     * Operations to calculate the historical statistics of the current template
     */

    public Integer getApplicationsNumber() {
        return orderElements.size();
    }

    public Integer getFinishedApplicationsNumber() {
        return getFinishedApplications().size();
    }

    public BigDecimal calculateAverageEstimatedHours() {
        return orderElementDAO.calculateAverageEstimatedHours(orderElements);
    }

    public EffortDuration calculateAverageWorkedHours() {
        final List<OrderElement> list = getFinishedApplications();
        return orderElementDAO.calculateAverageWorkedHours(list);
    }

    public BigDecimal calculateMaxEstimatedHours() {
        return orderElementDAO.calculateMaxEstimatedHours(orderElements);
    }

    public BigDecimal calculateMinEstimatedHours() {
        return orderElementDAO.calculateMinEstimatedHours(orderElements);
    }

    public EffortDuration calculateMaxWorkedHours() {
        final List<OrderElement> list = getFinishedApplications();
        return orderElementDAO.calculateMaxWorkedHours(list);
    }

    public EffortDuration calculateMinWorkedHours() {
        final List<OrderElement> list = getFinishedApplications();
        return orderElementDAO.calculateMinWorkedHours(list);
    }

    private List<OrderElement> getFinishedApplications() {
        List<OrderElement> result = new ArrayList<OrderElement>();
        for (OrderElement orderElement : orderElements) {
            if (orderElement.isFinishedAdvance()) {
                result.add(orderElement);
            }
        }
        return result;
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