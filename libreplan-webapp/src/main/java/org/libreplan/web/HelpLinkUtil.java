/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2016 LibrePlan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.web;

import org.zkoss.zk.ui.Executions;

import java.net.URL;

/**
 * Utilities class to check validation help/info link.
 *
 * @author Bogdan Bodnarjuk <b.bodnarjuk@libreplan-enterprise.com>
 */
public class HelpLinkUtil {

    /**
     * Checks in the current path file existing.
     * File path may contains a reference to the part of the page (symbol '#').
     * If current path doesn't exist then it will return page with relevant message.
     *
     * Should be public!
     * Used in _editQualityForm.zul
     *
     * @param path
     * @return The path to an existing page or the path to a warning message page if current page doesn't exist.
     */
    public static String checkUrlPath(String path) {
        URL url;

        if (path.contains("#")) {
            int end = path.indexOf('#');

            String newPath = path.substring(0, end);
            url = Executions.getCurrent().getDesktop().getWebApp().getResource(newPath);
        } else {
            url = Executions.getCurrent().getDesktop().getWebApp().getResource(path);
        }

        if (url == null) {
            return "/common/help_page_not_found.html";
        }

        return path;
    }
}
