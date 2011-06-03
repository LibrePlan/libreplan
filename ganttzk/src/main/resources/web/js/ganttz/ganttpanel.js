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

/**
 * Javascript behaviuor for GanttPanel element
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
zkGanttPanel = {};

SCROLL_DAY = 0;

zkGanttPanel.init = function(cmp){
}

zkGanttPanel.update_day_scroll = function(cmp,previousPixelPerDay) {
	fromPixelToDay(previousPixelPerDay);
}

/**
 * Scrolls horizontally the ganttpanel when the zoom has resized the component
 * width.
 */
zkGanttPanel.scroll_horizontal = function(cmp,daysDisplacement) {
	SCROLL_DAY = daysDisplacement;
}

zkGanttPanel.move_scroll = function(cmp,diffDays,pixelPerDay) {
	fromDayToPixel(diffDays,pixelPerDay);
}

function fromPixelToDay(previousPixelPerDay){
	var div1 = document.getElementById ("ganttpanel").parentNode;
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
}

function fromDayToPixel(diffDays,pixelPerDay){
	var div1 = document.getElementById ("ganttpanel").parentNode;
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
}