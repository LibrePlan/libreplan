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

package org.zkoss.ganttz.timetracker;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.Test;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Row;
import org.zkoss.zul.api.Label;

public class OnColumnsRowRendererTest {

    private static class Data {

    }

    private static class CellRenderer implements
            ICellForDetailItemRenderer<DetailItem, Data> {

        @Override
        public Component cellFor(DetailItem item, Data data) {
            return null;
        }

    }

    private static class CellRendererNotInferable<T> implements
            ICellForDetailItemRenderer<DetailItem, T> {

        @Override
        public Component cellFor(DetailItem item, T data) {
            return null;
        }

    }

    private List<DetailItem> detailItems;

    private OnColumnsRowRenderer<DetailItem, Data> rowRenderer;

    private DateTime start;

    private List<Data> data;

    private void givenOnDetailItemsRowRenderer(
            ICellForDetailItemRenderer<DetailItem, Data> cellRenderer) {
        if (detailItems == null) {
            givenDetailItems();
        }
        rowRenderer = OnColumnsRowRenderer.create(Data.class, cellRenderer,
                detailItems);
    }

    private void givenDetailItems() {
        detailItems = new ArrayList<DetailItem>();
        start = new LocalDate(2010, 1, 1).toDateMidnight().toDateTime();
        DateTime current = start;
        Period period = Period.months(2);
        for (int i = 1; i <= 10; i++) {
            DateTime end = current.plus(period);
            DetailItem detail = new DetailItem(200, i + "", current, end);
            current = end;
            detailItems.add(detail);
        }
    }

    private void givenData() {
        data = new ArrayList<Data>();
        data.add(new Data());
        data.add(new Data());
    }

    @Test(expected = IllegalArgumentException.class)
    public void itNeedsNotNullDetailItems() {
        OnColumnsRowRenderer.create(Data.class, createStub(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void itNeedsNotNullCellRenderer() {
        OnColumnsRowRenderer.create(Data.class, null,
                new ArrayList<DetailItem>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void itNeedsTheTypeAsClass() {
        OnColumnsRowRenderer.create(null, createStub(),
                new ArrayList<DetailItem>());
    }

    @Test
    public void itCanHaveEmptyDetailItems() {
        OnColumnsRowRenderer.create(Data.class, createStub(),
                new ArrayList<DetailItem>());
    }

    @Test
    public void itCanInferTheGenericType() {
        OnColumnsRowRenderer.create(new CellRenderer(),
                new ArrayList<DetailItem>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifComesFromRawTypeIsNotInferrable() {
        OnColumnsRowRenderer.create(createStub(),
                new ArrayList<DetailItem>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifItNotShowsTheActualTypeIsNotInferrable() {
        OnColumnsRowRenderer.create(new CellRendererNotInferable<Data>(),
                new ArrayList<DetailItem>());
    }

    @SuppressWarnings("serial")
    @Test(expected = IllegalArgumentException.class)
    public void noDetailItemCanBeNull() {
        OnColumnsRowRenderer.create(Data.class, createStub(),
                new ArrayList<DetailItem>() {
                    {
                        add(new DetailItem(300, "bla"));
                        add(null);
                    }
                });
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantRenderObjectsOfOtherType() {
        givenOnDetailItemsRowRenderer(createStub());
        rowRenderer.render(new Row(), "");
    }

    private ICellForDetailItemRenderer<DetailItem, Data> createStub() {
        return createNiceMock(ICellForDetailItemRenderer.class);
    }

    @Test
    public void theCellRendererIsUsedForEachCell() {
        givenData();
        givenDetailItems();
        ICellForDetailItemRenderer<DetailItem, Data> mock = expectTheCellRendererIsCalledForEachCell();
        givenOnDetailItemsRowRenderer(mock);

        renderingTheData();

        verify(mock);
    }

    private void renderingTheData() {
        for (Data d : data) {
            rowRenderer.render(new Row(), d);
        }
    }

    private ICellForDetailItemRenderer<DetailItem, Data> expectTheCellRendererIsCalledForEachCell() {
        ICellForDetailItemRenderer<DetailItem, Data> mock = createStrictMock(ICellForDetailItemRenderer.class);
        Label labelMock = createNiceMock(Label.class);
        for (Data d : data) {
            for (DetailItem item : detailItems) {
                expect(mock.cellFor(item, d)).andReturn(labelMock);
            }
        }
        replay(mock);
        return mock;
    }

    @Test
    public void theCreatedComponentsAreAddedToTheParents() {
        givenData();
        givenDetailItems();
        ICellForDetailItemRenderer<DetailItem, Data> mock = createMock(ICellForDetailItemRenderer.class);
        Label labelMock = expectTheCreatedLabelIsAddedToTheRow(mock);
        givenOnDetailItemsRowRenderer(mock);

        renderingTheData();

        verify(labelMock);
    }

    private Label expectTheCreatedLabelIsAddedToTheRow(
            ICellForDetailItemRenderer<DetailItem, Data> mock) {
        Label labelMock = createStrictMock(Label.class);
        for (Data d : data) {
            for (DetailItem item : detailItems) {
                expect(mock.cellFor(isA(DetailItem.class), isA(Data.class)))
                        .andReturn(labelMock);
                labelMock.setParent(isA(Row.class));
            }
        }
        replay(mock, labelMock);
        return labelMock;
    }

}
