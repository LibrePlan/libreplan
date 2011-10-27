zk.$package("ganttz");

ganttz.TaskRow = zk.$extends(zk.Widget, {
    hideResourceTooltip : function(){
        this.firstChild.hideResourceTooltip();
    },
    showResourceTooltip : function(){
        this.firstChild.showResourceTooltip();
    }
});