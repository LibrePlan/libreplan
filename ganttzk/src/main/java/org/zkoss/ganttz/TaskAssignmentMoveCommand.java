package org.zkoss.ganttz;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;

/**
 *
 * @author Francisco Javier Moran Rúa
 *
 */
public class TaskAssignmentMoveCommand extends Command {

    public TaskAssignmentMoveCommand(String event,int flags) {
        super(event,flags);
    }

    protected void process(AuRequest request) {

        System.out.println("Processing command");


    }

}
