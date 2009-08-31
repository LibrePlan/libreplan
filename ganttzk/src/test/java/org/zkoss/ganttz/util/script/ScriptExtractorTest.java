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

public class ScriptExtractorTest {

    @Test(expected = IllegalArgumentException.class)
    public void aClassWithoutScriptRequiredAnnotationIsNotIncluded() {
        ScriptExtractor.extractFrom(String.class);
    }

    @Test
    public void onlyPublicStringFieldsAreIncluded() {
        List<ScriptDependency> scripts = ScriptExtractor
                .extractFrom(ScriptsDeclarationsExample.class);
        assertThat(scripts.size(), equalTo(2));
        assertThat(scripts,
                hasItem(withURL(ScriptsDeclarationsExample.EXAMPLE_A)));
    }

    @Test
    public void testIncludesDependencies() {
        List<ScriptDependency> scripts = ScriptExtractor
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
