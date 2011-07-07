/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.zkoss.ganttz.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Equivalent to {@link LogFactory}. Use when logging profiling information.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ProfilingLogFactory {

    /**
     * Use this method instead of {@link LogFactory#getLog(Class)} in order to
     * get a {@link Log} that logs to a specific log for profiling.
     *
     * @param klass
     * @return a normal instance of a {@link Log} object with starting with the
     *         profiling string.
     */
    public static Log getLog(Class<?> klass) {
        return LogFactory.getLog("profiling." + klass.getName());
    }

    private ProfilingLogFactory() {
    }

}
