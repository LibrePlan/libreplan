package org.zkoss.ganttz.data;

import java.util.List;

/**
 * A {@link Task} that can't have children
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskLeaf extends Task {

    public TaskLeaf() {
        super();
    }

    public TaskLeaf(ITaskFundamentalProperties fundamentalProperties) {
        super(fundamentalProperties);
    }

    @Override
    public List<Task> getTasks() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public boolean isExpanded() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
