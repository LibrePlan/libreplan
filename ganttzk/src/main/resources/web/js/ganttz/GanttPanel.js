zk.$package("ganttz");

ganttz.GanttPanel = zk.$extends(zk.Widget,{
	$define: {
		xMouse : null,
		yMouse : null
	},
	$init : function(){
		this.$super('$init');
		if (!this.$class.getInstance()){
			this.$class.setInstance(this);
		}
		else
			throw "There's already one GanttPanel"
	},
	bind_ : function(evt){
		this.$supers('bind_', arguments);
		this.domListen_(this.$n(), "onMousemove", '_calcXY');
	},
	unbind_ : function(evt){
		this.domUnListen_(this.$n(), "onMousemove", '_calcXY');
		this.$supers('unbind_', arguments);
	},
	_calcXY : function(event){
		var arrPos = YAHOO.util.Event.getXY(event);
		this.setXMouse(arrPos[0]);
		this.setYMouse(arrPos[1]);
	}
},{
	getInstance : function(){
		return this._instance;
	},
	setInstance : function(instance){
		this._instance = instance;
	}
});