/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
package org.navalplanner.business.hibernate.notification;

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
            return previousValue.getValue();
        }

        private T getValueFromFuture() {
            try {
                return ongoingCalculation.get();
            } catch (Exception e) {
                LOG.error("error creating new snapshot, keeping old value",
                        e);
                return previousValue.getValue();
            }
        }

        @Override
        void cancel() {
            try {
                ongoingCalculation.cancel(true);
            } catch (Exception e) {
                LOG.error("error cancelling future", e);
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

    public NotBlockingAutoUpdatedSnapshot(Callable<T> callable) {
        Validate.notNull(callable);
        this.callable = callable;
        currentState = new AtomicReference<State>(new NotLaunchState());
    }

    @Override
    public T getValue() {
        return currentState.get().getValue();
    }

    public void reloadNeeded(ExecutorService executorService) {
        Future<T> future = executorService.submit(callable);
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
        Future<T> future = executorService.submit(callable);
        State previous = currentState.get();
        State newState = previous.nextState(future);
        boolean compareAndSet = currentState.compareAndSet(previous, newState);
        if (!compareAndSet) {
            newState.cancel();
        }
    }

    private boolean hasBeenInitialized() {
        return currentState.get().hasBeenInitialized();
    }

}
