zk.$package("ganttz");

ganttz.TaskList = zk.$extends(zk.Widget, {
	$init : function(){
		this.$supers('$init', arguments);
		this.$class.setInstance(this);
	},
	showAllTaskLabels : function(){
		for(var child = this.firstChild; child; child = child.nextSibling)
            child.showTaskLabel();
	},
	hideAllTaskLabels : function(){
		for(var child = this.firstChild; child; child = child.nextSibling)
            child.hideTaskLabel();
	}
},{//Class stuff
	setInstance : function(instance){
		this.instance = instance;
	},
	getInstance	: function(){
		return this.instance;
	}
});