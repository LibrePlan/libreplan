package org.zkoss.ganttz.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zkoss.ganttz.extensions.ICommand;

/**
 * A object that defines several extension points for gantt planner
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class PlannerConfiguration<T> {

    private IAdapterToTaskFundamentalProperties<T> adapter;

    private IStructureNavigator<T> navigator;

    private List<? extends T> data;

    private List<ICommand> commands = new ArrayList<ICommand>();

    public PlannerConfiguration(IAdapterToTaskFundamentalProperties<T> adapter,
            IStructureNavigator<T> navigator, List<? extends T> data) {
        this.adapter = adapter;
        this.navigator = navigator;
        this.data = data;
    }

    public IAdapterToTaskFundamentalProperties<T> getAdapter() {
        return adapter;
    }

    public IStructureNavigator<T> getNavigator() {
        return navigator;
    }

    public List<? extends T> getData() {
        return data;
    }

    public void addCommand(ICommand command) {
        this.commands.add(command);
    }

    public List<ICommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }

}
