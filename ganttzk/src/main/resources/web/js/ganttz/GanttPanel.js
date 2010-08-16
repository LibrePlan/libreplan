zk.$package("ganttz");

ganttz.GanttPanel = zk.$extends(zk.Widget,{
    $define: {
        xMouse : null,
        yMouse : null
    },
    $init : function(){
        this.$super('$init');
        if (!this.$class.getInstance()){
            this.$class.setInstance(this);
        }
        else
            throw "There's already one GanttPanel";
    },
    bind_ : function(evt){
        this.$supers('bind_', arguments);

        this._initializeProperties();

        this.domListen_(this.$n(), 'onMousemove', '_calcXY');
        this.domListen_(this._rightpannellayout, 'onScroll', '_listenToScroll');
    },
    unbind_ : function(evt){
        this.domUnlisten_(this._rightpannellayout, 'onScroll', '_listenToScroll');
        this.domUnListen_(this.$n(), 'onMousemove', '_calcXY');
        this.$supers('unbind_', arguments);
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
        this._timeplotcontainer.each(jq.proxy(function(index, element){
                jq(element).css("left", "-" + this._rightpannellayout.scrollLeft() + "px")
                }, this));
    },
    _initializeProperties : function(){
        /*The canvas is inserted in the DOM after this component so
         * it's not avaliable right now. We set up a handler to do
         * job*/
        jq(document).ready(jq.proxy(
                function(){    this._timeplotcontainer = jq('canvas.timeplot-canvas');
            },this));

        this._timetrackergap    = jq('.timetrackergap');
        this._rightpannellayout    = jq('.rightpanellayout div:first');
        this._taskdetails        = jq('.listdetails .z-tree-body');
        this._plannergraph        = jq('.plannergraph:first');
    }
},{
    getInstance : function(){
        return this._instance;
    },
    setInstance : function(instance){
        this._instance = instance;
    }
});