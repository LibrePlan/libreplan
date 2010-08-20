zk.$package("ganttz.resourceload");

ganttz.resourceload.ResourceLoadList = zk.$extends(zk.Widget,{
    $init : function(){
        this.$super('init');
        this.$class.setInstance(this);
    }
},{ //Class stuff
    setInstance : function(instance){
        this._instance = instance;
    },
    getInstance : function(){
        return this._instance;
    }
});