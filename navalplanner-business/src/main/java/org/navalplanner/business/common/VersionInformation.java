/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2010-2011 Wireless Galicia, S.L.
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

package org.navalplanner.business.common;

/**
 * It contains the current version of project and implements of singleton
 * pattern.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class VersionInformation {

    private static final VersionInformation singleton = new VersionInformation();

    private String projectVersion;

    private VersionInformation() {
    }

    public static VersionInformation getInstance() {
        return singleton;
    }

    /**
     * It returns the current version of the project for retrieval at any place.
     */
    public static String getVersion() {
        return singleton.getProjectVersion();
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String argVersion) {
        projectVersion = argVersion;
    }

}