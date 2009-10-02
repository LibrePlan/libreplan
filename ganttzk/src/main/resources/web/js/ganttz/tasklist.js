/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Javascript behaviuor for TaskList elements
 * @author Javier Morán Rúa <jmoran@igalia.com>
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
zkTasklist = {};

HEIGHT_PER_ROW = 15;             // Ganttz task row height
HEIGHT_TIME_TRACKER = -10;        // Timetracker legend height (80)

MIN_RESOLUTION_X = 600;          // Minimun horizontal autoresizable window
MIN_RESOLUTION_Y = 600;          // Minimun vertical autoresizable window

TASKDETAILS_WIDTH = 300;         // Taskdetails column fixed width (300)
TASKDETAILS_HEIGHT = 180; //260       // Design-relative reservated height for taskdetails (300,260)
TIMETRACKER_OFFSET_TOP = 120 //    // Design-relative height above timetracker

FOOTER_HEIGHT = 40;             // Design-relative footer height


SCROLLBAR_WIDTH = 15;            // Scrollbars default width

zkTasklist.init = function(cmp) {
    zkTasklist.adjust_height();
    make_visible();
    relocateScrolls();
    listenToScroll();
}

zkTasklist.adjust_height = function(cmp) {

    document.getElementById('ganttpanel').style["height"]
        = document.getElementById('scroll_container').style["height"];

    adjustScrollableDimensions();
}

function listenToScroll(){
    var onHorizontalScroll = function() {
        var scroller = document.getElementById('ganttpanel_scroller_x');
        document.getElementById('timetracker').scrollLeft = scroller.scrollLeft;
        document.getElementById('scroll_container').scrollLeft = scroller.scrollLeft;
        document.getElementById('zoom_buttons').style["left"] = scroller.scrollLeft+"px";
    };
    var onVerticalScroll = function() {
        var offset = document.getElementById('ganttpanel_scroller_y').scrollTop;
        document.getElementById('listdetails_container').scrollTop = offset;
        document.getElementById('scroll_container').scrollTop = offset;
    };
    document.getElementById('ganttpanel_scroller_x').onscroll = onHorizontalScroll;
    document.getElementById('ganttpanel_scroller_y').onscroll = onVerticalScroll;
 // TODO listen to container onwheel scroll move
}


YAHOO.util.Event.addListener(window, 'resize',relocateScrolls);
/*
 * Move scrollbars to locate them on left and bottom window borders
 */
function relocateScrolls() {

    scroller_y = document.getElementById('ganttpanel_scroller_y');
    scroller_x = document.getElementById('ganttpanel_scroller_x');
    listdetails = document.getElementById('listdetails_container');

    // Shift scroll-y and scroll-x width (Width change)
    if ( window.innerWidth > MIN_RESOLUTION_X ) {
        scroller_y.style["left"] =
            (window.innerWidth - SCROLLBAR_WIDTH*3 ) +"px"; // Extra padding
        scroller_x.style["width"] =
            (window.innerWidth - TASKDETAILS_WIDTH - SCROLLBAR_WIDTH*2 ) +"px"; // Extra padding
    }

    // Shift scroll-y and scroll-x width (Height change)
    if ( window.innerHeight > MIN_RESOLUTION_Y ) {
        scroller_x.style["top"] = (window.innerHeight - SCROLLBAR_WIDTH*2 - HEIGHT_TIME_TRACKER ) +"px";
        scroller_y.style["height"] = (window.innerHeight - TASKDETAILS_HEIGHT + SCROLLBAR_WIDTH*2) +"px";
        listdetails.style["height"] = (window.innerHeight - TASKDETAILS_HEIGHT + SCROLLBAR_WIDTH*2) +"px";
    }

    adjustScrollableDimensions();
}

/*
 * Recalculate component dimensions
 */
function adjustScrollableDimensions() {

    // Timetracker is recalculated when the window is resized and when zoom
    // level is changed as the component is recreated
    // timetracker = document.getElementById('timetracker');
    timetracker = YAHOO.util.Selector.query('#timetracker')[0];

    watermark = document.getElementById('watermark');
    scroll_container = document.getElementById('scroll_container');

    timetracker.style["width"] =
        (window.innerWidth - TASKDETAILS_WIDTH - SCROLLBAR_WIDTH*2 ) +"px";
    scroll_container.style["width"] = timetracker.style["width"];

    timetracker.style["height"] =
        (window.innerHeight - TIMETRACKER_OFFSET_TOP + 5 ) +"px"; // Extra padding
    scroll_container.style["height"] =
        (window.innerHeight - TIMETRACKER_OFFSET_TOP -
        ( FOOTER_HEIGHT + SCROLLBAR_WIDTH*2 )) +"px";

    // Watermark heigh also needs recalculations due to the recreation
    document.getElementById('watermark').style["height"]
        = (window.innerHeight - TIMETRACKER_OFFSET_TOP -
        ( FOOTER_HEIGHT + SCROLLBAR_WIDTH )) +"px";

    // Inner divs need recalculation to adjust to new scroll displacement lenght
    document.getElementById('ganttpanel_inner_scroller_y').style["height"]
        = document.getElementById('listdetails_container').scrollHeight + "px";

    // Inner divs need recalculation to adjust to new scroll displacement lenght
    document.getElementById('ganttpanel_inner_scroller_x').style["width"]
        = watermark.offsetWidth +"px";
}

function make_visible() {
    document.getElementById('ganttpanel_scroller_x').style["display"]="inline";
    document.getElementById('ganttpanel_scroller_y').style["display"]="inline";
}

/**
 *
 * task.js
 *
 */

zkTask = {};

zkTask.CORNER_WIDTH = 20;
zkTask.HEIGHT = 10;
zkTask.HALF_HEIGHT = 5;
zkTask.DEPENDENCY_PADDING = 4;
zkTask.HALF_DEPENDENCY_PADDING = 2;
// Task borders are default 1px

zkTask.getDD = function(cmp) {
    if (typeof (cmp.created_dd) == 'undefined') {

        // Create the laned drag&drop component
        cmp.created_dd = new YAHOO.example.DDRegion(cmp.id, '', {
            cont : 'row' + cmp.id
        });
    }
    return cmp.created_dd;
}

zkTask.relocateAfterAdding = function(cmp) {
    var row = cmp.parentNode;
    var taskList = row.parentNode.parentNode;
    var nextTask = row.parentNode.nextSibling;
    row.parentNode.removeChild(row);
    taskList.insertBefore(row, nextTask);
}
/*
 * relocateAfterAdding works for all but in the case that the task added would
 * be the first one, so we create another method for that case
 */
zkTask.relocateFirstAfterAdding = function(cmp) {
    var row = cmp.parentNode;
    var taskList = row.parentNode.parentNode;
    var nextTask = row.parentNode.previousSibling;
    row.parentNode.removeChild(row);
    taskList.insertBefore(row, nextTask);
}

zkTask.cleanup = function(cmp) {
    var row = cmp.parentNode;
    row.parentNode.removeChild(row);
}

zkTask.init = function(cmp) {
    // Instead of executing the code directly, a callback is created
    // that will be executed when the user passes the mouse over the task
    var callback = function() {
        // Configure the drag&drop over the component
        var dd = zkTask.getDD(cmp);
        // when the tasks is being dragged the related dependencies are redrawn
        dd.on('dragEvent', function(ev) {
            // Slight overload. It could be more efficent to overwrite the YUI
            // method
                // that is setting the top property
                cmp.style.top = "";
                if (cmp['relatedDependencies']) {
                    for ( var i = 0; i < cmp.relatedDependencies.length; i++) {
                        zkDependency.draw(cmp.relatedDependencies[i]);
                    }
                }
            }, null, false);
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
        cmp2 = document.getElementById('completion' + cmp.id);

        var resize2 = new YAHOO.util.Resize(cmp2, {
            handles : [ 'r' ],
            proxy : true,
            maxWidth : cmp.clientWidth - 2
        // Considering 1px borders
                });

        resize2.on('resize', function(ev) {
            cmp.style.top = "";
            cmp2.style.top = "";
            zkau.send( {
                uuid : cmp2.id,
                cmd : "updateProgress",
                data : [ cmp2.style.width ]
            });
        }, zkTask, true);

        resize.on('resize', function(ev) {
            cmp.style.top = "";
            cmp2.style.top = "";
            cmp2 = document.getElementById('completion' + cmp.id);
            resize2 = new YAHOO.util.Resize(cmp2, {
                handles : [ 'r' ],
                proxy : true,
                maxWidth : cmp.clientWidth - 2
            // Considering 1px borders
                    });
            if ((cmp.clientWidth) < (cmp2.clientWidth)) {
                cmp2.style.width = cmp.clientWidth - 2 + 'px';
            }

            zkau.send( {
                uuid : cmp.id,
                cmd : "updateSize",
                data : [ cmp.style.width ]
            });

        }, zkTask, true);
        // it removes itself, so it's not executed again:
        YAHOO.util.Event.removeListener(cmp, "mouseover", callback);
    }
    YAHOO.util.Event.addListener(cmp, "mouseover", callback);
};

zkTask.xMouse;
zkTask.yMouse;

// Listen to mousemove events
YAHOO.util.Event.on(document.body, 'mousemove', function(e) {
    var arrPos = YAHOO.util.Event.getXY(e);
    zkTask.xMouse = arrPos[0];
    zkTask.yMouse = arrPos[1];
});

zkTask.setClass = function(cmp, newclass) {
    cmp.className = newclass;
};

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
    // Obsolete as far as tasks will not show name inside
    // if ("name" == name) {
    // var span = YAHOO.util.Selector.query("span", cmp, true);
    // span.innerHTML = value;
    // return true;
    // }
    return false;
}

zkTask.addRelatedDependency = function(cmp, dependency) {
    if (!cmp['relatedDependencies']) {
        cmp.relatedDependencies = [];
    }
    cmp.relatedDependencies.push(dependency);
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
        if ((task = zkTask.isOverTask(cmp, arrow))) {
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

/*
 * This method is binded to the mouse click listener to determine if it is
 * positioned over any task
 */
zkTask.isOverTask = function(cmp, arrow) {

    var listtasksNode = document.getElementById("listtasks");
    // var ganttPanelNode = document.getElementById("ganttpanel");
    var ganttPanelNode = document.getElementById("scroll_container");

    arrayTasks = zkTask.getElementsByAttribute(listtasksNode, "div", "z.type",
            "ganttz.task.Task");
    arrayTasks = arrayTasks.concat(zkTask.getElementsByAttribute(listtasksNode,
            "div", "z.type", "ganttz.taskcontainer.TaskContainer"));

    var xpos = zkTask.xMouse - ganttPanelNode.offsetLeft
            + ganttPanelNode.scrollLeft;
    var ypos = zkTask.yMouse - ganttPanelNode.offsetTop
            + ganttPanelNode.scrollTop - listtasksNode.offsetTop;

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

zkTask.resizeCompletionAdvance = function(cmp, width) {
	var completionDiv = YAHOO.util.Selector.query('.completion', cmp, true);
	completionDiv["style"].width = width;
}

zkTask.resizeCompletion2Advance = function(cmp, width) {
	var completionDiv = YAHOO.util.Selector.query('.completion2', cmp, true);
	completionDiv["style"].width = width;
}

YAHOO.example.DDRegion = function(id, sGroup, config) {
    this.cont = config.cont;
    YAHOO.example.DDRegion.superclass.constructor.apply(this, arguments);
};

var myDom = YAHOO.util.Dom, myEvent = YAHOO.util.Event

YAHOO.extend(YAHOO.example.DDRegion, YAHOO.util.DD, {
    cont : null,
    init : function() {
        // Call the parent's init method
    YAHOO.example.DDRegion.superclass.init.apply(this, arguments);
    this.initConstraints();

    myEvent.on(window, 'resize', function() {
        this.initConstraints();
    }, this, true);
},
initConstraints : function() {

    // Get the top, right, bottom and left positions
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

zkTaskContainer.relocateFirstAfterAdding = zkTask.relocateFirstAfterAdding;

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

//Listen to mousemove events
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

zkTaskContainer.resizeCompletionAdvance = zkTask.resizeCompletionAdvance;
zkTaskContainer.resizeCompletion2Advance = zkTask.resizeCompletion2Advance;

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