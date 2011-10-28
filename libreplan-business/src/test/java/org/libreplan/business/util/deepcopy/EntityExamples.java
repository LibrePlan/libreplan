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
package org.libreplan.business.util.deepcopy;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class EntityExamples {

    private EntityExamples() {
    }

    public enum TestEnum {
        A {
        },
        B;
    }

    public static class Parent {
        private EntityA entityAProperty;

        private String prueba = "bar";

        private Set<Object> setProperty = new HashSet<Object>();

        public EntityA getEntityAProperty() {
            return entityAProperty;
        }

        public void setEntityAProperty(EntityA entityAProperty) {
            this.entityAProperty = entityAProperty;
        }

        public Set<Object> getSetProperty() {
            return setProperty;
        }

        public void setSetProperty(Set<Object> setProperty) {
            this.setProperty = setProperty;
        }

    }

    public static class EntityA {

        private static final String staticProperty = "foo";

        private final String finalProperty = "bar";
        private String stringProperty;

        private int intProperty = 2;

        private Object nullProperty = null;

        private Date date = null;

        private Set<Object> setProperty;

        private Map<Object, Object> mapProperty = new HashMap<Object, Object>();

        private List<Object> listProperty;

        private TestEnum enumProperty = null;

        @OnCopy(Strategy.IGNORE)
        private String ignoredProperty;

        @OnCopy(Strategy.SHARE)
        private Date sharedProperty;

        @OnCopy(Strategy.SHARE)
        private List<String> sharedListProperty;

        @OnCopy(Strategy.SHARE_COLLECTION_ELEMENTS)
        private Set<Object> sharedElementsProperty;

        @OnCopy(Strategy.ONLY_SHARE_KEYS)
        private Map<Object, Object> sharedKeysMapProperty;

        @OnCopy(Strategy.ONLY_SHARE_VALUES)
        private Map<Object, Object> sharedValuesMapProperty;

        @OnCopy(Strategy.SHARE_COLLECTION_ELEMENTS)
        private Map<Object, Object> sharedCollectionElementsMapProperty;

        private Parent parentProperty;

        private boolean firstHookCalled = false;

        private boolean secondHookCalled = false;

        private Set<Object> set1;

        private Set<Object> set2;

        public String getStringProperty() {
            return stringProperty;
        }

        public void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public int getIntProperty() {
            return intProperty;
        }

        public void setIntProperty(int intProperty) {
            this.intProperty = intProperty;
        }

        public Object getNullProperty() {
            return nullProperty;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Set<Object> getSetProperty() {
            return setProperty;
        }

        public void setSetProperty(Set<Object> setProperty) {
            this.setProperty = setProperty;
        }

        public Map<Object, Object> getMapProperty() {
            return mapProperty;
        }

        public void setMapProperty(Map<Object, Object> mapProperty) {
            this.mapProperty = mapProperty;
        }

        public List<Object> getListProperty() {
            return listProperty;
        }

        public void setListProperty(List<Object> listProperty) {
            this.listProperty = listProperty;
        }

        public void setIgnoredProperty(String ignoredProperty) {
            this.ignoredProperty = ignoredProperty;
        }

        public String getIgnoredProperty() {
            return ignoredProperty;
        }

        public Date getSharedProperty() {
            return sharedProperty;
        }

        public void setSharedProperty(Date sharedProperty) {
            this.sharedProperty = sharedProperty;
        }

        public List<String> getSharedListProperty() {
            return sharedListProperty;
        }

        public void setSharedListProperty(List<String> sharedListProperty) {
            this.sharedListProperty = sharedListProperty;
        }

        public Set<Object> getSharedElementsProperty() {
            return sharedElementsProperty;
        }

        public void setSharedElementsProperty(Set<Object> sharedElementsProperty) {
            this.sharedElementsProperty = sharedElementsProperty;
        }

        public Map<Object, Object> getSharedKeysMapProperty() {
            return sharedKeysMapProperty;
        }

        public void setSharedKeysMapProperty(
                Map<Object, Object> sharedKeysMapProperty) {
            this.sharedKeysMapProperty = sharedKeysMapProperty;
        }

        public void setSharedValuesMapProperty(Map<Object, Object> originalMap) {
            this.sharedValuesMapProperty = originalMap;
        }

        public Map<Object, Object> getSharedValuesMapProperty() {
            return sharedValuesMapProperty;
        }

        public void setSharedCollectionElementsMapProperty(Map<Object, Object> map) {
            this.sharedCollectionElementsMapProperty = map;
        }

        public Map<Object, Object> getSharedCollectionElementsMapProperty() {
            return sharedCollectionElementsMapProperty;
        }

        public Parent getParentProperty() {
            return parentProperty;
        }

        public void setParentProperty(Parent parentProperty) {
            this.parentProperty = parentProperty;
        }

        public TestEnum getEnumProperty() {
            return enumProperty;
        }

        public void setEnumProperty(TestEnum enumProperty) {
            this.enumProperty = enumProperty;
        }

        public String getFinalProperty() {
            return finalProperty;
        }

        @AfterCopy
        private void firstCopyHook() {
            firstHookCalled = true;
        }

        @AfterCopy
        private void secondCopyHook() {
            secondHookCalled = true;
        }

        public boolean isFirstHookCalled() {
            return firstHookCalled;
        }

        public boolean isSecondHookCalled() {
            return secondHookCalled;
        }

        public Set<Object> getSet1() {
            return set1;
        }

        public void setSet1(Set<Object> set1) {
            this.set1 = set1;
        }

        public Set<Object> getSet2() {
            return set2;
        }

        public void setSet2(Set<Object> set2) {
            this.set2 = set2;
        }

    }

    public static class EntityWithoutNoArgsConstructor {
        public EntityWithoutNoArgsConstructor(String arg) {
        }
    }

    public static class SuperclassExample {
        private String superClassStringProperty;

        private boolean afterCopyHookCalled = false;

        public String getSuperClassStringProperty() {
            return superClassStringProperty;
        }

        public void setSuperClassStringProperty(String superClassStringProperty) {
            this.superClassStringProperty = superClassStringProperty;
        }

        @AfterCopy
        private void afterCopy() {
            afterCopyHookCalled = true;
        }

        public boolean isAfterCopyHookCalled() {
            return afterCopyHookCalled;
        }
    }

    public static class SubClassExample extends SuperclassExample {

        private int intProperty;

    }

}
