zk.$package("ganttz.resourceload");

ganttz.resourceload.ResourceLoadList = zk.$extends(zk.Widget,{
    $init : function(){
        this.$supers('$init', arguments);
        this.$class.setInstance(this);
        common.Common.mixInDayPositionRestorer(this);
    },
    bind_ : function(evt){
        this.$supers('bind_', arguments);
        this.domListen_(jq(window), 'onResize', 'adjustTimeTrackerSize');
        this.domListen_(jq('.rightpanellayout div:first'), 'onScroll', '_listenToScroll');
    },
    unbind_ : function(evt){
        this.domUnlisten_(jq(window), 'onResize', 'adjustTimeTrackerSize');
        this.domUnlisten_(jq('.rightpanellayout div:first'), 'onScroll', '_listenToScroll');
        this.$supers('unbind_', arguments);
    },
    _divsToRestoreDayInto: function() {
        var first = this.$n();
        return [first, first.parentNode, first.parentNode.parentNode];
    },
    recalculateTimeTrackerHeight : function(){
        var DOMResourceLoadList = jq('.resourceloadlist');
        var DOMfirstWatermarkColumn = jq('.rightpanellayout tr#watermark td :first');

        if ( DOMResourceLoadList != undefined && DOMfirstWatermarkColumn != undefined){
            DOMResourceLoadList.height(
                    Math.max(
                            DOMResourceLoadList.innerHeight() + this.$class.WATERMARK_MARGIN_BOTTOM,
                            this.$class.WATERMARK_MIN_HEIGHT));
        }
    },
    adjustTimeTrackerSize : function(){
        this.recalculateTimeTrackerHeight();

        /*We can't use this.getHeight() as the _height property
         * won't be set for this object and even
         *
         * TODO: maybe create a _height property and update it
         * when it changes (recalculateTimeTrackerHeight) son we avoid
         * using DOM selectors
         * */
        jq('#watermark').height(jq(this.$n()).innerHeight());
        jq('#timetracker').width(jq(this.$n()).innerWidth());

        /*this.$n() is <div class="resourceloadlist" ...>*/
        jq(this.$n()).width(jq('.second_level_ :first'));
    },
    adjustResourceLoadRows : function(){
        jq('.row_resourceload').each(jq.proxy(function(index, element){
            jq(element).width( jq(this.$n()).innerWidth() );
        }, this));
    },
    _listenToScroll : function(){
        var scrolledPannelScrollLeft = jq('.rightpanellayout div:first').scrollLeft();
        var scrolledPannelScrollTop = jq('.rightpanellayout div:first').scrollTop();

        jq('canvas.timeplot-canvas').css("left", "-" + scrolledPannelScrollLeft + "px");
        jq('.timetrackergap').css("left", "-" + scrolledPannelScrollLeft + "px");
        jq('.leftpanelgap .z-tree-body').css("top", "-" + scrolledPannelScrollTop + "px");
        jq('.resourcesloadgraph div').scrollLeft(scrolledPannelScrollLeft + "px");
    }
},{ //Class stuff
    WATERMARK_MIN_HEIGHT : 450,
    WATERMARK_MARGIN_BOTTOM : 40,
    setInstance : function(instance){
        this._instance = instance;
    },
    getInstance : function(){
        return this._instance;
    }
});