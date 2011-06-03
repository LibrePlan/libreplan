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

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

public class ScriptDependenciesSorterTest {

    private ScriptDependenciesSorter scriptDependenciesSorter;

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddNullDependency() {
        givenAllScriptsRequired();
        scriptDependenciesSorter.add(null);
    }

    @Test
    public void testWithoutDependencies() {
        givenAllScriptsRequired();
        scriptDependenciesSorter.add(new ScriptDependency("A"));
        scriptDependenciesSorter.add(new ScriptDependency("B"));
        assertThat(scriptDependenciesSorter.getScriptDependenciesOrderered(),
                scriptsReturnedAre("A", "B"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void theDependenciesOrderedCannotBeModified() {
        givenAllScriptsRequired();
        scriptDependenciesSorter.getScriptDependenciesOrderered().add(
                new ScriptDependency("bla"));
    }

    @Test
    public void dependenciesGoesFirst() {
        givenAllScriptsRequired();
        scriptDependenciesSorter.add(new ScriptDependency("A", Arrays
                .asList(new ScriptDependency("B"))));
        assertThat(scriptDependenciesSorter.getScriptDependenciesOrderered(),
                scriptsReturnedAre("B", "A"));
    }

    @Test
    public void dependenciesAreNotRepeated() {
        givenAllScriptsRequired();
        scriptDependenciesSorter.add(new ScriptDependency("A", Arrays
                .asList(new ScriptDependency("B"))));
        scriptDependenciesSorter.add(new ScriptDependency("C", Arrays
                .asList(new ScriptDependency("B"))));
        scriptDependenciesSorter.add(new ScriptDependency("D"));
        assertThat(scriptDependenciesSorter.getScriptDependenciesOrderered(),
                scriptsReturnedAre("B", "A", "C", "D"));
    }

    private Matcher<List<ScriptDependency>> scriptsReturnedAre(String... urls) {
        final List<String> urlsList = Arrays.asList(urls);
        return new BaseMatcher<List<ScriptDependency>>() {

            @Override
            public boolean matches(Object object) {
                if (object instanceof List) {
                    List<ScriptDependency> scriptsRequired = (List<ScriptDependency>) object;
                    List<String> onlyURLs = ScriptDependency
                            .getOnlyURLs(scriptsRequired);
                    return onlyURLs.equals(urlsList);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("must return " + urlsList);
            }
        };
    }

    private void givenAllScriptsRequired() {
        scriptDependenciesSorter = new ScriptDependenciesSorter();
    }

}
