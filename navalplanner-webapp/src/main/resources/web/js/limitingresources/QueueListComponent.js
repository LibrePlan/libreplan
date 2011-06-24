zk.$package("limitingresources");

limitingresources.QueueListComponent = zk.$extends(zk.Widget,{
    $init: function() {
        this.$supers('$init', arguments);
        common.Common.mixInDayPositionRestorer(this);
    },
    bind_: function() {
        this.$supers('bind_', arguments);
    },
    unbind_ : function() {
        this.$supers('unbind_', arguments);
    },
    _divsToRestoreDayInto: function() {
        var first = this.$n();
        return [first, first.parentNode, first.parentNode.parentNode];
    },
	adjustResourceLoadRows : function() {
		var width = jq('.rightpanellayout #timetracker .z-grid-header :first').innerWidth();
		jq('.row_resourceload').each(function(index, element){
			jq(element).width(width);
		});
	},
	adjustTimeTrackerSize: function() {
        var limitingResourcesList = jq('.limitingresourceslist');
        jq('#watermark').height(this.$n().clientHeight);
        jq('#timetracker').width(this.$n().clientWidth).each(function() {
            // timetracker exists
            var limitingResourcesList = jq('.limitingresourceslist');
            limitingResourcesList.width(jq('.second_level_').get(0).clientWidth);
            jq('.rightpanellayout tr#watermark td').height(limitingResourcesList.get(0).clientHeight + 120);
        });
    }
});