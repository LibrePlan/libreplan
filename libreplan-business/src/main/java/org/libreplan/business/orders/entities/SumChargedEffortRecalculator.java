/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.orders.daos.ISumChargedEffortDAO;
import org.libreplan.business.workreports.entities.WorkReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Class to recalculate {@link SumChargedEffort} for an {@link Order}.<br />
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
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SumChargedEffortRecalculator implements
        ISumChargedEffortRecalculator {

    private static final Log LOG = LogFactory
            .getLog(SumChargedEffortRecalculator.class);

    /**
     * Number of times that an order is tried to be recalculated if there is any
     * concurrency issue.<br />
     *
     * Concurrency problems could happen because while the recalculation is
     * being done a {@link WorkReport} is saved with elements in the same
     * {@link Order}.
     */
    protected static final int MAX_ATTEMPS_BECAUSE_CONCURRENCY = 100;

    @Autowired
    private ISumChargedEffortDAO sumChargedEffortDAO;

    /**
     * Queue to store the id of the {@link Order} to be recalculated.
     */
    private BlockingQueue<Long> queue = new ArrayBlockingQueue<Long>(1);

    /**
     * The constructor launch the thread, that will be waiting for elements in
     * the <code>queue</code> in order to perform the recalculations.<br />
     *
     * The class is instantiated by Spring and it is a singleton, so you don't
     * need to worry about calling this constructor. This will be done by Spring
     * while launching the application.
     */
    public SumChargedEffortRecalculator() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        LOG.info("Waiting for orders to recalculate from queue");
                        Long orderId = queue.take();
                        recalculateSumChargedEfforts(orderId);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            private void recalculateSumChargedEfforts(Long orderId)
                    throws InterruptedException {
                recalculateSumChargedEfforts(orderId, 0);
            }

            private void recalculateSumChargedEfforts(Long orderId, int counter)
                    throws InterruptedException {
                if (counter > MAX_ATTEMPS_BECAUSE_CONCURRENCY) {
                    LOG.error("Impossible to recalculate order (id=" + orderId
                            + ") due to concurrency problems");
                    return;
                }

                try {
                    LOG.info("Recalculate order (id=" + orderId + ")");
                    sumChargedEffortDAO.recalculateSumChargedEfforts(orderId);
                } catch (OptimisticLockingFailureException e) {
                    // Wait 1 second and try again
                    LOG.info("Concurrency problem recalculating order (id="
                            + orderId + ") trying again in 1 second (attempt "
                            + counter + ")");
                    Thread.sleep(1000);
                    recalculateSumChargedEfforts(orderId, counter++);
                }
            }
        });
    }

    @Override
    public void recalculate(Long orderId) {
        try {
            queue.put(orderId);
            LOG.info("Add order (id=" + orderId + ") to recalculate in queue");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
