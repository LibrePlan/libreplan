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
