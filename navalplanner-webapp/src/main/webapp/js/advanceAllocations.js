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

// Check if this is advanced location tab
ADVANCE_ALLOCATIONS = {};

ADVANCE_ALLOCATIONS.listenToScroll = function() {
//     var timetrackergap_ = YAHOO.util.Selector.query('#timeTracker .z-vbox')[0];
//    var taskspanel_ = YAHOO.util.Selector.query('.rightpanellayout .z-grid-body')[0];
    var taskdetails_ = YAHOO.util.Selector.query('.advancedassignmentdetails .z-grid-body')[0];
    var taskspanelcenter_ = YAHOO.util.Selector.query('.rightpanellayout')[0];

    var onVerticalScroll = function() {
        taskdetails_.style["top"] = "-" + taskspanelcenter_.scrollTop + "px";
    };
    taskspanelcenter_.onscroll = onVerticalScroll;
};


ADVANCE_ALLOCATIONS.listenToHorizontalScroll = function() {
	var horizontalScroll_ = YAHOO.util.Selector.query('.horizontalscroller')[0];
    var timeTrackedTable_ = YAHOO.util.Selector.query('.timeTrackedTableWithLeftPane .z-grid-body table')[0];
    var timetrackergap_ = YAHOO.util.Selector.query('#timeTracker .z-vbox')[0];
    var innerScroll_ = YAHOO.util.Selector.query('#horizontalScrollContainer')[0];

	var onHorizontalScroll = function() {
		timeTrackedTable_.style["left"] = "-" + horizontalScroll_.scrollLeft + "px";
        timetrackergap_.style["left"] = "-" + horizontalScroll_.scrollLeft + "px";
    };

    innerScroll_.style["width"] = (timeTrackedTable_.clientWidth + 10 ) +"px";
    horizontalScroll_.scrollLeft = "0";
    horizontalScroll_.onscroll = onHorizontalScroll;
};
