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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

public class ScriptDependenciesSorter implements IScriptsRegister {

    public static List<ScriptDependency> extractFrom(Class<?> classWithScripts) {
        ScriptsRequiredDeclaration annotation = classWithScripts
                .getAnnotation(ScriptsRequiredDeclaration.class);
        if (annotation == null) {
            throw new IllegalArgumentException(classWithScripts
                    + " must be annotated with "
                    + ScriptsRequiredDeclaration.class.getName());
        }
        List<ScriptDependency> dependsOn = getDependencies(annotation);
        List<ScriptDependency> result = new ArrayList<ScriptDependency>();
        for (Field field : getStringFields(getStaticFields(classWithScripts
                .getFields()))) {
            result.add(new ScriptDependency(getValueFromStringField(field),
                    dependsOn));
        }
        return result;
    }

    static ArrayList<ScriptDependency> getDependencies(
            ScriptsRequiredDeclaration declaration) {
        Class<?>[] dependsOn = declaration.dependsOn();
        ArrayList<ScriptDependency> result = new ArrayList<ScriptDependency>();
        for (Class<?> klass : dependsOn) {
            result.addAll(extractFrom(klass));
        }
        return result;
    }

    static String getValueFromStringField(Field stringField) {
        try {
            return (String) stringField.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Field> getStaticFields(Field[] fields) {
        List<Field> result = new ArrayList<Field>();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                result.add(field);
            }
        }
        return result;
    }

    static List<Field> getStringFields(Collection<Field> fields) {
        List<Field> stringFields = new ArrayList<Field>();
        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                stringFields.add(field);
            }
        }
        return stringFields;
    }

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
        if (alreadyAdded.contains(scriptDependency)) {
            return result;
        }
        for (ScriptDependency d : scriptDependency.getDependsOn()) {
            result.addAll(extract(alreadyAdded, d));
        }
        result.add(scriptDependency);
        alreadyAdded.add(scriptDependency);
        return result;
    }

    @Override
    public void register(Class<?> klassContainingScripts)
            throws IllegalArgumentException {
        addAll(extractFrom(klassContainingScripts));
    }

}
