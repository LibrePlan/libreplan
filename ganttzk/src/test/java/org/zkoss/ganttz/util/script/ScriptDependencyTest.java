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
