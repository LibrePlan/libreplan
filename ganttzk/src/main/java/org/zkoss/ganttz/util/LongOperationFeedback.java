/*
 * This file is part of NavalPlan
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
package org.zkoss.ganttz.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    public static void execute(final Component component,
            final ILongOperation longOperation) {
        Validate.notNull(component);
        Validate.notNull(longOperation);
        Clients.showBusy(longOperation.getName(), true);
        final String eventName = generateEventName();
        component.addEventListener(eventName, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                try {
                    longOperation.doAction();
                } finally {
                    Clients.showBusy(null, false);
                    component.removeEventListener(eventName, this);
                }
            }
        });
        Events.echoEvent(eventName, component, null);
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

    private static final Executor executor = Executors.newCachedThreadPool();

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
     * {@link IDesktopUpdatesEmitter} that handle this objects is necessary.
     * Trying to update the components in any other way would fail.
     */
    public static <T> void progressive(final Desktop desktop,
            final IBackGroundOperation<T> operation,
            final IDesktopUpdatesEmitter<T> emitter) {
        desktop.enableServerPush(true);
        executor.execute(new Runnable() {
            public void run() {
                try {
                    operation.doOperation(decorateWithActivations(desktop,
                            emitter));
                } catch (Exception e) {
                    LOG.error("error executing background operation", e);
                } finally {
                    desktop.enableServerPush(false);
                }
            }
        });
    }

    private static <T> IDesktopUpdatesEmitter<T> decorateWithActivations(
            final Desktop desktop, final IDesktopUpdatesEmitter<T> emitter) {
        return new EmitterWithActivations<T>(desktop, emitter);
    }

    private static final class EmitterWithActivations<T> implements
            IDesktopUpdatesEmitter<T> {
        private final Desktop desktop;

        private final IDesktopUpdatesEmitter<T> emitter;

        private EmitterWithActivations(Desktop desktop,
                IDesktopUpdatesEmitter<T> emitter) {
            this.desktop = desktop;
            this.emitter = emitter;
        }

        @Override
        public void doUpdate(T value) {
            try {
                Executions.activate(desktop);
            } catch (Exception e) {
                LOG.error("unable to access desktop", e);
                throw new RuntimeException(e);
            }
            try {
                emitter.doUpdate(value);
            } finally {
                Executions.deactivate(desktop);
            }
        }
    }

}
