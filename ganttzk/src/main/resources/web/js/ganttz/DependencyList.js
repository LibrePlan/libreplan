zk.$package("ganttz");

ganttz.DependencyList = zk.$extends(zk.Widget, {
	appendChild : function(child){
		//true for ignoring DOM insertion
		this.$supers('appendChild',[child,true]);
		jq('#listdependencies').append(child);
	}
});