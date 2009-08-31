package org.zkoss.ganttz.util.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

public class ScriptDependenciesSorter {

    private List<ScriptDependency> allScripts = new ArrayList<ScriptDependency>();

    public ScriptDependenciesSorter() {
    }

    public void add(ScriptDependency scriptDependency) {
        addAll(Arrays.asList(scriptDependency));
    }

    public void addAll(List<ScriptDependency> dependencies) {
        Validate.noNullElements(dependencies);
        allScripts.addAll(dependencies);
    }

    public List<ScriptDependency> getScriptDependenciesOrderered() {
        List<ScriptDependency> result = new ArrayList<ScriptDependency>();
        Set<ScriptDependency> alreadyAdded = new HashSet<ScriptDependency>();
        for (ScriptDependency scriptDependency : allScripts) {
            result.addAll(extract(alreadyAdded, scriptDependency));
        }
        return Collections.unmodifiableList(result);
    }

    private List<ScriptDependency> extract(Set<ScriptDependency> alreadyAdded,
            ScriptDependency scriptDependency) {
        List<ScriptDependency> result = new ArrayList<ScriptDependency>();
        for (ScriptDependency d : scriptDependency.getDependsOn()) {
            if (!alreadyAdded.contains(d)) {
                result.addAll(extract(alreadyAdded, d));
            }
        }
        if (!alreadyAdded.contains(scriptDependency)) {
            result.add(scriptDependency);
            alreadyAdded.add(scriptDependency);
        }
        return result;
    }

}
