/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

zkResourcesLoadList = addResourcesLoadListMethods( {});

zkResourcesLoadList.WATERMARK_MIN_HEIGHT = 450;
zkResourcesLoadList.WATERMARK_MARGIN_BOTTOM = 40;


zkResourcesLoadList.recalculateTimetrackerHeight = function (cmp) {

	zkResourcesLoadList.resourceloadlist = function(elem) {
		return YAHOO.util.Selector.query('.resourceloadlist')[0];
	}

	zkResourcesLoadList.firstWatermarkColumn = function(elem) {
		return YAHOO.util.Selector.query('.rightpanellayout tr#watermark td')[0];
	}

	if (zkResourcesLoadList.resourceloadlist() != undefined && zkResourcesLoadList.firstWatermarkColumn() != undefined) {
		var height = Math.max(
				zkResourcesLoadList.resourceloadlist().clientHeight + zkResourcesLoadList.WATERMARK_MARGIN_BOTTOM,
				zkResourcesLoadList.WATERMARK_MIN_HEIGHT);
		zkResourcesLoadList.firstWatermarkColumn().style.height = height + "px";
	}
}

function addResourcesLoadListMethods(object) {
	var scrollSync;

	function watermark() {
		return document.getElementById('watermark');
	}

	function timetracker() {
		return document.getElementById('timetracker');
	}

	function resourceloadlist() {
		return YAHOO.util.Selector.query('.resourceloadlist')[0];
	}

	function taskspanelgap() {
		return YAHOO.util.Selector.query('.taskspanelgap')[0];
	}

	function resourcesloadgraph() {
		return YAHOO.util.Selector.query('.resourcesloadgraph div')[0];
	}

	function scrolledpannel() {
		return YAHOO.util.Selector.query('.rightpanellayout div')[0];
	}

	function timetrackergap() {
		return YAHOO.util.Selector.query('.timetrackergap')[0];
	}

	function leftpanel() {
		return YAHOO.util.Selector.query('.leftpanelgap .z-tree-body')[0];
	}

	function rightpanel() {
		return YAHOO.util.Selector.query('.rightpanellayout div')[0];
	}

	object.init = function(cmp) {
		this.adjustTimeTrackerSize(cmp);
		YAHOO.util.Event.addListener(window, 'resize',
				zkResourcesLoadList.adjustTimeTrackerSize, cmp);
		scrollSync = new ScrollSync(cmp);
		scrollSync.synchXChangeTo(timetracker);
		listenToScroll();
	};

	function listenToScroll() {
		var timetrackergap_ = timetrackergap();
		var scrolledpannel_ = scrolledpannel();
		var resourcesloadgraph_ = resourcesloadgraph();
		var leftpanel_ = leftpanel();
		var rightpanel_ = rightpanel();

		var onScroll = function() {
			var timeplotcontainer_ = YAHOO.util.Selector.query('canvas.timeplot-canvas')[0];
			timeplotcontainer_.style["left"] = "-" + scrolledpannel_.scrollLeft + "px";
			timetrackergap_.style["left"] = "-" + scrolledpannel_.scrollLeft + "px";
			leftpanel_.style["top"] = "-" + scrolledpannel_.scrollTop + "px";
			resourcesloadgraph_.scrollLeft = scrolledpannel_.scrollLeft;
		};

		rightpanel_.onscroll = onScroll;
	}


	object.adjustTimeTrackerSize = function(cmp) {
		zkResourcesLoadList.recalculateTimetrackerHeight();
		watermark().style["height"] = cmp.clientHeight + "px";
		timetracker().style["width"] = cmp.clientWidth + "px";
		/* Set watermark width */
		YAHOO.util.Selector.query('.resourceloadlist')[0].style["width"] = YAHOO.util.Selector
				.query('.second_level_')[0].clientWidth + "px";
	};

	object.adjustResourceLoadRows = function(cmp) {
		YAHOO.util.Selector.query('.row_resourceload').each(function(node) {
			node.style["width"] = cmp.clientWidth + "px";
		});
	};

	object.adjustScrollHorizontalPosition = function(cmp, offsetInPx) {
		cmp.scrollLeft = offsetInPx;
	}

	return object;
}