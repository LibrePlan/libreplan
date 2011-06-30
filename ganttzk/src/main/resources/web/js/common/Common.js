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
	},
	/**
	 * It can be called in the constructor of a widget.
	 * It is required that the widget has the method _divsToRestoreDayInto that returns
	 * the divs which their scroll must be changed back to the previous day.
	 */

// TODO: Refactoring should be done, not so many methods should be needed to synchronize the day.
	mixInDayPositionRestorer: function(widget) {
	    if (! ('_divsToRestoreDayInto' in widget)) {
	        throw '_divsToRestoreDayInto function must be present in widget';
	    }
	    var scrollDay = 0;
	    /**
	     * Scrolls horizontally the ganttpanel when the zoom has resized the component
	     * width.
	     */
	    widget.scroll_horizontal = function(daysDisplacement) {
            scrollDay = daysDisplacement;
        };
        widget.update_day_scroll = function(previousPixelPerDay) {
            var divs = this._divsToRestoreDayInto();
            var topScrollDiv = divs[divs.length - 1];

            var maxHPosition = topScrollDiv.scrollWidth - topScrollDiv.clientWidth;
            if (maxHPosition > 0) {
                var proportion = topScrollDiv.scrollWidth / maxHPosition;
                var positionInScroll = topScrollDiv.scrollLeft;
                var positionInPx = positionInScroll * proportion;
                if (positionInPx > 0) {
                    scrollDay = positionInPx / previousPixelPerDay;
                }
            }
        };
        widget.move_scroll = function(diffDays, pixelPerDay) {
            var divs = this._divsToRestoreDayInto();
            var topScrollDiv = divs[divs.length - 1];

            var day = this.scrollDay + parseInt(diffDays);
            var newPosInPx = parseInt(day * pixelPerDay);
            var maxHPosition = topScrollDiv.scrollWidth - topScrollDiv.clientWidth;
            var newProportion = topScrollDiv.scrollWidth / maxHPosition;
            if (newProportion > 0) {
                var newPosInScroll = newPosInPx / newProportion;
                if (newPosInScroll < 0) {
                    newPosInScroll = 0;
                }
                for ( var i = 0; i < divs.length; i++) {
                    divs[i].scrollLeft = newPosInScroll;
                }
            }
        };
	}
});