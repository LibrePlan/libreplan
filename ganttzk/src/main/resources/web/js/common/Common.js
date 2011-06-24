zk.$package("common");

common.Common = zk.$extends(zk.Widget,{},{

	webAppContextPath : function(){ return window.location.pathname.split( '/' )[1];},

	throttle: function(timeoutTimeMillis, functionToExecute) {
	    var lastTimeCalled = null;
	    var cachedResult = null;
	    return function() {
	        var now = Date.now();
	        if (lastTimeCalled !== null && ((now - lastTimeCalled) < timeoutTimeMillis)) {
	            return cachedResult;
	        }
	        lastTimeCalled = now;
	        cachedResult = functionToExecute.apply(null, arguments);
	        return cachedResult;
	    };
	}
});