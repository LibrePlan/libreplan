package org.zkoss.ganttz.resourceload;

import org.zkoss.ganttz.ScrollSyncScript;
import org.zkoss.ganttz.YUIMin;
import org.zkoss.ganttz.util.script.ScriptsRequiredDeclaration;

@ScriptsRequiredDeclaration(dependsOn = { YUIMin.class, ScrollSyncScript.class })
public class ScriptsRequiredByResourceLoadPanel {

    public static final String SELECTOR = "/zkau/web/js/yui/2.7.0/selector/selector-min.js";
    public static final String YAHOO_DOM_EVENT = "/zkau/web/js/yui/2.7.0/yahoo-dom-event/yahoo-dom-event.js";

}
