package org.zkoss.ganttz.timetracker;

import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.zk.ui.Component;

public interface ICellForDetailItemRenderer<T> {

    Component cellFor(DetailItem item, T data);

}
