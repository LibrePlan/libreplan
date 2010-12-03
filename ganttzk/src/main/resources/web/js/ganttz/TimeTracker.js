zk.$package("ganttz");

ganttz.TimeTracker = zk.$extends(zk.Macro,{
	$init : function(){
    this.$supers('$init', arguments);
    	this.$class.setInstance(this);
	}
},{
    getInstance : function(){
    	return this._instance;
	},
	setInstance : function(instance){
		this._instance = instance;
	}
})