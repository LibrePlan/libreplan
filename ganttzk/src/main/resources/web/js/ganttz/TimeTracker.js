zk.$package("ganttz");

ganttz.TimeTracker = zk.$extends(zk.Macro,{
    $init : function(){
        this.$supers('$init', arguments);
        this.$class.setInstance(this);
    },
    bind_ : function (){
        this.$supers('bind_', arguments);
        this._timetrackergap = jq('.timetrackergap');
    },
    scrollLeft : function(ammount){
        this._timetrackergap.css({left : -ammount});
    }
},{
    getInstance : function(){
        return this._instance;
    },
    setInstance : function(instance){
        this._instance = instance;
    }
})