zk.$package("ganttz");

ganttz.DependencyComponent = zk.$extends(zk.Widget,{
	$define : {
		idTaskOrig : null,
		idTaskEnd : null,
		dependencyType : null
	},
	draw : function(){}
})