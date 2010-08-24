zk.$package("limitingresources");

limitingresources.QueueListComponent = zk.$extends(zk.Widget,{
	adjustResourceLoadRows : function(){
		var width = jq('.rightpanellayout #timetracker .z-grid-header :first').innerWidth();
		jq('.row_resourceload').each(function(index, element){
			jq(element).width(width);
		});
	}
})