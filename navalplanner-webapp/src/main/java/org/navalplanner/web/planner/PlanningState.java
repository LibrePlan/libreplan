package org.navalplanner.web.planner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.planner.entities.TaskElement;

public class PlanningState {
    private final ArrayList<TaskElement> initial;

    private final Set<TaskElement> toSave;

    private final Set<TaskElement> toRemove;

    public PlanningState(Collection<? extends TaskElement> initialState) {
        this.initial = new ArrayList<TaskElement>(initialState);
        this.toSave = new HashSet<TaskElement>(initialState);
        this.toRemove = new HashSet<TaskElement>();
    }

    public Collection<? extends TaskElement> getTasksToSave() {
        return Collections.unmodifiableCollection(toSave);
    }

    public List<TaskElement> getInitial() {
        return new ArrayList<TaskElement>(initial);
    }

    public Collection<? extends TaskElement> getToRemove() {
        return Collections.unmodifiableCollection(toRemove);
    }

    public void removed(TaskElement taskElement) {
        taskElement.detach();
        toSave.remove(taskElement);
        toRemove.add(taskElement);
    }

    public void added(TaskElement taskElement) {
        toRemove.remove(taskElement);
        toSave.add(taskElement);
    }
}