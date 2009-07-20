package org.zkoss.ganttz;

import org.zkoss.ganttz.extensions.IContext;

public class FunctionalityExposedForExtensions implements IContext {

    private final Planner planner;

    public FunctionalityExposedForExtensions(Planner planner) {
        this.planner = planner;
    }

}
