/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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


package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.navalplanner.business.advance.daos.IAdvanceAssignmentDAO;
import org.navalplanner.business.advance.daos.IAdvanceMeasurementDAO;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceMeasurementComparator;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.consolidations.CalculatedConsolidatedValue;
import org.navalplanner.business.planner.entities.consolidations.CalculatedConsolidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.SimpleXYModel;
import org.zkoss.zul.XYModel;
/**
 * Service to manage the advance of a selected order element
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ManageOrderElementAdvancesModel implements
        IManageOrderElementAdvancesModel {

    @Autowired
    private final IAdvanceTypeDAO advanceTypeDAO;

    @Autowired
    private final IOrderElementDAO orderElementDAO;

    private OrderElement orderElement;

    private DirectAdvanceAssignment advanceAssignment;

    private boolean isIndirectAdvanceAssignment = false;

    private List<AdvanceAssignment> listAdvanceAssignments;

    private List<AdvanceType> listAdvanceTypes;

    @Autowired
    public ManageOrderElementAdvancesModel(
            IAdvanceMeasurementDAO advanceMeasurementDAO,
            IAdvanceTypeDAO advanceTypeDAO,
            IOrderElementDAO orderElementDAO,
            IAdvanceAssignmentDAO advanceAssignmentDAO) {
        Validate.notNull(advanceMeasurementDAO);
        this.advanceTypeDAO = advanceTypeDAO;
        this.orderElementDAO = orderElementDAO;
    }

    @Override
    public String getInfoAdvanceAssignment(){
        if (this.advanceAssignment == null || this.orderElement == null) {
            return "";
        }
        return getInfoAdvanceAssignment(this.advanceAssignment);
    }

    private String getInfoAdvanceAssignment(
            DirectAdvanceAssignment assignment) {
        if (assignment == null) {
            return "";
        }
        if ((assignment.getAdvanceType() == null)
                || assignment.getMaxValue() == null) {
            return "";
        }
        return _("{0} (max: {1})", assignment.getAdvanceType()
                .getUnitName(), assignment.getMaxValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceMeasurement> getAdvanceMeasurements() {
        if (this.advanceAssignment == null || this.orderElement == null) {
            return new ArrayList<AdvanceMeasurement>();
        }
        return new ArrayList<AdvanceMeasurement>(this.advanceAssignment
                .getAdvanceMeasurements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceAssignment> getAdvanceAssignments() {
        if (orderElement == null) {
            return new ArrayList<AdvanceAssignment>();
        }
        return listAdvanceAssignments;
    }

    @Override
    public void refreshChangesFromOrderElement() {
        List<AdvanceAssignment> listAdvanceAssignmentsCopy = new ArrayList<AdvanceAssignment>(
                listAdvanceAssignments);
        fillVariables();
        for (AdvanceAssignment advance : listAdvanceAssignmentsCopy) {
            if ((!listAdvanceAssignments.contains(advance))
                    && (advance instanceof DirectAdvanceAssignment)) {
                listAdvanceAssignments.add(advance);
            }
        }
    }

    @Override
    public void prepareEditAdvanceMeasurements(AdvanceAssignment assignment) {
        if (assignment instanceof IndirectAdvanceAssignment) {
            this.advanceAssignment = orderElement
                    .calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) assignment);
            this.isIndirectAdvanceAssignment = true;
        } else {
            if (assignment instanceof DirectAdvanceAssignment) {
                this.advanceAssignment = (DirectAdvanceAssignment) assignment;
                this.isIndirectAdvanceAssignment = false;
            }
        }
    }

    public void createPercentageAdvances(OrderElement orderElement)
            throws DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException {

        if (orderElement != null) {
            DirectAdvanceAssignment advancePercentage = orderElement
                    .getAdvanceAssignmentByType(PredefinedAdvancedTypes.PERCENTAGE
                            .getType());

            boolean existDirectPercentageAdvance = ((advancePercentage != null) && (!advancePercentage
                    .isFake()));

            if ((orderElement.isSchedulingPoint())
                    && (orderElement.getReportGlobalAdvanceAssignment() == null)
                    && (!existDirectPercentageAdvance)) {
                createPercentageAdvance(orderElement);

            } else if (!existDirectPercentageAdvance) {

                for (OrderElement child : orderElement.getChildren()) {
                    createPercentageAdvances(child);
                }
            }
        }
    }

    private void createPercentageAdvance(OrderElement orderElement)
            throws DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException {
        DirectAdvanceAssignment newAdvance = DirectAdvanceAssignment.create();
        newAdvance.setOrderElement(orderElement);

        for (AdvanceType type : this.listAdvanceTypes) {
            if (type.getUnitName().equals(
                    PredefinedAdvancedTypes.PERCENTAGE.getTypeName())) {
                newAdvance.setAdvanceType(type);
                newAdvance.setMaxValue(getMaxValue(type));
            }
        }
        newAdvance.setReportGlobalAdvance(true);
        orderElement.addAdvanceAssignment(newAdvance);
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(OrderElement orderElement) {
        this.orderElement = orderElement;
        this.advanceAssignment = null;
        if (orderElement != null){
            loadAdvanceTypes();
            reattachmentOrderElement();
            forceLoadAdvanceAssignmentsAndMeasurements();
            fillVariables();
        }
    }

    private void forceLoadAdvanceAssignmentsAndMeasurements() {
        for (DirectAdvanceAssignment each : orderElement
                .getDirectAdvanceAssignments()) {
            forceLoadAdvanceConsolidatedValues(each);
            each.getNonCalculatedConsolidation().size();
            each.getAdvanceType().getUnitName();
        }

        for (IndirectAdvanceAssignment each : orderElement
                    .getIndirectAdvanceAssignments()) {
            each.getCalculatedConsolidation().size();
            each.getAdvanceType().getUnitName();
            forceLoadAdvanceConsolidatedValues(orderElement
                    .calculateFakeDirectAdvanceAssignment(each));
        }

    }

    private void forceLoadAdvanceConsolidatedValues(
            DirectAdvanceAssignment advance) {
        for (AdvanceMeasurement measurement : advance.getAdvanceMeasurements()) {
            measurement.getNonCalculatedConsolidatedValues().size();
        }
    }

    public void reattachmentOrderElement() {
        orderElementDAO.reattach(orderElement);
    }

    private void fillVariables() {
        this.listAdvanceAssignments = new ArrayList<AdvanceAssignment>();

        for (DirectAdvanceAssignment each : this.orderElement
                .getDirectAdvanceAssignments()) {
            this.listAdvanceAssignments.add(each);
        }

        for (IndirectAdvanceAssignment each : orderElement
                .getIndirectAdvanceAssignments()) {
                this.listAdvanceAssignments.add(each);
            }
    }

    @Override
    public boolean addNewLineAdvaceAssignment() {
        DirectAdvanceAssignment newAdvance = DirectAdvanceAssignment.create();
        newAdvance.setOrderElement(this.orderElement);

        /*
         * set the first advance type of the list as the default
         */
        List<AdvanceType> listAdvanceType = getPossibleAdvanceTypes(newAdvance);
        if (!listAdvanceType.isEmpty()) {
            newAdvance.setAdvanceType(listAdvanceType.get(0));
            newAdvance.setMaxValue(getMaxValue(listAdvanceType.get(0)));
        } else {
            return false;
        }

        if (listAdvanceAssignments.isEmpty()) {
            newAdvance.setReportGlobalAdvance(true);
        }
        listAdvanceAssignments.add(newAdvance);
        return true;
    }

    @Override
    public BigDecimal getMaxValue(AdvanceType advanceType) {
        if (advanceType != null) {
            return advanceType.getDefaultMaxValue();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public AdvanceAssignment getSpreadAdvance() {
        for(AdvanceAssignment advance : getAdvanceAssignments()){
            if(advance.getReportGlobalAdvance()){
                return advance;
            }
        }
        return null;
    }

    @Override
    public AdvanceMeasurement addNewLineAdvaceMeasurement() {
        if (this.advanceAssignment != null) {
            AdvanceMeasurement newMeasurement = AdvanceMeasurement.create();
            newMeasurement.setDate(new LocalDate());
            newMeasurement.setAdvanceAssignment(this.advanceAssignment);
            if (!this.advanceAssignment.addAdvanceMeasurements(
                    newMeasurement)) {
                newMeasurement.setDate(null);
                this.advanceAssignment.addAdvanceMeasurements(newMeasurement);
            }
            return newMeasurement;
        }
        return null;
    }

    @Override
    public void removeLineAdvanceAssignment(AdvanceAssignment advance) {
        this.listAdvanceAssignments.remove(advance);
        orderElement.removeAdvanceAssignment(advance);
        this.advanceAssignment = null;
    }

    @Override
    public void removeLineAdvanceMeasurement(AdvanceMeasurement advance) {
        this.advanceAssignment.removeAdvanceMeasurements(advance);
    }

    @Override
    public List<AdvanceType> getPossibleAdvanceTypes(
            DirectAdvanceAssignment directAdvanceAssignment) {
        if(orderElement == null){
             return new ArrayList<AdvanceType>();
        }
        List<AdvanceType> advanceTypes = new ArrayList<AdvanceType>();
        for (AdvanceType advanceType : this.listAdvanceTypes) {
            if (advanceType.getUnitName().equals(
                    PredefinedAdvancedTypes.CHILDREN.getTypeName())) {
                continue;
            }
            if (existsAdvanceTypeAlreadyInThisOrderElement(advanceType)) {
                if ((directAdvanceAssignment.getAdvanceType() == null)
                        || (!directAdvanceAssignment.getAdvanceType()
                                .getUnitName()
                                .equals(advanceType.getUnitName()))) {
                    continue;
                }
            }
            advanceTypes.add(advanceType);
        }
        return getSpecificOrder(advanceTypes);
    }

    private List<AdvanceType> getSpecificOrder(List<AdvanceType> advanceTypes ){
        Collections.sort(advanceTypes, new Comparator<AdvanceType>(){

            @Override
            public int compare(AdvanceType arg0, AdvanceType arg1) {
                if((arg0 == null) || (arg0.getUnitName() == null)){
                    return -1;
                }
                if((arg1 == null) || (arg1.getUnitName() == null) || (arg1.getUnitName().equals(PredefinedAdvancedTypes.PERCENTAGE.getTypeName()))){
                    return 1;
                }
                if (arg0.getUnitName().equals(
                        PredefinedAdvancedTypes.PERCENTAGE.getTypeName())) {
                    return -1;
                }
                return (arg0.getUnitName().compareTo(arg1.getUnitName()));
            }
        });
        return advanceTypes;
    }

    private boolean existsAdvanceTypeAlreadyInThisOrderElement(
            AdvanceType advanceType) {
        if (listAdvanceAssignments != null) {
            for (AdvanceAssignment each : listAdvanceAssignments) {
                if ((each.getAdvanceType() != null)
                        && (each.getAdvanceType().getUnitName()
                                .equals(advanceType.getUnitName()))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    private void loadAdvanceTypes() {
        this.listAdvanceTypes = this.advanceTypeDAO.findActivesAdvanceTypes();
    }

    @Override
    public boolean isReadOnlyAdvanceMeasurements(){
        if (this.advanceAssignment == null) {
            return true;
        }

        AdvanceType advanceType = this.advanceAssignment.getAdvanceType();
        if (advanceType != null) {
            if (advanceType.isQualityForm()) {
                return true;
            }
        }

        return this.isIndirectAdvanceAssignment;
    }

    @Override
    public void cleanAdvance(DirectAdvanceAssignment advanceAssignment) {
        if (advanceAssignment != null) {
            advanceAssignment.clearAdvanceMeasurements();
        }
    }

    @Override
    public void resetAdvanceAssignment() {
        this.advanceAssignment = null;
    }

    @Override
    @Transactional(readOnly = true)
    public void confirmSave() throws InstanceNotFoundException,
            DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException {
        orderElementDAO.checkVersion(orderElement);
        reattachmentOrderElement();
        validateBasicData();
    }

    private void validateBasicData()  throws InstanceNotFoundException,
        DuplicateAdvanceAssignmentForOrderElementException,
        DuplicateValueTrueReportGlobalAdvanceException{
        updateRemoveAdvances();
        for (AdvanceAssignment each : this.listAdvanceAssignments) {
            if (each instanceof DirectAdvanceAssignment) {
                validateBasicData((DirectAdvanceAssignment) each);
            }
        }
    }

    private void updateRemoveAdvances(){
        for (AdvanceAssignment each : this.listAdvanceAssignments) {
            AdvanceAssignment advance = yetExistAdvanceAssignment(each);
            if (advance == null) {
                removeAdvanceAssignment(each);
            }
        }
    }

    private void validateBasicData(
            DirectAdvanceAssignment directAdvanceAssignment)
            throws InstanceNotFoundException,DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        if (directAdvanceAssignment.getVersion() == null) {
            addAdvanceAssignment(directAdvanceAssignment);
        }
    }

    private AdvanceAssignment yetExistAdvanceAssignment(
            AdvanceAssignment assignment) {
        for (AdvanceAssignment advance : this.orderElement
                .getDirectAdvanceAssignments()) {
            if (advance.getVersion() != null
                    && advance.getId().equals(assignment.getId())) {
                return advance;
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    private void addAdvanceAssignment(
            DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        this.orderElement.addAdvanceAssignment(newAdvanceAssignment);
    }

    private void removeAdvanceAssignment(AdvanceAssignment assignment) {
        if (assignment != null) {
            orderElement.removeAdvanceAssignment(assignment);
        }
    }

    @Override
    public boolean isPrecisionValid(AdvanceMeasurement advanceMeasurement) {
        if ((this.advanceAssignment != null)
                && (this.advanceAssignment.getAdvanceType() != null)) {
            return advanceMeasurement.checkConstraintValidPrecision();
        }
        return true;
    }

    @Override
    public boolean greatThanMaxValue(AdvanceMeasurement advanceMeasurement) {
        if (this.advanceAssignment == null
                || this.advanceAssignment.getMaxValue() == null) {
            return false;
        }
        return !(advanceMeasurement.checkConstraintValueIsLessThanMaxValue());
    }

    @Override
    public boolean lessThanPreviousMeasurements() {
        if (this.advanceAssignment == null) {
            return false;
        }
        return !(this.advanceAssignment
                .checkConstraintValidAdvanceMeasurements());
    }

    @Override
    public boolean isDistinctValidDate(Date value,
            AdvanceMeasurement newAdvanceMeasurement) {
        if (this.advanceAssignment == null) {
            return true;
        }
        for (AdvanceMeasurement advanceMeasurement : advanceAssignment
                .getAdvanceMeasurements()) {
            LocalDate oldDate = advanceMeasurement.getDate();
            if (oldDate != null
                    && !newAdvanceMeasurement.equals(advanceMeasurement)
                    && oldDate.compareTo(new LocalDate(value)) == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BigDecimal getUnitPrecision(){
        if (this.advanceAssignment == null) {
            return BigDecimal.ZERO;
        }
        return this.advanceAssignment.getAdvanceType().getUnitPrecision();
    }

    @Override
    @Transactional(readOnly = true)
    public AdvanceMeasurement getLastAdvanceMeasurement(
            DirectAdvanceAssignment assignment) {
        if (assignment != null) {
            SortedSet<AdvanceMeasurement> advanceMeasurements = assignment
                    .getAdvanceMeasurements();
            if (advanceMeasurements.size() > 0) {
                return advanceMeasurements.first();
            }
        }
        return null;
    }

    @Override
    public void sortListAdvanceMeasurement() {
        if (advanceAssignment != null) {
            ArrayList<AdvanceMeasurement> advanceMeasurements = new ArrayList<AdvanceMeasurement>(
                advanceAssignment.getAdvanceMeasurements());
            Collections.sort(advanceMeasurements,
                new AdvanceMeasurementComparator());
            TreeSet<AdvanceMeasurement> measurements = new TreeSet<AdvanceMeasurement>(
                new AdvanceMeasurementComparator());
            measurements.addAll(advanceMeasurements);
            this.advanceAssignment
                .setAdvanceMeasurements(measurements);
        }
    }

    @Override
    public BigDecimal getPercentageAdvanceMeasurement(
            AdvanceMeasurement advanceMeasurement) {
        AdvanceAssignment assignment = advanceMeasurement
                .getAdvanceAssignment();
        if (assignment == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxValue;
        if (assignment instanceof IndirectAdvanceAssignment) {
            maxValue = orderElement.calculateFakeDirectAdvanceAssignment(
                    (IndirectAdvanceAssignment) assignment).getMaxValue();
        } else {
            maxValue = ((DirectAdvanceAssignment) assignment)
                    .getMaxValue();
        }

        if (maxValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = advanceMeasurement.getValue();
        if (value == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal division = value.divide(maxValue.setScale(2), 4,
                RoundingMode.DOWN);
        return (division.multiply(new BigDecimal(100))).setScale(2,
                RoundingMode.DOWN);

    }

    @Override
    @Transactional(readOnly = true)
    public DirectAdvanceAssignment calculateFakeDirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        if ((orderElement == null) || (orderElement.isLeaf())) {
            return null;
        }

        reattachmentOrderElement();

        return orderElement
                .calculateFakeDirectAdvanceAssignment(indirectAdvanceAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAdvancePercentageChildren() {
        if ((orderElement == null) || orderElement.isLeaf()) {
            return null;
        }

        reattachmentOrderElement();

        return orderElement.getAdvancePercentageChildren();
    }

    @Override
    @Transactional(readOnly = true)
    public XYModel getChartData(Set<AdvanceAssignment> selectedAdvances) {
        XYModel xymodel = new SimpleXYModel();

        for (AdvanceAssignment each : selectedAdvances) {
            DirectAdvanceAssignment directAdvanceAssignment;
            if (each instanceof DirectAdvanceAssignment) {
                directAdvanceAssignment = (DirectAdvanceAssignment) each;
            } else {
                directAdvanceAssignment = calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) each);
            }
            String title = getInfoAdvanceAssignment(directAdvanceAssignment);
            SortedSet<AdvanceMeasurement> listAdvanceMeasurements = directAdvanceAssignment
                    .getAdvanceMeasurements();
            if (listAdvanceMeasurements.size() > 1) {
                for (AdvanceMeasurement advanceMeasurement : listAdvanceMeasurements) {
                    BigDecimal value = advanceMeasurement.getValue();
                    if ((selectedAdvances.size() > 1) && (value != null)) {
                        BigDecimal maxValue = directAdvanceAssignment
                                .getMaxValue();
                        value = value.divide(maxValue, RoundingMode.DOWN);
                    }
                    LocalDate date = advanceMeasurement.getDate();
                    if ((value != null) && (date != null)) {
                        xymodel.addValue(title, Long.valueOf(date
                                .toDateTimeAtStartOfDay().getMillis()), value);
                    }
                }
            }
        }

        return xymodel;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConsolidatedAdvances(AdvanceAssignment advance) {
        if (advance instanceof DirectAdvanceAssignment) {
            if ((advance.getReportGlobalAdvance())
                    && (!((DirectAdvanceAssignment) advance)
                            .getNonCalculatedConsolidation().isEmpty())) {
                return true;
            }
            return ((!((DirectAdvanceAssignment) advance).isFake()) && (!canBeRemovedAllAdvanceMeasurements((DirectAdvanceAssignment) advance)));

        } else {
            return ((advance.getReportGlobalAdvance()) && (!((IndirectAdvanceAssignment) advance)
                    .getCalculatedConsolidation().isEmpty()));
        }
    }

    private boolean canBeRemovedAllAdvanceMeasurements(
            DirectAdvanceAssignment advance) {
        Iterator<AdvanceMeasurement> iterator = advance
                .getAdvanceMeasurements().iterator();
        while (iterator.hasNext()) {
            if (!canRemoveOrChange(iterator.next())) {
                return false;
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public boolean canRemoveOrChange(AdvanceMeasurement advanceMeasurement) {
        return (!hasConsolidatedAdvances(advanceMeasurement));
    }

    @Transactional(readOnly = true)
    public boolean hasConsolidatedAdvances(AdvanceMeasurement advanceMeasurement) {
        return hasConsolidatedAdvances(advanceMeasurement,
                isIndirectAdvanceAssignment);
    }

    private boolean hasConsolidatedAdvances(
            AdvanceMeasurement advanceMeasurement,
            boolean isIndirectAdvanceAssignment) {

        if (isIndirectAdvanceAssignment) {
            return false;
        }

        if (!advanceMeasurement.getNonCalculatedConsolidatedValues().isEmpty()) {
            return true;
        }

        return findIndirectConsolidation(advanceMeasurement);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isQualityForm(AdvanceAssignment advance) {
        AdvanceType advanceType = advance.getAdvanceType();
        advanceTypeDAO.reattach(advanceType);
        return advanceType.isQualityForm();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean findIndirectConsolidation(
            AdvanceMeasurement advanceMeasurement) {
        AdvanceAssignment advance = advanceMeasurement.getAdvanceAssignment();
        if ((orderElement != null) && (orderElement.getParent() != null) && (advance instanceof DirectAdvanceAssignment)) {

            List<String> types = new ArrayList<String>();

            types.add(advance.getAdvanceType().getUnitName());
            if (advance.getReportGlobalAdvance()) {
                types.add(PredefinedAdvancedTypes.CHILDREN.getTypeName());
            }

            orderElementDAO.reattach(orderElement);
            Set<IndirectAdvanceAssignment> indirects = getSpreadIndirectAdvanceAssignmentWithSameType(
                    orderElement, types);

            for (IndirectAdvanceAssignment indirect : indirects) {
                if (findConsolidatedAdvance(indirect
                    .getCalculatedConsolidation(), advanceMeasurement)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<IndirectAdvanceAssignment> getSpreadIndirectAdvanceAssignmentWithSameType(
            OrderElement orderElement, List<String> types) {

        Set<IndirectAdvanceAssignment> result = new HashSet<IndirectAdvanceAssignment>();

        for (IndirectAdvanceAssignment indirect : orderElement
                .getIndirectAdvanceAssignments()) {
            if ((indirect.getReportGlobalAdvance())
                    && (types.contains(indirect.getAdvanceType().getUnitName()))) {
                result.add(indirect);
            }
        }

        OrderElement parent = orderElement.getParent();
        if (parent != null) {
            result.addAll(getSpreadIndirectAdvanceAssignmentWithSameType(
                    parent, types));
        }

        return result;
    }

    private boolean findConsolidatedAdvance(
            Set<CalculatedConsolidation> consolidations,
            AdvanceMeasurement advance) {
        for (CalculatedConsolidation consolidation : consolidations) {
            for (CalculatedConsolidatedValue value : consolidation
                    .getCalculatedConsolidatedValues()) {
                if ((value.getDate() != null) && (advance.getDate() != null)
                        && (value.getDate().compareTo(advance.getDate()) == 0)) {
                    return true;
                }
            }
        }
        return false;
    }

}
