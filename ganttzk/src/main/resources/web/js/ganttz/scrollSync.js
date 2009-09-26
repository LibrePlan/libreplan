function ScrollSync(element){
    var xChanges = [];
    var yChanges = [];
    var notifyScrollX = function(){
        for ( var i = 0; i < xChanges.length; i++) {
            xChanges[i]();
        }
    };
    var notifyScrollY = function(){
        for ( var i = 0; i < yChanges.length; i++) {
            yChanges[i]();
        }
    };
    var notifyListeners = function(){
        notifyScrollX();
        notifyScrollY();
    };
    var toFunction = function(value){
        var result = value;
        if(typeof(value) !== 'function'){
            result = function(){return synched};
        }
        return result;
    };

    this.synchXChangeTo = function(synched){
        var target = toFunction(synched);
        xChanges.push(function(){ target().scrollLeft = element.scrollLeft; });
    };
    this.synchYChangeTo = function(synched){
        var target = toFunction(synched);
        yChanges.push(function(){ target().scrollTop = element.scrollTop; });
    };

    this.notifyXChangeTo = function(listenerReceivingScroll){
        xChanges.push(function(){
            listenerReceivingScroll(element.scrollLeft);
        });
    };

    this.notifyYChangeTo = function(listenerReceivingScroll){
        yChanges.push(function() {
            listenerReceivingScroll(element.scrollTop);
        });
    };

    YAHOO.util.Event.addListener(element,'scroll', notifyListeners);
    return this;
}