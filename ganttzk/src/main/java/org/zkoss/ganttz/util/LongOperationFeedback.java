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

import org.apache.commons.lang.Validate;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class LongOperationFeedback {

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

}
