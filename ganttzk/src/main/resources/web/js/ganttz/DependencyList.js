zk.$package("ganttz");

ganttz.DependencyList = zk.$extends(zk.Widget, {
	appendChild : function(child){
		jq('#listdependencies').append(child);
	}
});