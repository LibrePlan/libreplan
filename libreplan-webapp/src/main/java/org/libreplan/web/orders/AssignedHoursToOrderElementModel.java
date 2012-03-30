/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.entities.MoneyCostCalculator;
import org.libreplan.business.reports.dtos.WorkReportLineDTO;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to show the asigned hours of a selected order element
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Ignacio Díaz Teijido <ignacio.diaz@comtecsf.es>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedHoursToOrderElementModel implements
        IAssignedHoursToOrderElementModel {

    @Autowired
    private final IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private MoneyCostCalculator moneyCostCalculator;

    private EffortDuration assignedDirectEffort;

    private OrderElement orderElement;

    private List<WorkReportLineDTO> listWRL;

    @Autowired
    public AssignedHoursToOrderElementModel(IWorkReportLineDAO workReportLineDAO) {
        Validate.notNull(workReportLineDAO);
        this.workReportLineDAO = workReportLineDAO;
        this.assignedDirectEffort = EffortDuration.zero();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReportLineDTO> getWorkReportLines() {
        if (orderElement == null) {
            return new ArrayList<WorkReportLineDTO>();
        }
        orderElementDAO.reattach(orderElement);
        this.assignedDirectEffort = EffortDuration.zero();
        this.listWRL = workReportLineDAO
                .findByOrderElementGroupByResourceAndHourTypeAndDate(orderElement);

        this.listWRL = groupByDate(listWRL);
        Iterator<WorkReportLineDTO> iterador = listWRL.iterator();
        while (iterador.hasNext()) {
            WorkReportLineDTO w = iterador.next();
            w.getResource().getShortDescription();
            w.getTypeOfWorkHours().getName();
            this.assignedDirectEffort = this.assignedDirectEffort.plus(w
                    .getSumEffort());
        }
        return sortByDate(listWRL);
    }

    private List<WorkReportLineDTO> sortByDate(List<WorkReportLineDTO> listWRL) {
        Collections.sort(listWRL, new Comparator<WorkReportLineDTO>() {
            public int compare(WorkReportLineDTO arg0, WorkReportLineDTO arg1) {
            if (arg0.getDate() == null) {
                return -1;
            }
            if (arg1.getDate() == null) {
                return 1;
            }
                return arg0.getDate().compareTo(arg1.getDate());
            }
        });
        return listWRL;
    }

    private List<WorkReportLineDTO> groupByDate(
            List<WorkReportLineDTO> listWRL) {
        List<WorkReportLineDTO> groupedByDateList = new ArrayList<WorkReportLineDTO>();

        if (!listWRL.isEmpty()) {
            Iterator<WorkReportLineDTO> iterador = listWRL.iterator();
            WorkReportLineDTO currentWRL = iterador.next();
            groupedByDateList.add(currentWRL);

            while (iterador.hasNext()) {
                WorkReportLineDTO nextWRL = iterador.next();

                LocalDate currentDate = currentWRL.getLocalDate();
                LocalDate nextDate = nextWRL.getLocalDate();

                if ((currentWRL.getResource().getId().equals(nextWRL
                        .getResource().getId()))
                        && (currentWRL.getTypeOfWorkHours().getId()
                                .equals(nextWRL.getTypeOfWorkHours().getId()))
                        && (currentDate.compareTo(nextDate) == 0)) {
                    // sum the number of hours to the next WorkReportLineDTO
                    currentWRL.setSumEffort(currentWRL.getSumEffort().plus(
                            nextWRL.getSumEffort()));
                } else {
                    groupedByDateList.add(nextWRL);
                    currentWRL = nextWRL;
                }
            }
        }
        return groupedByDateList;
    }

    @Override
    public EffortDuration getAssignedDirectEffort() {
        if (orderElement == null) {
            return EffortDuration.zero();
        }
        return this.assignedDirectEffort;
    }

    @Override
    public EffortDuration getTotalAssignedEffort() {
        if (orderElement == null || orderElement.getSumChargedEffort() == null) {
            return EffortDuration.zero();
        }
        return this.orderElement.getSumChargedEffort().getTotalChargedEffort();
    }

    @Override
    @Transactional(readOnly = true)
    public EffortDuration getAssignedDirectEffortChildren() {
        if (orderElement == null) {
            return EffortDuration.zero();
        }
        if (orderElement.getChildren().isEmpty()) {
            return EffortDuration.zero();
        }
        EffortDuration assignedDirectChildren = getTotalAssignedEffort().minus(
                this.assignedDirectEffort);
        return assignedDirectChildren;
    }

    @Override
    @Transactional(readOnly = true)
    public void initOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @Override
    @Transactional(readOnly = true)
    public EffortDuration getEstimatedEffort() {
        if (orderElement == null) {
            return EffortDuration.zero();
        }
        //TODO this must be changed when changing HoursGroup
        return EffortDuration.hours(orderElement.getWorkHours());
    }

    @Override
    @Transactional(readOnly = true)
    public int getProgressWork() {
        if (orderElement == null) {
            return 0;
        }
        return orderElementDAO.getHoursAdvancePercentage(orderElement)
                .multiply(new BigDecimal(100)).intValue();
    }

    @Override
    public BigDecimal getBudget() {
        if (orderElement == null) {
            return BigDecimal.ZERO;
        }
        return orderElement.getBudget();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMoneyCost() {
        if (orderElement == null) {
            return BigDecimal.ZERO;
        }
        return moneyCostCalculator.getMoneyCost(orderElement);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMoneyCostPercentage() {
        if (orderElement == null) {
            return BigDecimal.ZERO;
        }
        return MoneyCostCalculator.getMoneyCostProportion(
                moneyCostCalculator.getMoneyCost(orderElement),
                orderElement.getBudget()).multiply(new BigDecimal(100));
    }

}
