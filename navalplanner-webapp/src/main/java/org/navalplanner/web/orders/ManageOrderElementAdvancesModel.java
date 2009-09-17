
package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.advance.daos.IAdvanceAssignmentDAO;
import org.navalplanner.business.advance.daos.IAdvanceMeasurementDAO;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceMeasurementComparator;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private AdvanceAssignment advanceAssignment;

    private List<AdvanceAssignment> listAdvanceAssignments;

    private SortedSet<AdvanceMeasurement> listAdvanceMeasurements;

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
        if ((this.advanceAssignment == null) ||
                (this.orderElement == null)) {
            return "";
        }
        if ((this.advanceAssignment.getAdvanceType() == null)
                || this.advanceAssignment.getMaxValue() == null) {
            return "";
        }
        return "    " + this.advanceAssignment.getAdvanceType().getUnitName()
                + _(". Max value: ") + this.advanceAssignment.getMaxValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceMeasurement> getAdvanceMeasurements() {
        if ((this.advanceAssignment == null) ||
                (this.orderElement == null)) {
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
    public void prepareEditAdvanceMeasurements(AdvanceAssignment advanceAssignment) {
        this.advanceAssignment = advanceAssignment;
    }

    @Override
    @Transactional(readOnly = true)
    public void init(OrderElement orderElement) {
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
        for (AdvanceAssignment advanceAssignment : orderElement
                .getAdvanceAssignments()) {
            advanceAssignment.getAdvanceMeasurements().size();
        }
    }

    public void reattachmentOrderElement() {
        orderElementDAO.save(orderElement);
    }

    private void fillVariables() {
        this.listAdvanceAssignments = new ArrayList<AdvanceAssignment>();
        this.listAdvanceMeasurements = new TreeSet<AdvanceMeasurement>(
                new AdvanceMeasurementComparator());
        for (AdvanceAssignment advanceAssignment : this.orderElement
                .getAdvanceAssignments()) {
            this.listAdvanceAssignments.add(advanceAssignment);
            for (AdvanceMeasurement advanceMeasurement : advanceAssignment
                    .getAdvanceMeasurements()) {
                this.listAdvanceMeasurements.add(advanceMeasurement);
            }
        }
    }

    @Override
    public void addNewLineAdvaceAssignment() {
        AdvanceAssignment newAdvance = AdvanceAssignment.create();
        newAdvance.setType(AdvanceAssignment.Type.DIRECT);
        newAdvance.setOrderElement(this.orderElement);

        listAdvanceAssignments.add(newAdvance);
    }

    @Override
    public void addNewLineAdvaceMeasurement() {
        if (this.advanceAssignment != null) {
            AdvanceMeasurement newMeasurement = AdvanceMeasurement.create();
            newMeasurement.setDate(new LocalDate());
            newMeasurement.setAdvanceAssignment(this.advanceAssignment);
            this.advanceAssignment.getAdvanceMeasurements().add(newMeasurement);
        }
    }

    @Override
    public void removeLineAdvanceAssignment(AdvanceAssignment advance) {
        this.listAdvanceAssignments.remove(advance);
        this.advanceAssignment = null;
    }

    @Override
    public void removeLineAdvanceMeasurement(AdvanceMeasurement advance) {
        this.advanceAssignment.getAdvanceMeasurements().remove(advance);
    }

    @Override
    public List<AdvanceType> getActivesAdvanceTypes() {
        if(orderElement == null){
             return new ArrayList<AdvanceType>();
        }
        return this.listAdvanceTypes;
    }

    @Transactional(readOnly = true)
    private void loadAdvanceTypes() {
        this.listAdvanceTypes = this.advanceTypeDAO.findActivesAdvanceTypes(orderElement);
    }

    @Override
    public boolean isReadOnlyAdvanceMeasurements(){
        if (this.advanceAssignment == null)
            return true;
        return this.advanceAssignment.getType().equals(
                AdvanceAssignment.Type.CALCULATED);
    }

    @Override
    public void cleanAdvance(){
        if (this.advanceAssignment != null) {
            this.advanceAssignment.setReportGlobalAdvance(false);
            List<AdvanceMeasurement> listAdvanceMeasurements = new ArrayList<AdvanceMeasurement>(
                    this.advanceAssignment.getAdvanceMeasurements());
            for (AdvanceMeasurement advanceMeasurement : listAdvanceMeasurements) {
                advanceMeasurement.setValue(BigDecimal.ZERO);
                advanceMeasurement.setDate(null);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void accept()throws InstanceNotFoundException,
            DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
            orderElementDAO.checkVersion(orderElement);
            reattachmentOrderElement();
            validateBasicData();
    }

    private void validateBasicData()  throws InstanceNotFoundException,
        DuplicateAdvanceAssignmentForOrderElementException,
        DuplicateValueTrueReportGlobalAdvanceException{
        updateRemoveAdvances();
        for (AdvanceAssignment advanceAssignment : this.listAdvanceAssignments) {
            if(advanceAssignment.getType().equals(AdvanceAssignment.Type.DIRECT))
                validateBasicData(advanceAssignment);
        }
    }

    private void updateRemoveAdvances(){
        for(AdvanceAssignment advanceAssignment : this.listAdvanceAssignments){
            AdvanceAssignment advance = yetExistAdvanceAssignment(advanceAssignment);
            if (advance == null) {
                removeAdvanceAssignment(advance);
            }else{
                for(AdvanceMeasurement advanceMeasurement : this.listAdvanceMeasurements){
                    if (!yetExistAdvanceMeasurement(advance, advanceMeasurement)) {
                        removeAdvanceMeasurement(advanceMeasurement);
                    }
                }
            }
        }
    }

    private void validateBasicData(AdvanceAssignment advanceAssignment)
            throws InstanceNotFoundException,DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        if (advanceAssignment.getVersion() == null) {
            addAdvanceAssignment(advanceAssignment);
        }
    }

    private AdvanceAssignment yetExistAdvanceAssignment(
            AdvanceAssignment advanceAssignment) {
        for (AdvanceAssignment advance : this.orderElement
                .getAdvanceAssignments()) {
            if ((advance.getId() == advanceAssignment.getId()))
                return advance;
        }
        return null;
    }

    private boolean yetExistAdvanceMeasurement(AdvanceAssignment advanceAssignment,
            AdvanceMeasurement advanceMeasurement){
        for (AdvanceMeasurement advance : advanceAssignment
                .getAdvanceMeasurements()) {
            if (advance.getId() == advanceMeasurement.getId()) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    private void addAdvanceAssignment(AdvanceAssignment newAdvanceAssignment)
            throws DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        this.orderElement.addAdvanceAssignment(newAdvanceAssignment);
     }

    private void removeAdvanceAssignment(AdvanceAssignment advanceAssignment){
        orderElement.removeAdvanceAssignment(advanceAssignment);
    }

    private void removeAdvanceMeasurement(AdvanceMeasurement advanceMeasurement){
        AdvanceAssignment advanceAssignment = advanceMeasurement.getAdvanceAssignment();
        advanceAssignment.getAdvanceMeasurements().remove(advanceMeasurement);
    }

    @Override
    public boolean isPrecisionValid(BigDecimal value){
        if ((this.advanceAssignment != null)
                && (this.advanceAssignment.getAdvanceType() != null)) {
            BigDecimal precision = this.advanceAssignment.getAdvanceType()
                    .getUnitPrecision();
            BigDecimal result[] = value.divideAndRemainder(precision);
            if(result[1].compareTo(BigDecimal.ZERO) == 0) return true;
            return false;
        }
        return true;
    }

    @Override
    public boolean greatThanMaxValue(BigDecimal value){
        if ((this.advanceAssignment == null)
                || (this.advanceAssignment.getMaxValue() == null))
            return false;
        if (value.compareTo(this.advanceAssignment.getMaxValue()) > 0)
             return true;
        return false;
    }

    @Override
    public boolean isDistinctValidDate(Date value,
            AdvanceMeasurement newAdvanceMeasurement) {
        if (this.advanceAssignment == null)
            return true;
        for (AdvanceMeasurement advanceMeasurement : advanceAssignment
                .getAdvanceMeasurements()) {
            LocalDate oldDate = advanceMeasurement.getDate();
            if ((oldDate != null)
                    && (!newAdvanceMeasurement.equals(advanceMeasurement))
                    && (oldDate.compareTo(new LocalDate(value)) == 0))
                return false;
        }
        return true;
    }

    @Override
    public BigDecimal getUnitPrecision(){
        if (this.advanceAssignment == null) {
            return new BigDecimal(0);
        }
        return this.advanceAssignment.getAdvanceType().getUnitPrecision();
    }

    @Override
    public AdvanceMeasurement getFirstAdvanceMeasurement(AdvanceAssignment advanceAssignment){
        if((advanceAssignment != null) &&
            (advanceAssignment.getAdvanceMeasurements().size() > 0)) {
            SortedSet<AdvanceMeasurement> listAM = (SortedSet<AdvanceMeasurement>) advanceAssignment.getAdvanceMeasurements();
            final AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement) listAM.first();
            return advanceMeasurement;
        }
        return null;
    }

    @Override
    public void sortListAdvanceMeasurement() {
        ArrayList<AdvanceMeasurement> advanceMeasurements = new ArrayList<AdvanceMeasurement>(
                this.advanceAssignment.getAdvanceMeasurements());
        Collections.sort(advanceMeasurements,
                new AdvanceMeasurementComparator());
        TreeSet<AdvanceMeasurement> measurements = new TreeSet<AdvanceMeasurement>(
                new AdvanceMeasurementComparator());
        measurements.addAll(advanceMeasurements);
        this.advanceAssignment
                .setAdvanceMeasurements(measurements);
    }

    @Override
    public BigDecimal getPercentageAdvanceMeasurement(
            AdvanceMeasurement advanceMeasurement) {
        if (advanceMeasurement.getAdvanceAssignment() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxValue = advanceMeasurement.getAdvanceAssignment()
                .getMaxValue();
        if (maxValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = advanceMeasurement.getValue();
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.divide(maxValue).multiply(new BigDecimal(100));
    }

}
