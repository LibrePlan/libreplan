/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.zkoss.ganttz.print;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.management.timer.Timer;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class GanttDiagramURIStore {

    private static Map<String, GanttDiagramURIData> resources = new HashMap<String, GanttDiagramURIData>();

    public static String storeURI(String URI) {
        List<String> URIs = new ArrayList<String>();
        URIs.add(URI);
        return storeURIs(URIs);
    }

    public static String storeURIs(List<String> URIs) {
        String key = UUID.randomUUID().toString();
        resources.put(key, new GanttDiagramURIData(URIs));
        return key;
    }

    public static List<String> getURIsById(String id) {
        clean();
        final GanttDiagramURIData data = resources.get(id);
        if (data != null) {
            return data.getURIs();
        }
        return new ArrayList<String>();
    }

    public static void clean() {
        clean(GanttDiagramURIData.EXPIRE_TIME);
    }

    public static void clean(long lifespan) {
        final Set<String> keys = resources.keySet();
        for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            final String key = (String) i.next();
            final GanttDiagramURIData data = resources.get(key);
            if (data.hasExpired(lifespan)) {
                i.remove();
            }
        }
    }

    public static int size() {
        return resources.size();
    }

    /**
     *
     * @author Diego Pino Garcia <dpino@igalia.com>
     *
     */
    private static class GanttDiagramURIData {

        private static final long EXPIRE_TIME = Timer.ONE_HOUR;

        private long timestamp;

        private List<String> URIs = new ArrayList<String>();

        public GanttDiagramURIData(List<String> URIs) {
            this.timestamp = currentTime();
            this.URIs.addAll(URIs);
        }

        public boolean hasExpired(long lifespan) {
            return ((timestamp + lifespan - currentTime()) <= 0);
        }

        private long currentTime() {
            return System.currentTimeMillis();
        }

        public List<String> getURIs() {
            return Collections.unmodifiableList(URIs);
        }

    }

}
