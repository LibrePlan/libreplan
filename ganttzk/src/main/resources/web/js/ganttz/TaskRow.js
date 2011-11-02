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
    _resourcesHidden : true,
    hideResourceTooltip : function(){
        this._resourcesHidden = true;
        this.firstChild.hideResourceTooltip();
    },
    showResourceTooltip : function(){
        this._resourcesHidden = false;
        this.firstChild.showResourceTooltip();
    }
});