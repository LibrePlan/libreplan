zk.$package("ganttz");

ganttz.DependencyList = zk.$extends(zk.Widget, {
    $init : function(){
        this.$supers('$init', arguments);
        this.$class.setInstance(this);
    }
},{
    setInstance : function(instance){
        this._instance = instance;
    },
    getInstance : function(){
        return this._instance;
    }
});