package org.navalplanner.web.common;

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
import org.zkoss.zul.Label;

/**
 * It shows messages to the user. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class MessagesForUser extends GenericForwardComposer implements
        IMessagesForUser {

    private static final long DEFAULT_MINIMUM_VISUALIZATION_TIME_MILLIS = 1000 * 2; // 2

    // seconds

    private class ComponentHolderTimestamped {
        final Component component;
        final long timestamp;

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
        container.getPage().getDesktop().addListener(new EventInterceptor() {

            @Override
            public void afterProcessEvent(Event event) {
            }

            @Override
            public Event beforePostEvent(Event event) {
                return event;
            }

            @Override
            public Event beforeProcessEvent(Event event) {
                if (event.getName().equals(DETACH_EVENT_NAME)
                        || pendingToDetach.isEmpty()) {
                    return event;
                }
                long currentTime = System.currentTimeMillis();
                ComponentHolderTimestamped currrent = null;
                while ((currrent = pendingToDetach.peek()) != null
                        && currrent
                                .minimumVisualizationTimeSurpased(currentTime)) {
                    currrent.component.detach();
                    pendingToDetach.poll();
                }
                return event;
            }

            @Override
            public Event beforeSendEvent(Event event) {
                return event;
            }
        });
    }

    @Override
    public void invalidValue(InvalidValue invalidValue, ICustomLabelCreator customLabelCreator) {
        addMessage(customLabelCreator.createLabelFor(invalidValue));
    }

    @Override
    public void invalidValue(InvalidValue invalidValue) {
        addMessage(createLabelFor(invalidValue));
    }

    public static Label createLabelFor(InvalidValue invalidValue) {
        Label result = new Label();
        result.setValue(invalidValue.getPropertyName() + ": "
                + invalidValue.getMessage());
        return result;
    }

    @Override
    public void showMessage(Level level, String message) {
        final Label label = new Label(message);
        Div div = new Div();
        div.setSclass("message_" + level.toString());
        div.appendChild(label);
        addMessage(div);
    }

    private void addMessage(final Component label) {
        container.appendChild(label);
        Events.echoEvent(DETACH_EVENT_NAME, label, "");
        label.addEventListener(DETACH_EVENT_NAME, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                pendingToDetach.offer(new ComponentHolderTimestamped(label));
            }
        });
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
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            invalidValue(invalidValue);
        }
        if (!StringUtils.isEmpty(e.getMessage())) {
            showMessage(Level.INFO, e.getMessage());
        }
    }

    @Override
    public void showInvalidValues(ValidationException e, ICustomLabelCreator customLabelCreator) {
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            invalidValue(invalidValue, customLabelCreator);
        }
    }

}
