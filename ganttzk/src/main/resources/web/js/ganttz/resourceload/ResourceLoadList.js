zk.$package("ganttz.resourceload");

ganttz.resourceload.ResourceLoadList = zk.$extends(zk.Widget,{
    $init : function(){
        this.$supers('$init', arguments);
        this.$class.setInstance(this);
    },
    bind_ : function(evt){
        this.$supers('bind_', arguments);
        this.domListen_(jq(window), 'onResize', 'adjustTimeTrackerSize');
    },
    unbind_ : function(evt){
        this.domUnlisten_(jq(window), 'onResize', 'adjustTimeTrackerSize');
        this.$supers('unbind_', arguments);
    },
    recalculateTimeTrackerHeight : function(){
        var DOMresourceLoadList = jq('.resourceloadlist');
        var DOMfirstWatermarkColumn = jq('.rightpanellayout tr#watermark td :first');

        if ( DOMresourceLoadList != undefined && DOMfirstWatermarkColumn != undefined){
            DOMresourceLoadList.height(
                    Math.max(
                            DOMresourceLoadList.innerHeight() + this.$class.WATERMARK_MARGIN_BOTTOM,
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

        jq('.resourceloadlist').width(jq('.second_level_ :first'));
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