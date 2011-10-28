/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.zkoss.ganttz.util.MutableTreeModel.IChildrenExtractor;
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
    public void findObjectAtInvalidPath() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        model.findObjectAt(0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findObjectAtInvalidPathInTheMiddle() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        Prueba p1 = new Prueba();
        model.addToRoot(p1);
        model.addToRoot(new Prueba());
        model.add(p1, new Prueba());
        model.findObjectAt(1, 1);
    }

    @Test
    public void findAtPathCanReceiveGetPathResult() {
        Prueba root = new Prueba();
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class,
                root);
        assertTrue(canBeRetrievedWithGetPath(model, model.getRoot()));
        model.addToRoot(new Prueba());
        Prueba p2 = new Prueba();
        model.addToRoot(p2);
        assertTrue(canBeRetrievedWithGetPath(model, p2));
        Prueba grandChild = new Prueba();
        model.add(p2, grandChild);
        assertTrue(canBeRetrievedWithGetPath(model, grandChild));
    }

    private static <T> boolean canBeRetrievedWithGetPath(
            final MutableTreeModel<T> tree, T object) {
        int[] path = tree.getPath(object);
        return tree.findObjectAt(path).equals(object);
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
    public void canAddSeveral() {
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
        model.add(model.getRoot(), Arrays.asList(child1, child2));
        assertThat(eventsFired.size(), equalTo(1));
        TreeDataEvent event = getLast(eventsFired);
        checkIsValid(event, TreeDataEvent.INTERVAL_ADDED, model.getRoot(), 0, 1);
    }

    @Test
    public void canAddSeveralAtPosition() {
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class);
        Prueba p1 = new Prueba();
        model.add(model.getRoot(), p1);
        Prueba p2 = new Prueba();
        Prueba p3 = new Prueba();
        model.add(model.getRoot(), 0, Arrays.asList(p2, p3));
        assertThat(model.getChild(model.getRoot(), 0), equalTo(p2));
        assertThat(model.getChild(model.getRoot(), 1), equalTo(p3));
        assertThat(model.getChild(model.getRoot(), 2), equalTo(p1));
    }

    @Test
    public void canAddSeveralAtPositionSendEventsWithCorrectValue() {
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class);
        Prueba p1 = new Prueba();
        model.add(model.getRoot(), p1);
        final ArrayList<TreeDataEvent> eventsFired = new ArrayList<TreeDataEvent>();
        model.addTreeDataListener(new TreeDataListener() {

            @Override
            public void onChange(TreeDataEvent event) {
                eventsFired.add(event);
            }
        });
        model
                .add(model.getRoot(), 0, Arrays.asList(new Prueba(),
                        new Prueba()));
        TreeDataEvent event = getLast(eventsFired);
        checkIsValid(event, TreeDataEvent.INTERVAL_ADDED, model.getRoot(), 0, 1);
    }

    @Test
    public void addingSeveralGroupsEvents() {
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class);
        Prueba child1 = new Prueba();
        Prueba child2 = new Prueba();
        model.add(model.getRoot(), Arrays.asList(child1, child2));
        assertThat(model.getChildCount(model.getRoot()), equalTo(2));
    }

    @Test
    public void addingAnEmptyListOfElementsDontDoNothing() {
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class);
        final List<TreeDataEvent> events = new ArrayList<TreeDataEvent>();
        model.addTreeDataListener(new TreeDataListener() {

            @Override
            public void onChange(TreeDataEvent event) {
                events.add(event);
            }
        });
        model.add(model.getRoot(), new ArrayList<Prueba>());
        assertThat(events.size(), equalTo(0));
    }

    @Test
    public void childrenCanBeAddedAutomaticallyIfAChildrenExtractorIsProvided() {
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class);
        final Prueba newlyAdded = new Prueba();
        final Prueba child1 = new Prueba();
        final Prueba child2 = new Prueba();
        model.add(model.getRoot(), Collections.singletonList(newlyAdded),
                childrenFor(newlyAdded, child1, child2));
        assertThat(model.getChild(model.getRoot(), 0), equalTo(newlyAdded));
        assertThat(model.getChildCount(newlyAdded), equalTo(2));
    }

    private IChildrenExtractor<Prueba> childrenFor(final Prueba parent,
            final Prueba... children) {
        return new IChildrenExtractor<Prueba>() {

            @Override
            public List<Prueba> getChildren(Prueba p) {
                if (parent == p) {
                    return Arrays.asList(children);
                } else {
                    return Collections.emptyList();
                }
            }
        };
    }

    @Test
    public void whenChildrenAreAutomaticallyAddedOnlySendsEventForTopInsertion() {
        MutableTreeModel<Prueba> model = MutableTreeModel.create(Prueba.class);
        final Prueba newlyAdded = new Prueba();
        final Prueba child1 = new Prueba();
        final Prueba child2 = new Prueba();
        final List<TreeDataEvent> eventsFired = new ArrayList<TreeDataEvent>();
        model.addTreeDataListener(new TreeDataListener() {

            @Override
            public void onChange(TreeDataEvent event) {
                eventsFired.add(event);
            }
        });
        model.add(model.getRoot(), Collections.singletonList(newlyAdded),
                childrenFor(newlyAdded, child1, child2));
        assertThat(eventsFired.size(), equalTo(1));
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

    @Test
    public void aNodeCanBeReplacedByOther() {
        final MutableTreeModel<Prueba> model = MutableTreeModel
                .create(Prueba.class);
        Prueba toRemove = new Prueba();
        Prueba prueba2 = new Prueba();
        Prueba grandChild = new Prueba();
        model.add(model.getRoot(), toRemove);
        model.add(model.getRoot(), prueba2);
        model.add(toRemove, grandChild);
        Prueba substitution = new Prueba();
        model.replace(toRemove, substitution);

        assertThat(model.getChildCount(substitution), equalTo(0));
        assertThat(model.getChild(model.getRoot(), 0), equalTo(substitution));
    }

    @Test
    public void aNodeCanBeMovedDown() {
        final MutableTreeModel<Prueba> model = MutableTreeModel
                .create(Prueba.class);
        Prueba prueba1 = new Prueba();
        model.addToRoot(prueba1);
        Prueba prueba2 = new Prueba();
        model.addToRoot(prueba2);
        Prueba prueba3 = new Prueba();
        model.addToRoot(prueba3);
        model.down(prueba1);
        assertThat(model.getChild(model.getRoot(), 0), equalTo(prueba2));
        assertThat(model.getChild(model.getRoot(), 1), equalTo(prueba1));
        assertThat(model.getChild(model.getRoot(), 2), equalTo(prueba3));
    }

    @Test
    public void aNodeCanBeMovedUp() {
        final MutableTreeModel<Prueba> model = MutableTreeModel
                .create(Prueba.class);
        Prueba prueba1 = new Prueba();
        model.addToRoot(prueba1);
        Prueba prueba2 = new Prueba();
        model.addToRoot(prueba2);
        Prueba prueba3 = new Prueba();
        model.addToRoot(prueba3);
        model.up(prueba2);
        assertThat(model.getChild(model.getRoot(), 0), equalTo(prueba2));
        assertThat(model.getChild(model.getRoot(), 1), equalTo(prueba1));
        assertThat(model.getChild(model.getRoot(), 2), equalTo(prueba3));
    }

    @Test
    public void IfItIsAtTheTopUpDoesNothing() {
        final MutableTreeModel<Prueba> model = MutableTreeModel
                .create(Prueba.class);
        Prueba prueba1 = new Prueba();
        model.addToRoot(prueba1);
        Prueba prueba2 = new Prueba();
        model.addToRoot(prueba2);
        Prueba prueba3 = new Prueba();
        model.addToRoot(prueba3);
        model.up(prueba1);
        assertThat(model.getChild(model.getRoot(), 0), equalTo(prueba1));
        assertThat(model.getChild(model.getRoot(), 1), equalTo(prueba2));
        assertThat(model.getChild(model.getRoot(), 2), equalTo(prueba3));
    }

    @Test
    public void movingUpAndDownSendsRemovalAndAddingEventsSoZKReloadsCorrectlyTheData() {
        final MutableTreeModel<Prueba> model = MutableTreeModel
                .create(Prueba.class);
        Prueba prueba1 = new Prueba();
        model.addToRoot(prueba1);
        Prueba prueba2 = new Prueba();
        model.addToRoot(prueba2);
        Prueba prueba3 = new Prueba();
        model.addToRoot(prueba3);
        final ArrayList<TreeDataEvent> eventsFired = new ArrayList<TreeDataEvent>();
        model.addTreeDataListener(new TreeDataListener() {

            @Override
            public void onChange(TreeDataEvent event) {
                eventsFired.add(event);
            }
        });
        model.up(prueba2);
        checkIsValid(getPreviousToLast(eventsFired),
                TreeDataEvent.INTERVAL_REMOVED, model.getRoot(), 0, 1);
        checkIsValid(getLast(eventsFired), TreeDataEvent.INTERVAL_ADDED,
                model.getRoot(), 0, 1);
        model.down(prueba1);
        checkIsValid(getPreviousToLast(eventsFired),
                TreeDataEvent.INTERVAL_REMOVED, model.getRoot(), 1, 2);
        checkIsValid(getLast(eventsFired), TreeDataEvent.INTERVAL_ADDED,
                model.getRoot(), 1, 2);
    }

    @Test
    public void ifItIsAtTheBottomDownDoesNothing() {
        final MutableTreeModel<Prueba> model = MutableTreeModel
                .create(Prueba.class);
        Prueba prueba1 = new Prueba();
        model.addToRoot(prueba1);
        Prueba prueba2 = new Prueba();
        model.addToRoot(prueba2);
        model.down(prueba2);
        assertThat(model.getChild(model.getRoot(), 1), equalTo(prueba2));
    }

    @Test
    public void canBeKnownIfAnEntityIsOnTheTree() {
        final MutableTreeModel<Prueba> model = MutableTreeModel
                .create(Prueba.class);
        Prueba prueba1 = new Prueba();
        model.addToRoot(prueba1);

        assertTrue(model.contains(prueba1));
        assertTrue(model.contains(model.getRoot()));
        assertFalse(model.contains(new Prueba()));
    }

    @Test
    public void treeParentContainsChild() {
        final MutableTreeModel<Prueba> model = MutableTreeModel
            .create(Prueba.class);

        Prueba parent = new Prueba();
        model.addToRoot(parent);
        Prueba child = new Prueba();
        model.add(parent, child);
        assertTrue(model.contains(parent, child));
    }

    @Test
    public void treeParentDoesNotContainChild() {
        final MutableTreeModel<Prueba> model = MutableTreeModel
            .create(Prueba.class);

        Prueba parent = new Prueba();
        model.addToRoot(parent);
        Prueba child = new Prueba();
        assertFalse(model.contains(parent, child));
    }

    private void checkIsValid(TreeDataEvent event, int type,
            Prueba expectedParent, int expectedPosition) {
        checkIsValid(event, type, expectedParent, expectedPosition,
                expectedPosition);
    }

    private void checkIsValid(TreeDataEvent event, int type,
            Prueba expectedParent, int expectedFromPosition,
            int expectedToPosition) {
        assertEquals(expectedParent, event.getParent());
        assertThat(event.getIndexFrom(), equalTo(expectedFromPosition));
        assertThat(event.getIndexTo(), equalTo(expectedToPosition));
        assertThat(event.getType(), equalTo(type));
    }

    private TreeDataEvent getPreviousToLast(List<TreeDataEvent> list) {
        return list.get(list.size() - 2);
    }

    private TreeDataEvent getLast(List<TreeDataEvent> list) {
        if (list.isEmpty()) {
            throw new RuntimeException("no events");
        }
        return list.get(list.size() - 1);
    }

}
