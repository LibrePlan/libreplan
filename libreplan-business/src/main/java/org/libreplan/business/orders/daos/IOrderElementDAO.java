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

package org.libreplan.business.orders.daos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.business.workingday.EffortDuration;

/**
 * Contract for {@link OrderElementDAO}
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IOrderElementDAO extends IIntegrationEntityDAO<OrderElement> {

    public List<OrderElement> findWithoutParent();

    public OrderElement findUniqueByCode(String code)
            throws InstanceNotFoundException;

    public List<OrderElement> findByCodeAndParent(OrderElement parent,
            String code);

    /**
     * Find an order element with the <code>code</code> passed as parameter
     * and which is a son of the <code>parent</code> {@link OrderElement}
     *
     * @param parent Parent {@link OrderElement}
     * @param code code of the {@link OrderElement} to find
     * @return the {@link OrderElement} found
     */
    public OrderElement findUniqueByCodeAndParent(OrderElement parent,
            String code) throws InstanceNotFoundException;

    /**
     * Returns the number of directly assigned effort for an
     * {@link OrderElement}. It means that the hours assigned to its children
     * aren't included.
     *
     * It is recommended to use {@link OrderElement}.getSumChargedEffort().
     * getDirectChargedEffort() instead, because getAssignedHours calculates
     * that number iterating on the element's WorkReporLines.
     *
     * @param orderElement
     *            must be attached
     * @return The direct effort
     */
    EffortDuration getAssignedDirectEffort(OrderElement orderElement);

    /**
     * Returns the advance percentage in hours for an {@link OrderElement}
     *
     * @param orderElement
     *            must be attached
     * @return The advance percentage (a {@link BigDecimal} between 0-1)
     */
    BigDecimal getHoursAdvancePercentage(OrderElement orderElement);

    OrderElement findUniqueByCodeAnotherTransaction(String code)
            throws InstanceNotFoundException;

    boolean existsOtherOrderElementByCode(OrderElement orderElement);

    boolean existsByCodeAnotherTransaction(OrderElement orderElement);

    List<OrderElement> getAll();

    public List<OrderElement> findOrderElementsWithExternalCode();

    List<OrderElement> findByTemplate(OrderElementTemplate template);

    BigDecimal calculateAverageEstimatedHours(final List<OrderElement> list);

    EffortDuration calculateAverageWorkedHours(final List<OrderElement> list);

    BigDecimal calculateMaxEstimatedHours(final List<OrderElement> list);

    BigDecimal calculateMinEstimatedHours(final List<OrderElement> list);

    EffortDuration calculateMaxWorkedHours(final List<OrderElement> list);

    EffortDuration calculateMinWorkedHours(final List<OrderElement> list);

    boolean isAlreadyInUseThisOrAnyOfItsChildren(OrderElement orderElement);

    /**
     * Returns codes in DB searching in all order elements but excluding orderElements
     *
     * @param orderElements
     * @return
     */
    Set<String> getAllCodesExcluding(List<OrderElement> orderElements);

    /**
     * Checks if there's another {@link OrderElement} in DB which code is the same as
     * some of the ones in order (and its children)
     *
     * @param order
     * @return
     */
    OrderElement findRepeatedOrderCodeInDB(OrderElement order);

}
