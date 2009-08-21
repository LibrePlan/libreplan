package org.zkoss.ganttz.data.resourceload;

/**
 * This class contains the information of a ResourceLoad unit. It will fetch
 * information from the DailyAssignments and build all its associated
 * ResourceLoad intervals <br/>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class ResourceLoad {

    private String name;

    public ResourceLoad(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}