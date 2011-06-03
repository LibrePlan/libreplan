/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.workreports.daos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IntegrationEntityDAO;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link WorkReportDAO}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkReportDAO extends IntegrationEntityDAO<WorkReport>
        implements IWorkReportDAO {

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    @Autowired
    private IOrderDAO orderDAO;

	@SuppressWarnings("unchecked")
	@Override
	public List<WorkReport> getAllByWorkReportType(WorkReportType workReportType) {
		final Criteria criteria = getSession().createCriteria(WorkReport.class);
		return criteria.add(Restrictions.eq("workReportType", workReportType)).list();
	}

    @Override
    public List<WorkReport> getAll() {
        return list(WorkReport.class);
    }

    @Override
    public List<WorkReport> allWorkReportsWithAssociatedOrdersUnproxied() {
        forceOrdersUnproxied();
        return list(WorkReport.class);
    }

    private void forceOrdersUnproxied() {
        List<OrderElement> elements = adHocTransactionService
                .runOnAnotherReadOnlyTransaction(new IOnTransaction<List<OrderElement>>() {

                    @Override
                    public List<OrderElement> execute() {
                        return getOrderElementsAssociatedWithWorkReports();
                    }
                });
        orderDAO.loadOrdersAvoidingProxyFor(elements);
    }

    private List<OrderElement> getOrderElementsAssociatedWithWorkReports() {
        Set<OrderElement> result = new HashSet<OrderElement>();
        result.addAll(elementsFrom(getSession().createQuery(
                "select w.orderElement from WorkReport w")));
        result
                .addAll(elementsFrom(getSession()
                        .createQuery(
                                "select line.orderElement from WorkReport w JOIN w.workReportLines line")));
        return new ArrayList<OrderElement>(result);
    }

    @SuppressWarnings("unchecked")
    private List<OrderElement> elementsFrom(Query orderElementsQuery) {
        return orderElementsQuery.list();
    }

}
