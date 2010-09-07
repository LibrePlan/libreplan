zk.$package("ganttz");

ganttz.DependencyList = zk.$extends(zk.Widget, {
	$init : function(){
		this.$supers('$init', arguments);
		this.$class.setInstance(this);
	},
	appendChild : function(child){
		//true for ignoring DOM insertion
		this.$supers('appendChild',[child,true]);
		jq('#listdependencies').append(child);
	}
},{
	setInstance : function(instance){
		this._instance = instance;
	},
	getInstance : function(){
		return this._instance;
	}
});