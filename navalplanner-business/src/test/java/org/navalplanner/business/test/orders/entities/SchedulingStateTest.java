/*
 * This file is part of NavalPlan
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
package org.navalplanner.business.test.orders.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.each;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.navalplanner.business.orders.entities.SchedulingState;
import org.navalplanner.business.orders.entities.SchedulingState.ITypeChangedListener;
import org.navalplanner.business.orders.entities.SchedulingState.Type;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class SchedulingStateTest {

    private SchedulingState root;

    private SchedulingState childA;

    private SchedulingState childB;

    private SchedulingState grandChildA1;

    private SchedulingState grandChildA2;

    private SchedulingState grandChildB1;

    private SchedulingState grandChildB2;

    @Before
    public void setUp() {
        root = new SchedulingState();
        root.add(childA = new SchedulingState());
        childA.add(grandChildA1 = new SchedulingState());
        childA.add(grandChildA2 = new SchedulingState());
        root.add(childB = new SchedulingState());
        childB.add(grandChildB1 = new SchedulingState());
        childB.add(grandChildB2 = new SchedulingState());
    }

    private List<SchedulingState> all(){
        return Arrays.asList(root,childA, childB, grandChildA1, grandChildA2,
                grandChildB1, grandChildB2);
    }

    private List<SchedulingState> allRootDescendants() {
        return Arrays.asList(childA, childB, grandChildA1, grandChildA2,
                grandChildB1, grandChildB2);
    }

    @Test
    public void aNewlyCreatedSchedulingStateIsNoScheduled() {
        SchedulingState schedulingState = new SchedulingState();
        assertThat(schedulingState.getType(),
                equalTo(Type.NO_SCHEDULED));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateASchedulingStateWithChildrenAlreadyAssigned() {
        new SchedulingState(Type.NO_SCHEDULED, Arrays.asList(childA));
    }

    @Test
    public void anAddedSchedulingStateHasAParent() {
        assertThat(childA.getParent(), equalTo(root));
    }

    @Test
    public void theRootOfASchedulingStateTreeHasNoParent() {
        assertNull(root.getParent());
    }

    @Test
    public void ifHasNoParentItsRoot() {
        assertTrue(root.isRoot());
    }

    @Test
    public void whenSchedulingAElementItTursIntoASchedulingPoint() {
        grandChildA1.schedule();
        assertThat(grandChildA1.getType(), equalTo(Type.SCHEDULING_POINT));
    }

    @Test
    public void whenChangingTheTypeItsNotified() {
        final boolean typeChanged[] = { false };
        childA.addTypeChangeListener(new ITypeChangedListener() {

            @Override
            public void typeChanged(Type newType) {
                typeChanged[0] = true;
            }
        });
        childA.schedule();
        assertTrue(typeChanged[0]);
    }

    @Test
    public void afterRemovingTheListenerItsNotNotified() {
        ITypeChangedListener listener = new ITypeChangedListener() {

            @Override
            public void typeChanged(Type newType) {
                fail("the listener shouldn't be called since it's removed");
            }
        };
        childA.addTypeChangeListener(listener);
        childA.removeTypeChangeListener(listener);
        childA.schedule();
    }

    @Test
    public void whenSchedulingAElementItTurnsAllItsDescendantsIntoScheduledSubelements() {
        root.schedule();
        assertThat(allRootDescendants(),
                each(hasType(Type.SCHEDULED_SUBELEMENT)));
    }

    @Test
    public void aScheduledElementIsCompletelyScheduled() {
        root.schedule();
        assertThat(all(), each(completelyScheduled()));
    }

    @Test
    public void aSomewhatScheduledElemenetCannotBeScheduled() {
        grandChildA1.schedule();
        grandChildB1.schedule();
        for (SchedulingState schedulingState : all()) {
            if (schedulingState == grandChildA2
                    || schedulingState == grandChildB2) {
                // can be scheduled
                continue;
            }
            try {
                schedulingState.schedule();
                fail("must send " + IllegalStateException.class);
            } catch (IllegalStateException e) {
                // ok
            }
        }
    }

    @Test
    public void scheduledSubelementsCantBeScheduled() {
        root.schedule();
        assertThat(allRootDescendants(), each(not(canBeScheduled())));
    }

    @Test
    public void aNoScheduledElementCanBeScheduled() {
        assertThat(all(), each(canBeScheduled()));
    }

    @Test
    public void aSchedulingPointCantBeScheduled() {
        root.schedule();
        assertFalse(root.canBeScheduled());
    }

    @Test
    public void rootIsPartiallyScheduledWhenSchedulingOneOfTheChildren() {
        childA.schedule();
        assertThat(root, hasType(Type.PARTIALY_SCHEDULED_SUPERELEMENT));
    }

    @Test
    public void rootIsCompletelyScheduledWhenSchedulingAllOfTheChildren() {
        childA.schedule();
        childB.schedule();
        assertThat(root, hasType(Type.COMPLETELY_SCHEDULED_SUPERELEMENT));
    }

    @Test
    public void whenSchedulingAGrandChildrenTheRootIsPartiallyScheduled() {
        grandChildA1.schedule();
        assertThat(root, hasType(Type.PARTIALY_SCHEDULED_SUPERELEMENT));
    }

    @Test
    public void ifSchedulingAllGrandChildrenTheRootIsCompletelyScheduled() {
        grandChildA1.schedule();
        grandChildA2.schedule();
        grandChildB1.schedule();
        grandChildB2.schedule();
        assertThat(root, hasType(Type.COMPLETELY_SCHEDULED_SUPERELEMENT));
    }

    @Test
    public void addingANewChildToACompletelyScheduled() {
        childA.schedule();
        childB.schedule();
        root.add(new SchedulingState());
        assertThat(root, hasType(Type.PARTIALY_SCHEDULED_SUPERELEMENT));
    }

    @Test
    public void removingTheOnlyNoScheduled() {
        childA.schedule();
        root.removeChild(childB);
        assertThat(root, hasType(Type.COMPLETELY_SCHEDULED_SUPERELEMENT));
    }

    @Test
    public void removingAChildMakesItHasNoParent() {
        childA.schedule();
        root.removeChild(childB);
        assertThat(childB.getParent(), nullValue());
    }

    @Test
    public void aNotScheduledElementCantBeUnScheduled(){
        assertFalse(root.canBeUnscheduled());
    }

    @Test
    public void aCompletelyScheduledElementCanBeUnScheduled() {
        root.schedule();
        assertTrue(root.canBeUnscheduled());
    }

    @Test
    public void callingUnscheduleIfYouCantScheduleThrowsException() {
        for (SchedulingState each : all()) {
            try {
                each.unschedule();
                fail("unscheduling " + each + " must send exception");
            } catch (IllegalStateException e) {
                // ok
            }
        }
    }

    @Test
    public void scheduledSubelementsCantBeUnScheduled() {
        root.schedule();
        assertThat(allRootDescendants(), each(not(canBeUnsheduled())));
    }

    @Test
    public void afterUnschedulingAllDescendantsAreNoScheduled() {
        root.schedule();
        root.unschedule();
        assertThat(allRootDescendants(), each(hasType(Type.NO_SCHEDULED)));
    }

    @Test
    public void afterUnSchedulingItsNotScheduled() {
        root.schedule();
        root.unschedule();
        assertThat(root, hasType(Type.NO_SCHEDULED));
    }

    @Test
    public void theChangeOfTypeIsNotified() {
        root.schedule();
        final boolean[] typeChanged = { false };
        childA.addTypeChangeListener(new ITypeChangedListener() {
            @Override
            public void typeChanged(Type newType) {
                typeChanged[0] = true;
            }
        });
        root.unschedule();
        assertTrue(typeChanged[0]);
    }

    @Test
    public void addingAChildrenThatAlreadyHasBeenAddedIsIgnored() {
        childA.add(grandChildA1);
        assertThat(childA.getChildrenNumber(), equalTo(2));
    }

    @Test
    public void removingAllTheChildrenOfACompletelyScheduledSuperelementMakesItNoScheduled() {
        childA.schedule();
        childB.schedule();
        root.removeChild(childA);
        root.removeChild(childB);
        assertThat(root, hasType(Type.NO_SCHEDULED));
    }

    @Test
    public void addingAChildToASchedulingPointMakesItAScheduledSubelementAndAllItsDescendants() {
        childA.schedule();
        SchedulingState newChild = new SchedulingState();
        SchedulingState grandChild = new SchedulingState();
        newChild.add(grandChild);
        childA.add(newChild);
        assertThat(newChild, hasType(Type.SCHEDULED_SUBELEMENT));
        assertThat(grandChild, hasType(Type.SCHEDULED_SUBELEMENT));
    }

    @Test
    public void addingAChildToAScheduledSubelementMakesItAScheduledSubelementAndAllItsDescendants() {
        childA.schedule();
        SchedulingState newChild = new SchedulingState();
        SchedulingState grandChild = new SchedulingState();
        newChild.add(grandChild);
        grandChildA1.add(newChild);
        assertThat(newChild, hasType(Type.SCHEDULED_SUBELEMENT));
        assertThat(grandChild, hasType(Type.SCHEDULED_SUBELEMENT));
    }

    @Test
    public void movingAScheduledSubelementToAScheduledSuperElementMakesItNoScheduled() {
        grandChildA1.schedule();
        grandChildA2.schedule();
        childB.schedule();
        childB.removeChild(grandChildB1);
        childA.add(grandChildB1);
        assertThat(grandChildB1, hasType(Type.NO_SCHEDULED));
    }

    abstract static class SchedulingStateMatcher extends
            BaseMatcher<SchedulingState> {
        @Override
        public boolean matches(Object object) {
            if (object instanceof SchedulingState) {
                return matches((SchedulingState) object);
            } else {
                return false;
            }
        }

        protected abstract boolean matches(SchedulingState schedulingState);
    }

    private Matcher<SchedulingState> hasType(final Type type) {
        return new SchedulingStateMatcher() {

            @Override
            public boolean matches(SchedulingState state) {
                return state.getType() == type;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("the type of the SchedulingState must be: "
                                + type);
            }
        };
    }

    private Matcher<SchedulingState> completelyScheduled() {
        return new SchedulingStateMatcher() {

            @Override
            public void describeTo(Description description) {
                description.appendText("completely scheduled");
            }

            @Override
            protected boolean matches(SchedulingState schedulingState) {
                return schedulingState.isCompletelyScheduled();
            }
        };
    }

    private Matcher<SchedulingState> canBeScheduled() {
        return new SchedulingStateMatcher() {

            @Override
            public boolean matches(SchedulingState state) {
                return state.canBeScheduled();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("can be scheduled");
            }
        };
    }

    private Matcher<SchedulingState> canBeUnsheduled() {
        return new SchedulingStateMatcher() {

            @Override
            protected boolean matches(SchedulingState schedulingState) {
                return schedulingState.canBeUnscheduled();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("cannot be scheduled");
            }
        };
    }

}
