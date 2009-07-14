/**
 *
 * taskContainer.js
 *
 */


zkTaskContainer = {};

/* We will not allow taskcontainer move or resize untill its behaviour its
 * clearly specified
zkTaskContainer.getDD = function(cmp) {
    zkTask.getDD(cmp);
}; */

zkTaskContainer.relocateAfterAdding = function(cmp) {
    zkTask.relocateAfterAdding (cmp);
};

zkTaskContainer.cleanup = function(cmp){
    zkTask.cleanup(cmp);
};

zkTaskContainer.init = function(cmp) {
/* We will not allow taskcontainer move or resize untill its behaviour its
 * clearly specified
    zkTask.init(cmp); */
};

zkTaskContainer.xMouse;
zkTaskContainer.yMouse;

// Listen to mousemove events
YAHOO.util.Event.on(document.body, 'mousemove', function(e) {
	var arrPos = YAHOO.util.Event.getXY(e);
	zkTaskContainer.xMouse = arrPos[0];
	zkTaskContainer.yMouse = arrPos[1];
});

zkTaskContainer.setAttr = function(cmp, nm, val) {
    zkTask.setAttr(cmp, nm, val);
};

zkTaskContainer.addDependency = function(cmp) {
    zkTask.addDependency(cmp);
};

zkTaskContainer.setAttr = function(cmp, name, value) {
    zkTask.setAttr(cmp, name, value);
};

zkTaskContainer.addRelatedDependency = function(cmp, dependency) {
    zkTask.addRelatedDependency(cmp, dependency);
};

/* Dependencies with origin in a task container will be redrawn with a
 * different algorithm */
zkTaskContainer.createArrow = function(cmp) {
    zkTask.createArrow(cmp)
};

zkTaskContainer.isOverTask = function(cmp, arrow) {
    zkTask.isOverTask(cmp, arrow);
};

zkTaskContainer.getElementsByAttribute = function(oElm, strTagName, strAttributeName,
    strAttributeValue) {
    zkTask.getElementsByAttribute(oElm, strTagName, strAttributeName, strAttributeValue);
}

zkTaskContainer.setClass = function(cmp, newclass) {
    cmp.className = newclass;
};

/* We will not allow taskcontainer move or resize untill its behaviour its
 * clearly specified

YAHOO.example.DDRegion = function(id, sGroup, config) {
    this.cont = config.cont;
    YAHOO.example.DDRegion.superclass.constructor.apply(this, arguments);
};

var myDom = YAHOO.util.Dom, myEvent = YAHOO.util.Event

YAHOO.extend(YAHOO.example.DDRegion, YAHOO.util.DD, {
    cont : null,
    init : function() {
        //Call the parent's init method
        YAHOO.example.DDRegion.superclass.init.apply(this, arguments);
        this.initConstraints();

        myEvent.on(window, 'resize', function() {
            this.initConstraints();
        }, this, true);
    },
    initConstraints : function() {

        //Get the top, right, bottom and left positions
        var region = myDom.getRegion(this.cont);

        // Get the element we are working on
        var el = this.getEl();

        // Get the xy position of it
        var xy = myDom.getXY(el);

        // Get the width and height
        var width = parseInt(myDom.getStyle(el, 'width'), 10);
        var height = parseInt(myDom.getStyle(el, 'height'), 10);

        // Set left to x minus left
        var left = xy[0] - region.left;

        // Set right to right minus x minus width
        var right = region.right - xy[0] - width;

        // Set top to y minus top
        var top = xy[1] - region.top;

        // Set bottom to bottom minus y minus height
        var bottom = region.bottom - xy[1] - height;

        // Set the constraints based on the above calculations
        this.setXConstraint(left, right);
        this.setYConstraint(top, bottom);
    }
}); */