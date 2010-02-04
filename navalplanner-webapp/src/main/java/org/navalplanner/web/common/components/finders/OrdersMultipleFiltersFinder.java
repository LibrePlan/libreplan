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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


/**
 * Implements all the methods needed to search the criterion to filter the
 * orders. Provides multiples criterions to filter like {@link Criterion},
 * {@link Label}, {@link OrderStatusEnum},{@link ExternalCompany} object , or
 * filter by order code or customer reference.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class OrdersMultipleFiltersFinder extends MultipleFiltersFinder {

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ILabelDAO labelDAO;

    @Autowired
    private IOrderDAO orderDAO;

    private static final Map<CriterionType, List<Criterion>> mapCriterions = new HashMap<CriterionType, List<Criterion>>();

    private static final Map<LabelType, List<Label>> mapLabels = new HashMap<LabelType, List<Label>>();

    private static final List<ExternalCompany> externalCompanies = new ArrayList<ExternalCompany>();

    private static final List<String> customerReferences = new ArrayList<String>();

    private static OrderStatusEnum[] ordersStatusEnums;

    private static final List<String> ordersCodes = new ArrayList<String>();

    protected OrdersMultipleFiltersFinder() {

    }

    @Transactional(readOnly = true)
    public void init() {
        getAdHocTransactionService()
                .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
            @Override
                    public Void execute() {
                        loadLabels();
                        loadCriterions();
                        loadExternalCompanies();
                        loadOrdersStatusEnums();
                        loadOrderCodesAndCustomerReferences();
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

    private void loadLabels() {
        mapLabels.clear();
        List<LabelType> labelTypes = labelTypeDAO.getAll();
        for (LabelType labelType : labelTypes) {
            List<Label> labels = new ArrayList<Label>(labelDAO
                    .findByType(labelType));
            mapLabels.put(labelType, labels);
        }
    }

    private void loadExternalCompanies() {
        externalCompanies.clear();
        externalCompanies.addAll(externalCompanyDAO
                .getExternalCompaniesAreClient());
    }

    private void loadOrdersStatusEnums() {
        ordersStatusEnums = OrderStatusEnum.values();
    }

    private void loadOrderCodesAndCustomerReferences() {
        customerReferences.clear();
        ordersCodes.clear();
        for (Order order : orderDAO.getOrders()) {
            // load customer references
            if ((order.getCustomerReference() != null)
                    && (!order.getCustomerReference().isEmpty())) {
                customerReferences.add(order.getCustomerReference());
            }
            // load the order codes
            ordersCodes.add(order.getCode());
        }
    }

    public List<FilterPair> getFirstTenFilters() {
        getListMatching().clear();
        fillWithFirstTenFiltersLabels();
        fillWithFirstTenFiltersCriterions();
        fillWithFirstTenFiltersCustomer();
        fillWithFirstTenFiltersState();
        fillWihtFirstTenFiltersCodes();
        fillWihtFirstTenFiltersCustomerReferences();
        getListMatching().add(
                new FilterPair(OrderFilterEnum.None,
                OrderFilterEnum.None.toString(), null));
        return getListMatching();
    }

    private List<FilterPair> fillWithFirstTenFiltersLabels() {
        Iterator<LabelType> iteratorLabelType = mapLabels.keySet().iterator();
        while (iteratorLabelType.hasNext() && getListMatching().size() < 10) {
            LabelType type = iteratorLabelType.next();
            for (int i = 0; getListMatching().size() < 10
                    && i < mapLabels.get(type).size(); i++) {
                Label label = mapLabels.get(type).get(i);
                String pattern = type.getName() + " :: " + label.getName();
                getListMatching().add(
                        new FilterPair(OrderFilterEnum.Label, pattern,
                        label));
            }
        }
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
                String pattern = type.getName() + " :: " + criterion.getName();
                getListMatching().add(
                        new FilterPair(OrderFilterEnum.Criterion,
                        pattern, criterion));
            }
        }
        return getListMatching();
    }

    private List<FilterPair> fillWithFirstTenFiltersCustomer() {
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
        for (int i = 0; getListMatching().size() < 10 && i < ordersCodes.size(); i++) {
            String code = ordersCodes.get(i);
            addCode(code);
        }
        return getListMatching();
    }

    private List<FilterPair> fillWihtFirstTenFiltersCustomerReferences() {
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
            filter = filter.toLowerCase();
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

    private void searchInLabelTypes(String filter) {
        boolean limited = (filter.length() < 3);
        for (LabelType type : mapLabels.keySet()) {
            if (type.getName().toLowerCase().contains(filter)) {
                setFilterPairLabelType(type, limited);
            } else {
                searchInLabels(type, filter);
            }
        }
    }

    private void searchInLabels(LabelType type, String filter) {
        for (Label label : mapLabels.get(type)) {
            if (label.getName().toLowerCase().contains(filter)) {
                addLabel(type, label);
                if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                    return;
                }
            }
        }
    }

    private void setFilterPairLabelType(LabelType type, boolean limited) {
        for (Label label : mapLabels.get(type)) {
            addLabel(type, label);
            if ((limited) && (getListMatching().size() > 9)) {
                return;
            }
        }
    }

    private void searchInExternalCompanies(String filter){
        for(ExternalCompany externalCompany : externalCompanies){
            if ((externalCompany.getName().toLowerCase().contains(filter))
                    || (externalCompany.getNif().toLowerCase().contains(filter))) {
                addExternalCompany(externalCompany);
                if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                    return;
                }
            }
        }
    }

    private void searchInOrderStatus(String filter) {
        for (OrderStatusEnum state : ordersStatusEnums) {
            if (state.name().toLowerCase().contains(filter)) {
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
            codeFilter = codeFilter.replace(" ", "");
            for (String code : ordersCodes) {
                if (code.toLowerCase().equals(codeFilter)) {
                    addCode(code);
                    return;
                }
            }
        }
    }

    private void searchInCustomerReferences(String filter) {
        if (filter.indexOf("rc:") == 0) {
            String referenceFilter = filter.replaceFirst("rc:", "");
            referenceFilter = referenceFilter.replace(" ", "");
            for (String reference : customerReferences) {
                if (reference.toLowerCase().equals(referenceFilter)) {
                    addCustomerReference(reference);
                    return;
                }
            }
        }
    }

    private void addCriterion(CriterionType type, Criterion criterion) {
        String pattern = type.getName() + " :: " + criterion.getName();
        getListMatching().add(
                new FilterPair(OrderFilterEnum.Criterion, pattern,
                criterion));
    }

    private void addLabel(LabelType type, Label label) {
        String pattern = type.getName() + " :: " + label.getName();
        getListMatching().add(
                new FilterPair(OrderFilterEnum.Label, pattern, label));
    }

    private void addExternalCompany(ExternalCompany externalCompany) {
        String pattern = externalCompany.getName() + " :: "
                + externalCompany.getNif();
        getListMatching().add(
                new FilterPair(OrderFilterEnum.ExternalCompany,
                pattern, externalCompany));
    }

    private void addState(OrderStatusEnum state) {
        getListMatching().add(
                new FilterPair(OrderFilterEnum.State, state.name(),
                state));
    }

    private void addCode(String code) {
        getListMatching().add(new FilterPair(OrderFilterEnum.Code, code, code));
    }

    private void addCustomerReference(String reference) {
        getListMatching().add(
                new FilterPair(OrderFilterEnum.CustomerReference,
                reference, reference));
    }

    private void addNoneFilter() {
        getListMatching().add(
                new FilterPair(OrderFilterEnum.None,
                OrderFilterEnum.None.toString(), null));
    }

}
