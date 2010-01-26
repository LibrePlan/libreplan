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

package org.navalplanner.web.templates.historicalStatistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.web.templates.IOrderTemplatesModel;
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

    private IOrderTemplatesModel model;

    private IAdHocTransactionService adHocTransactionService;

    private IOrderElementDAO orderElementDAO;

    private List<OrderElement> orderElements = new ArrayList<OrderElement>();

    public String applications = "0";
    public String finishApplications = "0";
    public String averageEstimatedHours = "0";
    public String averageWorkedHours = "0";
    public String differentialEstimatedHours = "0";
    public String differentialWorkedHours = "0";

    @Transactional(readOnly = true)
    public void afterCompose() {
        super.afterCompose();
        this.adHocTransactionService = (IAdHocTransactionService) getBean("adHocTransactionService");
        this.orderElementDAO = (IOrderElementDAO) getBean("orderElementDAO");
    }

    public void useModel(IOrderTemplatesModel model) {
        template = model.getTemplate();
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
                            orderElements = getOrderElementsWithThisAssignedTemplate();
                            applications = getApplicationsNumber().toString();
                            finishApplications = getFinishedApplicationsNumber()
                                    .toString();
                            averageEstimatedHours = calculateAverageEstimatedHours()
                                    .toString();
                            averageWorkedHours = calculateAverageWorkedHours()
                                    .toString();
                            differentialEstimatedHours = calculateEstimatedDifferential()
                                    .toString();
                            differentialWorkedHours = calculateWorkedDifferential()
                                    .toString();
                            return null;
                        }
                    });
        }
    }

    public List<OrderElement> getOrderElementsWithThisAssignedTemplate() {
        orderElements.clear();
        if ((model != null) && (template != null) && (!template.isNewObject())) {
            orderElements.addAll(orderElementDAO.findByTemplate(template));
        }
        return orderElements;
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

    public String getDifferentialEstimatedHours() {
        return differentialEstimatedHours;
    }

    public String getDifferentialWorkedHours() {
        return differentialWorkedHours;
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
        BigDecimal sum = new BigDecimal(0);
        BigDecimal average = new BigDecimal(0);
        final List<OrderElement> list = getFinishedApplications();

        for (OrderElement orderElement : list) {
            sum = sum.add(new BigDecimal(orderElement.getWorkHours()));
        }
        if (sum.compareTo(new BigDecimal(0)) > 0) {
            average = sum.divide(new BigDecimal(list.size()));
        }
        return average;
    }

    public BigDecimal calculateAverageWorkedHours() {
        BigDecimal sum = new BigDecimal(0);
        BigDecimal average = new BigDecimal(0);
        final List<OrderElement> list = getFinishedApplications();

        for (OrderElement orderElement : list) {
            sum = sum.add(new BigDecimal(orderElementDAO
                    .getAssignedDirectHours(orderElement)));
        }
        if (sum.compareTo(new BigDecimal(0)) > 0) {
            average = sum.divide(new BigDecimal(list.size()));
        }
        return average;
    }

    public BigDecimal calculateEstimatedDifferential() {
        BigDecimal average = new BigDecimal(0);
        final List<OrderElement> list = getFinishedApplications();
        if (!list.isEmpty()) {
            BigDecimal initValue = new BigDecimal(list.get(0).getWorkHours());
            BigDecimal max = initValue.setScale(2);
            BigDecimal min = initValue.setScale(2);

            for (OrderElement orderElement : list) {
                BigDecimal value = new BigDecimal(orderElement.getWorkHours());
                if (max.compareTo(value) < 0) {
                    max = value.setScale(2);
                }
                if (min.compareTo(value) > 0) {
                    min = value.setScale(2);
                }
            }
            if (max.compareTo(new BigDecimal(0)) > 0) {
                average = max.divide(min).setScale(2);
            }
        }
        return average;
    }

    public BigDecimal calculateWorkedDifferential() {
        BigDecimal average = new BigDecimal(0);
        final List<OrderElement> list = getFinishedApplications();
        if (!list.isEmpty()) {
            BigDecimal initValue = new BigDecimal(orderElementDAO
                    .getAssignedDirectHours(list.get(0)));
            BigDecimal max = initValue.setScale(2);
            BigDecimal min = initValue.setScale(2);

            for (OrderElement orderElement : list) {
                BigDecimal value = new BigDecimal(orderElementDAO
                        .getAssignedDirectHours(orderElement));
                if (max.compareTo(value) < 0) {
                    max = value.setScale(2);
                }
                if (min.compareTo(value) > 0) {
                    min = value.setScale(2);
                }
            }
            if (max.compareTo(new BigDecimal(0)) > 0) {
                average = max.divide(min).setScale(2);
            }
        }
        return average;
    }

    private List<OrderElement> getFinishedApplications() {
        List<OrderElement> result = new ArrayList<OrderElement>();
        for (OrderElement orderElement : orderElements) {
            if (isFinishApplication(orderElement)) {
                result.add(orderElement);
            }
        }
        return result;
    }

    private boolean isFinishApplication(OrderElement orderElement) {
        // look up into the order elements tree
        TaskElement task = lookToUpAssignedTask(orderElement);
        if (task != null) {
            return isFinished(task.getOrderElement());
        }
        // look down into the order elements tree
        List<TaskElement> listTask = lookToDownAssignedTask(orderElement);
        if (!listTask.isEmpty()) {
            for (TaskElement taskElement : listTask) {
                if (!isFinished(taskElement.getOrderElement())) {
                    return false;
                }
            }
        }
        // not exist assigned task
        return isFinished(orderElementDAO
                .loadOrderAvoidingProxyFor(orderElement));
    }

    private TaskElement lookToUpAssignedTask(OrderElement current) {
        OrderElement result = current;
        while (current != null) {
            if (current.isSchedulingPoint()) {
                return current.getAssociatedTaskElement();
            }
            result = current;
            current = current.getParent();
        }
        return null;
    }

    private List<TaskElement> lookToDownAssignedTask(OrderElement current) {
        List<TaskElement> resultTask = new ArrayList<TaskElement>();
        for (OrderElement child : current.getAllChildren()) {
            if (child.isSchedulingPoint()) {
                TaskElement task = child.getAssociatedTaskElement();
                if (task != null) {
                    resultTask.add(task);
                }
            }
        }
        return resultTask;
    }

    private boolean isFinished(OrderElement orderElement) {
        BigDecimal measuredProgress = orderElement.getAdvancePercentage();
        measuredProgress = (measuredProgress.setScale(0, BigDecimal.ROUND_UP)
                .multiply(new BigDecimal(100)));
        return (measuredProgress.compareTo(new BigDecimal(100)) == 0);
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