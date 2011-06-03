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
 * Javascript behaviour and drawing algorithms for queue dependencies
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */

webapp_context_path = window.location.pathname.split( '/' )[1];


zkLimitingDependency = {};


zkLimitingDependency.origin = function(dependency) {
	var id = dependency.getAttribute("idTaskOrig");
	return document.getElementById(id);
}

zkLimitingDependency.destination = function(dependency) {
	var id = dependency.getAttribute("idTaskEnd");
	return document.getElementById(id);
}

/* ----------- Generic Limiting dependency draw function ---------- */
zkLimitingDependencies.newdraw = function(arrow, orig, dest, param) {
	var xorig = orig[0];
	var yorig = orig[1];
	var xend = dest[0];
	var yend = dest[1];

	var width = Math.abs(xend - xorig);

	if (yend == yorig) {
		yend = yend + zkLimitingDependencies.HEIGHT;
		yorig = yorig + zkLimitingDependencies.HEIGHT;
	} else if (yend < yorig) {
		yend = yend + zkLimitingDependencies.HEIGHT;
	} else {
		yorig = yorig + zkLimitingDependencies.HEIGHT;
	}

	var height = Math.abs(yorig - yend);

	// --------- First segment -----------
	var depstart = this.findImageElement(arrow, 'start');
	depstart.style.left = xorig + "px";
	if (yend > yorig) {
		depstart.style.top = yorig + "px";
		depstart.style.height = ( height - param ) + "px";
	} else if (yend == yorig) {
		depstart.style.top = yorig + "px";
		depstart.style.height = param + "px";
	} else if (yend < yorig) {
		depstart.style.top = ( yend + param ) + "px";
		depstart.style.height = ( height - param ) + "px";
	}

	// --------- Second segment -----------
	var depmid = this.findImageElement(arrow, 'mid');
	depmid.style.width = width + "px";
	if (xorig < xend ) {
		depmid.style.left = xorig + "px";
	} else {
		depmid.style.left = xend + "px";
	}
	if (yend > yorig) {
		depmid.style.top = ( yend - param ) + "px";
	} else if (yend == yorig) {
		depmid.style.top = ( yend + param ) + "px";
	} else if (yend < yorig) {
		depmid.style.top = ( yend + param ) + "px";
	}

	// --------- Third segment -----------
	var depend = this.findImageElement(arrow, 'end');
	depend.style.left = xend + "px";
	if (yend > yorig) {
		depend.style.top = ( yend - param ) + "px";
		depend.style.height = param + "px";
	} else if (yend == yorig) {
		depend.style.top = yorig + "px";
		depend.style.height = param + "px";
	} else if (yend < yorig) {
		depend.style.top = yend + "px";
		depend.style.height = param + "px";
	}

	// --------- Arrow -----------
    var deparrow = this.findImageElement(arrow, 'arrow');
    deparrow.style.left = ( xend - zkLimitingDependencies.HALF_ARROW_PADDING ) + "px";
	if (yend > yorig) {
		deparrow.src = this.getImagesDir()+"arrow2.png";
		deparrow.style.top = ( yend - zkLimitingDependencies.ARROW_PADDING ) + "px";
	} else if (yend == yorig) {
		deparrow.src = this.getImagesDir()+"arrow4.png";
		deparrow.style.top = yorig + "px";
	} else if (yend < yorig) {
		deparrow.src = this.getImagesDir()+"arrow4.png";
		deparrow.style.top = yend + "px";
	}
}


zkLimitingDependency.draw = function(dependency) {
        var posOrig = this.origin(dependency);
        var posDest = this.destination(dependency);
        if ( (posOrig != null) && (posDest != null) ) {
                var orig = zkLimitingDependencies.findPos(posOrig);
                var dest = zkLimitingDependencies.findPos(posDest);
                orig[0] = Math.max(orig[0], orig[0] + this.origin(dependency).offsetWidth - zkLimitingDependencies.CORNER);
                var verticalSeparation = zkLimitingDependencies.ROW_HEIGHT;
                zkLimitingDependencies.newdraw(dependency, orig, dest, verticalSeparation);
        }
}

zkLimitingDependency.init = function(dependency) {
        zkLimitingDependencies.setupArrow(dependency);
	var parent = dependency.parentNode;
	if (parent.id !== "listlimitingdependencies") {
		document.getElementById("listlimitingdependencies").appendChild(dependency);
	}
	YAHOO.util.Event.onDOMReady(function() {
		zkLimitingDependency.draw(dependency);
	});
}
