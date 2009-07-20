package org.zkoss.ganttz;

import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

class CommandContextualized {

    public static CommandContextualized create(ICommand command,
            IContext context) {
        return new CommandContextualized(command, context);
    }

    private final ICommand command;

    private final IContext context;

    private CommandContextualized(ICommand command, IContext context) {
        this.command = command;
        this.context = context;
    }

    public void doAction() {
        command.doAction(context);
    }

    Button toButton() {
        Button result = new Button();
        result.setLabel(command.getName());
        result.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                doAction();
            }
        });
        return result;
    }

}
