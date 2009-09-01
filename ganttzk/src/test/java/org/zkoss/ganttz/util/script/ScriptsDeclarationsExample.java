package org.zkoss.ganttz.util.script;

@ScriptsRequiredDeclaration(dependsOn = ScriptIncludedExample.class)
public class ScriptsDeclarationsExample {

    public static final String EXAMPLE_A = "/project-a/blabla/a.js";

    public static final String EXAMPLE_B = "/project-a/blabla/b.js";

    private static String EXAMPLE_NOT_INCLUDED = "/project-a/blablaadsf/a.js";

    public static int NOT_INCLUDED_BECAUSE_IS_NOT_STRING = 4;

    public String NOT_INCLUDED_BECAUSE_IS_NOT_STATIC = "balbla/bladsfafa/ba.js";

}
