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

package org.zkoss.ganttz;

import org.apache.commons.lang.StringUtils;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

class CommandContextualized<T> {

    public static <T> CommandContextualized<T> create(ICommand<T> command,
            IContext<T> context) {
        return new CommandContextualized<T>(command, context);
    }

    private final ICommand<T> command;

    private final IContext<T> context;

    private Button button;

    private CommandContextualized(ICommand<T> command, IContext<T> context) {
        this.command = command;
        this.context = context;
    }

    public void doAction() {
        command.doAction(context);
    }

    Button toButton() {
        if (button != null) {
            return button;
        }
        Button result = new Button();
        if (StringUtils.isEmpty(command.getImage())) {
            result.setLabel(command.getName());
        } else {
            result.setImage(command.getImage());
            result.setTooltiptext(command.getName());
        }
        result.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) {
                doAction();
            }
        });
        button = result;
        return result;
    }

    public ICommand<T> getCommand() {
        return command;
    }
}
