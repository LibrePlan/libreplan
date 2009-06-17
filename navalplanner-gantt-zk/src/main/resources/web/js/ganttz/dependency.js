zkDependency = {};

zkDependency.origin = function(dependency) {
    return document.getElementById(dependency.getAttribute("idTaskOrig"));
}

zkDependency.destination = function(dependency) {
    return document.getElementById(dependency.getAttribute("idTaskEnd"));
}

zkDependency.draw = function(dependency) {
	var orig = zkPlanner.findPos(this.origin(dependency));
	var dest = zkPlanner.findPos(this.destination(dependency));

   // This corner case may depend on dependence type
    orig[0] = orig[0] + Math.max( 0,
        this.origin(dependency).offsetWidth - zkTask.CORNER_WIDTH);
    orig[1] = orig[1] + zkTask.HEIGHT;
    dest[1] = dest[1] + zkTask.HALF_HEIGHT;

    if ( ( orig[1] > dest[1] ) ) {

        orig[1] = orig[1] - zkTask.HEIGHT;
    }

	zkPlanner.drawArrow(dependency, orig, dest);

}

zkDependency.init = function(dependency) {
    zkPlanner.setupArrow(dependency);    
    YAHOO.util.Event.onDOMReady(function() {
        zkDependency.draw(dependency);
    });
    var origin = zkDependency.origin(dependency);
    var destination = zkDependency.destination(dependency);
    zkTask.addRelatedDependency(origin, dependency);
    zkTask.addRelatedDependency(destination, dependency);
}

