zkPlanner = {};

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


zkPlanner.setupArrow = function(arrowDiv){
    var base_url_images = "zkau/web/ganttz/img/";
    var image_data = [ [ "start", "pixel.gif" ], [ "mid", "pixel.gif" ],
            [ "end", "pixel.gif" ], [ "arrow", "arrow.png" ] ];
    for ( var i = 0; i < image_data.length; i++) {
        var img = document.createElement('img');
        img.setAttribute("class", image_data[i][0]+" extra_padding");
        img.src = base_url_images + image_data[i][1];
        arrowDiv.appendChild(img);
    }
}

zkPlanner.drawArrow = function(arrow, orig, dest){
    var xorig = orig[0] - zkTask.DEPENDENCY_PADDING;
    var yorig = orig[1] - zkTask.HALF_DEPENDENCY_PADDING;
    var xend = dest[0] - zkTask.DEPENDENCY_PADDING;
    var yend = dest[1] - zkTask.HALF_DEPENDENCY_PADDING;

    var width = (xend - xorig);
    var xmid = xorig + width;

    var depstart = this.findImageElement(arrow, 'start');
    /*    depstart.style.top = yorig + "px";
    depstart.style.left = xorig + "px";
    depstart.style.width = width + "px"; */
    depstart.style.display = "none";

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
    var baseURL = 'zkau/web/ganttz/img/';
    if ( width == 0 ) {
        deparrow.src = baseURL+"arrow2.png";
        deparrow.style.top = yend - 10 + "px";
        deparrow.style.left = xend - 5 + "px";
        if ( yorig > yend ) {
            deparrow.src = baseURL+"arrow4.png";
            deparrow.style.top = yend + "px";
        }
    } else {
        deparrow.style.top = yend - 5 + "px";
        deparrow.style.left = xend - 10 + "px";
        deparrow.src = baseURL+"arrow.png";

        if (width < 0) {
            deparrow.src = baseURL+"arrow3.png";
            deparrow.style.left = xend + "px";
            deparrow.style.top = yend - 5 + "px";
        }
    }
}