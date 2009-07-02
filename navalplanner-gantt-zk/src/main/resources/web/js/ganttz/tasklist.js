/**
 * Javascript behaviuor for TaskList elements
 * @author Javier Morán Rúa <jmoran@igalia.com>
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
zkTasklist = {};

HEIGHT_PER_ROW = 15;             // Ganttz task row height
HEIGHT_TIME_TRACKER = 120;

MIN_RESOLUTION_X = 600;          // Minimun horizontal autoresizable window
MIN_RESOLUTION_Y = 600;          // Minimun vertical autoresizable window

TASKDETAILS_WIDTH = 300;         // Taskdetails column width
TASKDETAILS_HEIGHT = 300;
TIMETRACKER_OFFSET_TOP = 200     // Design-relative height above timetracker
SCROLLBAR_WIDTH = 15;            // Scrollbars default width

zkTasklist.init = function(cmp) {
    zkTasklist.adjust_height(cmp);
}

zkTasklist.adjust_height = function(cmp) {
    var height = cmp.style.height;
    var component_to_adjust = document.getElementById(cmp
            .getAttribute('sameHeightElementId'));

    function setHeight(element, offset) {
        if (!offset) {
            offset = 0;
        }
        var newheigth = parseInt(height) + offset;
        element.style["height"] = document.getElementById('scroll_container').style["height"];
    }

    setHeight(document.getElementById('ganttpanel'), HEIGHT_TIME_TRACKER);
    if (component_to_adjust) {
        setHeight(component_to_adjust);
    }

    adjustScrollableDimensions();
}


// Simultaneous timetracker and canvas horizontal scroll
document.getElementById('ganttpanel_scroller_x').onscroll = function() {
   scroller = document.getElementById('ganttpanel_scroller_x');
   document.getElementById('timetracker').scrollLeft = scroller.scrollLeft;
   document.getElementById('scroll_container').scrollLeft = scroller.scrollLeft;
}


// Simultaneous listdetails and canvas vertical scroll
// Pending to listen to container onwheel scroll move
document.getElementById('ganttpanel_scroller_y').onscroll = function() {
    offset = document.getElementById('ganttpanel_scroller_y').scrollTop;
    document.getElementById('listdetails_container').scrollTop = offset;
    document.getElementById('scroll_container').scrollTop = offset;
   }

// Scroll panel when detected movements on listdetails container
document.getElementById('listdetails_container').onscroll = function() {
    offset = document.getElementById('listdetails_container').scrollTop;
    document.getElementById('scroll_container').scrollTop = offset;
    document.getElementById('ganttpanel_scroller_y').scrollTop = offset;
}

window.onresize = relocateScrolls;
window.onload = relocateScrolls;
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
            (window.innerWidth - SCROLLBAR_WIDTH*2) +"px";
        scroller_x.style["width"] =
            (window.innerWidth - TASKDETAILS_WIDTH - SCROLLBAR_WIDTH ) +"px";
    }

    // Shift scroll-y and scroll-x width (Height change)
    if ( window.innerHeight > MIN_RESOLUTION_Y ) {
        scroller_x.style["top"] = (window.innerHeight - SCROLLBAR_WIDTH*2) +"px";
        scroller_y.style["height"] = (window.innerHeight - TASKDETAILS_HEIGHT ) +"px";
        listdetails.style["height"] = scroller_y.style["height"];
    }

    adjustScrollableDimensions();
}

/*
 * Recalculate component dimensions
 */
function adjustScrollableDimensions() {

    // Timetracker is recalculated when the window is resized and when zoom
    // level is changed as the component is recreated
    timetracker = document.getElementById('timetracker');
    scroll_container = document.getElementById('scroll_container');

    timetracker.style["width"] =
        (window.innerWidth - TASKDETAILS_WIDTH - SCROLLBAR_WIDTH*2 ) +"px";
    scroll_container.style["width"] = timetracker.style["width"];

    timetracker.style["height"] =
        (window.innerHeight - TIMETRACKER_OFFSET_TOP ) +"px";
    scroll_container.style["height"] =
        (window.innerHeight - TIMETRACKER_OFFSET_TOP - 90 ) +"px";

    // Watermark heigh also needs recalculations due to the recreation
    document.getElementById('watermark').style["height"]
        = (window.innerHeight - TIMETRACKER_OFFSET_TOP - 60 ) +"px";

    // Inner divs need recalculation to adjust to new scroll displacement lenght
    document.getElementById('ganttpanel_inner_scroller_y').style["height"]
        = document.getElementById('listdetails_container').scrollHeight + "px";

}