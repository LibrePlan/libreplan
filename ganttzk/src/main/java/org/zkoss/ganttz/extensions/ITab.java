package org.zkoss.ganttz.extensions;

import org.zkoss.zk.ui.Component;

public interface ITab {

    String getName();

    void addToParent(Component parent);

    void show();

    void hide();

}
