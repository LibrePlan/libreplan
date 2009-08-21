package org.zkoss.ganttz.data.resourceload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

public class LoadTimelinesGroup {

    private final LoadTimeLine principal;

    private final List<LoadTimeLine> children;

    public LoadTimelinesGroup(LoadTimeLine principal,
            List<? extends LoadTimeLine> children) {
        Validate.notNull(principal);
        Validate.notNull(children);
        this.principal = principal;
        this.children = Collections
                .unmodifiableList(new ArrayList<LoadTimeLine>(children));
    }

    public LoadTimeLine getPrincipal() {
        return principal;
    }

    public List<LoadTimeLine> getChildren() {
        return children;
    }

}

