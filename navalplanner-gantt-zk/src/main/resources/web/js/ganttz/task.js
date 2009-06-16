/**
 * 
 * ganttz.js
 * 
 */

zkTask = {};

zkTask.CORNER_WIDTH = 20;
zkTask.HEIGHT = 20;
zkTask.HALF_HEIGHT = 10;
zkTask.DEPENDENCY_PADDING = 4;
zkTask.HALF_DEPENDENCY_PADDING = 2;

zkTask.getDD = function(cmp) {
    if (typeof (cmp.created_dd) == 'undefined') {

        // Create the laned drag&drop component
        cmp.created_dd = new YAHOO.example.DDRegion(cmp.id, '', {
            cont : 'row' + cmp.id
        });
    }
    return cmp.created_dd;
}

zkTask.recolocateAfterAdding = function(cmp){
    var row = cmp.parentNode;
    var taskList = row.parentNode.parentNode;
    row.parentNode.removeChild(row);
    taskList.appendChild(row);
}

zkTask.init = function(cmp) {
    // Configure the drag&drop over the component
    var dd = zkTask.getDD(cmp);

    // Register the event endDragEvent
    dd.on('endDragEvent', function(ev) {
        zkau.send( {
            uuid : cmp.id,
            cmd : "updatePosition",
            data : [ cmp.style.left, cmp.style.top ]
        });

    }, null, false);

    // Configure the task element to be resizable
    var resize = new YAHOO.util.Resize(cmp.id, {
        handles : [ 'r' ],
        proxy : true
    });

    // Configure the task element to be resizable
    cmp2 = document.getElementById('completion'+cmp.id);

    var resize2 = new YAHOO.util.Resize(cmp2, {
        handles : [ 'r' ],
        proxy : true,
        maxWidth: cmp.clientWidth - 2
    });


    resize2.on('resize', function(ev) {

        zkau.send( {
            uuid : cmp2.id,
            cmd : "updateProgress",
            data : [ cmp2.style.width ],
        });
    }, zkTask, true);


    resize.on('resize', function(ev) {

        cmp2 = document.getElementById('completion'+cmp.id);
        resize2 = new YAHOO.util.Resize(cmp2, {
            handles : [ 'r' ],
            proxy : true,
            maxWidth: cmp.clientWidth - 2
        });
        if ( (cmp.clientWidth) < (cmp2.clientWidth) ) {
            cmp2.style.width = cmp.clientWidth - 2  + 'px';
        }

        zkau.send( {
            uuid : cmp.id,
            cmd : "updateSize",
            data : [ cmp.style.width ]
        });

    }, zkTask, true);

};

zkTask.xMouse;
zkTask.yMouse;

// Listen to mousemove events
YAHOO.util.Event.on(document.body, 'mousemove', function(e) {
	var arrPos = YAHOO.util.Event.getXY(e);
	zkTask.xMouse = arrPos[0];
	zkTask.yMouse = arrPos[1];
});

zkTask.setAttr = function(cmp, nm, val) {

    switch (nm) {

        case "style.top":
        case "style.width":
        case "style.left": {
            zkau.setAttr(cmp, nm, val);
            return true;
        }

    }
};

zkTask.addDependency = function(cmp) {
    zkTask.createArrow(cmp);
};

zkTask.setAttr = function(cmp, name, value) {
    if ("name" == name) {
        var span = YAHOO.util.Selector.query("span", cmp, true);
        span.innerHTML = value;
        return true;
    }
    return false;
}

zkTask.createArrow = function(cmp) {
    var arrow = document.createElement('div');
    var listtasksNode = document.getElementById("listtasks");
    var listdependenciesNode = document.getElementById("listdependencies");
    var cmpNode = document.getElementById(cmp.id);

    cmp.parentNode.appendChild(arrow);
    zkPlanner.setupArrow(arrow);
    var xMouse = zkTask.xMouse;
    var yMouse = zkTask.yMouse;
    function updateArrow() {
        var origin = zkPlanner.findPos(cmp);
        origin[0] = origin[0]
        + Math.max(0, cmpNode.offsetWidth - zkTask.CORNER_WIDTH);
        origin[1] = origin[1] - listtasksNode.offsetTop
        + listdependenciesNode.offsetTop + zkTask.HEIGHT;
        var destination = zkPlanner.findPosForMouseCoordinates(xMouse, yMouse);
        zkPlanner.drawArrow(arrow, origin, destination);
    }
    updateArrow();
    mousemoveListener = function(e) {
        var arrPos = YAHOO.util.Event.getXY(e);
        xMouse = arrPos[0];
        yMouse = arrPos[1];
        updateArrow();
    };
    mouseClickListener = function(e) {
        var parentNode = arrow.parentNode;
        var task;
        if (task = zkTask.isOverTask(cmp, arrow)) {
            zkau.send( {
                uuid : cmp.id,
                cmd : "addDependency",
                data : [ task.getAttribute('idtask') ]
            });
        }
        parentNode.removeChild(arrow);
        YAHOO.util.Event.removeListener(document.body, 'click',
            mouseClickListener);
        YAHOO.util.Event.removeListener(document.body, 'mousemove',
            mousemoveListener);
    };
    YAHOO.util.Event.on(document.body, 'mousemove', mousemoveListener);
    YAHOO.util.Event.on(document.body, 'click', mouseClickListener);
};

zkTask.isOverTask = function(cmp, arrow) {

    var listtasksNode = document.getElementById("listtasks");
    var ganttPanelNode = document.getElementById("ganttpanel");

    arrayTasks = zkTask.getElementsByAttribute(listtasksNode, "div", "z.type",
        "ganttz.task.Task");

    /* Cursor relative positions to #listtasks (439,160) and ganttPanel scroll */
    var xpos = zkTask.xMouse - listtasksNode.offsetLeft
    + ganttPanelNode.scrollLeft;
    var ypos = zkTask.yMouse - listtasksNode.offsetTop;
    /*
     * This way of getting cursor coordinates, is unable to calculate scrollbar
     * offset.
     */

    for ( var i = 0; i < arrayTasks.length; i++) {

        var task = arrayTasks[i];
        
        if (((xpos) > (task.offsetLeft))
            && ((xpos) < (task.offsetLeft + task.offsetWidth))
            && (ypos > (task.offsetTop))
            && (ypos < (task.offsetTop + task.offsetHeight))) {
            return task;
        }
    }
    return false;
};

zkTask.getElementsByAttribute = function(oElm, strTagName, strAttributeName,
    strAttributeValue) {

    var arrElements = (strTagName == "*" && oElm.all) ? oElm.all : oElm
    .getElementsByTagName(strTagName);
    var arrReturnElements = new Array();
    var oAttributeValue = (typeof strAttributeValue != "undefined") ? new RegExp(
        "(^|\\s)" + strAttributeValue + "(\\s|$)")
    : null;
    var oCurrent;
    var oAttribute;

    for ( var i = 0; i < arrElements.length; i++) {
        oCurrent = arrElements[i];
        oAttribute = oCurrent.getAttribute
        && oCurrent.getAttribute(strAttributeName);
        if (typeof oAttribute == "string" && oAttribute.length > 0) {
            if (typeof strAttributeValue == "undefined"
                || (oAttributeValue && oAttributeValue.test(oAttribute))) {
                arrReturnElements.push(oCurrent);
            }
        }
    }
    return arrReturnElements;
}

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
});
