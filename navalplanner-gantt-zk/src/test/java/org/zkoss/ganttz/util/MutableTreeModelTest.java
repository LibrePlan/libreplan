package org.zkoss.ganttz.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
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
    public void getPathReturnsEmptyArrayWhenParentNotFound() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        Prueba child = new Prueba();
        model.add(root, child);
        assertThat(model.getPath(null, child), equalTo(new int[0]));
    }

    @Test
    public void getPathReturnsEmptyArrayWhenChildNotFound() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        Prueba child = new Prueba();
        model.add(root, child);
        assertThat(model.getPath(root, new Prueba()), equalTo(new int[0]));
    }

    @Test
    public void ifThereisNotPathReturnEmptyArray() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        Prueba child = new Prueba();
        model.add(root, child);
        assertThat(model.getPath(child, root), equalTo(new int[0]));
    }

    @Test
    public void hasMethodGetParentToMakeNavigationEasier() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        Prueba child = new Prueba();
        model.add(root, child);
        Prueba grandChild = new Prueba();
        model.add(child, grandChild);
        assertThat(model.getParent(grandChild), equalTo(child));
        assertThat(model.getParent(child), equalTo(root));
    }

    @Test
    public void hasMethodGetAllParentsUntilRoot() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        Prueba child = new Prueba();
        model.add(root, child);
        Prueba grandChild = new Prueba();
        model.add(child, grandChild);
        List<Prueba> parents = model.getParents(grandChild);
        assertThat(parents.size(), equalTo(2));
        assertThat(parents, equalTo(Arrays.asList(child, root)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getParentOfRootThrowsException() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        model.getParent(root);
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
        checkIsValid(getLast(eventsFired), TreeDataEvent.INTERVAL_ADDED, model
                .getRoot(), 0);
        model.add(model.getRoot(), child2);
        checkIsValid(getLast(eventsFired), TreeDataEvent.INTERVAL_ADDED, model
                .getRoot(), 1);
        model.add(child1, granChildren1);
        checkIsValid(getLast(eventsFired), TreeDataEvent.INTERVAL_ADDED,
                child1, 0);
        assertThat(eventsFired.size(), equalTo(3));
    }

    @Test
    public void aNodeCanBeRemoved() {
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class);
        Prueba prueba1 = new Prueba();
        model.add(model.getRoot(), prueba1);
        model.remove(prueba1);
        assertThat(model.getChildCount(model.getRoot()), equalTo(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theRootNodeCannotBeRemoved() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        model.remove(root);
    }

    @Test
    public void removingANodeWithChildrenRemovesTheChildren() {
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class);
        Prueba parent = new Prueba();
        model.add(model.getRoot(), parent);
        Prueba grandson = new Prueba();
        model.add(parent, grandson);
        model.remove(parent);
        assertThat(model.getPath(parent, grandson).length, equalTo(0));
    }

    @Test
    public void removingANodeTriggersEvent() {
        final MutableTreeModel<Prueba> model = MutableTreeModel
                .create(Prueba.class);
        final List<TreeDataEvent> removeEventsFired = new ArrayList<TreeDataEvent>();
        model.addTreeDataListener(new TreeDataListener() {

            @Override
            public void onChange(TreeDataEvent event) {
                if (event.getType() == TreeDataEvent.INTERVAL_REMOVED) {
                    removeEventsFired.add(event);
                }
            }
        });
        Prueba prueba1 = new Prueba();
        Prueba prueba2 = new Prueba();
        Prueba grandChild = new Prueba();
        model.add(model.getRoot(), prueba1);
        model.add(model.getRoot(), prueba2);
        model.add(prueba1, grandChild);

        model.remove(grandChild);
        assertThat(removeEventsFired.size(), equalTo(1));
        checkIsValid(getLast(removeEventsFired),
                TreeDataEvent.INTERVAL_REMOVED, prueba1, 0);

        model.remove(prueba2);
        assertThat(getLast(removeEventsFired).getParent(),
                equalTo((Object) model.getRoot()));
        checkIsValid(getLast(removeEventsFired),
                TreeDataEvent.INTERVAL_REMOVED, model.getRoot(), 1);

        model.remove(prueba1);
        assertThat(removeEventsFired.size(), equalTo(3));
        checkIsValid(getLast(removeEventsFired),
                TreeDataEvent.INTERVAL_REMOVED, model.getRoot(), 0);
    }

    private void checkIsValid(TreeDataEvent event, int type,
            Prueba expectedParent, int expectedPosition) {
        assertEquals(expectedParent, event.getParent());
        assertThat(event.getIndexFrom(), equalTo(expectedPosition));
        assertThat(event.getIndexTo(), equalTo(expectedPosition));
        assertThat(event.getType(), equalTo(type));
    }

    private TreeDataEvent getLast(List<TreeDataEvent> list) {
        return list.get(list.size() - 1);
    }
}
