package org.zkoss.ganttz.resourceload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.zkoss.ganttz.data.resourceload.ResourceLoad;
import org.zkoss.ganttz.data.resourceload.ResourceLoadLevel;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Div;

/**
 * This class wraps ResourceLoad data inside an specific HTML Div component.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class ResourceLoadComponent extends Div implements AfterCompose {

    private static final int HEIGHT_PER_ROW = 10;
    private final ResourceLoad resourceLoad;
    private List<ResourceLoadInterval> children;

    public class ResourceLoadInterval extends Div {

        private int length;
        private ResourceLoadLevel loadLevel;
        private int loadPercentage;

        public ResourceLoadInterval(int length, int loadPercentage) {
            this.length = length;
            this.loadPercentage = loadPercentage;
            this.loadLevel= ResourceLoadLevel.getFromPercentage(loadPercentage);
        }

        public int getLenght() {
            return this.length;
        }

        public ResourceLoadLevel getLoadLevel() {
            return this.loadLevel;
        }
    }

    public ResourceLoadComponent(ResourceLoad resourceLoad) {
        setHeight(HEIGHT_PER_ROW + "px");
        setContext("idContextMenuTaskAssigment");
        this.resourceLoad = resourceLoad;

        // Added some example ResourceLoadIntervals
        this.children = new ArrayList<ResourceLoadInterval>();
        setId(UUID.randomUUID().toString());
    }

    protected String calculateClass() {
        return "box";
    }

    protected void updateClass() {
        response(null, new AuInvoke(this, "setClass",
                new Object[] { calculateClass() }));
    }

    public void afterCompose() {
    }

    private String _color;

    public static ResourceLoadComponent asResourceLoadComponent(
            ResourceLoad ResourceLoad, ResourceLoadList ResourceLoadList,
            boolean isTopLevel) {
        final ResourceLoadComponent result;
        result = new ResourceLoadComponent(ResourceLoad);
        return result;
    }

    public static ResourceLoadComponent asResourceLoadComponent(
            ResourceLoad ResourceLoad, ResourceLoadList ResourceLoadList) {
        return asResourceLoadComponent(ResourceLoad, ResourceLoadList, true);
    }

    public String getResourceLoadName() {
        return this.resourceLoad.getName();
    }

    public List<ResourceLoadInterval> getChildren() {
        return this.children;
    }

    public void addInterval(int length, int plannificationPercentage) {
        this.children.add(new ResourceLoadInterval(length, plannificationPercentage));
    }

}