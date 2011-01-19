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

// Check if this is advanced location tab
ADVANCE_ALLOCATIONS = {};

ADVANCE_ALLOCATIONS.listenToScroll = function() {
	var scrollableArea_ = YAHOO.util.Selector.query('.advanced-assignment-area')[0];
	var innerScrollableArea_ = YAHOO.util.Selector.query('.advanced-assignment-area .z-center-body')[0];
    var taskdetails_ = YAHOO.util.Selector.query('.advancedassignmentdetails .z-grid-body')[0];
    var timetrackergap_ = YAHOO.util.Selector.query('.timetrackergap')[0];
    var timetracker_ = YAHOO.util.Selector.query('#timeTracker .z-vbox')[0];

    var onScroll = function() {
        taskdetails_.style["top"] = "-" + scrollableArea_.scrollTop + "px";
        timetrackergap_.style["left"] = "-" + scrollableArea_.scrollLeft + "px";
    };
	scrollableArea_.onscroll = onScroll;

	if (timetracker_ != undefined ) {
		innerScrollableArea_.style["width"] = timetracker_.clientWidth + "px";
	}
};
