zk.$package("ganttz");

ganttz.DependencyComponent = zk.$extends(zk.Widget,{
    $define : {
        idTaskOrig : null,
        idTaskEnd : null,
        dependencyType : null
    },
    bind_ : function(){
        this.$supers('bind_', arguments);
        this._initializeProperties();
        this._setupArrow();
        YAHOO.util.Event.onDOMReady(this.proxy(function() {
            this.draw();
//            zkTask.addRelatedDependency(origin, dependency);
//            zkTask.addRelatedDependency(destination, dependency);
        }));
    },
    draw : function(){
        var orig = this._findPos(this._origin);
        var dest = this._findPos(this._destination);

        // This corner case may depend on dependence type
        var offsetX = this._origin.outerWidth() - ganttz.TaskComponent.CORNER_WIDTH;
        var separation = orig.left + this._origin.outerWidth() - dest.left;

        if (separation > 0) {
            offsetX = offsetX - separation;
        }
        if (this.getDependencyType() == this.$class.END_START
                || this.getDependencyType() == null) {
            orig.left = orig.left + Math.max(0, offsetX);
        } else if (this.getDependencyType() == this.$class.END_END) {
            orig.left = orig.left + this._origin.outerWidth();
            dest.left = dest.left + this._destination.outerWidth();
        }

        orig.top = orig.top + ganttz.TaskComponent.HEIGHT;
        dest.top = dest.top + ganttz.TaskComponent.HALF_HEIGHT;

        if (orig.top > dest.top) {
            orig.top = orig.top - ganttz.TaskComponent.HEIGHT;
        }

        this._drawArrow(orig, dest);
    },
    _drawArrow : function(coordOrig, coordDest){
        switch(this.getDependencyType)
        {
            case this.$class.START_START:
                this._drawArrowStartStart(coordOrig, coordDest);
                break;
            case this.$class.END_END:
                this._drawArrowEndEnd(coordOrig, coordDest);
                break;
            case this.$class.END_START:
            default:
                this._drawArrowEND_START(coordOrig, coordDest);
        }
    },
    _drawArrowSTART_START : function(coordOrig, coordDest){
        var yorig = coordOrig.top -
                    ganttz.TaskComponent.CORNER_WIDTH/2 +
                    this.$class.HALF_DEPENDENCY_PADDING;
        var xend = coordDest.left + this.$class.HALF_DEPENDENCY_PADDING;
        var yend = coordDest.left - this.$class.HALF_DEPENDENCY_PADDING;
        if (yend < yorig) yorig = coordOrig.top + this.$class.DEPENDENCY_PADDING;

        var width1 = ganttz.TaskComponent.CORNER_WIDTH;
        var width2 = Math.abs(xend - xorig) + ganttz.TaskComponent.CORNER_WIDTH;
        var height = Math.abs(yend - yorig);

        if (xorig > xend) {
            width1 = width2;
            width2 = ganttz.TaskComponent.CORNER_WIDTH;
        }

        // First segment
        var depstart = this._findImageElement('start');
        depstart.css({    top : yorig,
                        left : (xorig - width1),
                        width : width ,
                        display : 'inline'})

        // Second segment
        var depmid = this._findImageElement('mid');
        var depmidcss = {left : depstart.css('left'), height : height};

        if (yend > yorig) {
          depmidcss.top = yorig;
        } else {
          depmidcss.top = yend;
        }

        depmid.css(depmidcss);

        // Third segment
        var depend = this._findImageElement('end');
        depend.css({    top : yend,
                        left : depstart.css('left'),
                        width : width2 - ganttz.TaskComponent.HALF_HEIGHT});

        var deparrow = this._findImageElement('arrow');
        deparrow.css({top : (yend - ganttz.TaskComponent.HALF_HEIGHT),left : xend - 15});
    },
    _drawArrowEND_END : function(coordOrig, coordDest){
        var xorig = coordOrig.left - this.$class.DEPENDENCY_PADDING;
        var yorig = coordOrig.top - ganttz.TaskComponent.CORNER_WIDTH/2 + this.$class.HALF_DEPENDENCY_PADDING;
        var xend = coordDest.left + this.$class.HALF_DEPENDENCY_PADDING;
        var yend = coordDest.top - this.$class.DEPENDENCY_PADDING;

        width1 = Math.abs(xend - xorig) + ganttz.TaskComponent.CORNER_WIDTH;
        width2 = ganttz.TaskComponent.CORNER_WIDTH;
        height = Math.abs(yend - yorig);

        if (xorig > xend) {
            width2 = width1;
            width1 = ganttz.TaskComponent.CORNER_WIDTH;
        }

        // First segment
        var depstart = this._findImageElement('start');
        var depstartcss = {left : xorig, width : width1, display : 'inline'}
        if (yend > yorig)
            depstartcss.top = yorig ;
        else
            depstartcss.top = yorig + ganttz.TaskComponent.HEIGHT;

        depstart.css(depstartcss);

        // Second segment
        var depmid = this._findImageElement('mid');
        var depmidcss = {left : (xorig + width1), height : height};
        if (yend > yorig) {
          depmidcsstop = yorig;
        } else {
          depmidcss.top = yend;
          depmidcss.height = height + 10;
        }
        depmid.css(depmidcss);

        // Third segment
        var depend = this._findImageElement('end');
        depend.css({    left : (xorig + width1 - width2),
                        top:yend,
                        width:width2 });


        var deparrow = this._findImageElement('arrow');
        deparrow.attr('src', this.$class.getImagesDir() + "arrow3.png");
        deparrow.css({top : yend - 5, left : xend - 8});
    },
    _drawArrowEND_START : function(coordOrig, coordDest){
        var xorig = coordOrig.left - this.$class.DEPENDENCY_PADDING;
        var yorig = coordOrig.top - this.$class.HALF_DEPENDENCY_PADDING;
        var xend = coordDest.left - this.$class.DEPENDENCY_PADDING;
        var yend = coordDest.top - this.$class.HALF_DEPENDENCY_PADDING;

        var width = (xend - xorig);
        var xmid = xorig + width;

        // First segment not used
        var depstart = this._findImageElement('start');
        depstart.hide();

        // Second segment not used
        var depmid = this._findImageElement('mid');
        var depmidcss;
        if (yend > yorig)
            depmidcss = {top : yorig, height : yend - yorig};
        else
            depmidcss = {top : yend, height : (yorig - yend)};

        depmidcss.left = xorig;
        depmid.css(depmidcss);

        var depend = this._findImageElement('end');
        depend.css({top : yend, left : xorig, width : width});

        if (width < 0) depend.css({left : xend + 'px', width : width + 'px'});

        var deparrow = this._findImageElement('arrow');
        var deparrowsrc, deparrowcss;
        if ( width == 0 ) {
            deparrowcss = {top : (yend - 10) , left : (xend - 5)};
            deparrowsrc = this.$class.getImagesDir() + "arrow2.png";
            if ( yorig > yend ) {
                deparrowcss = {top : yend};
                deparrowsrc = this.$class.getImagesDir() + "arrow4.png";
            }
        } else {
            deparrowcss = {top : (yend -5), left : (xend - 10)};
            deparrowsrc = this.$class.getImagesDir() + "arrow.png";
            if (width < 0) {
                deparrowcss = {top : (yend - 5), left : xend}
                deparrowsrc = this.$class.getImagesDir() + "arrow3.png";
            }
        }
        deparrow.attr('src', deparrowsrc);
        deparrow.css(deparrowcss);
    },
    _findPos : function(element){
        var pos1 = jq('#listdependencies').offset();
        var pos2 = element.offset();
        return {left : (pos2.left - pos1.left), top : (pos2.top - pos1.top)};
    },
    _findImageElement : function(name) {
        var img = jq('#' + this.uuid + ' > img[class*=' + name + ']');
        return img;
    },
    _initializeProperties : function(){
        this._origin = jq('#' + this.getIdTaskOrig());
        this._destination = jq('#' + this.getIdTaskEnd());
    },
    _setupArrow : function(){
        var image_data = [ [ "start", "pixel.gif" ], [ "mid", "pixel.gif" ],
                            [ "end", "pixel.gif" ], [ "arrow", "arrow.png" ] ];
        var img;
        var insertPoint = jq(this.$n());
        for ( var i = 0; i < image_data.length; i++) {
                img = jq(document.createElement('img'));
                img.attr({
                    'class' : image_data[i][0] + " extra_padding",
                    'src'    : this.$class.getImagesDir() + image_data[i][1]
                });

                insertPoint.append(img);
        }
    }
},{
    END_END        : "END_END",
    END_START    : "END_START",
    START_START    : "START_START",
    HALF_DEPENDENCY_PADDING : 2,
    DEPENDENCY_PADDING : 4,
    getImagesDir : function(){
        return "/" + common.Common.webAppContextPath() + "/zkau/web/ganttz/img/";
    }
})