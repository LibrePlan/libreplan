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

zkLimitingDependencylist = {};

zkLimitingDependencylist.init = function (cmp) {
}


zkLimitingDependencies = {};
zkLimitingDependencies.constants = {
    END_START: "END_START",
    START_START: "START_START",
    END_END: "END_END"
};

zkLimitingDependencies.CORNER = 10;
zkLimitingDependencies.ROW_HEIGHT = 15;
zkLimitingDependencies.HEIGHT = 12;
zkLimitingDependencies.ARROW_PADDING = 10;
zkLimitingDependencies.HALF_ARROW_PADDING = 5;

/* TODO: Optimize function */
zkLimitingDependencies.showDependenciesForQueueElement = function (task) {
	var dependencies = YAHOO.util.Selector.query('.dependency');
	for (var i = 0; i < dependencies.length; i++) {
		if ( (dependencies[i].getAttribute("idTaskOrig") ==  task) || (dependencies[i].getAttribute("idTaskEnd") ==  task) ) {
			dependencies[i].style.display ="inline";
			dependencies[i].style.opacity ="1";
		}
	}
}

/* TODO: Optimize function */
zkLimitingDependencies.hideDependenciesForQueueElement = function (task) {
	var dependencies = YAHOO.util.Selector.query('.dependency');
	for (var i = 0; i < dependencies.length; i++) {
		if ( (dependencies[i].getAttribute("idTaskOrig") ==  task) || (dependencies[i].getAttribute("idTaskEnd") ==  task) ) {
			dependencies[i].style.display ="none";
			dependencies[i].style.removeProperty("opacity");
		}
	}
}

/* TODO: Optimize function */
zkLimitingDependencies.toggleDependenciesForQueueElement = function (task) {
	var dependencies = YAHOO.util.Selector.query('.dependency');
	for (var i = 0; i < dependencies.length; i++) {
		if ( (dependencies[i].getAttribute("idTaskOrig") ==  task) || (dependencies[i].getAttribute("idTaskEnd") ==  task) ) {
			dependencies[i].setAttribute("class", "dependency toggled");
		}
	}
}


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