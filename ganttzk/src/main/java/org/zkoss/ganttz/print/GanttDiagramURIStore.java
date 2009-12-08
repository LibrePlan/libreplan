package org.zkoss.ganttz.print;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
