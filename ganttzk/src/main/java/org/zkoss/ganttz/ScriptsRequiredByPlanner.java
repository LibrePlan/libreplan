/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.zkoss.ganttz;

import org.zkoss.ganttz.util.script.ScriptsRequiredDeclaration;

@ScriptsRequiredDeclaration(dependsOn = { YUIMin.class, ScrollSyncScript.class })
public class ScriptsRequiredByPlanner {

    private ScriptsRequiredByPlanner() {
    }

    public static final String SELECTOR = "/zkau/web/js/yui/2.7.0/selector/selector-min.js";
    public static final String YAHOO_DOM_EVENT = "/zkau/web/js/yui/2.7.0/yahoo-dom-event/yahoo-dom-event.js";
    public static final String DRAGDROPMIN = "/zkau/web/js/yui/2.7.0/dragdrop/dragdrop-min.js";

    public static final String ELEMENT_MIN = "/zkau/web/js/yui/2.7.0/element/element-min.js";
    public static final String RESIZE_MIN = "/zkau/web/js/yui/2.7.0/resize/resize-min.js";
    public static final String LOGGER_MIN = "/zkau/web/js/yui/2.7.0/logger/logger-min.js";
    // adding manually js associated to components since they can be used by
    // other files with no dependencies being present
    public static final String DEPENDENCY_LIST = "/zkau/web/js/ganttz/dependencylist.js";
    public static final String DEPENDENCY = "/zkau/web/js/ganttz/dependency.js";
}
