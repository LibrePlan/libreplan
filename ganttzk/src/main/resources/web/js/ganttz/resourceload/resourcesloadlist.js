var TIMETRACKER_OFFSET_TOP = 120;

zkResourcesLoadList = initializeClass( {});

function initializeClass(classObject) {
    function watermark() {
        return document.getElementById('watermark');
    }

    function timetracker() {
        return document.getElementById('timetracker');
    }

    classObject.init = function(cmp) {
        this.adjustTimeTrackerSize(cmp);
        YAHOO.util.Event.addListener(window, 'resize',
                zkResourcesLoadList.adjustTimeTrackerSize, cmp);
    }

    classObject.adjustTimeTrackerSize = function(cmp) {
        watermark().style["height"] = cmp.clientHeight + "px";
        timetracker().style["width"] = cmp.clientWidth + "px";
    }
    return classObject;
}