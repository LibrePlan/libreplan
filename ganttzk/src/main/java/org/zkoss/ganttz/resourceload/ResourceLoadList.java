package org.zkoss.ganttz.resourceload;

import java.util.List;

import org.zkoss.ganttz.data.resourceload.ResourceLoad;
import org.zkoss.zul.impl.XulElement;

/**
 * Component to include a list of ResourceLoads inside the ResourcesLoadPanel.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class ResourceLoadList extends XulElement {

    private List<ResourceLoad> resourceLoads;

    public ResourceLoadList(List<ResourceLoad> resourceLoads) {
        this.resourceLoads = resourceLoads;
        insertFakeData();
    }

    private void insertFakeData() {
        ResourceLoadComponent rlc1 = new ResourceLoadComponent(
                new ResourceLoad("ResourceLoad 1"));
        ResourceLoadComponent rlc2 = new ResourceLoadComponent(
                new ResourceLoad("ResourceLoad 1"));

        rlc1.addInterval(40, 100);
        rlc1.addInterval(20, 80);
        rlc1.addInterval(30, 150);
        rlc1.addInterval(10, 0);

        rlc2.addInterval(10, 100);
        rlc2.addInterval(20, 60);
        rlc2.addInterval(30, 100);
        rlc2.addInterval(20, 0);
        rlc2.addInterval(20, 60);
        appendChild(rlc1);
        appendChild(rlc2);
    }

}

