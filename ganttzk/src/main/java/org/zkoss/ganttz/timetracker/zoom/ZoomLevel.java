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

package org.zkoss.ganttz.timetracker.zoom;

import static org.zkoss.ganttz.i18n.I18nHelper._;

/**
 * @author Francisco Javier Moran Rúa
 */
public enum ZoomLevel {

    DETAIL_ONE(_("Year")) {
        @Override
        public TimeTrackerState getTimeTrackerState(
                IDetailItemModificator firstLevel,
                IDetailItemModificator secondLevel) {
            return new DetailOneTimeTrackerState(firstLevel, secondLevel);
        }
    },
    DETAIL_TWO(_("Quarter")) {
        @Override
        public TimeTrackerState getTimeTrackerState(
                IDetailItemModificator firstLevel,
                IDetailItemModificator secondLevel) {
            return new DetailTwoTimeTrackerState(firstLevel, secondLevel);
        }
    },
    DETAIL_THREE(_("Month")) {
        @Override
        public TimeTrackerState getTimeTrackerState(
                IDetailItemModificator firstLevel,
                IDetailItemModificator secondLevel) {
            return new DetailThreeTimeTrackerState(firstLevel, secondLevel);
        }
    },
    DETAIL_FOUR(_("Week")) {
        @Override
        public TimeTrackerState getTimeTrackerState(
                IDetailItemModificator firstLevel,
                IDetailItemModificator secondLevel) {
            return new DetailFourTimeTrackerState(firstLevel, secondLevel);
        }
    },
    DETAIL_FIVE(_("Day")) {
        @Override
        public TimeTrackerState getTimeTrackerState(
                IDetailItemModificator firstLevel,
                IDetailItemModificator secondLevel) {
            return new DetailFiveTimeTrackerState(firstLevel, secondLevel);
        }
    };

    private String name;

    private ZoomLevel(String name) {
        this.name = name;
    }

    /**
     * @return if there is no next, returns <code>this</code>. Otherwise returns
     *         the next one.
     */
    public ZoomLevel next() {
        final int next = ordinal() + 1;
        if (next == ZoomLevel.values().length) {
            return this;
        }
        return ZoomLevel.values()[next];
    }

    /**
     * @return if there is no previous, returns <code>this</code>. Otherwise
     *         returns the previous one.
     */
    public ZoomLevel previous() {
        if (ordinal() == 0) {
            return this;
        }
        return ZoomLevel.values()[ordinal() - 1];
    }

    public abstract TimeTrackerState getTimeTrackerState(
            IDetailItemModificator firstLevel,
            IDetailItemModificator secondLevel);

    @Override
    public String toString() {
        return name;
    }

    public static ZoomLevel getFromString(String zoomLevelParameter) {
        ZoomLevel requiredZoomLevel = ZoomLevel.DETAIL_ONE;
        if (zoomLevelParameter != null) {
            for (ZoomLevel z : ZoomLevel.values()) {
                if (zoomLevelParameter.equals(z.name)) {
                    requiredZoomLevel = z;
                }
            }
        }
        return requiredZoomLevel;

    }

}
