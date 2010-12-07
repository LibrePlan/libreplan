zk.$package("ganttz");

ganttz.TimeTracker = zk.$extends(zk.Macro,{
    $init : function(){
        this.$supers('$init', arguments);
        this.$class.setInstance(this);
    },
    bind_ : function (){
        this.$supers('bind_', arguments);
        this._timetrackerGap = jq('.timetrackergap');
        this._timetrackerHeader = jq('#timetrackerheader .z-vbox');
    },
    realWidth : function(){
        return this._timetrackerHeader.width();
    },
    scrollLeft : function(ammount){
        this._timetrackerGap.css({left : -ammount});
    }
},{
    getInstance : function(){
        return this._instance;
    },
    setInstance : function(instance){
        this._instance = instance;
    }
})