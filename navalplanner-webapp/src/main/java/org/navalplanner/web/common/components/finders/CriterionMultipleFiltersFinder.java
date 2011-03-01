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
import org.navalplanner.business.resources.entities.Criterion;
import org.springframework.beans.factory.annotation.Autowired;

public class CriterionMultipleFiltersFinder extends MultipleFiltersFinder {

    @Autowired
    private PredefinedDatabaseSnapshots databaseSnapshots;

    private IFilterEnum criterionFilterEnum = new IFilterEnum() {
        @Override
        public String toString() {
            return "criterion";
        }
    };

    @Override
    public List<FilterPair> getFirstTenFilters() {
        getListMatching().clear();
        Iterator<Criterion> iteratorCriterion = getCriterions().iterator();
        while(iteratorCriterion.hasNext() && getListMatching().size() < 10) {
            Criterion criterion = iteratorCriterion.next();
            getListMatching().add(new FilterPair(
                    criterionFilterEnum, criterion.getName(), criterion));
        }
        addNoneFilter();
        return getListMatching();
    }

    private List<Criterion> getCriterions() {
        return databaseSnapshots.snapshotListCriterion();
    }

    @Override
    public List<FilterPair> getMatching(String filter) {
        getListMatching().clear();
        if ((filter != null) && (!filter.isEmpty())) {
            filter = StringUtils.deleteWhitespace(filter.toLowerCase());
            searchInCriteria(filter);
        }
        addNoneFilter();
        return getListMatching();

    }
    private void searchInCriteria(String filter) {
        for (Criterion criterion : getCriterions()) {
            String name = StringUtils.deleteWhitespace(
                    criterion.getName().toLowerCase());
            if(name.contains(filter)) {
                getListMatching().add(new FilterPair(
                        criterionFilterEnum, criterion.getName(), criterion));
            }
        }
    }

}
