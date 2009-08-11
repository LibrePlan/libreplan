package org.zkoss.ganttz;

import java.util.List;

import org.zkoss.ganttz.data.ResourceLoad;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

/**
 * Component to include a list of ResourceLoads inside the ResourcesLoadPanel.
 * Created at Aug 11, 2009
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class ResourceLoadList extends XulElement implements AfterCompose {

    private List<ResourceLoad> resourceLoads;

    public ResourceLoadList(List<ResourceLoad> resourceLoads) {
        this.resourceLoads = resourceLoads;
    }

    @Override
    public void afterCompose() {
    }

    public Planner getPlanner() {
        return getGanttPanel().getPlanner();
    }

    private GanttPanel getGanttPanel() {
        return (GanttPanel) getParent();
    }

    public synchronized void addResourceLoadComponent(ResourceLoad r) {
        resourceLoads.add(r);
    }

}
