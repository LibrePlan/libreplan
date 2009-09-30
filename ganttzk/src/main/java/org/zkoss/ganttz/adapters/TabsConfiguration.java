package org.zkoss.ganttz.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zkoss.ganttz.extensions.ITab;

public class TabsConfiguration {

    public static TabsConfiguration create() {
        return new TabsConfiguration();
    }

    private List<ITab> tabs = new ArrayList<ITab>();

    private TabsConfiguration() {
    }

    public TabsConfiguration add(ITab tab) {
        tabs.add(tab);
        return this;
    }

    public List<ITab> getTabs() {
        return Collections.unmodifiableList(tabs);
    }

}
