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
 * Javascript behaviuor for Planner elements
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
zkPlanner = {};
zkPlanner.constants = {
    END_START: "END_START",
    START_START: "START_START",
    END_END: "END_END"
};

zkPlanner.getImagesDir = function() {
    return webapp_context_path + "/zkau/web/ganttz/img/";
}

zkPlanner.init = function(planner){

}

zkPlanner.findImageElement = function(arrow, name) {
    var children = arrow.getElementsByTagName("div");
    for (var i = 0; i < children.length; i++) {
        var child = children[i];
        if (child.getAttribute("class").indexOf(name) != -1) {
            return child;
        }
    }
    return null;
}

function get_origin() {
    return YAHOO.util.Dom.getXY('listdependencies');
}

zkPlanner.findPos = function(obj) {
    var pos1 = get_origin();
    var pos2 = YAHOO.util.Dom.getXY(obj.id);
    return [ pos2[0] - pos1[0], pos2[1] - pos1[1] ];
}
zkPlanner.findPosForMouseCoordinates = function(x, y){
    /* var pos1 = get_origin() */
    var pos1 = YAHOO.util.Dom.getXY('listtasks');
    return [x -  pos1[0], y - pos1[1]];
}

function getContextPath(element){
    return element.getAttribute('contextpath');
}

zkPlanner.setupArrow = function(arrowDiv){

    var image_data2 = [ "start", "mid", "end", "arrow" ];
    for ( var i = 0; i < image_data2.length; i++) {
        var img = document.createElement('div');
        img.setAttribute("class", image_data[i]+" extra_padding");
        arrowDiv.appendChild(img);
    }
}

zkPlanner.drawArrow = function(dependency, orig, dest) {
	switch(dependency.getAttribute('type'))
    {
	case zkPlanner.constants.START_START:
		zkPlanner.drawArrowStartStart(dependency, orig, dest);
		break;
	case zkPlanner.constants.END_END:
		zkPlanner.drawArrowEndEnd(dependency, orig, dest);
		break;
	case zkPlanner.constants.END_START:
	default:
		zkPlanner.drawArrowEndStart(dependency, orig, dest);
    }
}
