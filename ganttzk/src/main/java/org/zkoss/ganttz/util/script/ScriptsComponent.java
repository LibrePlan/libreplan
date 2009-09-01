package org.zkoss.ganttz.util.script;

import java.util.List;

import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.zk.ui.HtmlMacroComponent;

public class ScriptsComponent extends HtmlMacroComponent {

    private ScriptDependenciesSorter dependenciesSorter;

    public ScriptsComponent() {
        OnZKDesktopRegistry<IScriptsRegister> singleton = getScriptsRegister();
        dependenciesSorter = new ScriptDependenciesSorter();
        singleton.store(new IScriptsRegister() {

            @Override
            public void register(Class<?> klassContainingScripts)
                    throws IllegalArgumentException {
                dependenciesSorter.register(klassContainingScripts);
                recreate();
            }
        });
    }

    private OnZKDesktopRegistry<IScriptsRegister> getScriptsRegister() {
        return OnZKDesktopRegistry
                .getLocatorFor(IScriptsRegister.class);
    }

    public List<ScriptDependency> getScriptDependencies() {
        return dependenciesSorter.getScriptDependenciesOrderered();
    }

}
