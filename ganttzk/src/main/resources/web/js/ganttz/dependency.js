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

/**
 * Javascript behaviuor for TaskList elements
 * @author Javier Morán Rúa <jmoran@igalia.com>
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
zkDependency = {};

zkDependency.origin = function(dependency) {
	var id = dependency.getAttribute("idTaskOrig");
	return document.getElementById(id);
}

zkDependency.destination = function(dependency) {
	var id = dependency.getAttribute("idTaskEnd");
	return document.getElementById(id);
}

zkDependency.draw = function(dependency) {
	var orig = zkPlanner.findPos(this.origin(dependency));
	var dest = zkPlanner.findPos(this.destination(dependency));

	// This corner case may depend on dependence type
	offsetX = this.origin(dependency).offsetWidth - zkTask.CORNER_WIDTH;
	separation = orig[0] + this.origin(dependency).offsetWidth - dest[0];

	if (separation > 0) {
		offsetX = offsetX - separation;
	}
	if (dependency.getAttribute('type') == zkPlanner.constants.END_START
			|| dependency.getAttribute('type') == null) {
		orig[0] = orig[0] + Math.max(0, offsetX);
	} else if (dependency.getAttribute('type') == zkPlanner.constants.END_END) {
		orig[0] = orig[0] + this.origin(dependency).offsetWidth;
		dest[0] = dest[0] + this.destination(dependency).offsetWidth;
	}

	orig[1] = orig[1] + zkTask.HEIGHT;
	dest[1] = dest[1] + zkTask.HALF_HEIGHT;

	if ((orig[1] > dest[1])) {
		orig[1] = orig[1] - zkTask.HEIGHT;
	}

	zkPlanner.drawArrow(dependency, orig, dest);

}

zkDependency.init = function(dependency) {
	zkPlanner.setupArrow(dependency);
	var parent = dependency.parentNode;
	if (parent.id !== "listdependencies") {
		document.getElementById("listdependencies").appendChild(dependency);
	}
	YAHOO.util.Event.onDOMReady(function() {
		var origin = zkDependency.origin(dependency);
		var destination = zkDependency.destination(dependency);
		zkDependency.draw(dependency);
		zkTask.addRelatedDependency(origin, dependency);
		zkTask.addRelatedDependency(destination, dependency);
	});
}
