package org.zkoss.ganttz.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.event.TreeDataEvent;
import org.zkoss.zul.event.TreeDataListener;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class MutableTreeModelTest {

    public static class Prueba {
    }

    @Test
    public void aMutableTreeModelIsAZkTreeModel() {
        assertTrue(TreeModel.class.isAssignableFrom(MutableTreeModel.class));
    }

    @Test
    public void aMutableTreeModelCanBeCreatedPassingType() {
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class);
        assertNotNull(model);
        assertNull(model.getRoot());
    }

    @Test
    public void aMutableTreeModelCanBeCreatedPassingTypeAndRootObject() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        assertNotNull(model);
        assertThat(model.getRoot(), equalTo(root));
    }

    @Test
    public void childrenCanBeAdded() {
        Prueba prueba = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                prueba);
        Prueba other = new Prueba();
        model.add(model.getRoot(), other);
        Prueba otherChild = new Prueba();
        model.addToRoot(otherChild);
        assertThat(model.getChildCount(model.getRoot()), equalTo(2));
        assertThat(model.getChild(model.getRoot(), 0), equalTo(other));
        assertThat(model.getChild(model.getRoot(), 1), equalTo(otherChild));
    }

    @Test
    public void testLeaf() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        Prueba other = new Prueba();
        model.add(model.getRoot(), other);
        assertTrue(model.isLeaf(other));
        assertFalse(model.isLeaf(root));
    }

    @Test
    public void childAddedCanBeFoundUsingGetPath() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        Prueba child = new Prueba();
        model.add(root, child);
        int[] path = model.getPath(model.getRoot(), child);
        assertThat(path.length, equalTo(1));
        assertThat(path[0], equalTo(0));
    }

    @Test
    public void addingTriggersEvent() {
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class);
        final ArrayList<TreeDataEvent> eventsFired = new ArrayList<TreeDataEvent>();
        model.addTreeDataListener(new TreeDataListener() {

            @Override
            public void onChange(TreeDataEvent event) {
                eventsFired.add(event);
            }
        });
        Prueba child1 = new Prueba();
        Prueba child2 = new Prueba();
        Prueba granChildren1 = new Prueba();
        model.add(model.getRoot(), child1);
        checkIsValid(getLast(eventsFired), model.getRoot(), 0);
        model.add(model.getRoot(), child2);
        checkIsValid(getLast(eventsFired), model.getRoot(), 1);
        model.add(child1, granChildren1);
        checkIsValid(getLast(eventsFired), child1, 0);
        assertThat(eventsFired.size(), equalTo(3));
    }

    private void checkIsValid(TreeDataEvent event, Prueba expectedParent,
            int expectedPosition) {
        assertEquals(expectedParent, event.getParent());
        assertThat(event.getIndexFrom(), equalTo(expectedPosition));
        assertThat(event.getIndexTo(), equalTo(expectedPosition));
        assertThat(event.getType(), equalTo(TreeDataEvent.INTERVAL_ADDED));
    }

    private TreeDataEvent getLast(List<TreeDataEvent> list) {
        return list.get(list.size() - 1);
    }
}
