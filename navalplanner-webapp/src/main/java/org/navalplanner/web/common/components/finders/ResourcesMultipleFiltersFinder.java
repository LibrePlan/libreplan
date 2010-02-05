/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.costcategories.daos.ICostCategoryDAO;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements all the methods needed to search the criterion to filter the
 * resources. Provides multiples criterions to filter like {@link Criterion},
 * {@link Category} or filter by name or nif.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class ResourcesMultipleFiltersFinder extends MultipleFiltersFinder {

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICostCategoryDAO costCategoryDAO;

    private static final Map<CriterionType, List<Criterion>> mapCriterions = new HashMap<CriterionType, List<Criterion>>();

    private static final List<CostCategory> costCategories = new ArrayList<CostCategory>();

    protected ResourcesMultipleFiltersFinder() {

    }

    @Override
    @Transactional(readOnly = true)
    public void init() {
        getAdHocTransactionService()
                .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                    @Override
                    public Void execute() {
                        loadCriterions();
                        loadCostCategories();
                        return null;
                    }
                });
    }

    private void loadCriterions() {
        mapCriterions.clear();
        List<CriterionType> criterionTypes = criterionTypeDAO
                .getCriterionTypes();
        for (CriterionType criterionType : criterionTypes) {
            List<Criterion> criterions = new ArrayList<Criterion>(criterionDAO
                    .findByType(criterionType));

            mapCriterions.put(criterionType, criterions);
        }
    }

    private void loadCostCategories() {
        costCategories.clear();
        costCategories.addAll(costCategoryDAO.findActive());
    }

    @Override
    public List<FilterPair> getFirstTenFilters() {
        getListMatching().clear();
        fillWithFirstTenFiltersCriterions();
        fillWithFirstTenFiltersCostCategories();
        getListMatching().add(
                new FilterPair(OrderFilterEnum.None,
                OrderFilterEnum.None.toString(), null));
        return getListMatching();
    }

    private List<FilterPair> fillWithFirstTenFiltersCriterions() {
        Iterator<CriterionType> iteratorCriterionType = mapCriterions.keySet()
                .iterator();
        while (iteratorCriterionType.hasNext() && getListMatching().size() < 10) {
            CriterionType type = iteratorCriterionType.next();
            for (int i = 0; getListMatching().size() < 10
                    && i < mapCriterions.get(type).size(); i++) {
                Criterion criterion = mapCriterions.get(type).get(i);
                addCriterion(type, criterion);
            }
        }
        return getListMatching();
    }

    private List<FilterPair> fillWithFirstTenFiltersCostCategories() {
        for (int i = 0; getListMatching().size() < 10
 && i < costCategories.size(); i++) {
            CostCategory costCategory = costCategories.get(i);
            addCostCategory(costCategory);
        }
        return getListMatching();
    }

    @Override
    public List<FilterPair> getMatching(String filter) {
        getListMatching().clear();
        if ((filter != null) && (!filter.isEmpty())) {
            filter = filter.toLowerCase();
            searchInCriterionTypes(filter);
            searchInCostCategories(filter);
        }
        addNoneFilter();
        return getListMatching();
    }

    private void searchInCriterionTypes(String filter) {
        boolean limited = (filter.length() < 3);
        for (CriterionType type : mapCriterions.keySet()) {
            if (type.getName().toLowerCase().contains(filter)) {
                setFilterPairCriterionType(type, limited);
            } else {
                searchInCriterions(type, filter);
            }
        }
    }

    private void searchInCriterions(CriterionType type, String filter) {
        for (Criterion criterion : mapCriterions.get(type)) {
            if (criterion.getName().toLowerCase().contains(filter)) {
                addCriterion(type, criterion);
                if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                    return;
                }
            }
        }
    }

    private void setFilterPairCriterionType(CriterionType type, boolean limited) {
        for (Criterion criterion : mapCriterions.get(type)) {
            addCriterion(type, criterion);
            if ((limited) && (getListMatching().size() > 9)) {
                return;
            }
        }
    }

    private void searchInCostCategories(String filter) {
        for (CostCategory costCategory : costCategories) {
            if (costCategory.getName().toLowerCase().contains(filter)) {
                addCostCategory(costCategory);
                if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                    return;
                }
            }
        }
    }

    private void addCriterion(CriterionType type, Criterion criterion) {
        String pattern = type.getName() + " :: " + criterion.getName();
        getListMatching()
                .add(
                        new FilterPair(ResourceFilterEnum.Criterion, pattern,
                criterion));
    }

    private void addCostCategory(CostCategory costCategory) {
        String pattern = costCategory.getName();
        getListMatching().add(
                new FilterPair(ResourceFilterEnum.CostCategory,
                pattern, costCategory));
    }

    private void addNoneFilter() {
        getListMatching().add(
                new FilterPair(ResourceFilterEnum.None,
                ResourceFilterEnum.None.toString(), null));
    }

}
