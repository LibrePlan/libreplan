HEIGHT_PER_ROW = 15;
HEIGHT_TIME_TRACKER = 120;

MIN_RESOLUTION_X = 600;
MIN_RESOLUTION_Y = 600;

TASKDETAILS_WIDTH = 300
TIMETRACKER_OFFSET_TOP = 200
SCROLLBAR_WIDTH = 15


zkTasklist = {};

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
    var found = YAHOO.util.Selector.query(".fake_column", component_to_adjust,
            false);
    found.each( function(element) {
        setHeight(element,HEIGHT_PER_ROW);
    });
}


//document.getElementById('scroll-container').onscroll = scrollEvent;
document.getElementById('ganttpanel_scroller_x').onscroll = scrollEvent;
function scrollEvent() {
    //scroller = document.getElementById('scroll-container');
    scroller = document.getElementById('ganttpanel_scroller_x');
   document.getElementById('timetracker').scrollLeft = scroller.scrollLeft;
   document.getElementById('scroll_container').scrollLeft = scroller.scrollLeft;

   }

document.getElementById('ganttpanel_scroller_y').onscroll = scrollEvent_y;
function scrollEvent_y() {
    //scroller = document.getElementById('scroll-container');
    scroller = document.getElementById('ganttpanel_scroller_y');
    document.getElementById('listdetails_container').scrollTop = scroller.scrollTop ;
   document.getElementById('scroll_container').scrollTop = scroller.scrollTop ;
   }

window.onresize = relocateScrolls;
window.onload = relocateScrolls;
// document.body.onresize = relocateScrolls;
function relocateScrolls() {

    scroller_y = document.getElementById('ganttpanel_scroller_y');
    scroller_x = document.getElementById('ganttpanel_scroller_x');
    timetracker = document.getElementById('timetracker');
    listdetails = document.getElementById('listdetails_container');
    scroll_container = document.getElementById('scroll_container');


    // Reposition scroll-y and scroll-x width (Width change)
    if ( window.innerWidth > MIN_RESOLUTION_X ) {
        scroller_y.style["left"] =
            (window.innerWidth - SCROLLBAR_WIDTH*2) +"px";
        scroller_x.style["width"] =
            (window.innerWidth - TASKDETAILS_WIDTH - SCROLLBAR_WIDTH ) +"px";
        timetracker.style["width"] = (window.innerWidth - TASKDETAILS_WIDTH
            - SCROLLBAR_WIDTH - SCROLLBAR_WIDTH*2 ) +"px";
        scroll_container.style["width"] = timetracker.style["width"];
    }

    // Reposition scroll-y and scroll-x width (Height change)
    if ( window.innerHeight > MIN_RESOLUTION_Y ) {
        scroller_x.style["top"] = (window.innerHeight - SCROLLBAR_WIDTH*2) +"px";
        scroller_y.style["height"] = (window.innerHeight - TASKDETAILS_WIDTH ) +"px";

        timetracker.style["height"] = (window.innerHeight - TIMETRACKER_OFFSET_TOP ) +"px";

        listdetails.style["height"] = scroller_y.style["height"];
        scroll_container.style["height"] = (window.innerHeight - TIMETRACKER_OFFSET_TOP - 90 ) +"px";
    }

    /* FIX: Needed to recalculate native watermark td */

    adjustScrolls();
}


function adjustScrolls() {

    // Resize scroller inner divs to adapt scrollbars lenght
    document.getElementById('ganttpanel_inner_scroller_y').style["height"]
        = document.getElementById('listdetails_container').scrollHeight + "px";

    document.getElementById('ganttpanel_inner_scroller_x').style["width"]
        = document.getElementById('timetracker').scrollWidth + "px";

}