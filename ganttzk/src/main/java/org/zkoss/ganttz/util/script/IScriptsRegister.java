package org.zkoss.ganttz.util.script;

public interface IScriptsRegister {

    public void register(Class<?> klassContainingScripts)
            throws IllegalArgumentException;

}
