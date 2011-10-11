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

package org.navalplanner.web.common.components.finders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.hibernate.notification.PredefinedDatabaseSnapshots;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Implements all the methods needed to search the criteria to filter the
 * {@link TaskGroup} in company view Gantt. Provides multiples criteria to
 * filter like {@link Criterion} and {@link Label}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class TaskGroupsMultipleFiltersFinder extends MultipleFiltersFinder {

    @Autowired
    private PredefinedDatabaseSnapshots databaseSnapshots;

    protected TaskGroupsMultipleFiltersFinder() {
    }

    public List<FilterPair> getFirstTenFilters() {
        getListMatching().clear();
        fillWithFirstTenFiltersLabels();
        fillWithFirstTenFiltersCriterions();
        fillWithFirstTenFiltersCustomer();
        fillWithFirstTenFiltersState();
        fillWihtFirstTenFiltersCodes();
        fillWihtFirstTenFiltersCustomerReferences();
        addNoneFilter();
        return getListMatching();
    }

    private List<FilterPair> fillWithFirstTenFiltersLabels() {
        Map<LabelType, List<Label>> mapLabels = databaseSnapshots
                .snapshotLabelsMap();
        Iterator<LabelType> iteratorLabelType = mapLabels.keySet().iterator();
        while (iteratorLabelType.hasNext() && getListMatching().size() < 10) {
            LabelType type = iteratorLabelType.next();
            for (int i = 0; getListMatching().size() < 10
                    && i < mapLabels.get(type).size(); i++) {
                Label label = mapLabels.get(type).get(i);
                addLabel(type, label);
            }
        }
        return getListMatching();
    }

    private List<FilterPair> fillWithFirstTenFiltersCriterions() {
        SortedMap<CriterionType, List<Criterion>> mapCriterions = getMapCriterions();
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

    private SortedMap<CriterionType, List<Criterion>> getMapCriterions() {
        return databaseSnapshots.snapshotCriterionsMap();
    }

    private List<FilterPair> fillWithFirstTenFiltersCustomer() {
        List<ExternalCompany> externalCompanies = databaseSnapshots
                .snapshotExternalCompanies();
        for (int i = 0; getListMatching().size() < 10
                && i < externalCompanies.size(); i++) {
            ExternalCompany externalCompany = externalCompanies.get(i);
            addExternalCompany(externalCompany);
        }
        return getListMatching();
    }

    private List<FilterPair> fillWithFirstTenFiltersState() {
        for (int i = 0; getListMatching().size() < 10
                && i < OrderStatusEnum.values().length; i++) {
            OrderStatusEnum state = OrderStatusEnum.values()[i];
            addState(state);
        }
        return getListMatching();
    }

    private List<FilterPair> fillWihtFirstTenFiltersCodes() {
        List<String> ordersCodes = databaseSnapshots.snapshotOrdersCodes();
        for (int i = 0; getListMatching().size() < 10 && i < ordersCodes.size(); i++) {
            String code = ordersCodes.get(i);
            addCode(code);
        }
        return getListMatching();
    }

    private List<FilterPair> fillWihtFirstTenFiltersCustomerReferences() {
        List<String> customerReferences = databaseSnapshots
                .snapshotCustomerReferences();
        for (int i = 0; getListMatching().size() < 10
                && i < customerReferences.size(); i++) {
            String reference = customerReferences.get(i);
            addCustomerReference(reference);
        }
        return getListMatching();
    }

    public List<FilterPair> getMatching(String filter) {
        getListMatching().clear();
        if ((filter != null) && (!filter.isEmpty())) {

            filter = StringUtils.deleteWhitespace(filter.toLowerCase());

            if (filter.indexOf("rc:") == 0) {
                searchInCustomerReferences(filter);
            } else if (filter.indexOf("cod:") == 0) {
                this.searchInOrderCodes(filter);
            } else {
                searchInCriterionTypes(filter);
                searchInLabelTypes(filter);
                searchInExternalCompanies(filter);
                searchInOrderStatus(filter);
            }
        }

        addNoneFilter();
        return getListMatching();
    }

    private void searchInCriterionTypes(String filter) {
        boolean limited = (filter.length() < 3);
        for (CriterionType type : getMapCriterions().keySet()) {
            String name = StringUtils.deleteWhitespace(type.getName()
                    .toLowerCase());
            if (name.contains(filter)) {
                setFilterPairCriterionType(type, limited);
            } else {
                searchInCriterions(type, filter);
            }
        }
    }

    private void searchInCriterions(CriterionType type, String filter) {
        List<Criterion> list = getMapCriterions().get(type);
        if (list == null) {
            return;
        }
        for (Criterion criterion : list) {
            String name = StringUtils.deleteWhitespace(criterion.getName()
                    .toLowerCase());
            if (name.contains(filter)) {
                addCriterion(type, criterion);
                if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                    return;
                }
            }
        }
    }

    private void setFilterPairCriterionType(CriterionType type, boolean limited) {
        for (Criterion criterion : getMapCriterions().get(type)) {
            addCriterion(type, criterion);
            if ((limited) && (getListMatching().size() > 9)) {
                return;
            }
        }
    }

    private void searchInLabelTypes(String filter) {
        Map<LabelType, List<Label>> mapLabels = getLabelsMap();
        boolean limited = (filter.length() < 3);
        for (LabelType type : mapLabels.keySet()) {
            String name = StringUtils.deleteWhitespace(type.getName()
                    .toLowerCase());
            if (name.contains(filter)) {
                setFilterPairLabelType(type, limited);
            } else {
                searchInLabels(type, filter);
            }
        }
    }

    private Map<LabelType, List<Label>> getLabelsMap() {
        return databaseSnapshots.snapshotLabelsMap();
    }

    private void searchInLabels(LabelType type, String filter) {
        for (Label label : getLabelsMap().get(type)) {
            String name = StringUtils.deleteWhitespace(label.getName()
                    .toLowerCase());
            if (name.contains(filter)) {
                addLabel(type, label);
                if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                    return;
                }
            }
        }
    }

    private void setFilterPairLabelType(LabelType type, boolean limited) {
        for (Label label : getLabelsMap().get(type)) {
            addLabel(type, label);
            if ((limited) && (getListMatching().size() > 9)) {
                return;
            }
        }
    }

    private void searchInExternalCompanies(String filter){
        for(ExternalCompany externalCompany : databaseSnapshots
                .snapshotExternalCompanies()){
            String name = StringUtils.deleteWhitespace(externalCompany
                    .getName().toLowerCase());
            String nif = StringUtils.deleteWhitespace(externalCompany.getNif()
                    .toLowerCase());
            if ((name.contains(filter)) || (nif.contains(filter))) {
                addExternalCompany(externalCompany);
                if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                    return;
                }
            }
        }
    }

    private void searchInOrderStatus(String filter) {
        for (OrderStatusEnum state : OrderStatusEnum.values()) {
            String name = StringUtils.deleteWhitespace(state.name()
                    .toLowerCase());
            if (name.contains(filter)) {
                addState(state);
                if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                    return;
                }
            }
        }
    }

    private void searchInOrderCodes(String filter) {
        if (filter.indexOf("cod:") == 0) {
            String codeFilter = filter.replaceFirst("cod:", "");
            for (String code : databaseSnapshots.snapshotOrdersCodes()) {
                code = StringUtils.deleteWhitespace(code.toLowerCase());
                if (code.equals(codeFilter)) {
                    addCode(code);
                    return;
                }
            }
        }
    }

    private void searchInCustomerReferences(String filter) {
        if (filter.indexOf("rc:") == 0) {
            String referenceFilter = filter.replaceFirst("rc:", "");
            for (String reference : databaseSnapshots
                    .snapshotCustomerReferences()) {
                reference = StringUtils.deleteWhitespace(reference
                        .toLowerCase());
                if (reference.equals(referenceFilter)) {
                    addCustomerReference(reference);
                    return;
                }
            }
        }
    }

    private void addCriterion(CriterionType type, Criterion criterion) {
        String pattern = criterion.getName() + " ( " + type.getName() + " )";
        getListMatching().add(
                new FilterPair(TaskGroupFilterEnum.Criterion, type
                        .getResource()
                        .toLowerCase(), pattern, criterion));
    }

    private void addLabel(LabelType type, Label label) {
        String pattern = label.getName() + " ( " + type.getName() + " )";
        getListMatching().add(
                new FilterPair(TaskGroupFilterEnum.Label, pattern, label));
    }

    private void addExternalCompany(ExternalCompany externalCompany) {
        String pattern = externalCompany.getName() + " :: "
                + externalCompany.getNif();
        getListMatching().add(
                new FilterPair(TaskGroupFilterEnum.ExternalCompany,
                pattern, externalCompany));
    }

    private void addState(OrderStatusEnum state) {
        getListMatching().add(
                new FilterPair(TaskGroupFilterEnum.State, state.name(),
                state));
    }

    private void addCode(String code) {
        getListMatching().add(
                new FilterPair(TaskGroupFilterEnum.Code, code, code));
    }

    private void addCustomerReference(String reference) {
        getListMatching().add(
                new FilterPair(TaskGroupFilterEnum.CustomerReference,
                reference, reference));
    }

}
