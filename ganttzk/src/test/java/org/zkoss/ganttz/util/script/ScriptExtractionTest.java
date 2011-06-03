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
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.each;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

public class ScriptExtractionTest {

    @Test(expected = IllegalArgumentException.class)
    public void aClassWithoutScriptRequiredAnnotationIsNotIncluded() {
        ScriptDependenciesSorter.extractFrom(String.class);
    }

    @Test
    public void onlyPublicStringFieldsAreIncluded() {
        List<ScriptDependency> scripts = ScriptDependenciesSorter
                .extractFrom(ScriptsDeclarationsExample.class);
        assertThat(scripts.size(), equalTo(2));
        assertThat(scripts,
                hasItem(withURL(ScriptsDeclarationsExample.EXAMPLE_A)));
    }

    @Test
    public void testIncludesDependencies() {
        List<ScriptDependency> scripts = ScriptDependenciesSorter
                .extractFrom(ScriptsDeclarationsExample.class);
        assertThat(scripts, each(withDependencies(ScriptIncludedExample.base,
                ScriptIncludedExample.other)));

    }

    private Matcher<ScriptDependency> withDependencies(final String... urls) {
        final Set<String> urlsSet = new HashSet<String>(Arrays.asList(urls));
        return new BaseMatcher<ScriptDependency>() {

            @Override
            public boolean matches(Object object) {
                if (object instanceof ScriptDependency) {
                    ScriptDependency dependency = (ScriptDependency) object;
                    Set<String> urls = new HashSet<String>();
                    for (ScriptDependency s : dependency.getDependsOn()) {
                        urls.add(s.getURL());
                    }
                    return urlsSet.equals(urls);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("depends on "
                        + Arrays.toString(urls));
            }
        };
    }

    private Matcher<ScriptDependency> withURL(final String url) {
        return new BaseMatcher<ScriptDependency>() {

            @Override
            public boolean matches(Object dependency) {
                if (dependency instanceof ScriptDependency) {
                    ScriptDependency d = (ScriptDependency) dependency;
                    return d.getURL().equals(url);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("it has url:" + url);
            }
        };
    }

}
