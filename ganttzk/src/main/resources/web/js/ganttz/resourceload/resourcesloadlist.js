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

var TIMETRACKER_OFFSET_TOP = 120;

zkResourcesLoadList = addResourcesLoadListMethods( {});

function addResourcesLoadListMethods(object) {
	var scrollSync;

	function watermark() {
		return document.getElementById('watermark');
	}

	function timetracker() {
		return document.getElementById('timetracker');
	}

	function zoom_buttons() {
		return document.getElementById('zoom_buttons');
	}

	function resourceloadlist() {
		return YAHOO.util.Selector.query('.resourceloadlist')[0];
	}

	function taskspanelgap() {
		return YAHOO.util.Selector.query('.taskspanelgap')[0];
	}

	object.init = function(cmp) {
		this.adjustTimeTrackerSize(cmp);
		YAHOO.util.Event.addListener(window, 'resize',
				zkResourcesLoadList.adjustTimeTrackerSize, cmp);
		scrollSync = new ScrollSync(cmp);
		scrollSync.notifyXChangeTo(function(scroll) {
			zoom_buttons().style["left"] = scroll + "px";
		});
		scrollSync.synchXChangeTo(timetracker);

		listenToScroll();
	};

	function listenToScroll() {
		var onHorizontalScroll = function() {
			alert('horizontalscroll');
		};
		var onVerticalScroll = function() {
			var scrolledpannel = YAHOO.util.Selector
					.query('.rightpanellayout div')[0];
			elem = YAHOO.util.Selector.query('.timetrackergap')[0];
			elem.style["position"] = "relative";
			elem.style["left"] = "-" + scrolledpannel.scrollLeft + "px";

			var leftpanel = YAHOO.util.Selector.query('.leftpanelgap .z-tree-body')[0];
			leftpanel.style["position"] = "relative";
			leftpanel.style["top"] = "-" + scrolledpannel.scrollTop + "px";
		};

		YAHOO.util.Selector.query('.rightpanellayout div')[0].onscroll = onVerticalScroll;

	}

	object.adjustTimeTrackerSize = function(cmp) {
		watermark().style["height"] = cmp.clientHeight + "px";
		timetracker().style["width"] = cmp.clientWidth + "px";
		/* Set watermark width */
		YAHOO.util.Selector.query('.resourceloadlist')[0].style["width"] = YAHOO.util.Selector
				.query('.second_level_')[0].clientWidth
				+ "px";
		YAHOO.util.Selector.query('.rightpanellayout tr#watermark td')[0].style["height"] =
		/* Calculate min : taskspanelgap().clientHeight + 120 + 'px'; )  */
		YAHOO.util.Selector.query('.resourceloadlist')[0].clientHeight + 120
				+ "px";
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