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
package org.libreplan.business.hibernate.notification;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Óscar González Fernández
 *
 */
class NotBlockingAutoUpdatedSnapshot<T> implements IAutoUpdatedSnapshot<T> {

    private static final Log LOG = LogFactory
            .getLog(NotBlockingAutoUpdatedSnapshot.class);

    private final Callable<T> callable;

    private final AtomicReference<State> currentState;

    private final String name;

    private final ExecutionsReport executionsReport;

    private abstract class State {
        abstract T getValue();

        void cancel() {
        }

        State nextState(Future<T> future) {
            return new PreviousValueAndOngoingCalculation(this, future);
        }

        boolean hasBeenInitialized() {
            return true;
        }
    }

    private class NotLaunchState extends State {

        @Override
        T getValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        State nextState(Future<T> future) {
            return new FirstCalculation(future);
        }

        @Override
        boolean hasBeenInitialized() {
            return false;
        }

    }

    private class NoOngoingCalculation extends State {
        private final T value;

        NoOngoingCalculation(T value) {
            this.value = value;
        }

        @Override
        T getValue() {
            return value;
        }
    }

    private class PreviousValueAndOngoingCalculation extends State {
        private final State previousValue;

        private final Future<T> ongoingCalculation;

        private PreviousValueAndOngoingCalculation(State value,
                Future<T> ongoingCalculation) {
            Validate.notNull(value);
            Validate.notNull(ongoingCalculation);
            this.previousValue = value;
            this.ongoingCalculation = ongoingCalculation;
        }

        @Override
        T getValue() {
            if (!ongoingCalculation.isCancelled()
                    && ongoingCalculation.isDone()) {
                T newValue = getValueFromFuture();
                currentState.compareAndSet(this, new NoOngoingCalculation(
                        newValue));
                return newValue;
            }
            LOG.debug(name + " the ongoing calculation has not been completed. "
                    + "Returning previous value");
            return previousValue.getValue();
        }

        private T getValueFromFuture() {
            try {
                return ongoingCalculation.get();
            } catch (Exception e) {
                LOG.error("error creating new value for " + name
                        + ", keeping old value", e);
                return previousValue.getValue();
            }
        }

        @Override
        void cancel() {
            if (ongoingCalculation.isDone() || ongoingCalculation.isCancelled()) {
                return;
            }
            LOG.debug(name + " cancelling ongoing future");
            try {
                ongoingCalculation.cancel(true);
            } catch (Exception e) {
                LOG.error("error cancelling future for " + name, e);
            }
        }
    }

    private class FirstCalculation extends State {
        private final Future<T> ongoingCalculation;

        private FirstCalculation(Future<T> ongoingCalculation) {
            this.ongoingCalculation = ongoingCalculation;
        }

        @Override
        T getValue() {
            try {
                return ongoingCalculation.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        void cancel() {
            ongoingCalculation.cancel(true);
        }

    }

    public NotBlockingAutoUpdatedSnapshot(String name, Callable<T> callable) {
        Validate.notNull(callable);
        Validate.notNull(name);
        this.name = "*" + name + "*";
        this.callable = callable;
        this.currentState = new AtomicReference<State>(new NotLaunchState());
        this.executionsReport = new ExecutionsReport();
    }

    @Override
    public T getValue() {
        return currentState.get().getValue();
    }

    public void reloadNeeded(ExecutorService executorService) {
        Future<T> future = executorService
                .submit(callableDecoratedWithStatistics());
        State previousState;
        State newState = null;
        do {
            if (newState != null) {
                newState.cancel();
            }
            previousState = currentState.get();
            newState = previousState.nextState(future);
        } while (!currentState.compareAndSet(previousState, newState));
        previousState.cancel();
    }

    public void ensureFirstLoad(ExecutorService executorService) {
        if (hasBeenInitialized()) {
            return;
        }
        Future<T> future = executorService
                .submit(callableDecoratedWithStatistics());
        State previous = currentState.get();
        State newState = previous.nextState(future);
        boolean compareAndSet = currentState.compareAndSet(previous, newState);
        if (!compareAndSet) {
            newState.cancel();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    private boolean hasBeenInitialized() {
        return currentState.get().hasBeenInitialized();
    }

    private Callable<T> callableDecoratedWithStatistics() {
        final long requestTime = System.currentTimeMillis();
        return new Callable<T>() {

            @Override
            public T call() throws Exception {
                long start = System.currentTimeMillis();
                long timeWaiting = start - requestTime;
                Exception error = null;
                try {
                    return callable.call();
                } catch (Exception e) {
                    error = e;
                    LOG.error("error executing snapshot " + name);
                    throw e;
                } finally {
                    long timeExecuting = System.currentTimeMillis() - start;
                    executionsReport.newData(timeWaiting, timeExecuting, error);
                }
            }
        };
    }

    private static class Data {
        final int executionTimes;
        final int errors;
        long totalMsWaiting;
        long totalMsExecuting;

        private Data(int executionTimes, int errors, long totalMsWaiting,
                long totalMsExecuting) {
            this.executionTimes = executionTimes;
            this.totalMsWaiting = totalMsWaiting;
            this.totalMsExecuting = totalMsExecuting;
            this.errors = errors;
        }

        public Data newData(long timeWaiting, long timeExcuting, Exception e) {
            return new Data(executionTimes + 1, errors + (e != null ? 1 : 0),
                    totalMsWaiting + timeWaiting,
                    totalMsExecuting + timeExcuting);
        }

    }

    private class ExecutionsReport {

        private AtomicReference<Data> data = new AtomicReference<Data>(
                new Data(0, 0, 0, 0));

        public void newData(long timeWaiting, long timeExecuting,
                Exception possibleError) {
            Data previousData;
            Data newData;
            do {
                previousData = data.get();
                newData = previousData.newData(timeWaiting, timeExecuting,
                        possibleError);
            } while (!data.compareAndSet(previousData, newData));
            report(timeWaiting, timeExecuting, newData, possibleError);
        }

        private void report(long timeWaiting, long timeExecuting, Data data,
                Exception possibleError) {
            LOG.debug(name + " took " + timeExecuting + " ms executing");
            if (possibleError != null) {
                LOG.error("error loading " + name, possibleError);
            }
            LOG.debug(name + " waited for " + timeWaiting
                    + " ms until executing");
            LOG.debug(name + " mean time waiting for execution: "
                    + data.totalMsWaiting / data.executionTimes + " ms");
            LOG.debug(name + " mean time  executing: "
                    + data.totalMsExecuting / data.executionTimes + " ms");
            LOG.debug(name + " has been executed " + data.executionTimes
                    + " times");
            LOG.debug(name + " has produced errors " + data.errors + " times");
        }
    }

}
