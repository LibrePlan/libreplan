/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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

package org.libreplan.business.orders.entities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.business.orders.daos.ISumExpensesDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Class to recalculate {@link SumExpenses} for an {@link Order}.<br />
 *
 * This is needed to be called when some elements are moved in the {@link Order}
 * .<br />
 *
 * This class uses a thread, in order to call one by one all the requests
 * received. Moreover, if there's any concurrency issue (because of some reports
 * were saving in the meanwhile) the recalculation is repeated again (with
 * <code>MAX_ATTEMPS_BECAUSE_CONCURRENCY</code> as maximum) till it's performed
 * without concurrency problems.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)

public class SumExpensesRecalculator implements ISumExpensesRecalculator {

    private static final Log LOG = LogFactory.getLog(SumExpensesRecalculator.class);

    /**
     * Number of times that an order is tried to be recalculated if there is any
     * concurrency issue.<br />
     *
     * Concurrency problems could happen because while the recalculation is
     * being done a {@link ExpenseSheet} is saved with elements in the same
     * {@link Order}.
     */
    protected static final int MAX_ATTEMPS_BECAUSE_CONCURRENCY = 100;

    @Autowired
    private ISumExpensesDAO sumExpensesDAO;

    /**
     * Single thread executor in order to perform the recalculations one by one.
     */
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void recalculate(Long orderId) {
        LOG.info("Mark order (id=" + orderId + ") to be recalculated");
        executor.execute(getRecalculationThread(orderId));
    }

    private Runnable getRecalculationThread(final Long orderId) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    recalculateSumExpensess(orderId);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            private void recalculateSumExpensess(Long orderId) throws InterruptedException {
                recalculateSumExpensess(orderId, 0);
            }

            private void recalculateSumExpensess(Long orderId, int counter)
                    throws InterruptedException {
                if (counter > MAX_ATTEMPS_BECAUSE_CONCURRENCY) {
                    LOG.error("Impossible to recalculate order (id=" + orderId
                            + ") due to concurrency problems");
                    return;
                }

                try {
                    LOG.info("Recalculate order (id=" + orderId + ")");
                    sumExpensesDAO.recalculateSumExpenses(orderId);
                } catch (OptimisticLockingFailureException e) {
                    // Wait 1 second and try again
                    LOG.info("Concurrency problem recalculating order (id=" + orderId
                            + ") trying again in 1 second (attempt " + counter + ")");
                    Thread.sleep(1000);

                    counter++;
                    recalculateSumExpensess(orderId, counter);
                }
            }
        };
    }

}