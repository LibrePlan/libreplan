package org.zkoss.ganttz.util;

import java.util.List;

/**
 * A {@link TaskBean} that can't have children
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskLeafBean extends TaskBean {

    public TaskLeafBean() {
        super();
    }

    public TaskLeafBean(ITaskFundamentalProperties fundamentalProperties) {
        super(fundamentalProperties);
    }

    @Override
    public List<TaskBean> getTasks() throws UnsupportedOperationException {
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
