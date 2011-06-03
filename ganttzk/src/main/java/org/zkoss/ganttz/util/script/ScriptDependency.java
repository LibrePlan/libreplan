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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a dependency to a script
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ScriptDependency {

    public static List<String> getOnlyURLs(
            Collection<? extends ScriptDependency> dependencies) {
        List<String> result = new ArrayList<String>();
        for (ScriptDependency scriptDependency : dependencies) {
            result.add(scriptDependency.getURL());
        }
        return result;
    }

    private final String url;
    private final List<ScriptDependency> dependsOn;

    public ScriptDependency(String url) {
        this(url, Collections.<ScriptDependency> emptyList());
    }

    public ScriptDependency(String url, Collection<? extends ScriptDependency> dependencies) {
        Validate.notEmpty(url);
        Validate.noNullElements(dependencies);
        this.url = url;
        this.dependsOn = Collections.unmodifiableList(new ArrayList<ScriptDependency>(
                dependencies));
    }

    public String getURL() {
        return this.url;
    }

    public List<ScriptDependency> getDependsOn() {
        return dependsOn;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ScriptDependency) {
            return url.equals(((ScriptDependency) other).url);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(url).toHashCode();
    }

    @Override
    public String toString() {
        return url;
    }

}
