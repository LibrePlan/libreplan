/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
 *
 * @author Javier Morán Rúa <jmoran@igalia.com>
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
zkTasklist = {};

HEIGHT_PER_ROW = 15; // Ganttz task row height
HEIGHT_TIME_TRACKER = -10; // Timetracker legend height (80)

MIN_RESOLUTION_X = 600; // Minimun horizontal autoresizable window
MIN_RESOLUTION_Y = 600; // Minimun vertical autoresizable window

TASKDETAILS_WIDTH = 300; // Taskdetails column fixed width (300)
TASKDETAILS_HEIGHT = 180; // 260 // Design-relative reservated height for
							   // taskdetails (300,260)
TIMETRACKER_OFFSET_TOP = 120   // Design-relative height above timetracker

FOOTER_HEIGHT = 40; // Design-relative footer height

SCROLLBAR_WIDTH = 15; // Scrollbars default width

DRAGABLE_PADDING = 20; // Drag padding for dependency creation

PERSPECTIVES_WIDTH = 90;

LEGEND_CONTAINER_OFFSET = 75; // Taskdetail width - legend container width

zkTasklist.DELAY = 10         // Delay in ms to show task tooltips

zkTasklist.tooltipTimeout = "";

zkTasklist.showTooltip = function(elem) {
	zkTasklist.tooltipTimeout = setTimeout(function(offset) {
		component = document.getElementById(elem);
		if (component!=null) {
			component.style['display'] = 'block';
			offset = zkTask.xMouse - component.parentNode.offsetLeft - taskdetailsContainer().offsetWidth - PERSPECTIVES_WIDTH + rightpanellayout().scrollLeft;
			component.style['left'] = offset + 'px';
		}
	}, zkTasklist.DELAY);
}

zkTasklist.showAllTooltips = function(elem) {
	var tooltips = YAHOO.util.Selector.query('.task-labels');
	for (j=0;j<tooltips.length;j++) {
		tooltips[j].style["display"] = "inline";
	}
}

zkTasklist.hideAllTooltips = function(elem) {
	var tooltips = YAHOO.util.Selector.query('.task-labels');
	for (j=0;j<tooltips.length;j++) {
		tooltips[j].style["display"] = "none";
	}
}

zkTasklist.showResourceTooltips = function(elem) {
	var tooltips = YAHOO.util.Selector.query('.task-resources');
	for (j=0;j<tooltips.length;j++) {
		tooltips[j].style["display"] = "inline";
	}
}

zkTasklist.hideResourceTooltips = function(elem) {
	var tooltips = YAHOO.util.Selector.query('.task-resources');
	for (j=0;j<tooltips.length;j++) {
		tooltips[j].style["display"] = "none";
	}
}

/* Refreshes
 * Can be optimized creating the new tasks with
 */
zkTasklist.refreshTooltips = function(elem) {
	var resourcesButton = YAHOO.util.Selector.query('.show-resources')[0];
	if (resourcesButton.className.indexOf("clicked") != -1 ) {
		zkTasklist.showResourceTooltips();
	}
	var resourcesButton = YAHOO.util.Selector.query('.show-labels')[0];
	if (resourcesButton.className.indexOf("clicked") != -1 ) {
		zkTasklist.showAllTooltips();
	}
}


zkTasklist.hideTooltip = function(elem) {
	if (zkTasklist.tooltipTimeout) {
		clearTimeout(zkTasklist.tooltipTimeout);
	}
	node = document.getElementById(elem);
	if (elem != null) node.style["display"] = "none";
}

zkTasklist.timeplotcontainer_rescroll = function(elem) {
	var timeplotcontainer_all_ = YAHOO.util.Selector.query('.timeplot-canvas');
	var scrolledpannel_ = scrolledpannel();
	for (j=0;j<timeplotcontainer_all_.length;j++) {
		timeplotcontainer_all_[j].style["left"] = "-" + scrolledpannel_.scrollLeft + "px";
	}
}



function scrolledpannel() {
	return YAHOO.util.Selector.query('.rightpanellayout div')[0];
}

function taskdetailsBody() {
	return YAHOO.util.Selector.query('.listdetails .z-tree-body')[0];
}

function plannergraph() {
	return YAHOO.util.Selector.query('.plannergraph')[0];
}

function timetrackergap() {
	return YAHOO.util.Selector.query('.timetrackergap')[0];
}

function taskheadersgap() {
	return YAHOO.util.Selector.query('.taskheadersgap')[0];
}

function taskheaderscontainer() {
	return YAHOO.util.Selector.query('.taskheaderscontainer')[0];
}

function rightpanellayout() {
	return YAHOO.util.Selector.query('.rightpanellayout div')[0];
}

function taskdetailsContainer() {
	return YAHOO.util.Selector.query('.taskdetailsContainer')[0];
}

function timeplotcontainer_load() {
	return YAHOO.util.Selector.query('.timeplot-canvas')[0];
}

function timeplotcontainer_earnedvalue() {
	return YAHOO.util.Selector.query('.timeplot-canvas')[1];
}

function timeplotcontainer_all() {
	return YAHOO.util.Selector.query('.timeplot-canvas');
}

zkTasklist.init = function(cmp) {
	zkTasklist.adjust_height();
	listenToScroll();
}

/* Resizes ganttpanel heigh to fit window size */
zkTasklist.adjust_height = function(cmp) {
	document.getElementById('ganttpanel').style["height"] = document
			.getElementById('scroll_container').style["height"];
	adjustScrollableDimensions();
}

/* Scrolls taskdetails component when scrolling ganttpanel component */
function listenToScroll() {

	timetrackergap_ = timetrackergap();
	scrolledpannel_ = scrolledpannel();
	leftpanel_ = taskdetailsBody();
	rightpanellayout_ = rightpanellayout();

	var onScroll = function() {

		// Can be optimized caching it outside of onScroll method
		// explicitly invalidating its value when timeplot is regenerated
		var timeplotcontainer_all_ = YAHOO.util.Selector.query('canvas.timeplot-canvas');

		timetrackergap_.style["left"] = "-" + scrolledpannel_.scrollLeft + "px";
		leftpanel_.style["top"] = "-" + scrolledpannel_.scrollTop + "px";
		plannergraph_ = plannergraph();
		if(plannergraph_ != undefined) {
			plannergraph_.scrollLeft = scrolledpannel_.scrollLeft;
		}
		for (j=0;j<timeplotcontainer_all_.length;j++)
		{
			timeplotcontainer_all_[j].style["left"] = "-" + scrolledpannel_.scrollLeft + "px";
		}
	};
	rightpanellayout_.onscroll = onScroll;

}

/*
 * Move scrollbars to locate them on left and bottom window borders
 */
function relocateScrolls() {

	scroller_y = document.getElementById('ganttpanel_scroller_y');
	scroller_x = document.getElementById('ganttpanel_scroller_x');
	listdetails = document.getElementById('listdetails_container');

	// Shift scroll-y and scroll-x width (Width change)
	if (window.innerWidth > MIN_RESOLUTION_X) {
		scroller_y.style["left"] = (window.innerWidth - SCROLLBAR_WIDTH * 3)
				+ "px"; // Extra padding
		scroller_x.style["width"] = (window.innerWidth - TASKDETAILS_WIDTH - SCROLLBAR_WIDTH * 2)
				+ "px"; // Extra padding
	}

	// Shift scroll-y and scroll-x width (Height change)
	if (window.innerHeight > MIN_RESOLUTION_Y) {
		scroller_x.style["top"] = (window.innerHeight - SCROLLBAR_WIDTH * 2 - HEIGHT_TIME_TRACKER)
				+ "px";
		scroller_y.style["height"] = (window.innerHeight - TASKDETAILS_HEIGHT + SCROLLBAR_WIDTH * 2)
				+ "px";
		listdetails.style["height"] = (window.innerHeight - TASKDETAILS_HEIGHT + SCROLLBAR_WIDTH * 2)
				+ "px";
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

	timetracker.style["width"] = (window.innerWidth - TASKDETAILS_WIDTH - SCROLLBAR_WIDTH * 2)
			+ "px";

	scroll_container.style["width"] = YAHOO.util.Selector.query('.second_level_')[0].clientWidth +"px";

	document.getElementById('timetracker').style["width"] = scroll_container.style["width"];

	timetracker.style["height"] =
		Math.max((window.innerHeight - TIMETRACKER_OFFSET_TOP + 26 ),
				  document.getElementById('listdetails_container').scrollHeight + 12 )
				  +"px";

	scroll_container.style["height"] = (window.innerHeight
			- TIMETRACKER_OFFSET_TOP - (FOOTER_HEIGHT + SCROLLBAR_WIDTH * 2))
			+ "px";

	/* Watermark heigh also needs recalculations due to the recreation
	document.getElementById('watermark').style["height"] = (window.innerHeight
			- TIMETRACKER_OFFSET_TOP - (FOOTER_HEIGHT + SCROLLBAR_WIDTH))
			+ "px";
	// Pbs with document.getElementById('watermark').firstChild
	YAHOO.util.Selector.query('.timetracker_column_even')[0].style["height"]= (window.innerHeight
			- TIMETRACKER_OFFSET_TOP - (FOOTER_HEIGHT + SCROLLBAR_WIDTH))
			+ "px"; */

	// Inner divs need recalculation to adjust to new scroll displacement lenght
	document.getElementById('ganttpanel_inner_scroller_y').style["height"] = document
			.getElementById('listdetails_container').scrollHeight
			+ "px";

	// Inner divs need recalculation to adjust to new scroll displacement lenght
	document.getElementById('ganttpanel_inner_scroller_x').style["width"] = watermark.offsetWidth
			+ "px";
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
			cont : cmp.parentNode.id
		});
	}
	return cmp.created_dd;
}

zkTask.init = function(cmp) {
    function addDragSupport() {
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
    }

    function addResizeSupport() {
        // Configure the task element to be resizable
        var resize = new YAHOO.util.Resize(cmp.id, {
            handles : [ 'r' ],
            proxy : true
        });

        resize.on('resize', function(ev) {
            cmp.style.top = "";
            zkau.send( {
                uuid : cmp.id,
                cmd : "updateSize",
                data : [ cmp.style.width ]
            });

        }, zkTask, true);
    }

    function movingTasksEnabled() {
        return cmp.getAttribute('movingTasksEnabled') === 'true';
    }

    function resizingTasksEnabled() {
        return cmp.getAttribute('resizingTasksEnabled') === 'true';
    }
	// Instead of executing the code directly, a callback is created
	// that will be executed when the user passes the mouse over the task
	var callback = function() {
	    if (movingTasksEnabled()) {
            addDragSupport();
        }
	        addResizeSupport();
	    if (!resizingTasksEnabled()) {
	        cmp.className = cmp.className.replace("yui-resize", "");
	    }
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

zkTask.setAttr = function(cmp, name, val) {
	switch (name) {
        case "resourcesText":{
            var resourcesTextElement = YAHOO.util.Selector.query(
                    '.task-resources .task-resources-inner', cmp, true);
            resourcesTextElement.innerHTML = val;
            return true;
        }
        case "taskTooltipText":{
            var taskTooltipTextElement = YAHOO.util.Selector.query(
                    '.task_tooltip', cmp, true);
            taskTooltipTextElement.innerHTML = val;
            return true;
        }
        default: {
            return false;
        }
	}
};

zkTask.addDependency = function(cmp) {
	zkTask.createArrow(cmp);
};

zkTask.next = function(elem) {
    do {
        elem = elem.nextSibling;
    } while (elem && elem.nodeType != 1);
    return elem;
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

function findPosX(obj)
{
  var curleft = 0;
  if(obj.offsetParent)
      while(1)
      {
        curleft += obj.offsetLeft;
        if(!obj.offsetParent)
          break;
        obj = obj.offsetParent;
      }
  else if(obj.x)
      curleft += obj.x;
  return curleft;
}

function findPosY(obj)
{
  var curtop = 0;
  if(obj.offsetParent)
      while(1)
      {
        curtop += obj.offsetTop;
        if(!obj.offsetParent)
          break;
        obj = obj.offsetParent;
      }
  else if(obj.y)
      curtop += obj.y;
  return curtop;
}


/*
 * This method is binded to the mouse click listener to determine if it is
 * positioned over any task
 */
zkTask.isOverTask = function(cmp, arrow) {

	var listtasksNode = document.getElementById("listtasks");
	var ganttPanelNode = document.getElementById("ganttpanel");
	var scrollContainerPanelNode = document.getElementById("scroll_container");
	var innerLayout = YAHOO.util.Selector.query('.rightpanellayout div')[0];

	arrayTasks = zkTask.getElementsByAttribute(listtasksNode, "div", "z.type",
			"ganttz.task.Task");
	arrayTasks = arrayTasks.concat(zkTask.getElementsByAttribute(listtasksNode,
			"div", "z.type", "ganttz.taskcontainer.TaskContainer"));

	a = findPosX(innerLayout);
	b = findPosY(innerLayout);

	var xpos = zkTask.xMouse - findPosX(innerLayout)
			+ innerLayout.scrollLeft;
	var ypos = zkTask.yMouse - findPosY(innerLayout)
			+ innerLayout.scrollTop - listtasksNode.offsetTop;

	for ( var i = 0; i < arrayTasks.length; i++) {
		var task = arrayTasks[i];
		/* Added margins to pointing errors */
		if (((xpos) > (task.offsetLeft - DRAGABLE_PADDING))
				&& ((xpos) < (task.offsetLeft + task.offsetWidth + DRAGABLE_PADDING))
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


zkTask.moveDeadline = function(cmp, width) {
	var deadlineDiv = zkTask.next(cmp);
	deadlineDiv["style"].left = width;
}

zkTask.moveConsolidatedline = function(cmp, width) {
	var deadlineDiv = zkTask.next(cmp);
	var consolidatedlineDiv = zkTask.next(deadlineDiv);
	consolidatedlineDiv["style"].left = width;
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

/*
 * We will not allow taskcontainer move or resize untill its behaviour its
 * clearly specified zkTaskContainer.getDD = function(cmp) { zkTask.getDD(cmp); };
 */


zkTaskContainer.init = function(cmp) {
	/*
	 * We will not allow taskcontainer move or resize untill its behaviour its
	 * clearly specified zkTask.init(cmp);
	 */
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

/*
 * Dependencies with origin in a task container will be redrawn with a different
 * algorithm
 */
zkTaskContainer.createArrow = function(cmp) {
	zkTask.createArrow(cmp)
};

zkTaskContainer.isOverTask = function(cmp, arrow) {
	zkTask.isOverTask(cmp, arrow);
};

zkTaskContainer.getElementsByAttribute = function(oElm, strTagName,
		strAttributeName, strAttributeValue) {
	zkTask.getElementsByAttribute(oElm, strTagName, strAttributeName,
			strAttributeValue);
}

zkTaskContainer.setClass = function(cmp, newclass) {
	cmp.className = newclass;
};

zkTaskContainer.legendResize = function(cmp) {
	var taskdetailsContainer = YAHOO.util.Selector.query('.taskdetailsContainer')[0];
	var legendContainer = YAHOO.util.Selector.query('.legend-container')[0];
	var legendContainerEarned = YAHOO.util.Selector.query('.legend-container')[1];
	legendContainer.style["width"] = (taskdetailsContainer.clientWidth - LEGEND_CONTAINER_OFFSET )+"px";
	legendContainerEarned.style["width"] = (taskdetailsContainer.clientWidth - LEGEND_CONTAINER_OFFSET )+"px";

};

zkTaskContainer.resizeCompletionAdvance = zkTask.resizeCompletionAdvance;
zkTaskContainer.resizeCompletion2Advance = zkTask.resizeCompletion2Advance;
