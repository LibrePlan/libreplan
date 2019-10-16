zk.$package("ganttz");

ganttz.TaskList = zk.$extends(
    zk.Widget,
    {
        $init : function() {
            this.$supers('$init', arguments);
            this.$class.setInstance(this);
        },

        showAllTaskLabels : function() {
            for (var child = this.firstChild; child; child = child.nextSibling)
                child.showLabels();
        },

        hideAllTaskLabels : function() {
            for (var child = this.firstChild; child; child = child.nextSibling)
                child.hideLabels();
        },

        showResourceTooltips : function() {
            for (var child = this.firstChild; child; child = child.nextSibling)
                child.showResourceTooltip();
        },

        hideResourceTooltips : function() {
            for(var child = this.firstChild; child; child = child.nextSibling)
                child.hideResourceTooltip();
        },

        legendResize : function() {
            console.log("LegendResize");
            // Calculate width dependant to the preceeding elements. Otherwise
            // the elements will be horizontally shifted.
            var taskdetailsContainer = jq('.taskdetailsContainer')[0];
            var tabControl = jq('.legend-container').closest('.z-tabbox').find('.charts-tabbox');
            jq('.legend-container').width(taskdetailsContainer.clientWidth - tabControl.width()); // 75px is the width of the tabs on the left
        },

        refreshTooltips : function() {
            if (jq('.show-resources').hasClass('clicked')) {
                this.showResourceTooltips();
            }
            if (jq('.show-labels').hasClass('clicked')) {
                this.showAllTaskLabels();
            }
        }
    },
    {
        // Class stuff
        setInstance : function(instance) {
            this.instance = instance;
        },

        getInstance    : function(){
            return this.instance;
        }
    });
