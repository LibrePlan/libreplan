package org.zkoss.ganttz.print;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.management.timer.Timer;

import org.junit.Test;

public class GanttDiagramURIStoreTest {

    private String URI = "http://www.igalia.com";

    @Test
    public void testStoreURIs() {
        String id = GanttDiagramURIStore.storeURI(URI);
        List<String> URIs = GanttDiagramURIStore.getURIsById(id);
        assertNotNull(URIs);
        String uri = URIs.get(0);
        assertEquals(uri, URI);
    }

    @Test
    public void testCleanAllResources() {
        GanttDiagramURIStore.storeURI(URI);
        GanttDiagramURIStore.clean(0);
        assertEquals(0, GanttDiagramURIStore.size());
    }

    @Test
    public void testDoNotCleanResourceInmediatelyAfterInsertingIt() {
        GanttDiagramURIStore.storeURI(URI);
        GanttDiagramURIStore.clean(Timer.ONE_MINUTE);
        assertEquals(1, GanttDiagramURIStore.size());
    }

}
