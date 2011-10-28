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

package org.libreplan.business.util;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

public class ListSorterTest {

    private ListSorter<DumbContainer> listSorter;
    private List<DumbContainer> initialElements;

    private void givenSortedList() {
        initialElements = containers("A", "B", "C");
        listSorter = ListSorter.create(initialElements);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theInitialElementsMustBeNotNull() {
        ListSorter.<String> create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theInitialElementsDontHaveNullElements() {
        ListSorter.create(asList("A", "C", null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theComparatorCannotBeNull() {
        ListSorter.create(Arrays.asList("A"), null);
    }

    @Test
    public void itSortsInitialElements() {
        givenSortedList();
        assertThat(listSorter.toListView(), equalTo(containers("A", "B", "C")));
    }

    private static List<DumbContainer> containers(String... strings) {
        ArrayList<DumbContainer> result = new ArrayList<DumbContainer>();
        for (String s : strings) {
            result.add(new DumbContainer(s));
        }
        return result;
    }

    @Test(expected = UnsupportedOperationException.class)
    public void theListReturnedCannotBeModified() {
        givenSortedList();
        listSorter.toListView().clear();
    }

    @Test
    public void theListKeepsTrackOfChanges() {
        givenSortedList();
        List<DumbContainer> listView = listSorter.toListView();
        listSorter.add(new DumbContainer("D"));
        assertThat(listView, equalTo(containers("A", "B", "C", "D")));
    }

    @Test
    public void ifModifiedTheElementIsRepositioned() {
        givenSortedList();
        DumbContainer first = initialElements.get(0);
        first.setData("D");
        listSorter.modified(first);
        assertThat(listSorter.toListView(), equalTo(containers("B", "C", "D")));
    }

    @Test(expected = NoSuchElementException.class)
    public void modifiedCanOnlyBeCalledWithElementsContained() {
        givenSortedList();
        listSorter.modified(new DumbContainer("B"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void anAlreadyAddedCannotBeAdded() {
        givenSortedList();
        listSorter.add(initialElements.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullElementCannotBeAdded() {
        givenSortedList();
        listSorter.add(null);
    }

    @Test
    public void addInsertsAtRightPosition() {
        givenSortedList();
        listSorter.add(new DumbContainer("0"));
        assertThat(listSorter.toListView(), equalTo(containers("0", "A", "B", "C")));
    }

    @Test(expected = NoSuchElementException.class)
    public void cantRemoveEqualButNotSameInstance() {
        givenSortedList();
        listSorter.remove(new DumbContainer("A"));
    }

    @Test
    public void removingExistent() {
        givenSortedList();
        listSorter.remove(initialElements.get(0));
        assertThat(listSorter.toListView(), equalTo(containers("B", "C")));
    }

}

class DumbContainer implements Comparable<DumbContainer> {

    private String data;

    public DumbContainer(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public int compareTo(DumbContainer o) {
        return this.data.compareTo(o.data);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DumbContainer) {
            DumbContainer dumb = (DumbContainer) obj;
            return dumb.data.equals(this.data);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.data.hashCode();
    }
}
