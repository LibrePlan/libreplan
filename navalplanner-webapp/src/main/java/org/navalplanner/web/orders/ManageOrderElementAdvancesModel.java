
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
import org.navalplanner.business.advance.daos.IAdvanceAssigmentDAO;
import org.navalplanner.business.advance.daos.IAdvanceMeasurementDAO;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceMeasurementComparator;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssigmentForOrderElementException;
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

    private AdvanceAssigment advanceAssigment;

    private List<AdvanceAssigment> listAdvanceAssigments;

    private SortedSet<AdvanceMeasurement> listAdvanceMeasurements;

    private List<AdvanceType> listAdvanceTypes;

    @Autowired
    public ManageOrderElementAdvancesModel(
            IAdvanceMeasurementDAO advanceMeasurementDAO,
            IAdvanceTypeDAO advanceTypeDAO,
            IOrderElementDAO orderElementDAO,
            IAdvanceAssigmentDAO advanceAssigmentDAO) {
        Validate.notNull(advanceMeasurementDAO);
        this.advanceTypeDAO = advanceTypeDAO;
        this.orderElementDAO = orderElementDAO;
    }

    @Override
    public String getInfoAdvanceAssigment(){
        if ((this.advanceAssigment == null) ||
                (this.orderElement == null)) {
            return "";
        }
        if ((this.advanceAssigment.getAdvanceType() == null)
                || this.advanceAssigment.getMaxValue() == null) {
            return "";
        }
        return "    " + this.advanceAssigment.getAdvanceType().getUnitName()
                + _(". Max value: ") + this.advanceAssigment.getMaxValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceMeasurement> getAdvanceMeasurements() {
        if ((this.advanceAssigment == null) ||
                (this.orderElement == null)) {
            return new ArrayList<AdvanceMeasurement>();
        }
        return new ArrayList<AdvanceMeasurement>(this.advanceAssigment
                .getAdvanceMeasurements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceAssigment> getAdvanceAssigments() {
        if (orderElement == null) {
            return new ArrayList<AdvanceAssigment>();
        }
        return listAdvanceAssigments;
    }

    @Override
    public void prepareEditAdvanceMeasurements(AdvanceAssigment advanceAssigment) {
        this.advanceAssigment = advanceAssigment;
    }

    @Override
    @Transactional(readOnly = true)
    public void init(OrderElement orderElement) {
        this.orderElement = orderElement;
        this.advanceAssigment = null;
        if (orderElement != null){
            loadAdvanceTypes();
            reattachmentOrderElement();
            forceLoadAdvanceAssigmentsAndMeasurements();
            fillVariables();
        }
    }

    private void forceLoadAdvanceAssigmentsAndMeasurements() {
        for (AdvanceAssigment advanceAssigment : orderElement
                .getAdvanceAssigments()) {
            advanceAssigment.getAdvanceMeasurements().size();
        }
    }

    public void reattachmentOrderElement() {
        orderElementDAO.save(orderElement);
    }

    private void fillVariables() {
        this.listAdvanceAssigments = new ArrayList<AdvanceAssigment>();
        this.listAdvanceMeasurements = new TreeSet<AdvanceMeasurement>(
                new AdvanceMeasurementComparator());
        for (AdvanceAssigment advanceAssigment : this.orderElement
                .getAdvanceAssigments()) {
            this.listAdvanceAssigments.add(advanceAssigment);
            for (AdvanceMeasurement advanceMeasurement : advanceAssigment
                    .getAdvanceMeasurements()) {
                this.listAdvanceMeasurements.add(advanceMeasurement);
            }
        }
    }

    @Override
    public void addNewLineAdvaceAssigment() {
        AdvanceAssigment newAdvance = AdvanceAssigment.create();
        newAdvance.setType(AdvanceAssigment.Type.DIRECT);
        newAdvance.setOrderElement(this.orderElement);

        listAdvanceAssigments.add(newAdvance);
    }

    @Override
    public void addNewLineAdvaceMeasurement() {
        if (this.advanceAssigment != null) {
            AdvanceMeasurement newMeasurement = AdvanceMeasurement.create();
            newMeasurement.setDate(new Date());
            newMeasurement.setAdvanceAssigment(this.advanceAssigment);
            this.advanceAssigment.getAdvanceMeasurements().add(newMeasurement);
        }
    }

    @Override
    public void removeLineAdvanceAssigment(AdvanceAssigment advance) {
        this.listAdvanceAssigments.remove(advance);
        this.advanceAssigment = null;
    }

    @Override
    public void removeLineAdvanceMeasurement(AdvanceMeasurement advance) {
        this.advanceAssigment.getAdvanceMeasurements().remove(advance);
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
        if (this.advanceAssigment == null)
            return true;
        return this.advanceAssigment.getType().equals(
                AdvanceAssigment.Type.CALCULATED);
    }

    @Override
    public void cleanAdvance(){
        if (this.advanceAssigment != null) {
            this.advanceAssigment.setReportGlobalAdvance(false);
            List<AdvanceMeasurement> listAdvanceMeasurements = new ArrayList<AdvanceMeasurement>(
                    this.advanceAssigment.getAdvanceMeasurements());
            for (AdvanceMeasurement advanceMeasurement : listAdvanceMeasurements) {
                advanceMeasurement.setValue(BigDecimal.ZERO);
                advanceMeasurement.setDate(null);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void accept()throws InstanceNotFoundException,
            DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
            orderElementDAO.checkVersion(orderElement);
            reattachmentOrderElement();
            validateBasicData();
    }

    private void validateBasicData()  throws InstanceNotFoundException,
        DuplicateAdvanceAssigmentForOrderElementException,
        DuplicateValueTrueReportGlobalAdvanceException{
        updateRemoveAdvances();
        for (AdvanceAssigment advanceAssigment : this.listAdvanceAssigments) {
            if(advanceAssigment.getType().equals(AdvanceAssigment.Type.DIRECT))
                validateBasicData(advanceAssigment);
        }
    }

    private void updateRemoveAdvances(){
        for(AdvanceAssigment advanceAssigment : this.listAdvanceAssigments){
            AdvanceAssigment advance = yetExistAdvanceAssigment(advanceAssigment);
            if (advance == null) {
                removeAdvanceAssigment(advance);
            }else{
                for(AdvanceMeasurement advanceMeasurement : this.listAdvanceMeasurements){
                    if (!yetExistAdvanceMeasurement(advance, advanceMeasurement)) {
                        removeAdvanceMeasurement(advanceMeasurement);
                    }
                }
            }
        }
    }

    private void validateBasicData(AdvanceAssigment advanceAssigment)
            throws InstanceNotFoundException,DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        if (advanceAssigment.getVersion() == null) {
            addAdvanceAssigment(advanceAssigment);
        }
    }

    private AdvanceAssigment yetExistAdvanceAssigment(
            AdvanceAssigment advanceAssigment) {
        for (AdvanceAssigment advance : this.orderElement
                .getAdvanceAssigments()) {
            if ((advance.getId() == advanceAssigment.getId()))
                return advance;
        }
        return null;
    }

    private boolean yetExistAdvanceMeasurement(AdvanceAssigment advanceAssigment,
            AdvanceMeasurement advanceMeasurement){
        for (AdvanceMeasurement advance : advanceAssigment
                .getAdvanceMeasurements()) {
            if (advance.getId() == advanceMeasurement.getId()) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    private void addAdvanceAssigment(AdvanceAssigment newAdvanceAssigment)
            throws DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        this.orderElement.addAdvanceAssigment(newAdvanceAssigment);
     }

    private void removeAdvanceAssigment(AdvanceAssigment advanceAssigment){
        orderElement.removeAdvanceAssigment(advanceAssigment);
    }

    private void removeAdvanceMeasurement(AdvanceMeasurement advanceMeasurement){
        AdvanceAssigment advanceAssigment = advanceMeasurement.getAdvanceAssigment();
        advanceAssigment.getAdvanceMeasurements().remove(advanceMeasurement);
    }

    @Override
    public boolean isPrecisionValid(BigDecimal value){
        if ((this.advanceAssigment != null)
                && (this.advanceAssigment.getAdvanceType() != null)) {
            BigDecimal precision = this.advanceAssigment.getAdvanceType()
                    .getUnitPrecision();
            BigDecimal result[] = value.divideAndRemainder(precision);
            if(result[1].compareTo(BigDecimal.ZERO) == 0) return true;
            return false;
        }
        return true;
    }

    @Override
    public boolean greatThanMaxValue(BigDecimal value){
        if ((this.advanceAssigment == null)
                || (this.advanceAssigment.getMaxValue() == null))
            return false;
        if (value.compareTo(this.advanceAssigment.getMaxValue()) > 0)
             return true;
        return false;
    }

    @Override
    public boolean isDistinctValidDate(Date value,
            AdvanceMeasurement newAdvanceMeasurement) {
        if (this.advanceAssigment == null)
            return true;
        for (AdvanceMeasurement advanceMeasurement : advanceAssigment
                .getAdvanceMeasurements()) {
            Date oldDate = advanceMeasurement.getDate();
            if ((oldDate != null)
                    && (!newAdvanceMeasurement.equals(advanceMeasurement))
                    && (oldDate.compareTo(value) == 0))
                return false;
        }
        return true;
    }

    @Override
    public BigDecimal getUnitPrecision(){
        if (this.advanceAssigment == null) {
            return new BigDecimal(0);
        }
        return this.advanceAssigment.getAdvanceType().getUnitPrecision();
    }

    @Override
    public AdvanceMeasurement getFirstAdvanceMeasurement(AdvanceAssigment advanceAssigment){
        if((advanceAssigment != null) &&
            (advanceAssigment.getAdvanceMeasurements().size() > 0)) {
            SortedSet<AdvanceMeasurement> listAM = (SortedSet<AdvanceMeasurement>) advanceAssigment.getAdvanceMeasurements();
            final AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement) listAM.first();
            return advanceMeasurement;
        }
        return null;
    }

    @Override
    public void sortListAdvanceMeasurement() {
        ArrayList<AdvanceMeasurement> advanceMeasurements = new ArrayList<AdvanceMeasurement>(
                this.advanceAssigment.getAdvanceMeasurements());
        Collections.sort(advanceMeasurements,
                new AdvanceMeasurementComparator());
        TreeSet<AdvanceMeasurement> measurements = new TreeSet<AdvanceMeasurement>(
                new AdvanceMeasurementComparator());
        measurements.addAll(advanceMeasurements);
        this.advanceAssigment
                .setAdvanceMeasurements(measurements);
    }

    @Override
    public BigDecimal getPercentageAdvanceMeasurement(
            AdvanceMeasurement advanceMeasurement) {
        if (advanceMeasurement.getAdvanceAssigment() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxValue = advanceMeasurement.getAdvanceAssigment()
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
