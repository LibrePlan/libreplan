zk.$package("ganttz");

ganttz.GanttPanel = zk.$extends(zk.Widget,{
    $define: {
        xMouse : null,
        yMouse : null
    },
    scrollDay: 0,
    $init : function(){
        this.$supers('$init', arguments);
        this.$class.setInstance(this);
    },
    bind_ : function(evt){
        this.$supers('bind_', arguments);

        this._initializeProperties();

        this.domListen_(this.$n(), 'onMousemove', '_calcXY');
        this.domListen_(this._rightpannellayout, 'onScroll', '_listenToScroll');
    },
    unbind_ : function(evt){
        this.domUnlisten_(this._rightpannellayout, 'onScroll', '_listenToScroll');
        this.domUnlisten_(this.$n(), 'onMousemove', '_calcXY');
        this.$supers('unbind_', arguments);
    },
    timeplotContainerRescroll : function(){
        this._timeplotcontainer.each(jq.proxy(function(index, element){
            jq(element).css("left", "-" + this._rightpannellayout.scrollLeft() + "px")
            }, this));
    },
    adjust_height : function(){
        jq(this.$n()).height(jq('#scroll_container').height());
        ganttz.Planner.getInstance().adjustScrollableDimensions();
    },
    _calcXY : function(event){
        var arrPos = YAHOO.util.Event.getXY(event);
        this.setXMouse(arrPos[0]);
        this.setYMouse(arrPos[1]);
    },
    _listenToScroll : function(){
        this._timetrackergap.css("left","-" + this._rightpannellayout.scrollLeft() + "px");
        this._taskdetails.css("top", "-" + this._rightpannellayout.scrollTop() + "px");
        this._plannergraph.scrollLeft( this._rightpannellayout.scrollLeft() );
        this.timeplotContainerRescroll();
    },
    _initializeProperties : function(){
        /*The canvas is inserted in the DOM after this component so
         * it's not available right now. We set up a handler to do
         * job*/
        jq(document).ready(jq.proxy(
                function(){    this._timeplotcontainer = jq('canvas.timeplot-canvas');
            },this));

        this._timetrackergap    = jq('.timetrackergap');
        this._rightpannellayout    = jq('.rightpanellayout div:first');
        this._taskdetails        = jq('.listdetails .z-tree-body');
        this._plannergraph        = jq('.plannergraph:first');
    },
    reScrollY : function(px){
        jq('#ganttpanel_inner_scroller_y').height(px);
    },
    reScrollX : function(px){
        jq('#ganttpanel_inner_scroller_x').width(px);
    },
    /**
     * Scrolls horizontally the ganttpanel when the zoom has resized the component
     * width.
     */
    scroll_horizontal: function(daysDisplacement) {
        scrollDay = daysDisplacement;
    },

    // FIXME: this is quite awful, it should be simple
    update_day_scroll: function(previousPixelPerDay) {
        this._fromPixelToDay(previousPixelPerDay);
    },
    move_scroll: function(diffDays, pixelPerDay) {
        this._fromDayToPixel(diffDays,pixelPerDay);
    },
    _fromPixelToDay: function(previousPixelPerDay) {
        var div1 = document.getElementById ("ganttpanel").parentNode;
        var div2 = div1.parentNode;
        var div3 = div2.parentNode;

        var maxHPosition = div3.scrollWidth - div3.clientWidth;
        if( maxHPosition > 0 ){
            var proportion = div3.scrollWidth / maxHPosition;
            var positionInScroll = div3.scrollLeft;
            var positionInPx = positionInScroll * proportion;
            if(positionInPx > 0){
                var position = positionInPx / previousPixelPerDay;
                var day = position;
                this.scrollDay = position;
            }
        }
    },
    _fromDayToPixel: function(diffDays,pixelPerDay) {
        var div1 = document.getElementById ("ganttpanel").parentNode;
        var div2 = div1.parentNode;
        var div3 = div2.parentNode;

        var day = this.scrollDay;
        day += parseInt(diffDays);
        var newPosInPx = parseInt(day * pixelPerDay);
        var maxHPosition = div3.scrollWidth - div3.clientWidth;
        var newProportion = div3.scrollWidth / maxHPosition;
        if( newProportion > 0){
            var newPosInScroll = newPosInPx / newProportion;
            if(newPosInScroll < 0){
                newPosInScroll = 0;
            }
            div1.scrollLeft = newPosInScroll;
            div2.scrollLeft = newPosInScroll;
            div3.scrollLeft = newPosInScroll;
        }
    }

},{
    getInstance : function(){
        return this._instance;
    },
    setInstance : function(instance){
        this._instance = instance;
    }
});
