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
 * Javascript behaviuor for TaskList elements
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

zkLimitingDependencies.drawArrow = function(dependency, orig, dest) {
	switch(dependency.getAttribute('type'))
    {
	case zkLimitingDependencies.constants.START_START:
		zkLimitingDependencies.drawArrowStartStart(dependency, orig, dest);
		break;
	case zkLimitingDependencies.constants.END_END:
		zkLimitingDependencies.drawArrowEndEnd(dependency, orig, dest);
		break;
	case zkLimitingDependencies.constants.END_START:
	default:
		zkLimitingDependencies.drawArrowEndStart(dependency, orig, dest);
    }
}

zkLimitingDependencies.drawArrowStartStart = function(arrow, orig, dest){
    var xorig = orig[0] - zkTask.HALF_DEPENDENCY_PADDING;
    var yorig = orig[1] - zkTask.CORNER_WIDTH/2 + zkTask.HALF_DEPENDENCY_PADDING;
    var xend = dest[0] + zkTask.HALF_DEPENDENCY_PADDING;
    var yend = dest[1] - zkTask.HALF_DEPENDENCY_PADDING;
    if (yend < yorig) {
      yorig = orig[1] + zkTask.DEPENDENCY_PADDING;
    }

    width1 = zkTask.CORNER_WIDTH;
    width2 = Math.abs(xend - xorig) + zkTask.CORNER_WIDTH;
    height = Math.abs(yend - yorig);

	if (xorig > xend) {
		width1 = width2;
		width2 = zkTask.CORNER_WIDTH;
    }

	// First segment
	var depstart = this.findImageElement(arrow, 'start');
	depstart.style.left = (xorig - width1) + "px";
	depstart.style.top = yorig + "px";
	depstart.style.width = width1 + "px";
	depstart.style.display = "inline";

	// Second segment
	var depmid = this.findImageElement(arrow, 'mid');
	depmid.style.left = depstart.style.left;
	if (yend > yorig) {
	  depmid.style.top = yorig + "px";
	} else {
	  depmid.style.top = yend + "px";
	}
	depmid.style.height = height + "px";

	// Third segment
	var depend = this.findImageElement(arrow, 'end');
	depend.style.left = depstart.style.left;
	depend.style.top = yend + "px";
	depend.style.width = width2 - zkTask.HALF_HEIGHT + "px";

    var deparrow = this.findImageElement(arrow, 'arrow');
    deparrow.src = this.getImagesDir()+"arrow.png";
    deparrow.style.top = yend - zkTask.HALF_HEIGHT + "px";
    deparrow.style.left = xend - 15 + "px";
    }


zkLimitingDependencies.drawArrowEndEnd = function(arrow, orig, dest){
    var xorig = orig[0] - zkTask.DEPENDENCY_PADDING;
    var yorig = orig[1] - zkTask.CORNER_WIDTH/2 + zkTask.HALF_DEPENDENCY_PADDING;
    var xend = dest[0] + zkTask.HALF_DEPENDENCY_PADDING;
    var yend = dest[1] - zkTask.DEPENDENCY_PADDING;

    width1 = Math.abs(xend - xorig) + zkTask.CORNER_WIDTH;
    width2 = zkTask.CORNER_WIDTH;
    height = Math.abs(yend - yorig);

	if (xorig > xend) {
		width2 = width1;
		width1 = zkTask.CORNER_WIDTH;
    }

	// First segment
	var depstart = this.findImageElement(arrow, 'start');
	depstart.style.left = xorig + "px";
	if (yend > yorig) {
		depstart.style.top = yorig + "px";
	} else {
		depstart.style.top = yorig + zkTask.HEIGHT + "px";
	}
	depstart.style.width = width1 + "px";
	depstart.style.display = "inline";

	// Second segment
	var depmid = this.findImageElement(arrow, 'mid');
	depmid.style.left = (xorig + width1) + "px";
	if (yend > yorig) {
	  depmid.style.top = yorig + "px";
	} else {
	  depmid.style.top = yend + "px";
	  height = height + 10;
	}
	depmid.style.height = height + "px";

	// Third segment
	var depend = this.findImageElement(arrow, 'end');
	depend.style.left = (xorig + width1 - width2) + "px";
	depend.style.top = yend + "px";
	depend.style.width = width2 + "px";

    var deparrow = this.findImageElement(arrow, 'arrow');
    deparrow.src = this.getImagesDir()+"arrow3.png";
    deparrow.style.top = yend - 5 + "px";
    deparrow.style.left = xend - 8 + "px";
    }


zkLimitingDependencies.drawArrowEndStart = function(arrow, orig, dest){
    var xorig = orig[0] - zkTask.DEPENDENCY_PADDING;
    var yorig = orig[1] - zkTask.HALF_DEPENDENCY_PADDING;
    var xend = dest[0] - zkTask.DEPENDENCY_PADDING;
    var yend = dest[1] - zkTask.HALF_DEPENDENCY_PADDING;

    var width = (xend - xorig);
    var xmid = xorig + width;

	// First segment not used
    var depstart = this.findImageElement(arrow, 'start');
    depstart.style.display = "none";

    // Second segment not used
    var depmid = this.findImageElement(arrow, 'mid');
    if (yend > yorig) {
        depmid.style.top = yorig + "px";
        depmid.style.height = yend - yorig + "px";
    } else {
        depmid.style.top = yend + "px";
        depmid.style.height = yorig - yend + "px";
    }
    depmid.style.left = xorig + "px";

    var depend = this.findImageElement(arrow, 'end');
    depend.style.top = yend + "px";
    depend.style.left = xorig + "px";
    depend.style.width = width + "px";

    if (width < 0) {
        depend.style.left = xend + "px";
        depend.style.width = Math.abs(width) + "px";
    }
    var deparrow = this.findImageElement(arrow, 'arrow');
    if ( width == 0 ) {
        deparrow.src = this.getImagesDir()+"arrow2.png";
        deparrow.style.top = yend - 10 + "px";
        deparrow.style.left = xend - 5 + "px";
        if ( yorig > yend ) {
            deparrow.src = this.getImagesDir()+"arrow4.png";
            deparrow.style.top = yend + "px";
        }
    } else {
        deparrow.style.top = yend - 5 + "px";
        deparrow.style.left = xend - 10 + "px";
        deparrow.src = this.getImagesDir()+"arrow.png";

        if (width < 0) {
            deparrow.src = this.getImagesDir() + "arrow3.png";
            deparrow.style.left = xend + "px";
            deparrow.style.top = yend - 5 + "px";
        }
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

zkLimitingDependency.draw = function(dependency) {
	var orig = zkLimitingDependencies.findPos(this.origin(dependency));
	var dest = zkLimitingDependencies.findPos(this.destination(dependency));

	// This corner case may depend on dependence type
	offsetX = this.origin(dependency).offsetWidth - zkTask.CORNER_WIDTH;
	separation = orig[0] + this.origin(dependency).offsetWidth - dest[0];

	if (separation > 0) {
		offsetX = offsetX - separation;
	}
	if (dependency.getAttribute('type') == zkLimitingDependencies.constants.END_START
			|| dependency.getAttribute('type') == null) {
		orig[0] = orig[0] + Math.max(0, offsetX);
	} else if (dependency.getAttribute('type') == zkLimitingDependencies.constants.END_END) {
		orig[0] = orig[0] + this.origin(dependency).offsetWidth;
		dest[0] = dest[0] + this.destination(dependency).offsetWidth;
	}

	orig[1] = orig[1] + zkTask.HEIGHT;
	dest[1] = dest[1] + zkTask.HALF_HEIGHT;

	if ((orig[1] > dest[1])) {
		orig[1] = orig[1] - zkTask.HEIGHT;
	}

	zkLimitingDependencies.drawArrow(dependency, orig, dest);

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
