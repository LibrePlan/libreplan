package org.libreplan.business.util;

import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskGroup;

public abstract class Visitor {

    public abstract void visit(Task task);

    public abstract void visit(TaskGroup taskGroup);

}
