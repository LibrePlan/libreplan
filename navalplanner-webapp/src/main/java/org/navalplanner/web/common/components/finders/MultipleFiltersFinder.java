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
import java.util.List;
import java.util.Map;

import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;


/**
 * Implements all the methods needed to search the criterion to filter the
 * orders. Provides multiples criterions to filter like {@link Criterion},
 * {@link Label}, {@link OrderStatusEnum},{@link ExternalCompany} object , or
 * filter by order code or customer reference.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class MultipleFiltersFinder implements IMultipleFiltersFinder {

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

    private List<FilterPair> listMatching = new ArrayList<FilterPair>();

    private final String headers[] = { _("Filter type"), _("Filter pattern") };

    protected MultipleFiltersFinder() {

    }

    @Transactional(readOnly = true)
    public void init() {
        loadLabels();
        loadCriterions();
        loadExternalCompanies();
        loadOrdersStatusEnums();
        loadOrderCodesAndCustomerReferences();
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

    public List<FilterPair> getMatching(String filter) {
        listMatching.clear();
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
        return listMatching;
    }

    private void searchInCriterionTypes(String filter) {
        for (CriterionType type : mapCriterions.keySet()) {
            if (type.getName().toLowerCase().contains(filter)) {
                setFilterPairCriterionType(type);
            } else {
                searchInCriterions(type, filter);
            }
        }
    }

    private void searchInCriterions(CriterionType type, String filter) {
        for (Criterion criterion : mapCriterions.get(type)) {
            if (criterion.getName().toLowerCase().contains(filter)) {
                String pattern = type.getName() + " :: " + criterion.getName();
                listMatching.add(new FilterPair(OrderFilterEnum.Criterion,
                        pattern,
                        criterion));
            }
        }
    }

    private void setFilterPairCriterionType(CriterionType type) {
        for (Criterion criterion : mapCriterions.get(type)) {
            String pattern = type.getName() + " :: " + criterion.getName();
            listMatching.add(new FilterPair(OrderFilterEnum.Criterion, pattern,
                    criterion));
        }
    }

    private void searchInLabelTypes(String filter) {
        for (LabelType type : mapLabels.keySet()) {
            if (type.getName().toLowerCase().contains(filter)) {
                setFilterPairLabelType(type);
            } else {
                searchInLabels(type, filter);
            }
        }
    }

    private void searchInLabels(LabelType type, String filter) {
        for (Label label : mapLabels.get(type)) {
            if (label.getName().toLowerCase().contains(filter)) {
                String pattern = type.getName() + " :: " + label.getName();
                listMatching.add(new FilterPair(OrderFilterEnum.Label, pattern,
                        label));
            }
        }
    }

    private void setFilterPairLabelType(LabelType type) {
        for (Label label : mapLabels.get(type)) {
            String pattern = type.getName() + " :: " + label.getName();
            listMatching.add(new FilterPair(OrderFilterEnum.Label, pattern,
                    label));
        }
    }

    private void searchInExternalCompanies(String filter){
        for(ExternalCompany externalCompany : externalCompanies){
            if ((externalCompany.getName().toLowerCase().contains(filter))
                    || (externalCompany.getNif().toLowerCase().contains(filter))) {
                String pattern = externalCompany.getName() + " :: "
                        + externalCompany.getNif();
                listMatching.add(new FilterPair(
                        OrderFilterEnum.ExternalCompany, pattern,
                        externalCompany));
            }
        }
    }

    private void searchInOrderStatus(String filter) {
        for (OrderStatusEnum state : ordersStatusEnums) {
            if (state.name().toLowerCase().contains(filter)) {
                listMatching.add(new FilterPair(OrderFilterEnum.State, state
                        .name(), state));
            }
        }
    }

    private void searchInOrderCodes(String filter) {
        if (filter.indexOf("cod:") == 0) {
            String codeFilter = filter.replaceFirst("cod:", "");
            codeFilter = codeFilter.replace(" ", "");
            for (String code : ordersCodes) {
                if (code.toLowerCase().equals(codeFilter)) {
                    listMatching.add(new FilterPair(OrderFilterEnum.Code, code,
                            code));
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
                    listMatching.add(new FilterPair(
                            OrderFilterEnum.CustomerReference,
                            reference, reference));
                    return;
                }
            }
        }
    }

    public String objectToString(Object obj) {
        FilterPair filterPair = (FilterPair) obj;
        String text = filterPair.getType() + "(" + filterPair.getPattern()
                + "), ";
        return text;
    }

    public boolean isValidFormatText(List filterValues, String value) {
        String[] values = value.split(",");
        if (values.length != filterValues.size() + 1) {
            return false;
        }

        int i = 0;
        for (FilterPair filterPair : (List<FilterPair>) filterValues) {
            String filterValue = values[i].replace(" ", "");
            String filterPairText = filterPair.getType() + "("
                    + filterPair.getPattern() + ")";
            filterPairText = filterPairText.replace(" ", "");
            if (!filterValue.equals(filterPairText)) {
                return false;
            }
            i++;
        }
        return true;
    }

    public String[] getHeaders() {
        return headers;
    }

    public ListitemRenderer getItemRenderer() {
        return filterPairRenderer;
    }

    /**
         * Render for {@link QualityForm}
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
