package org.zkoss.ganttz.extensions;


/**
 * An action that can be applied to the planner and it's wanted to be available
 * to the user <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICommand<T> {

    public String getName();

    public void doAction(IContext<T> context);

}
