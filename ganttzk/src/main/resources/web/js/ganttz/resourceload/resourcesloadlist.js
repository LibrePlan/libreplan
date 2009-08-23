var TIMETRACKER_OFFSET_TOP = 120;

zkResourcesLoadList = addResourcesLoadListMethods({});

function addResourcesLoadListMethods(object) {
    var scrollSync;

    function watermark() {
        return document.getElementById('watermark');
    }

    function timetracker() {
        return document.getElementById('timetracker');
    }

    object.init = function(cmp) {
        this.adjustTimeTrackerSize(cmp);
        YAHOO.util.Event.addListener(window, 'resize',
                zkResourcesLoadList.adjustTimeTrackerSize, cmp);
        scrollSync = new ScrollSync(cmp);
        scrollSync.synchXChangeTo(timetracker);
    }

    object.adjustTimeTrackerSize = function(cmp) {
        watermark().style["height"] = cmp.clientHeight + "px";
        timetracker().style["width"] = cmp.clientWidth + "px";
    }
    return object;
}