zk.$package("ganttz");

/*
 * This YAHOO code is here because it's used for the Drag&Drop. Once the Drag&Drop is implemented with jQuery
 * this code must be removed
 * */
YAHOO.example.DDRegion = function(id, sGroup, config) {
    this.cont = config.cont;
    YAHOO.example.DDRegion.superclass.constructor.apply(this, arguments);
};

var myDom = YAHOO.util.Dom, myEvent = YAHOO.util.Event

YAHOO.extend(YAHOO.example.DDRegion, YAHOO.util.DD, {
    cont : null,
    init : function() {
        // Call the parent's init method
    YAHOO.example.DDRegion.superclass.init.apply(this, arguments);
    this.initConstraints();

    myEvent.on(window, 'resize', function() {
        this.initConstraints();
    }, this, true);
},
initConstraints : function() {

    // Get the top, right, bottom and left positions
    var region = myDom.getRegion(this.cont);

    // Get the element we are working on
    var el = this.getEl();

    // Get the xy position of it
    var xy = myDom.getXY(el);

    // Get the width and height
    var width = parseInt(myDom.getStyle(el, 'width'), 10);
    var height = parseInt(myDom.getStyle(el, 'height'), 10);

    // Set left to x minus left
    var left = xy[0] - region.left;

    // Set right to right minus x minus width
    var right = region.right - xy[0] - width;

    // Set top to y minus top
    var top = xy[1] - region.top;

    // Set bottom to bottom minus y minus height
    var bottom = region.bottom - xy[1] - height;

    // Set the constraints based on the above calculations
    this.setXConstraint(left, right);
    this.setYConstraint(top, bottom);
}
});


ganttz.TaskComponent = zk.$extends(zul.Widget, {
    $define :{
        resourcesText    : null,
        labelsText    : null,
        tooltipText : null
    },
    $init : function(){
        this.$supers('$init', arguments);

         /*
         * We have to implement the setLeft method because if we use the one provided by ZK
         * the tasks won't we moved back to its original position when they are dropped on an invalid position (i.e before the end
         * of the main task) on a dependency relation
         *
         * This is the default boddy for a ZK set<AttributeName>
         *
         * function (v, opts) {
                if (before) v = before.apply(this, arguments);
                var o = this[nm];
                this[nm] = v;
                if (after && (o !== v || (opts && opts.force)))
                    after.apply(this, arguments);
                return this;
            };
         *
         * The before and and after properties can be set to something different to  the default using the $define property.
         *
         *
         * Our problem happens because if the dependent task is already aligned at the end of the main tasks
         * and thats (for example) style. left = 800px, when we move it to an invalid position the server will try to set again
         * the left property to 800px but when the default setter works it checks if we are trying to set a value equal to the previous
         * one and in that case it doesn't apply the after function.
         *
         * Setting the force option to true does the trick
         * */
        var oldSetLeft = this.setLeft;
        this.setLeft = this.proxy(function(left, options){
            oldSetLeft.call(this, left, {force : true});
        })
    },
    bind_ : function(event){
        this.$supers('bind_', arguments);
        this.domListen_(this.$n(), "onMouseover", '_showTooltip');
        this.domListen_(this.$n(), "onMouseout", '_hideTooltip');
        if( jq(this.$n()).attr('movingtasksenabled') == "true" ) this._addDragDrop();
        if( jq(this.$n()).attr('resizingtasksenabled') == "true" ) this._addResize();
    },
    unbind_ : function(event){
        this.domUnlisten_(this.$n(), "onMouseout", '_hideTooltip');
        this.domUnlisten_(this.$n(), "onMouseover", '_showTooltip');
        this.$supers('unbind_', arguments);
    },
    addDependency : function(){
        this._createArrow();
    },
    consolidateNewDependency : function(task){
        zAu.send(new zk.Event(this, 'onAddDependency', {dependencyId : task.id}));
    },
    _addDragDrop : function(){
        var dragdropregion = this._getDragDropRegion();
        var thisTaskId = this.$n().id;
        var relatedDependencies = common.Common.throttle(3000, function() {
            return jq('.dependency[idtaskorig='+ thisTaskId + ']')
                    .add('.dependency[idtaskend='+ thisTaskId + ']')
                    .get()
                    .map(function(dep) {
                        return ganttz.DependencyComponentBase.$(dep);
                    });
        });
        var drawDependencies = common.Common.throttle(25, function() {
            relatedDependencies().forEach(function(dependency) {
                dependency.draw();
            });
        });
        dragdropregion.on('dragEvent', this.proxy(function(ev) {
            // Slight overload. It could be more efficent to overwrite the YUI
            // method
            // that is setting the top property
                jq(this.$n()).css('top','');
                drawDependencies();
            }), null, false);
         // Register the event endDragEvent
        dragdropregion.on('endDragEvent', this.proxy(function(ev) {
            var position = jq(this.$n()).position();
            zAu.send(new zk.Event(this, 'onUpdatePosition',{left : position.left, top : position.top}))
        }), null, false);
    },
    _addResize : function(){
        // Configure the task element to be resizable
        var resize = new YAHOO.util.Resize(this.uuid, {
            handles : [ 'r' ],
            proxy : true
        });

        resize.on("resize", function(event){
            jq(this.$n()).css({top : ""});
            zAu.send(new zk.Event(this, 'onUpdateWidth', { width : jq(this.$n()).width() }));
        },null , this);
    },
    _createArrow : function(){
        var WGTdependencylist = ganttz.DependencyList.getInstance();
        var unlinkedDependency = new ganttz.UnlinkedDependencyComponent();
        unlinkedDependency.setOrigin(this.$n());

        WGTdependencylist.appendChild(unlinkedDependency, false);

        unlinkedDependency.draw();
    },
    _getDragDropRegion : function(){
        if (typeof (this._dragDropRegion) == 'undefined') {
            // Create the laned drag&drop component
            this._dragDropRegion = new YAHOO.example.DDRegion(this.uuid, '', {
                cont : this.parent.getId()
            });
        }
        return this._dragDropRegion;
    },
    _showTooltip : function(){
        this.mouseOverTask = true;
        this._tooltipTimeout = setTimeout(jq.proxy(function(offset) {
            var element = jq("#tasktooltip" + this.uuid);
            if (element!=null) {
                element.show();
                offset = ganttz.GanttPanel.getInstance().getXMouse()
                        - element.parent().offset().left
                        - jq('.leftpanelcontainer').offsetWidth
                        - this.$class._PERSPECTIVES_WIDTH
                        + jq('.rightpanellayout div').scrollLeft();
                element.css( 'left' , offset +'px' );
            }
        }, this), this.$class._TOOLTIP_DELAY);
    },
    _hideTooltip : function(){
        this.mouseOverTask = false;
        if (this._tooltipTimeout) {
            clearTimeout(this._tooltipTimeout);
        }
        jq('#tasktooltip' + this.uuid).hide();
    },
    moveDeadline : function(width){
        jq('#deadline' + this.parent.uuid).css('left', width);
    },
    moveConsolidatedline : function(width){
        jq('#consolidatedline' + this.parent.uuid).css('left', width);
    },
    resizeCompletionAdvance : function(width){
        jq('#' + this.uuid + ' > .completion:first').css('width', width);
    },
    resizeCompletion2Advance : function(width){
        jq('#' + this.uuid + ' > .completion2:first').css('width', width);
    },
    showResourceTooltip : function(){
        jq('#'+ this.uuid + ' .task-resources').show();
    },
    hideResourceTooltip : function(){
        jq('#'+ this.uuid + ' .task-resources').hide();
    },
    setClass : function(cssClass){
        jq(this.$n()).addClass(cssClass);
    }
},{
    //"Class" methods and properties
    _TOOLTIP_DELAY : 10, // 10 milliseconds
    _PERSPECTIVES_WIDTH : 80,
    CORNER_WIDTH : 20,
    HEIGHT : 10,
    HALF_HEIGHT : 5,
    allTaskComponents: function() {
        var tasksArray = jq('div[z\\.type="ganttz.task.Task"]')
            .add('div[z\\.type="ganttz.taskcontainer.TaskContainer"]');
        return jq.map(tasksArray, function(element) {
            return ganttz.TaskComponent.$(element.id);
        });
    }
});