zk.$package("limitingresources");

limitingresources.QueueListComponent = zk.$extends(zk.Widget,{
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