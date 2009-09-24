
package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
import org.navalplanner.business.orders.entities.OrderLineGroup;
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
        return _("Advance assignment: {0} (max: {1})", this.advanceAssignment
                .getAdvanceType().getUnitName(), this.advanceAssignment
                .getMaxValue());
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
        if (advanceAssignment instanceof IndirectAdvanceAssignment) {
            this.advanceAssignment = ((OrderLineGroup) this.orderElement)
                    .calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) advanceAssignment);
            this.isIndirectAdvanceAssignment = true;
        } else {
            this.advanceAssignment = (DirectAdvanceAssignment) advanceAssignment;
            this.isIndirectAdvanceAssignment = false;
        }
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
        for (DirectAdvanceAssignment advanceAssignment : orderElement
                .getDirectAdvanceAssignments()) {
            advanceAssignment.getAdvanceMeasurements().size();
        }
        if (orderElement instanceof OrderLineGroup) {
            ((OrderLineGroup) orderElement).getIndirectAdvanceAssignments().size();
        }
    }

    public void reattachmentOrderElement() {
        orderElementDAO.save(orderElement);
    }

    private void fillVariables() {
        this.listAdvanceAssignments = new ArrayList<AdvanceAssignment>();
        this.listAdvanceMeasurements = new TreeSet<AdvanceMeasurement>(
                new AdvanceMeasurementComparator());

        for (DirectAdvanceAssignment advanceAssignment : this.orderElement
                .getDirectAdvanceAssignments()) {
            this.listAdvanceAssignments.add(advanceAssignment);
            for (AdvanceMeasurement advanceMeasurement : advanceAssignment
                    .getAdvanceMeasurements()) {
                this.listAdvanceMeasurements.add(advanceMeasurement);
            }
        }

        if (this.orderElement instanceof OrderLineGroup) {
            for (IndirectAdvanceAssignment advanceAssignment : ((OrderLineGroup) this.orderElement)
                    .getIndirectAdvanceAssignments()) {
                this.listAdvanceAssignments.add(advanceAssignment);
            }
        }
    }

    @Override
    public void addNewLineAdvaceAssignment() {
        DirectAdvanceAssignment newAdvance = DirectAdvanceAssignment.create();
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
        return advanceTypes;
    }

    private boolean existsAdvanceTypeAlreadyInThisOrderElement(
            AdvanceType advanceType) {
        if (listAdvanceAssignments != null) {
            for (AdvanceAssignment advanceAssignment : listAdvanceAssignments) {
                if ((advanceAssignment.getAdvanceType() != null)
                        && (advanceAssignment.getAdvanceType().getUnitName()
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
        if (this.advanceAssignment == null)
            return true;
        return this.isIndirectAdvanceAssignment;
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
            if (advanceAssignment instanceof DirectAdvanceAssignment)
                validateBasicData((DirectAdvanceAssignment) advanceAssignment);
        }
    }

    private void updateRemoveAdvances(){
        for(AdvanceAssignment advanceAssignment : this.listAdvanceAssignments){
            AdvanceAssignment advance = yetExistAdvanceAssignment(advanceAssignment);
            if (advance == null) {
                removeAdvanceAssignment(advance);
            }else{
                for (AdvanceMeasurement advanceMeasurement : this.listAdvanceMeasurements) {
                    if (advance instanceof DirectAdvanceAssignment) {
                        if (!yetExistAdvanceMeasurement(
                                (DirectAdvanceAssignment) advance,
                                advanceMeasurement)) {
                            removeAdvanceMeasurement(advanceMeasurement);
                        }
                    }
                }
            }
        }
    }

    private void validateBasicData(DirectAdvanceAssignment advanceAssignment)
            throws InstanceNotFoundException,DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        if (advanceAssignment.getVersion() == null) {
            addAdvanceAssignment(advanceAssignment);
        }
    }

    private AdvanceAssignment yetExistAdvanceAssignment(
            AdvanceAssignment advanceAssignment) {
        for (AdvanceAssignment advance : this.orderElement
                .getDirectAdvanceAssignments()) {
            if ((advance.getId() == advanceAssignment.getId()))
                return advance;
        }
        return null;
    }

    private boolean yetExistAdvanceMeasurement(
            DirectAdvanceAssignment advanceAssignment,
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
    private void addAdvanceAssignment(
            DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateAdvanceAssignmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        this.orderElement.addAdvanceAssignment(newAdvanceAssignment);
    }

    private void removeAdvanceAssignment(AdvanceAssignment advanceAssignment){
        orderElement.removeAdvanceAssignment(advanceAssignment);
    }

    private void removeAdvanceMeasurement(AdvanceMeasurement advanceMeasurement){
        DirectAdvanceAssignment advanceAssignment = (DirectAdvanceAssignment) advanceMeasurement
                .getAdvanceAssignment();
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
    @Transactional(readOnly = true)
    public AdvanceMeasurement getLastAdvanceMeasurement(
            DirectAdvanceAssignment advanceAssignment) {
        if (advanceAssignment != null) {
            SortedSet<AdvanceMeasurement> advanceMeasurements = advanceAssignment
                    .getAdvanceMeasurements();
            if (advanceMeasurements.size() > 0) {
                return advanceMeasurements.first();
            }
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
        AdvanceAssignment advanceAssignment = advanceMeasurement
                .getAdvanceAssignment();
        if (advanceAssignment == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxValue;
        if (advanceAssignment instanceof IndirectAdvanceAssignment) {
            maxValue = ((OrderLineGroup) this.orderElement)
                    .calculateFakeDirectAdvanceAssignment(
                            (IndirectAdvanceAssignment) advanceAssignment)
                    .getMaxValue();
        } else {
            maxValue = ((DirectAdvanceAssignment) advanceAssignment)
                    .getMaxValue();
        }

        if (maxValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = advanceMeasurement.getValue();
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.setScale(2).divide(maxValue, RoundingMode.DOWN).multiply(
                new BigDecimal(100));
    }

    @Override
    @Transactional(readOnly = true)
    public DirectAdvanceAssignment calculateFakeDirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        if (orderElement == null) {
            return null;
        }

        if (!(orderElement instanceof OrderLineGroup)) {
            return null;
        }

        reattachmentOrderElement();

        return ((OrderLineGroup) orderElement)
                .calculateFakeDirectAdvanceAssignment(indirectAdvanceAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAdvancePercentageChildren() {
        if (orderElement == null) {
            return null;
        }

        if (!(orderElement instanceof OrderLineGroup)) {
            return null;
        }

        reattachmentOrderElement();

        return ((OrderLineGroup) orderElement).getAdvancePercentageChildren();
    }

    @Override
    public XYModel getChartData() {
        XYModel xymodel = new SimpleXYModel();
        if (this.advanceAssignment != null) {
            String title = getInfoAdvanceAssignment();
            SortedSet<AdvanceMeasurement> listAdvanceMeasurements = this.advanceAssignment
                    .getAdvanceMeasurements();
            if (listAdvanceMeasurements.size() > 1) {
                for (AdvanceMeasurement advanceMeasurement : listAdvanceMeasurements) {
                    BigDecimal value = advanceMeasurement.getValue();
                    LocalDate date = advanceMeasurement.getDate();
                    if ((value != null) && (date != null)) {
                        xymodel.addValue(title, new Long(date
                                .toDateTimeAtStartOfDay().getMillis()),
                                value);
                    }
                }
            }
        }
        return xymodel;
    }

}
