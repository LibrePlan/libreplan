package org.zkoss.ganttz.util.script;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScriptExtractor {

    public static List<ScriptDependency> extractFrom(Class<?> classWithScripts) {
        ScriptsRequiredDeclaration annotation = classWithScripts
                .getAnnotation(ScriptsRequiredDeclaration.class);
        if (annotation == null)
            throw new IllegalArgumentException(classWithScripts
                    + " must be annotated with "
                    + ScriptsRequiredDeclaration.class.getName());
        List<ScriptDependency> dependsOn = getDependencies(annotation);
        List<ScriptDependency> result = new ArrayList<ScriptDependency>();
        for (Field field : getStringFields(getStaticFields(classWithScripts
                .getFields()))) {
            result.add(new ScriptDependency(getValueFromStringField(field),
                    dependsOn));
        }
        return result;
    }

    private static ArrayList<ScriptDependency> getDependencies(
            ScriptsRequiredDeclaration declaration) {
        Class<?>[] dependsOn = declaration.dependsOn();
        ArrayList<ScriptDependency> result = new ArrayList<ScriptDependency>();
        for (Class<?> klass : dependsOn) {
            result.addAll(extractFrom(klass));
        }
        return result;
    }


    private static String getValueFromStringField(Field stringField) {
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

    private static List<Field> getStringFields(Collection<Field> fields) {
        List<Field> stringFields = new ArrayList<Field>();
        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                stringFields.add(field);
            }
        }
        return stringFields;
    }

}
