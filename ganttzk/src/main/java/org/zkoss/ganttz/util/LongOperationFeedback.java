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
package org.zkoss.ganttz.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class LongOperationFeedback {

    private static final Log LOG = LogFactory
            .getLog(LongOperationFeedback.class);

    public interface ILongOperation {
        void doAction() throws Exception;

        String getName();
    }

    private static final ThreadLocal<Boolean> alreadyInside = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public static void execute(final Component component,
            final ILongOperation longOperation) {
        Validate.notNull(component);
        Validate.notNull(longOperation);

        if (alreadyInside.get()) {
            dispatchActionDirectly(longOperation);
            return;
        }

        Clients.showBusy(longOperation.getName(), true);
        executeLater(component, new Runnable() {
            public void run() {
                try {
                    alreadyInside.set(true);
                    longOperation.doAction();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    alreadyInside.remove();
                    Clients.showBusy(null, false);
                }
            }
        });
    }

    public static void executeLater(final Component component,
            final Runnable runnable) {
        Validate.notNull(runnable);
        Validate.notNull(component);
        final String eventName = generateEventName();
        component.addEventListener(eventName, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                try {
                    runnable.run();
                } finally {
                    component.removeEventListener(eventName, this);
                }
            }
        });
        Events.echoEvent(eventName, component, null);
    }

    private static void dispatchActionDirectly(
            final ILongOperation longOperation) {
        try {
            longOperation.doAction();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateEventName() {
        return "onLater";
    }

    private LongOperationFeedback() {
    }

    public interface IDesktopUpdatesEmitter<T> {
        public void doUpdate(T value);
    }

    public interface IDesktopUpdate {
        public void doUpdate();
    }

    public static IDesktopUpdate and(final IDesktopUpdate... desktopUpdates) {
        return new IDesktopUpdate() {

            @Override
            public void doUpdate() {
                for (IDesktopUpdate each : desktopUpdates) {
                    each.doUpdate();
                }
            }
        };
    }

    public interface IBackGroundOperation<T> {
        public void doOperation(IDesktopUpdatesEmitter<T> desktopUpdateEmitter);
    }

    private static final ExecutorService executor = Executors
            .newCachedThreadPool();

    public static <T> IDesktopUpdatesEmitter<T> doNothingEmitter() {
        return new IDesktopUpdatesEmitter<T>() {
            @Override
            public void doUpdate(T value) {
            }
        };
    }

    /**
     * Executes a long operation. The background operation can send
     * {@link IDesktopUpdate} objects that can update desktop state. Trying to
     * update the components in any other way would fail
     */
    public static void progressive(final Desktop desktop,
            final IBackGroundOperation<IDesktopUpdate> operation) {
        progressive(desktop, operation,
                new IDesktopUpdatesEmitter<IDesktopUpdate>() {

                    @Override
                    public void doUpdate(IDesktopUpdate update) {
                        update.doUpdate();
                    }
                });
    }

    /**
     * Executes a long operation. The background operation can send
     * <code>T</code> objects that can update desktop state. A
     * {@link IDesktopUpdatesEmitter} that handle these objects is necessary.
     * Trying to update the components in any other way would fail.
     */
    public static <T> void progressive(final Desktop desktop,
            final IBackGroundOperation<T> operation,
            final IDesktopUpdatesEmitter<T> emitter) {
        desktop.enableServerPush(true);
        executor.execute(new Runnable() {
            public void run() {
                try {
                    IBackGroundOperation<T> operationWithAsyncUpates = withAsyncUpates(
                            operation, desktop);
                    operationWithAsyncUpates.doOperation(emitter);
                } catch (Exception e) {
                    LOG.error("error executing background operation", e);
                } finally {
                    desktop.enableServerPush(false);
                }
            }
        });
    }

    private static <T> IBackGroundOperation<T> withAsyncUpates(
            final IBackGroundOperation<T> backgroundOperation,
            final Desktop desktop) {
        return new IBackGroundOperation<T>() {

            @Override
            public void doOperation(
                    IDesktopUpdatesEmitter<T> originalEmitter) {
                NotBlockingDesktopUpdates<T> notBlockingDesktopUpdates = new NotBlockingDesktopUpdates<T>(
                        desktop, originalEmitter);
                Future<?> future = executor.submit(notBlockingDesktopUpdates);
                try {
                    backgroundOperation.doOperation(notBlockingDesktopUpdates);
                } finally {
                    notBlockingDesktopUpdates.finish();
                    waitUntilShowingAllUpdates(future);
                }
            }

            private void waitUntilShowingAllUpdates(Future<?> future) {
                try {
                    future.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static class NotBlockingDesktopUpdates<T> implements
            IDesktopUpdatesEmitter<T>, Runnable {

        private BlockingQueue<EndOrValue<T>> queue = new LinkedBlockingQueue<EndOrValue<T>>();
        private final IDesktopUpdatesEmitter<T> original;
        private final Desktop desktop;

        NotBlockingDesktopUpdates(Desktop desktop,
                IDesktopUpdatesEmitter<T> original) {
            this.original = original;
            this.desktop = desktop;
        }

        @Override
        public void doUpdate(T value) {
            queue.add(EndOrValue.value(value));
        }

        void finish() {
            queue.add(EndOrValue.<T> end());
        }

        @Override
        public void run() {
            List<T> batch = new ArrayList<T>();
            while (true) {
                batch.clear();
                EndOrValue<T> current = null;
                try {
                    current = queue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (current.isEnd()) {
                    return;
                }
                try {
                    Executions.activate(desktop);
                } catch (Exception e) {
                    LOG.error("unable to access desktop", e);
                    throw new RuntimeException(e);
                }
                try {
                    original.doUpdate(current.getValue());
                    while ((current = queue.poll()) != null) {
                        if (current.isEnd()) {
                            break;
                        }
                        batch.add(current.getValue());
                        original.doUpdate(current.getValue());
                    }
                } finally {
                    Executions.deactivate(desktop);
                }
                if (current != null && current.isEnd()) {
                    return;
                }
            }
        }

    }

    private static abstract class EndOrValue<T> {
        public static <T> EndOrValue<T> end() {
            return new End<T>();
        }

        public static <T> EndOrValue<T> value(T value) {
            return new Value<T>(value);
        }

        public abstract boolean isEnd();
        public abstract T getValue() throws UnsupportedOperationException;
    }

    private static class Value<T> extends EndOrValue<T> {

        private final T value;

        Value(T value) {
            Validate.notNull(value);
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        @Override
        public boolean isEnd() {
            return false;
        }
    }

    private static class End<T> extends EndOrValue<T> {

        @Override
        public T getValue() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEnd() {
            return true;
        }

    }
}
