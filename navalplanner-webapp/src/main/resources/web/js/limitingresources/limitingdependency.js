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

/**
 * Javascript behaviour and drawing algorithms for queue dependencies
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */

webapp_context_path = window.location.pathname.split( '/' )[1];

zkLimitingDependencies = {};
zkLimitingDependencies.constants = {
    END_START: "END_START",
    START_START: "START_START",
    END_END: "END_END"
};

zkLimitingDependencies.CORNER_WIDTH = 20;
zkLimitingDependencies.HEIGHT = 10;
zkLimitingDependencies.HALF_HEIGHT = 5;
zkLimitingDependencies.DEPENDENCY_PADDING = 4;
zkLimitingDependencies.HALF_DEPENDENCY_PADDING = 2;

zkLimitingDependencies.addRelatedDependency = function(cmp, dependency) {
	if (!cmp['relatedDependencies']) {
		cmp.relatedDependencies = [];
	}
	cmp.relatedDependencies.push(dependency);
}

zkLimitingDependencies.getImagesDir = function() {
    return "/" + webapp_context_path + "/zkau/web/ganttz/img/";
}

zkLimitingDependencies.init = function(planner){
}

zkLimitingDependencies.findImageElement = function(arrow, name) {
    var children = arrow.getElementsByTagName("img");
    for (var i = 0; i < children.length; i++) {
        var child = children[i];
        if (child.getAttribute("class").indexOf(name) != -1) {
            return child;
        }
    }
    return null;
}


function get_origin() {
    return YAHOO.util.Dom.getXY('listlimitingdependencies');
}


zkLimitingDependencies.findPos = function(obj) {
    var pos1 = get_origin();
    var pos2 = YAHOO.util.Dom.getXY(obj.id);
    return [ pos2[0] - pos1[0], pos2[1] - pos1[1] ];
}


zkLimitingDependencies.findPosForMouseCoordinates = function(x, y){
    /* var pos1 = get_origin() */
    var pos1 = YAHOO.util.Dom.getXY('listtasks');
    return [x -  pos1[0], y - pos1[1]];
}


function getContextPath(element){
    return element.getAttribute('contextpath');
}


zkLimitingDependencies.setupArrow = function(arrowDiv){
    var image_data = [ [ "start", "pixel.gif" ], [ "mid", "pixel.gif" ],
            [ "end", "pixel.gif" ], [ "arrow", "arrow.png" ] ];
    for ( var i = 0; i < image_data.length; i++) {
        var img = document.createElement('img');
        img.setAttribute("class", image_data[i][0]+" extra_padding");
        img.src = this.getImagesDir() + image_data[i][1];
        arrowDiv.appendChild(img);
    }
}


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
	var height = Math.abs(yorig - yend);

	// --------- First segment -----------
	var depstart = this.findImageElement(arrow, 'start');
	depstart.style.left = xorig + "px";
	if (yend > yorig) {
		depstart.style.top = yorig + "px";
		depstart.style.height = ( height - param ) + "px";
	} else if (yend = yorig) {
		depstart.style.top = yorig + "px";
		depstart.style.height = param + "px";
	} else if (yend < yorig) {
		depstart.style.top = yorig + "px";
		depstart.style.height = ( height - param ) + "px";
	}

	// --------- Second segment -----------
	var depmid = this.findImageElement(arrow, 'mid');
	depmid.style.width = width + "px";
	depmid.style.left = depstart.style.left;
	if (yend > yorig) {
		depmid.style.top = ( yend - param ) + "px";
	} else if (yend = yorig) {
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
	} else if (yend = yorig) {
		depend.style.top = yorig + "px";
		depend.style.height = param + "px";
	} else if (yend < yorig) {
		depend.style.top = yorig + "px";
		depend.style.height = ( height - param ) + "px";
	}

	// --------- Arrow -----------
    var deparrow = this.findImageElement(arrow, 'arrow');
    deparrow.style.left = xend - 5 + "px";
	if (yend > yorig) {
		deparrow.src = this.getImagesDir()+"arrow2.png";
		deparrow.style.top = ( yend - param ) + "px";
	} else if (yend = yorig) {
		deparrow.src = this.getImagesDir()+"arrow4.png";
		deparrow.style.top = yorig + "px";
	} else if (yend < yorig) {
		deparrow.src = this.getImagesDir()+"arrow4.png";
		deparrow.style.top = yorig + "px";
	}
}


zkLimitingDependency.draw = function(dependency) {
	var orig = zkLimitingDependencies.findPos(this.origin(dependency));
	var dest = zkLimitingDependencies.findPos(this.destination(dependency));

	var verticalSeparation = 15;
	switch(dependency.getAttribute('type'))
    {
	case zkLimitingDependencies.constants.START_START:
		verticalSeparation = 5;
		break;
	case zkLimitingDependencies.constants.END_END:
		verticalSeparation = 10;
		break;
	case zkLimitingDependencies.constants.END_START:
	default:
		verticalSeparation = 15;
    }
	zkLimitingDependencies.newdraw(dependency,  orig, dest, verticalSeparation);

}

zkLimitingDependency.init = function(dependency) {
	zkLimitingDependencies.setupArrow(dependency);
	var parent = dependency.parentNode;
	if (parent.id !== "listlimitingdependencies") {
		document.getElementById("listlimitingdependencies").appendChild(dependency);
	}
	YAHOO.util.Event.onDOMReady(function() {
		var origin = zkLimitingDependency.origin(dependency);
		var destination = zkLimitingDependency.destination(dependency);
		zkLimitingDependency.draw(dependency);
		zkLimitingDependency.addRelatedDependency(origin, dependency);
		zkLimitingDependency.addRelatedDependency(destination, dependency);
	});
}
