zk.$package("ganttz");

ganttz.DependencyComponent = zk.$extends(zk.Widget,{
	$define : {
		idTaskOrig : null,
		idTaskEnd : null,
		dependencyType : null
	},
	$bind : function(){
		this.$supers('$bind', arguments);
		this._initializeProperties();
	}
	draw : function(){},
	_initializeProperties : function(){
		this._origin = jq('#' + this.getIdTaskOrig());
		this._destination = jq('#' + this.getIdTaskEnd());
	}
})