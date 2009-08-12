zkPlanner = {};

zkPlanner.getImagesDir = function() {      
    return webapp_context_path + "/zkau/web/ganttz/img/";
}

zkPlanner.init = function(planner){   
    
}

zkPlanner.findImageElement = function(arrow, name) {
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
    
    var image_data = [ [ "start", "pixel.gif" ], [ "mid", "pixel.gif" ],
            [ "end", "pixel.gif" ], [ "arrow", "arrow.png" ] ];
    for ( var i = 0; i < image_data.length; i++) {
        var img = document.createElement('img');
        img.setAttribute("class", image_data[i][0]+" extra_padding");
        img.src = this.getImagesDir() + image_data[i][1];
        arrowDiv.appendChild(img);
    }
}

zkPlanner.drawArrow = function(arrow, orig, dest){
    var xorig = orig[0] - zkTask.CORNER_WIDTH/2;
    var yorig = orig[1] - zkTask.CORNER_WIDTH/2;
    var xend = dest[0];
    var yend = dest[1];

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
	depend.style.width = width2 + "px";

    var deparrow = this.findImageElement(arrow, 'arrow');
    deparrow.src = this.getImagesDir()+"arrow.png"; 
    deparrow.style.top = yend - 5 + "px";
    deparrow.style.left = xend - 15 + "px";
    }


zkPlanner.drawArrow = function(dependency, orig, dest) {

	switch(dependency.getAttribute('type'))
    {
	case zkDependency.START_START: 
		zkPlanner.drawArrowStartEnd(dependency, orig, dest);
	case zkDependency.START_END:
	default:		
		zkPlanner.drawArrowStartEnd(dependency, orig, dest);				
    }
}

zkPlanner.drawArrowStartEnd = function(arrow, orig, dest){
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