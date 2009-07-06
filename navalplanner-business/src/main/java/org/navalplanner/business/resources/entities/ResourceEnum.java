/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.business.resources.entities;

import java.util.List;

/**
 *
 * @author Diego Pino Garcia<dpino@igalia.com>
 */
public enum ResourceEnum {

    RESOURCE(Resource.class),
    WORKER(Worker.class);

    Class clase;

    private ResourceEnum(Class clase) {
        this.clase = clase;
    }

    public Class asClass() {
        return clase;
    }

    public static ResourceEnum getDefault() {
        return RESOURCE;
    }

    public boolean isAssignableFrom(Class clase) {
        return asClass().equals(clase);
    }
}
