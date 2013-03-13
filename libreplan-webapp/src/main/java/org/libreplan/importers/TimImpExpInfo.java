/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.importers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps track the success/failure of Tim's import and/or export process
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class TimImpExpInfo {

    /**
     * action, import or export process
     */
    private String action;

    /**
     * Holds failed reasons
     */
    private List<String> failedReasons = new ArrayList<String>();

    public TimImpExpInfo(String action) {
        this.action = action;
    }

    /**
     * Returns the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Adds the specified <code>reason</code> to <code>failedReasons<code> list
     *
     * @param reason
     *            reason why import/export failed
     */
    public void addFailedReason(String reason) {
        failedReasons.add(reason);
    }

    /**
     * Is import or export succeeded
     *
     * @return true if <code>failedReasons</code> is empty
     */
    public boolean isSuccessful() {
        return failedReasons.isEmpty();
    }

    /**
     * returns reasons why import or export failed
     */
    public List<String> getFailedReasons() {
        return Collections.unmodifiableList(failedReasons);
    }
}
