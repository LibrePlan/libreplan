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
 * Keeps track the synchronization info.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class JiraSyncInfo {

    private List<String> syncFailedReasons = new ArrayList<String>();

    /**
     * Add the specified <code>reason</code> to syncFailedReasons list
     *
     * @param reason
     *            reason why synchronizition failed
     */
    public void addSyncFailedReason(String reason) {
        syncFailedReasons.add(reason);
    }

    /**
     * Is synchronization successful
     *
     * @return
     */
    public boolean isSyncSuccessful() {
        return syncFailedReasons.isEmpty();
    }

    /**
     * returns reasons why synchronization is failed
     */
    public List<String> getSyncFailedReasons() {
        return Collections.unmodifiableList(syncFailedReasons);
    }

}
