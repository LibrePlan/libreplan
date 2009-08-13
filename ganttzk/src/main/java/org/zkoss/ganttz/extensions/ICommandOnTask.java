package org.zkoss.ganttz.extensions;

public interface ICommandOnTask<T> {

    public String getName();

    public void doAction(IContextWithPlannerTask<T> context, T task);

}
