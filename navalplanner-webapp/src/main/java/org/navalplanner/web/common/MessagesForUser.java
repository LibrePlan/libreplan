package org.navalplanner.web.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.hibernate.validator.InvalidValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.EventInterceptor;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;

/**
 * It shows messages to the user. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class MessagesForUser extends GenericForwardComposer implements
        IMessagesForUser {

    private Component container;

    private Queue<Component> pendingToDetach = new ConcurrentLinkedQueue<Component>();

    private static final String DETACH_EVENT_NAME = "onMarkDetached";

    public MessagesForUser(Component container) {
        this.container = container;
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
                Component currrent = null;
                while ((currrent = pendingToDetach.poll()) != null) {
                    currrent.detach();
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
    public void invalidValue(InvalidValue invalidValue) {
        addMessage(createLabelFor(invalidValue));
    }

    private Component createLabelFor(InvalidValue invalidValue) {
        Label result = new Label();
        result.setValue(invalidValue.getPropertyName() + ": "
                + invalidValue.getMessage());
        return result;
    }

    @Override
    public void showMessage(Level level, String message) {
        final Label label = new Label(message);
        addMessage(label);
    }

    private void addMessage(final Component label) {
        container.appendChild(label);
        Events.echoEvent(DETACH_EVENT_NAME, label, "");
        label.addEventListener(DETACH_EVENT_NAME, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                pendingToDetach.offer(label);
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

}
