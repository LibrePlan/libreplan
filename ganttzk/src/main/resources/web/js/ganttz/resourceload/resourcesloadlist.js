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

	var SCROLL_DAY = 0;

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

	object.update_day_scroll = function(cmp,previousPixelPerDay) {
		var div1 = cmp;
		var div2 = div1.parentNode;
		var div3 = div2.parentNode;

		var maxHPosition = div3.scrollWidth - div3.clientWidth;
		if( maxHPosition > 0 ){
			var proportion = div3.scrollWidth / maxHPosition;
			var positionInScroll = div3.scrollLeft;
			var positionInPx = positionInScroll * proportion;
			if(positionInPx > 0){
				var position = positionInPx / previousPixelPerDay;
				var day = position;
				SCROLL_DAY = position;
			}
		}
	};

	/**
	 * Scrolls horizontally the ganttpanel when the zoom has resized the component
	 * width.
	 */

	object.scroll_horizontal = function(cmp,daysDisplacement) {
		SCROLL_DAY = daysDisplacement;
	};

	object.move_scroll = function(cmp,diffDays,pixelPerDay) {
		var div1 = cmp;
		var div2 = div1.parentNode;
		var div3 = div2.parentNode;

		var day = SCROLL_DAY;
		day += parseInt(diffDays);
		var newPosInPx = parseInt(day * pixelPerDay);
		var maxHPosition = div3.scrollWidth - div3.clientWidth;
		var newProportion = div3.scrollWidth / maxHPosition;
		if( newProportion > 0){
			var newPosInScroll = newPosInPx / newProportion;
			if(newPosInScroll < 0){
				newPosInScroll = 0;
			}
			div1.scrollLeft = newPosInScroll;
			div2.scrollLeft = newPosInScroll;
			div3.scrollLeft = newPosInScroll;
		}
	};

	return object;
}