/**
 * Javascript behaviuor for GanttPanel element
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
zkGanttPanel = {};

zkGanttPanel.init = function(cmp){
}

/**
 * Scrolls horizontally the ganttpanel when the zoom has resized the component
 * width.
 */
zkGanttPanel.scroll_horizontal = function(cmp, offsetInPx) {
	document.getElementById('ganttpanel_scroller_x').scrollLeft = offsetInPx;
}