zk.$package("ganttz");

ganttz.TaskRow = zk.$extends(zk.Widget, {
    _labelsHidden : true,
    showLabels : function(){
        this._labelsHidden = false;
        this.firstChild.showLabels();
    },
    hideLabels : function(){
        this._labelsHidden = true;
        this.firstChild.hideLabels();
    },
    hideResourceTooltip : function(){
        this.firstChild.hideResourceTooltip();
    },
    showResourceTooltip : function(){
        this.firstChild.showResourceTooltip();
    }
});