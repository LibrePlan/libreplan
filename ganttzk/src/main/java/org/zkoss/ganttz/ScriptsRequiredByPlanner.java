package org.zkoss.ganttz;

import org.zkoss.ganttz.util.script.ScriptsRequiredDeclaration;

@ScriptsRequiredDeclaration(dependsOn = { YUIMin.class, ScrollSyncScript.class })
public class ScriptsRequiredByPlanner {

    public static final String SELECTOR = "/zkau/web/js/yui/2.7.0/selector/selector-min.js";
    public static final String YAHOO_DOM_EVENT = "/zkau/web/js/yui/2.7.0/yahoo-dom-event/yahoo-dom-event.js";
    public static final String DRAGDROPMIN = "/zkau/web/js/yui/2.7.0/dragdrop/dragdrop-min.js";

    public static final String ELEMENT_MIN = "/zkau/web/js/yui/2.7.0/element/element-min.js";
    public static final String RESIZE_MIN = "/zkau/web/js/yui/2.7.0/resize/resize-min.js";
    public static final String LOGGER_MIN = "/zkau/web/js/yui/2.7.0/logger/logger-min.js";
}
