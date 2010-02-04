/*
 * This file is part of NavalPlan
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
