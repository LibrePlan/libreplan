zk.$package("ganttz");

ganttz.TaskList = zk.$extends(zk.Widget, {
    $init : function(){
        this.$supers('$init', arguments);
        this.$class.setInstance(this);
    },
    showAllTaskLabels : function(){
        for(var child = this.firstChild; child; child = child.nextSibling)
            child.showLabels();
    },
    hideAllTaskLabels : function(){
        for(var child = this.firstChild; child; child = child.nextSibling)
            child.hideLabels();
    },
    showResourceTooltips : function(){
        for(var child = this.firstChild; child; child = child.nextSibling)
            child.showResourceTooltip();
    },
    hideResourceTooltips : function(){
        for(var child = this.firstChild; child; child = child.nextSibling)
            child.hideResourceTooltip();
    },
    legendResize : function() {
        var taskdetailsContainer = jq('.taskdetailsContainer')[0];
        jq('.legend-container').width(taskdetailsContainer.clientWidth-75);
    },
    refreshTooltips : function() {
        if (jq('.show-resources').hasClass('clicked')) {
            this.showResourceTooltips();
        }
        if (jq('.show-labels').hasClass('clicked')) {
            this.showAllTaskLabels();
        }
    }

},{//Class stuff
    setInstance : function(instance){
        this.instance = instance;
    },
    getInstance    : function(){
        return this.instance;
    }
});
