/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
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

package org.zkoss.ganttz.timetracker.zoom;

import java.util.Collection;
import java.util.Vector;

import org.joda.time.DateTime;
import org.zkoss.ganttz.util.Interval;

/**
 * Zoom level with years in the first level and semesters in the second level
 * @author Francisco Javier Moran Rúa
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DetailOneTimeTrackerState extends TimeTrackerState {

    private static final int FIRST_LEVEL_ITEM_SIZE = 200;
    private static final int SECOND_LEVEL_ITEM_SIZE = 100;

    public final double daysPerPixel() {
        return ((double) 365 / FIRST_LEVEL_ITEM_SIZE);
    }

    DetailOneTimeTrackerState(IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        super(firstLevelModificator, secondLevelModificator);
    }

    private Collection<DetailItem> buildCollectionDetailsFirstLevel(
            int initialYear, int endYear) {

        Collection<DetailItem> detailsVector = new Vector<DetailItem>();

        for (int i = initialYear; i <= endYear; i++) {
            DetailItem d = new DetailItem(FIRST_LEVEL_ITEM_SIZE, String
                    .valueOf(i), new DateTime(i, 1, 1, 0, 0, 0, 0),
                    new DateTime(i, 12, 31, 0, 0, 0, 0));
            detailsVector.add(d);
        }

        return detailsVector;
    }

    /**
     * Creates secondary level DetailItems, adding currentDay tag to the
     * proper interval (Bank holidays function call pending).
     */
    private Collection<DetailItem> buildCollectionDetailsSecondLevel(
            int initialYear, int endYear) {

        Collection<DetailItem> detailsVector = new Vector<DetailItem>();

        DateTime beginDate = new DateTime(initialYear, 1, 1, 0, 0, 0, 0);
        DateTime endDate = new DateTime(initialYear, 7, 1, 0, 0, 0, 0);

        for (int i = initialYear; i <= endYear; i++) {

            DetailItem d1 = new DetailItem(SECOND_LEVEL_ITEM_SIZE, "H1",
                    beginDate, endDate);
            DetailItem d2 = new DetailItem(SECOND_LEVEL_ITEM_SIZE, "H2",
                    endDate, endDate.plusMonths(6));

            detailsVector.add(d1);
            detailsVector.add(d2);

            beginDate = beginDate.plusYears(1);
            endDate = endDate.plusYears(1);

        }

        return detailsVector;
    }

    @Override
    protected Collection<DetailItem> createDetailsForFirstLevel(
            Interval interval) {
        int[] pairYears = calculateInitialEndYear(interval.getStart(), interval
                .getFinish());
        return buildCollectionDetailsFirstLevel(pairYears[0], pairYears[1]);

    }

    @Override
    protected Collection<DetailItem> createDetailsForSecondLevel(
            Interval interval) {
        int[] pairYears = calculateInitialEndYear(interval.getStart(), interval
                .getFinish());
        return buildCollectionDetailsSecondLevel(pairYears[0], pairYears[1]);
    }

    public Interval getRealIntervalFor(Interval interval) {
        int[] pairYears = calculateInitialEndYear(interval.getStart(), interval
                .getFinish());
        return new Interval(year(pairYears[0]), year(pairYears[1] + 1));
    }

    @Override
    protected ZoomLevel getZoomLevel() {
        return ZoomLevel.DETAIL_ONE;
    }

    @Override
    public int getSecondLevelSize() {
        return SECOND_LEVEL_ITEM_SIZE;
    }

}
