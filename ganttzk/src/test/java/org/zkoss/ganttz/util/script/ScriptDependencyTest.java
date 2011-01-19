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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ScriptDependencyTest {

    private ScriptDependency script;
    private String url;
    private List<ScriptDependency> dependencies;

    private void givenScript() {
        url = "blabla/zkau/web/js/yui/2.7.0/selector/selector-min.js";
        dependencies = new ArrayList<ScriptDependency>();
        dependencies.add(new ScriptDependency("blabla/blabla/bla.js"));
        script = new ScriptDependency(url, dependencies);
    }

    @Test
    public void aScriptHasAURL() {
        givenScript();
        assertThat(script.getURL(), equalTo(url));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theURLMustBeNotNull() {
        new ScriptDependency(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theURLMustBeNotEmpty() {
        new ScriptDependency("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void noDependenciesCanBeNull() {
        List<ScriptDependency> list = new ArrayList<ScriptDependency>();
        list.add(null);
        new ScriptDependency("bla", list);
    }

    @Test
    public void aScriptCanHaveDependencies() {
        givenScript();
        assertThat(script.getDependsOn(), equalTo(dependencies));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void theDependenciesCannotBeModified() {
        givenScript();
        script.getDependsOn().clear();
    }

    @Test
    public void twoScriptsAreEqualsIfHaveTheSameURL() {
        givenScript();
        ScriptDependency scriptDependencyWithSameURL = new ScriptDependency(url);
        assertThat(scriptDependencyWithSameURL, equalTo(script));
        assertThat(scriptDependencyWithSameURL.hashCode(), equalTo(script
                .hashCode()));
        assertThat(new ScriptDependency(url + "b"), not(equalTo(script)));
    }

}
