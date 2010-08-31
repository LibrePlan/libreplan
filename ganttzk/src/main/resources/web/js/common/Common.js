zk.$package("common");

common.Common = zk.$extends(zk.Widget,{},{
	webAppContextPath : function(){ return window.location.pathname.split( '/' )[1];}
})