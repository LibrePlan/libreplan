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
package org.navalplanner.business.util.deepcopy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.Each.each;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.util.deepcopy.EntityExamples.EntityA;
import org.navalplanner.business.util.deepcopy.EntityExamples.EntityWithoutNoArgsConstructor;
import org.navalplanner.business.util.deepcopy.EntityExamples.Parent;
import org.navalplanner.business.util.deepcopy.EntityExamples.SubClassExample;
import org.navalplanner.business.util.deepcopy.EntityExamples.TestEnum;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class DeepCopyTest {

    @Test
    public void theCopyOfANullObjectIsANullObject() {
        assertThat(new DeepCopy().copy(null), equalTo(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theEntityToCopyMustHaveNotEmptyConstructor() {
        EntityWithoutNoArgsConstructor entity = new EntityWithoutNoArgsConstructor(
                "bla");
        new DeepCopy().copy(entity);
    }

    @Test
    public void stringPropertiesAreShared() {
        EntityA entityA = new EntityA();
        entityA.setStringProperty("foo");
        EntityA copy = new DeepCopy().copy(entityA);
        assertSame(entityA.getStringProperty(), copy.getStringProperty());
    }

    @Test
    public void intPropertiesAreShared() {
        EntityA entityA = new EntityA();
        EntityA copy = new DeepCopy().copy(entityA);
        assertEquals(entityA.getIntProperty(), copy.getIntProperty());
    }

    @Test
    public void nullPropertiesKeepBeingNull() {
        EntityA entityA = new EntityA();
        EntityA copy = new DeepCopy().copy(entityA);
        assertThat(copy.getNullProperty(), nullValue());
    }

    @Test
    public void enumPropertiesAreShared() {
        EntityA entityA = new EntityA();
        entityA.setEnumProperty(TestEnum.A);
        EntityA copy = new DeepCopy().copy(entityA);
        assertSame(TestEnum.A, copy.getEnumProperty());
    }

    @Test
    public void itKnowsSomeTypesAreImmutable() {
        Iterable<Class<?>> immutableTypes = Arrays.<Class<?>> asList(
                String.class, BigDecimal.class, Double.class, Float.class,
                Integer.class, Short.class, Byte.class, Character.class,
                LocalDate.class, Boolean.class, DateTime.class, double.class,
                float.class, int.class, short.class, byte.class, char.class);
        assertThat(immutableTypes, each(immutable()));
    }

    private Matcher<Class<?>> immutable() {
        return new BaseMatcher<Class<?>>() {

            @Override
            public boolean matches(Object value) {
                return DeepCopy.isImmutableType((Class<?>) value);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is immutable");
            }
        };
    }

    @Test
    public void itCopiesInheritedPropertiesToo() {
        SubClassExample subClassExample = new SubClassExample();
        subClassExample.setSuperClassStringProperty("foo");
        SubClassExample copy = new DeepCopy().copy(subClassExample);
        assertThat(copy.getSuperClassStringProperty(), equalTo("foo"));
    }

    @Test
    public void datesAreCopied() {
        EntityA entityA = new EntityA();
        Date originalDateValue = new Date();
        entityA.setDate(originalDateValue);
        EntityA copy = new DeepCopy().copy(entityA);
        assertThat(copy.getDate(), equalTo(originalDateValue));
        assertNotSame(originalDateValue, copy.getDate());
    }

    @Test
    public void setsAreCopied() {
        EntityA entityA = new EntityA();
        HashSet<Object> originalSet = new HashSet<Object>(Arrays.asList("test",
                2, 3, new Date()));
        entityA.setSetProperty(originalSet);
        EntityA copy = new DeepCopy().copy(entityA);
        assertEquals(originalSet, copy.getSetProperty());
        assertNotSame(originalSet, copy.getSetProperty());
    }

    @Test
    public void theSetImplementationClassIsPreservedIfPossible() {
        EntityA entityA = new EntityA();
        Set<Object> originalSet = new LinkedHashSet<Object>(Arrays.asList(
                "test", 2, 3, new Date()));
        entityA.setSetProperty(originalSet);
        EntityA copy = new DeepCopy().copy(entityA);
        assertThat(copy.getSetProperty(), is(LinkedHashSet.class));
    }

    @Test
    public void setsInsideSetsAreRecursivelyCopiedWithoutProblem() {
        EntityA entityA = new EntityA();
        HashSet<Object> innerSet = new HashSet<Object>(Arrays.asList("bla", 3));
        HashSet<Object> originalSet = new HashSet<Object>(Arrays.asList("test",
                2, 3, new Date(), innerSet));
        entityA.setSetProperty(originalSet);
        EntityA copy = new DeepCopy().copy(entityA);
        assertEquals(originalSet, copy.getSetProperty());
        assertNotSame(originalSet, copy.getSetProperty());
    }

    @Test
    public void mapsAreCopied() {
        EntityA entityA = new EntityA();
        HashMap<Object, Object> originalMap = new HashMap<Object, Object>();
        originalMap.put("aa", "blabla");
        entityA.setMapProperty(originalMap);
        EntityA copy = new DeepCopy().copy(entityA);
        assertEquals(originalMap, copy.getMapProperty());
        assertNotSame(originalMap, copy.getMapProperty());
    }

    @Test
    public void mapImplementationIsPreservedIfPossible() {
        EntityA entityA = new EntityA();
        LinkedHashMap<Object, Object> mapProperty = new LinkedHashMap<Object, Object>();
        mapProperty.put("ab", "abc");
        entityA.setMapProperty(mapProperty);
        EntityA copy = new DeepCopy().copy(entityA);
        assertThat(copy.getMapProperty(), is(LinkedHashMap.class));
    }

    @Test
    public void listsAreCopied() {
        EntityA entityA = new EntityA();
        ArrayList<Object> originalList = new ArrayList<Object>();
        originalList.add(2);
        originalList.add(10);
        originalList.add("abla");
        entityA.setListProperty(originalList);
        EntityA copy = new DeepCopy().copy(entityA);
        assertEquals(originalList, copy.getListProperty());
        assertNotSame(originalList, copy.getListProperty());
    }

    @Test
    public void listImplementationIsPreservedIfPossible() {
        EntityA entityA = new EntityA();
        LinkedList<Object> originalList = new LinkedList<Object>();
        originalList.add(2);
        entityA.setListProperty(originalList);
        EntityA copy = new DeepCopy().copy(entityA);
        assertThat(copy.getListProperty(), is(LinkedList.class));
    }

    @Test
    public void ignoredFieldsAreNotCopied() {
        EntityA entityA = new EntityA();
        entityA.setIgnoredProperty("blabla");
        EntityA copy = new DeepCopy().copy(entityA);
        assertThat(copy.getIgnoredProperty(), nullValue());
    }

    @Test
    public void sharedFieldsAreCopiedWithTheSameReference() {
        EntityA entityA = new EntityA();
        Date originalDate = new Date();
        entityA.setSharedProperty(originalDate);
        EntityA copy = new DeepCopy().copy(entityA);
        assertSame(originalDate, copy.getSharedProperty());
    }

    @Test
    public void sharedCollectionsAreCopiedWithTheSameReference() {
        EntityA entityA = new EntityA();
        List<String> originalList = Arrays.asList("bla");
        entityA.setSharedListProperty(originalList);
        EntityA copy = new DeepCopy().copy(entityA);
        assertSame(originalList, copy.getSharedListProperty());
    }

    @Test
    public void sharedCollectionElementsKeptTheReferences() {
        EntityA entityA = new EntityA();
        HashSet<Object> originalSet = new HashSet<Object>();
        originalSet.add(new Date());
        entityA.setSharedElementsProperty(originalSet);
        EntityA copy = new DeepCopy().copy(entityA);
        assertNotSame(originalSet, copy.getSharedElementsProperty());
        assertSame(originalSet.iterator().next(), copy
                .getSharedElementsProperty().iterator().next());
    }

    @Test
    public void sharedKeyElementsKeepTheSameReferencesForTheKeys() {
        EntityA entityA = new EntityA();
        Map<Object, Object> originalMap = new HashMap<Object, Object>();
        EntityA originalValue = new EntityA();
        Date originalKey = new Date();
        originalMap.put(originalKey, originalValue);
        entityA.setSharedKeysMapProperty(originalMap);
        EntityA copy = new DeepCopy().copy(entityA);
        Map<Object, Object> sharedKeysMapProperty = copy
                .getSharedKeysMapProperty();
        assertSame(originalKey, sharedKeysMapProperty.keySet().iterator()
                .next());
        assertNotSame(originalValue, sharedKeysMapProperty.values().iterator()
                .next());
    }

    @Test
    public void sharedValueElementsKeepTheSameReferencesForTheValues() {
        EntityA entityA = new EntityA();
        Map<Object, Object> originalMap = new HashMap<Object, Object>();
        EntityA originalValue = new EntityA();
        Date originalKey = new Date();
        originalMap.put(originalKey, originalValue);
        entityA.setSharedValuesMapProperty(originalMap);
        EntityA copy = new DeepCopy().copy(entityA);
        Map<Object, Object> copiedMap = copy
                .getSharedValuesMapProperty();
        assertNotSame(originalKey, copiedMap.keySet().iterator()
                .next());
        assertSame(originalValue, copiedMap.values().iterator()
                .next());
    }

    @Test
    public void aSharedCollectionElementsMapKeepTheSameReferencesForTheKeysAndTheValues() {
        EntityA entityA = new EntityA();
        Map<Object, Object> originalMap = new HashMap<Object, Object>();
        EntityA originalValue = new EntityA();
        Date originalKey = new Date();
        originalMap.put(originalKey, originalValue);
        entityA.setSharedCollectionElementsMapProperty(originalMap);
        EntityA copy = new DeepCopy().copy(entityA);
        Map<Object, Object> copiedMap = copy
                .getSharedCollectionElementsMapProperty();
        assertSame(originalKey, copiedMap.keySet().iterator()
                .next());
        assertSame(originalValue, copiedMap.values().iterator()
                .next());
    }

    @Test
    public void ifNotInnmutableNorCustomCopyRecursivelyCopiesIt() {
        Parent parent = new Parent();
        EntityA entityAProperty = new EntityA();
        Date originalDate = new Date();
        entityAProperty.setDate(originalDate);
        parent.setEntityAProperty(entityAProperty);
        Parent copy = new DeepCopy().copy(parent);
        assertNotSame(parent, copy);
        assertThat(copy.getEntityAProperty().getDate(), equalTo(originalDate));
        assertNotSame(copy.getEntityAProperty().getDate(), originalDate);
    }

    @Test
    public void alreadyCopiedInstancesAreReused() {
        Parent parent = new Parent();
        EntityA entityA = new EntityA();
        parent.setEntityAProperty(entityA);
        entityA.setParentProperty(parent);
        Parent copy = new DeepCopy().copy(parent);
        assertSame(copy, copy.getEntityAProperty().getParentProperty());
    }

    @Test
    public void alreadyCopiedSetsAreReused() {
        Parent parent = new Parent();
        EntityA entityA = new EntityA();
        parent.setEntityAProperty(entityA);
        entityA.setParentProperty(parent);
        HashSet<Object> originalSet = new HashSet<Object>();
        parent.setSetProperty(originalSet);
        entityA.setSetProperty(originalSet);
        Parent copy = new DeepCopy().copy(parent);
        assertSame(copy.getSetProperty(), copy.getEntityAProperty()
                .getSetProperty());
    }

    @Test
    public void canSpecifyReplacements() {
        DeepCopy deepCopy = new DeepCopy();
        Parent parent = new Parent();
        EntityA entityA = new EntityA();
        parent.setEntityAProperty(entityA);
        EntityA anotherEntity = new EntityA();
        deepCopy.replace(entityA, anotherEntity);
        Parent copy = deepCopy.copy(parent);
        assertSame(copy.getEntityAProperty(), anotherEntity);
    }

    @Test
    public void afterCopyHooksCanBeDefined() {
        DeepCopy deepCopy = new DeepCopy();
        EntityA entityA = new EntityA();
        EntityA copy = deepCopy.copy(entityA);
        assertTrue(copy.isFirstHookCalled());
        assertTrue(copy.isSecondHookCalled());
    }

    @Test
    public void superclassAfterHooksAreCalled() {
        DeepCopy deepCopy = new DeepCopy();
        SubClassExample subclass = deepCopy.copy(new SubClassExample());
        assertTrue(subclass.isAfterCopyHookCalled());
    }

    @Test
    public void equalObjectsButDifferentAreNotReused() {
        EntityA entityA = new EntityA();
        DeepCopy deepCopy = new DeepCopy();
        entityA.setSet1(new HashSet<Object>());
        entityA.setSet2(new HashSet<Object>());
        EntityA copied = deepCopy.copy(entityA);
        assertNotSame(copied.getSet1(), copied.getSet2());
    }

}
