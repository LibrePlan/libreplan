/*
 * This file is part of ###PROJECT_NAME###
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

/**
 * @author Francisco Javier Moran Rúa
 */
public enum ZoomLevel {

    DETAIL_ONE(DetailOneTimeTrackerState.INSTANCE), DETAIL_TWO(
            DetailTwoTimeTrackerState.INSTANCE), DETAIL_THREE(
            DetailThreeTimeTrackerState.INSTANCE), DETAIL_FOUR(
            DetailFourTimeTrackerState.INSTANCE), DETAIL_FIVE(
            DetailFiveTimeTrackerState.INSTANCE);

    private final TimeTrackerState state;

    private ZoomLevel(TimeTrackerState state) {
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }
        this.state = state;
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

    public TimeTrackerState getTimeTrackerState() {
        return state;
    }

}
