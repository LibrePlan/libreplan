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
