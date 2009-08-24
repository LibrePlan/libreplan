
package org.navalplanner.web.orders;

import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.apache.commons.lang.Validate;
import org.navalplanner.business.advance.daos.IAdvanceAssigmentDAO;
import org.navalplanner.business.advance.daos.IAdvanceMeasurementDAO;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssigmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
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

    private List<IAdvanceMeasurementDTO> advanceMeasurementDTOs;

    private List<AdvanceType> listAdvanceTypes;

    private List<AdvanceMeasurement> listAdvanceMeasurement;

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
    @Transactional(readOnly = true)
    public List<IAdvanceMeasurementDTO> getAdvanceMeasurements() {
        if (this.orderElement == null) {
            return new ArrayList<IAdvanceMeasurementDTO>();
        }
        return this.advanceMeasurementDTOs;
    }

    @Override
    @Transactional(readOnly = true)
    public void init(OrderElement orderElement) {
        this.orderElement = orderElement;
        if (orderElement != null){
            loadAdvanceTypes();
            reattchmentOrderElement();
            createAdvanceMeasurementDTOs();
        }
    }

    public void reattchmentOrderElement() {
        orderElementDAO.save(orderElement);
    }

    public void createAdvanceMeasurementDTOs() {
        this.listAdvanceMeasurement = new ArrayList<AdvanceMeasurement>();
        this.advanceMeasurementDTOs = new ArrayList<IAdvanceMeasurementDTO>();
        for (AdvanceAssigment advanceAssigment : this.orderElement
                .getAdvanceAssigments()) {
            AdvanceMeasurement advanceMeasurement = ((SortedSet<AdvanceMeasurement>) advanceAssigment
                    .getAdvanceMeasurements()).last();
            IAdvanceMeasurementDTO advanceDTO = new AdvanceMeasurementDTO(
                    advanceAssigment.getAdvanceType(), advanceAssigment,
                    advanceMeasurement);
            this.listAdvanceMeasurement.add(advanceMeasurement);
            this.advanceMeasurementDTOs.add(advanceDTO);
        }
    }

    @Override
    public void prepareForCreate() {
        AdvanceMeasurementDTO newAdvance = new AdvanceMeasurementDTO();
        this.advanceMeasurementDTOs.add(newAdvance);
    }

    @Override
    public void prepareForRemove(IAdvanceMeasurementDTO advanceDTO) {
        this.advanceMeasurementDTOs.remove(advanceDTO);
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
    @Transactional(readOnly = true)
    public void confirm()throws InstanceNotFoundException,
            DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
            orderElementDAO.checkVersion(orderElement);
            validateBasicData();
    }

    private void validateBasicData()  throws InstanceNotFoundException,
            DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        updateRemoveAdvanceMeasurement();
        for(IAdvanceMeasurementDTO advanceDTO : this.advanceMeasurementDTOs){
            validateBasicData(advanceDTO);
        }
    }

    private void updateRemoveAdvanceMeasurement(){
        for(AdvanceMeasurement advanceMeasurement : this.listAdvanceMeasurement){
            if(!yetExistAdvanceMeasurement(advanceMeasurement)){
                removeAdvanceMeasurement(advanceMeasurement);
            }
        }
    }

    private void validateBasicData(IAdvanceMeasurementDTO advanceDTO)
        throws InstanceNotFoundException,DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        if(advanceDTO.getIsNewDTO()){
            AdvanceAssigment newAdvanceAssigment = createNewAdvance(advanceDTO);
            addAdvanceAssigment(newAdvanceAssigment);
        }else{
            if((advanceDTO.getIsNewObject())||
                    (advanceDTO.getAdvanceMeasurement().getDate().compareTo(advanceDTO.getDate()) == 0)){
                updateAdvanceMeasurement(advanceDTO);
            }else{
                addAdvanceMeasurement(advanceDTO);
            }
        }
    }

    private void updateAdvanceMeasurement(IAdvanceMeasurementDTO advanceDTO){
        AdvanceAssigment advanceAssigment = advanceDTO.getAdvanceAssigment();
        advanceAssigment.setReportGlobalAdvance(advanceDTO.getReportGlobalAdvance());
        AdvanceMeasurement advanceMeasurement = advanceDTO.getAdvanceMeasurement();
        advanceMeasurement.setValue(advanceDTO.getValue());
        advanceMeasurement.setMaxValue(advanceDTO.getMaxValue());
        if(advanceDTO.getIsNewObject()){
            advanceMeasurement.setDate(advanceDTO.getDate());
            advanceAssigment.setAdvanceType(advanceDTO.getAdvanceType());
        }
    }

    private void addAdvanceMeasurement(IAdvanceMeasurementDTO advanceDTO){
        AdvanceMeasurement newAdvanceMeasurement = AdvanceMeasurement.create(advanceDTO.getDate(),
        advanceDTO.getValue(),advanceDTO.getMaxValue());
        AdvanceAssigment advanceAssigment = advanceDTO.getAdvanceAssigment();
        newAdvanceMeasurement.setAdvanceAssigment(advanceAssigment);
        advanceAssigment.getAdvanceMeasurements().add(newAdvanceMeasurement);
    }

    private boolean yetExistAdvanceMeasurement(AdvanceMeasurement advanceMeasurement){
        for(IAdvanceMeasurementDTO advanceDTO : this.advanceMeasurementDTOs){
            if((!advanceDTO.getIsNewDTO()) &&
                (advanceDTO.getAdvanceMeasurement().equals(advanceMeasurement)))
                    return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    private AdvanceAssigment createNewAdvance(IAdvanceMeasurementDTO advanceDTO)
            throws InstanceNotFoundException{
            //create AdvanceMeasurement
            AdvanceMeasurement newAdvanceMeasurement = AdvanceMeasurement.create(advanceDTO.getDate(),
                    advanceDTO.getValue(),advanceDTO.getMaxValue());

            //create AdvanceAssigment
            AdvanceAssigment newAdvanceAssigment = AdvanceAssigment.create(
                    advanceDTO.getReportGlobalAdvance());
            newAdvanceAssigment.setAdvanceType(advanceDTO.getAdvanceType());
            newAdvanceAssigment.setOrderElement(this.orderElement);

            //link AdvanceMeasurement to AdvanceAssigment
            newAdvanceMeasurement.setAdvanceAssigment(newAdvanceAssigment);
            newAdvanceAssigment.getAdvanceMeasurements().add(newAdvanceMeasurement);

            return newAdvanceAssigment;
    }

    private void addAdvanceAssigment(AdvanceAssigment newAdvanceAssigment)
            throws DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
                this.orderElement.addAvanceAssigment(newAdvanceAssigment);
     }

    private void removeAdvanceMeasurement(AdvanceMeasurement advanceMeasurement){
        AdvanceAssigment advanceAssigment = advanceMeasurement.getAdvanceAssigment();
        orderElement.getAdvanceAssigments().remove(advanceAssigment);
    }

    @Override
    public boolean isPrecisionValid(IAdvanceMeasurementDTO advanceDTO, BigDecimal value){
        if(advanceDTO.getAdvanceType() != null){
            BigDecimal precision = advanceDTO.getAdvanceType().getUnitPrecision();
            BigDecimal result[] = value.divideAndRemainder(precision);
            if(result[1].compareTo(BigDecimal.ZERO) == 0) return true;
            return false;
        }
        return true;
    }

    @Override
    public boolean greatThanMaxValue(IAdvanceMeasurementDTO advanceDTO, BigDecimal value){
        if(advanceDTO.getMaxValue() == null)
            return false;
        if(value.compareTo(advanceDTO.getMaxValue())>0)
             return true;
        return false;
    }

    @Override
    public boolean isGreatValidDate(IAdvanceMeasurementDTO advanceDTO, Date value){
        if((advanceDTO.getIsNewDTO())||(advanceDTO.getIsNewObject()))
            return true;

        AdvanceAssigment advanceAssigment = advanceDTO.getAdvanceAssigment();
        if(((SortedSet<AdvanceMeasurement>) advanceAssigment
                    .getAdvanceMeasurements()).size() > 0){
            AdvanceMeasurement advanceMeasurement = ((SortedSet<AdvanceMeasurement>) advanceAssigment
                    .getAdvanceMeasurements()).last();
            if(value.compareTo(advanceMeasurement.getDate()) < 0)
                return false;
        }
        return true;
    }

}
