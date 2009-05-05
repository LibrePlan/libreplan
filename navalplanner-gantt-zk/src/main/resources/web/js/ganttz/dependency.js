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

    YAHOO.util.Event.onDOMReady(function() {
        boxes = [ zkTask.getDD(zkDependency.origin(dependency)), zkTask.getDD(zkDependency.destination(dependency))];
        for(var i = 0; i < boxes.length; i++){
            boxes[i].on('dragEvent',function(ev){
                zkDependency.draw(dependency);
            }, null, false);
         } 
    });
}

