package org.zkoss.ganttz.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.WeakReferencedListeners.IListenerNotification;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class Milestone extends Task {

    public Milestone() {
        super();
    }

    public Milestone(ITaskFundamentalProperties fundamentalProperties) {
        super(fundamentalProperties);
    }

    private List<Task> tasks = new ArrayList<Task>();

    private boolean expanded = false;

    @Override
    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    @Override
    protected void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!this.expanded) {
            return;
        }
        for (Task task : tasks) {
            task.setVisible(true);
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

}