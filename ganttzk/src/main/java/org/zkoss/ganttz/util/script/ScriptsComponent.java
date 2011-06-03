/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.zkoss.ganttz.util.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;

public class ScriptsComponent extends HtmlMacroComponent {

    private List<ScriptDependency> current = Collections.emptyList();

    public ScriptsComponent() {
        OnZKDesktopRegistry<IScriptsRegister> singleton = getScriptsRegister();
        ScriptRegister register;
        if (singleton.isRegistered()) {
            register = (ScriptRegister) singleton.retrieve();
        } else {
            register = new ScriptRegister();
            singleton.store(register);
        }
        register.addDependant(this);
    }

    private OnZKDesktopRegistry<IScriptsRegister> getScriptsRegister() {
        return OnZKDesktopRegistry.getLocatorFor(IScriptsRegister.class);
    }

    public List<ScriptDependency> getScriptDependencies() {
        return current;
    }

    void setDependencies(List<ScriptDependency> current) {
        this.current = current;
        if (!executionIsUpdatingPage()) {
            recreate();
        }
    }

    private boolean executionIsUpdatingPage() {
        return Executions.getCurrent().isAsyncUpdate(null);
    }

}

class ScriptRegister implements IScriptsRegister {
    private ScriptDependenciesSorter dependenciesSorter = new ScriptDependenciesSorter();

    private List<ScriptsComponent> dependant = new ArrayList<ScriptsComponent>();

    void addDependant(ScriptsComponent component) {
        dependant.add(component);
    }

    @Override
    public void register(Class<?> klassContainingScripts)
            throws IllegalArgumentException {
        dependenciesSorter.register(klassContainingScripts);
        notifyDependant(encodeURLs(dependenciesSorter
                .getScriptDependenciesOrderered()));
    }

    private List<ScriptDependency> encodeURLs(
            List<ScriptDependency> scriptDependenciesOrderered) {
        List<ScriptDependency> result = new ArrayList<ScriptDependency>();
        Execution execution = Executions.getCurrent();
        for (ScriptDependency s : scriptDependenciesOrderered) {
            result.add(new ScriptDependency(execution.encodeURL(s.getURL())));
        }
        return result;
    }

    private void notifyDependant(List<ScriptDependency> current) {
        for (ScriptsComponent d : dependant) {
            d.setDependencies(current);
        }
    }
}
