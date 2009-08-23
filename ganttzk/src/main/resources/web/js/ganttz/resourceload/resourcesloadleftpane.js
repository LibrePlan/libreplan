zkResourcesLoadLeftPane = {};

addResourcesLoadLeftPaneMethods({});

function addResourcesLoadLeftPaneMethods(object) {
    var scrollSync;

    object.init = function(cmp) {
        scrollSync = new ScrollSync(cmp);
        scrollSync.synchYChangeTo(cmp);
    };
    return object;
}
