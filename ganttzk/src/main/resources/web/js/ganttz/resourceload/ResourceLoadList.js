zk.$package("ganttz.resourceload");

ganttz.resourceload.ResourceLoadList = zk.$extends(zk.Widget,{
    $init : function(){
        this.$super('$init');
        this.$class.setInstance(this);
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