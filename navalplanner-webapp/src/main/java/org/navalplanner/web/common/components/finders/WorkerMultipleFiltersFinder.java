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

package org.navalplanner.web.common.components.finders;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.hibernate.notification.PredefinedDatabaseSnapshots;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkerMultipleFiltersFinder extends MultipleFiltersFinder {

    @Autowired
    private PredefinedDatabaseSnapshots databaseSnapshots;

    private IFilterEnum workerFilterEnum = new IFilterEnum() {
        @Override
        public String toString() {
            return "worker";
        }
    };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    @Override
    public List<FilterPair> getFirstTenFilters() {
        getListMatching().clear();
        Iterator<Worker> iteratorWorker = getListWorkers().iterator();
        while(iteratorWorker.hasNext() && getListMatching().size() < 10) {
            Worker worker = iteratorWorker.next();
            getListMatching().add(new FilterPair(
                    workerFilterEnum, worker.getShortDescription(), worker));
        }
        addNoneFilter();
        return getListMatching();
    }

    private List<Worker> getListWorkers() {
        return databaseSnapshots.snapshotListWorkers();
    }

    @Override
    public List<FilterPair> getMatching(String filter) {
        getListMatching().clear();
        if ((filter != null) && (!filter.isEmpty())) {
            filter = StringUtils.deleteWhitespace(filter.toLowerCase());
            searchInWorkers(filter);
        }
        addNoneFilter();
        return getListMatching();

    }
    private void searchInWorkers(String filter) {
        for (Worker worker : getListWorkers()) {
            String name = StringUtils.deleteWhitespace(
                    worker.getShortDescription().toLowerCase());
            if(name.contains(filter)) {
                getListMatching().add(new FilterPair(
                        workerFilterEnum, worker.getShortDescription(), worker));
            }
        }
    }

}
