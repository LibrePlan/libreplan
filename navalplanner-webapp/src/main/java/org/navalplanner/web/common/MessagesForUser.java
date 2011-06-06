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

package org.navalplanner.web.common;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.EventInterceptor;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

/**
 * It shows messages to the user. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class MessagesForUser extends GenericForwardComposer implements
        IMessagesForUser {

    // 2 seconds
    private static final long DEFAULT_MINIMUM_VISUALIZATION_TIME_MILLIS = 1000 * 2;

    private static final class PreviousMessagesDiscarder implements
            EventInterceptor {

        private final WeakReference<MessagesForUser> messagesForUserRef;

        public PreviousMessagesDiscarder(MessagesForUser messagesForUser) {
            this.messagesForUserRef = new WeakReference<MessagesForUser>(
                    messagesForUser);
        }

        @Override
        public void afterProcessEvent(Event event) {
        }

        @Override
        public Event beforePostEvent(Event event) {
            return event;
        }

        @Override
        public Event beforeProcessEvent(Event event) {
            MessagesForUser messagesForUser = messagesForUserRef.get();
            if (messagesForUser == null) {
                return event;
            }

            if (event.getName().equals(DETACH_EVENT_NAME)
                    || messagesForUser.pendingToDetach.isEmpty()) {
                return event;
            }
            long currentTime = System.currentTimeMillis();
            ComponentHolderTimestamped currrent = null;
            while ((currrent = messagesForUser.pendingToDetach.peek()) != null
                    && currrent
                            .minimumVisualizationTimeSurpased(currentTime)) {
                currrent.component.detach();
                messagesForUser.pendingToDetach.poll();
            }
            return event;
        }

        @Override
        public Event beforeSendEvent(Event event) {
            return event;
        }
    }

    private class ComponentHolderTimestamped {
        private final Component component;
        private final long timestamp;

        ComponentHolderTimestamped(Component component) {
            this.component = component;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean minimumVisualizationTimeSurpased(long currentTime) {
            return currentTime - timestamp > minimumVisualizationTimeMilliseconds;
        }
    }

    private Component container;

    private final long minimumVisualizationTimeMilliseconds;

    private Queue<ComponentHolderTimestamped> pendingToDetach = new ConcurrentLinkedQueue<ComponentHolderTimestamped>();

    private static final String DETACH_EVENT_NAME = "onMarkDetached";

    public MessagesForUser(Component container) {
        this(container, DEFAULT_MINIMUM_VISUALIZATION_TIME_MILLIS);
    }

    public MessagesForUser(Component container,
            long minimumVisualizationTimeMilliseconds) {
        this.container = container;
        this.minimumVisualizationTimeMilliseconds = minimumVisualizationTimeMilliseconds;
        container.getPage().getDesktop()
                .addListener(new PreviousMessagesDiscarder(this));
    }

    @Override
    public void invalidValue(InvalidValue invalidValue, ICustomLabelCreator customLabelCreator) {
        if (customLabelCreator == null) {
            invalidValue(invalidValue);
        } else {
            addMessage(Level.WARNING, customLabelCreator
                    .createLabelFor(invalidValue));
        }
    }

    @Override
    public void invalidValue(InvalidValue invalidValue) {
        addMessage(Level.WARNING, createLabelFor(invalidValue));
    }

    public static Label createLabelFor(InvalidValue invalidValue) {
        Label result = new Label();
        result.setValue(invalidValue.getPropertyName() + ": "
                + invalidValue.getMessage());
        return result;
    }

    @Override
    public void showMessage(Level level, String message) {
        addMessage(level, new Label(message));
    }

    private void addMessage(Level level, final Component label) {
        final Div messageEntry = createMessage(level, label);
        container.appendChild(messageEntry);
        Events.echoEvent(DETACH_EVENT_NAME, messageEntry, "");
        messageEntry.addEventListener(DETACH_EVENT_NAME, new EventListener() {

            @Override
            public void onEvent(Event event) {
                pendingToDetach.offer(new ComponentHolderTimestamped(
                        messageEntry));
            }
        });
    }

    private Div createMessage(Level level, final Component label) {
        Div div = new Div();
        Image tick = new Image("/common/img/ico_ok.png");
        tick.setSclass("tick");
        div.setSclass("message_" + level.toString());
        div.appendChild(tick);
        div.appendChild(label);
        return div;
    }

    @Override
    public void clearMessages() {
        List<Object> children = new ArrayList<Object>(container.getChildren());
        for (Object child : children) {
            Component c = (Component) child;
            c.detach();
        }
    }


    @Override
    public void showInvalidValues(ValidationException e) {
        showInvalidValues(e, null);
    }

    @Override
    public void showInvalidValues(ValidationException e, ICustomLabelCreator customLabelCreator) {
        if (!StringUtils.isEmpty(e.getMessage())) {
            showMessage(Level.WARNING, e.getMessage());
        }
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            invalidValue(invalidValue, customLabelCreator);
        }
    }

}
