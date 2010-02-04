/*
 * This file is part of ###PROJECT_NAME###
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

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.costcategories.daos.ICostCategoryDAO;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Implements all the methods needed to search the criterion to filter the
 * resources. Provides multiples criterions to filter like {@link Criterion},
 * {@link Category} or filter by name or nif.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class ResourcesMultipleFiltersFinder implements IMultipleFiltersFinder {

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICostCategoryDAO costCategoryDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    private static final Map<CriterionType, List<Criterion>> mapCriterions = new HashMap<CriterionType, List<Criterion>>();

    private static final List<CostCategory> costCategories = new ArrayList<CostCategory>();

    private List<FilterPair> listMatching = new ArrayList<FilterPair>();

    private final String headers[] = { _("Filter type"), _("Filter pattern") };

    protected ResourcesMultipleFiltersFinder() {

    }

    @Transactional(readOnly = true)
    public void init() {
        adHocTransactionService
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

    public List<FilterPair> getFirstTenFilters() {
        listMatching.clear();
        fillWithFirstTenFiltersCriterions();
        fillWithFirstTenFiltersCostCategories();
        listMatching.add(new FilterPair(OrderFilterEnum.None,
                OrderFilterEnum.None.toString(), null));
        return listMatching;
    }

    private List<FilterPair> fillWithFirstTenFiltersCriterions() {
        Iterator<CriterionType> iteratorCriterionType = mapCriterions.keySet()
                .iterator();
        while (iteratorCriterionType.hasNext() && listMatching.size() < 10) {
            CriterionType type = iteratorCriterionType.next();
            for (int i = 0; listMatching.size() < 10
                    && i < mapCriterions.get(type).size(); i++) {
                Criterion criterion = mapCriterions.get(type).get(i);
                addCriterion(type, criterion);
            }
        }
        return listMatching;
    }

    private List<FilterPair> fillWithFirstTenFiltersCostCategories() {
        for (int i = 0; listMatching.size() < 10
 && i < costCategories.size(); i++) {
            CostCategory costCategory = costCategories.get(i);
            addCostCategory(costCategory);
        }
        return listMatching;
    }

    public List<FilterPair> getMatching(String filter) {
        listMatching.clear();
        if ((filter != null) && (!filter.isEmpty())) {
            filter = filter.toLowerCase();
            searchInCriterionTypes(filter);
            searchInCostCategories(filter);
        }
        addNoneFilter();
        return listMatching;
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
                if ((filter.length() < 3) && (listMatching.size() > 9)) {
                    return;
                }
            }
        }
    }

    private void setFilterPairCriterionType(CriterionType type, boolean limited) {
        for (Criterion criterion : mapCriterions.get(type)) {
            addCriterion(type, criterion);
            if ((limited) && (listMatching.size() > 9)) {
                return;
            }
        }
    }

    private void searchInCostCategories(String filter) {
        for (CostCategory costCategory : costCategories) {
            if (costCategory.getName().toLowerCase().contains(filter)) {
                addCostCategory(costCategory);
                if ((filter.length() < 3) && (listMatching.size() > 9)) {
                    return;
                }
            }
        }
    }

    private void addCriterion(CriterionType type, Criterion criterion) {
        String pattern = type.getName() + " :: " + criterion.getName();
        listMatching.add(new FilterPair(ResourceFilterEnum.Criterion, pattern,
                criterion));
    }

    private void addCostCategory(CostCategory costCategory) {
        String pattern = costCategory.getName();
        listMatching.add(new FilterPair(ResourceFilterEnum.CostCategory,
                pattern, costCategory));
    }

    private void addNoneFilter() {
        listMatching.add(new FilterPair(ResourceFilterEnum.None,
                ResourceFilterEnum.None.toString(), null));
    }

    public String objectToString(Object obj) {
        FilterPair filterPair = (FilterPair) obj;
        String text = filterPair.getType() + "(" + filterPair.getPattern()
                + "), ";
        return text;
    }

    @Override
    public String getNewFilterText(String inputText) {
        String newFilterText = new String("");
        String[] filtersText = inputText.split(",");
        newFilterText = getLastText(filtersText);
        newFilterText = newFilterText.replace(" ", "");
        newFilterText = newFilterText.trim();
        return newFilterText;
    }

    private String getLastText(String[] texts) {
        Integer last = texts.length - 1;
        if (texts.length > 0) {
            return texts[last];
        } else {
            return "";
        }
    }

    public boolean isValidNewFilter(Object obj) {
        FilterPair filter = (FilterPair) obj;
        if (filter.getType().equals(OrderFilterEnum.None)) {
            return false;
        }
        return true;
    }

    public boolean isValidFormatText(List filterValues, String value) {
        if (filterValues.isEmpty()) {
            return true;
        }

        filterValues = updateDeletedFilters(filterValues, value);
        value = value.replace(" ", "");
        String[] values = value.split(",");
        if (values.length != filterValues.size()) {
            return false;
        }

        int i = 0;
        for (FilterPair filterPair : (List<FilterPair>) filterValues) {
            String filterPairText = filterPair.getType() + "("
                    + filterPair.getPattern() + ")";
            if (!isFilterAdded(values, filterPairText)) {
                return false;
            }
            i++;
        }
        return true;
    }

    @Override
    public List<FilterPair> updateDeletedFilters(List filterValues, String value) {
        String[] values = value.split(",");
        List<FilterPair> listFilters = (List<FilterPair>) filterValues;
        List<FilterPair> list = new ArrayList<FilterPair>();
        list.addAll(listFilters);

        if (values.length < filterValues.size() + 1) {
            for (FilterPair filterPair : list) {
                String filter = filterPair.getType() + "("
                        + filterPair.getPattern() + ")";
                if (!isFilterAdded(values, filter)) {
                    listFilters.remove(filterPair);
                }
            }
        }
        return listFilters;
    }

    private boolean isFilterAdded(String[] values, String filter) {
        for (int i = 0; i < values.length; i++) {
            String value = values[i].replace(" ", "");
            filter = filter.replace(" ", "");

            if (filter.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public String[] getHeaders() {
        return headers;
    }

    public ListitemRenderer getItemRenderer() {
        return filterPairRenderer;
    }

    /**
     * Render for {@link FilterPair}
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    private final ListitemRenderer filterPairRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            FilterPair filterPair = (FilterPair) data;
            item.setValue(data);

            final Listcell labelType = new Listcell();
            labelType.setLabel(filterPair.getType().toString());
            labelType.setParent(item);

            final Listcell labelPattern = new Listcell();
            labelPattern.setLabel(filterPair.getPattern());
            labelPattern.setParent(item);

        }
    };

}
