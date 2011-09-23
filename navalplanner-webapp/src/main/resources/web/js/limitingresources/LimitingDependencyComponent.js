zk.$package("limitingresources");

limitingresources.LimitingDependencyComponent = zk.$extends(ganttz.DependencyComponent,{
    //TODO: reuse more code from ganttz.DependencyComponent
    draw : function() {
        this._withOriginAndDestination(function(origin, destination) {
            if ( (origin != null) && (destination != null) ) {
                    var orig = this.findPos_(origin);
                    var dest = this.findPos_(destination);
                    orig.left = Math.max(orig.left, orig.left + origin[0].offsetWidth - this.$class.CORNER);
                    var verticalSeparation = this.$class.ROW_HEIGHT;
                    this.newdraw(orig, dest, verticalSeparation);
            }
        });
    },
    newdraw: function(orig, dest, param) {
        var xorig = orig.left;
        var yorig = orig.top;
        var xend = dest.left;
        var yend = dest.top;

        var width = Math.abs(xend - xorig);

        if (yend == yorig) {
            yend = yend + this.$class.HEIGHT;
            yorig = yorig + this.$class.HEIGHT;
        } else if (yend < yorig) {
            yend = yend + this.$class.HEIGHT;
        } else {
            yorig = yorig + this.$class.HEIGHT;
        }

        var height = Math.abs(yorig - yend);

        // --------- First segment -----------
        var depstart = this._findImageElement('start');
        var depstartstyle = {};
        depstartstyle.left = xorig + "px";
        if (yend > yorig) {
            depstartstyle.top = yorig + "px";
            depstartstyle.height = ( height - param ) + "px";
        } else if (yend == yorig) {
            depstartstyle.top = yorig + "px";
            depstartstyle.height = param + "px";
        } else if (yend < yorig) {
            depstartstyle.top = ( yend + param ) + "px";
            depstartstyle.height = ( height - param ) + "px";
        }
        depstart.css(depstartstyle);

        // --------- Second segment -----------
        var depmid = this._findImageElement('mid');
        var depmidstyle = {};
        depmidstyle.width = width + "px";
        if (xorig < xend ) {
            depmidstyle.left = xorig + "px";
        } else {
            depmidstyle.left = xend + "px";
        }
        if (yend > yorig) {
            depmidstyle.top = ( yend - param ) + "px";
        } else if (yend == yorig) {
            depmidstyle.top = ( yend + param ) + "px";
        } else if (yend < yorig) {
            depmidstyle.top = ( yend + param ) + "px";
        }
        depmid.css(depmidstyle);

        // --------- Third segment -----------
        var depend = this._findImageElement('end');
        var dependstyle = {};
        dependstyle.left = xend + "px";
        if (yend > yorig) {
            dependstyle.top = ( yend - param ) + "px";
            dependstyle.height = param + "px";
        } else if (yend == yorig) {
            dependstyle.top = yorig + "px";
            dependstyle.height = param + "px";
        } else if (yend < yorig) {
            dependstyle.top = yend + "px";
            dependstyle.height = param + "px";
        }
        depend.css(dependstyle);

        // --------- Arrow -----------
        var deparrow = this._findImageElement('arrow');
        var deparrowstyle = {};
        var deparrowsrc;
        deparrowstyle.left = ( xend - this.$class.HALF_ARROW_PADDING ) + "px";
        if (yend > yorig) {
            deparrowsrc = this.getImagesDir()+"arrow2.png";
            deparrowstyle.top = ( yend - this.$class.ARROW_PADDING ) + "px";
        } else if (yend == yorig) {
            deparrowsrc = this.getImagesDir()+"arrow4.png";
            deparrowstyle.top = yorig + "px";
        } else if (yend < yorig) {
            deparrowsrc = this.getImagesDir()+"arrow4.png";
            deparrowstyle.top = yend + "px";
        }
        deparrow.css(deparrowstyle);
        deparrow.attr('src',deparrowsrc);
    },
    findPos_ : function(element){
        var pos1 = jq('#listlimitingdependencies').offset();
        var pos2 = element.offset();
        return {left : (pos2.left - pos1.left), top : (pos2.top - pos1.top)};
    },
    getImagesDir: function() {
        var webapp_context_path = window.location.pathname.split( '/' )[1];
        return "/" + webapp_context_path + "/zkau/web/ganttz/img/";
    }
},{
    CORNER: 10,
    ROW_HEIGHT: 15,
    HEIGHT: 12,
    ARROW_PADDING: 10,
    HALF_ARROW_PADDING: 5
});
