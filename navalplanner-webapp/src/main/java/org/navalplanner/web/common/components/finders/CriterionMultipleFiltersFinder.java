/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class CriterionMultipleFiltersFinder extends MultipleFiltersFinder {

    @Autowired
    private ICriterionDAO criterionDAO;

    private static final List<Criterion> criterionList = new ArrayList<Criterion>();

    private IFilterEnum criterionFilterEnum = new IFilterEnum() {
        @Override
        public String toString() {
            return _("criterion");
        }
    };

    @Transactional(readOnly = true)
    public void init() {
        getAdHocTransactionService()
                .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
            @Override
                    public Void execute() {
                        loadCriteria();
                        return null;
                    }
                });
    }

    @Transactional(readOnly = true)
    private void loadCriteria() {
        criterionList.clear();
        criterionList.addAll(criterionDAO.getAll());
    }

    @Override
    public List<FilterPair> getFirstTenFilters() {
        Iterator<Criterion> iteratorCriterion = criterionList.iterator();
        while(iteratorCriterion.hasNext() && getListMatching().size() < 10) {
            Criterion criterion = iteratorCriterion.next();
            getListMatching().add(new FilterPair(
                    criterionFilterEnum, criterion.getName(), criterion));
        }
        return getListMatching();
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
        for(Criterion criterion : criterionList) {
            String name = StringUtils.deleteWhitespace(
                    criterion.getName().toLowerCase());
            if(name.contains(filter)) {
                getListMatching().add(new FilterPair(
                        criterionFilterEnum, criterion.getName(), criterion));
            }
        }
    }

    private void addNoneFilter() {
        getListMatching().add(
                new FilterPair(OrderElementFilterEnum.None,
                        OrderElementFilterEnum.None.toString(), null));
    }
}
