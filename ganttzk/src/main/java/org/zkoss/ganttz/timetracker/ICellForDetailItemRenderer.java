package org.zkoss.ganttz.timetracker;

import org.zkoss.zk.ui.Component;

public interface ICellForDetailItemRenderer<C, T> {

    Component cellFor(C column, T data);

}
