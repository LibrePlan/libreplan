
package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    private AdvanceAssigmentDTO advanceAssigmentDTO;

    private List<AdvanceAssigmentDTO> advanceAssigmentDTOs;

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
        if((this.advanceAssigmentDTO == null) ||
                (this.orderElement == null)) {
            return "";
        }
        if((this.advanceAssigmentDTO.getUnitName() == null) ||
                this.advanceAssigmentDTO.getMaxValue() == null){
            return "";
        }
        return "    "+this.advanceAssigmentDTO.getUnitName()+_(". Max value: ")+
                this.advanceAssigmentDTO.getMaxValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceMeasurementDTO> getAdvanceMeasurementDTOs() {
        if((this.advanceAssigmentDTO == null) ||
                (this.orderElement == null)) {
            return new ArrayList<AdvanceMeasurementDTO>();
        }
        return this.advanceAssigmentDTO.getAdvanceMeasurementDTOs().toListView();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceAssigmentDTO> getAdvanceAssigmentDTOs(){
        if(this.orderElement == null){
            return new ArrayList<AdvanceAssigmentDTO>();
        }
        return this.advanceAssigmentDTOs;
    }

    @Override
    public void prepareEditAdvanceMeasurements(AdvanceAssigmentDTO advanceAssigmentDTO){
        this.advanceAssigmentDTO = advanceAssigmentDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public void init(OrderElement orderElement) {
        this.orderElement = orderElement;
        this.advanceAssigmentDTO = null;
        if (orderElement != null){
            loadAdvanceTypes();
            reattachmentOrderElement();
            createAdvanceDTOs();
        }
    }

    public void reattachmentOrderElement() {
        orderElementDAO.save(orderElement);
    }

    public void createAdvanceDTOs() {
        this.advanceAssigmentDTOs  =  new ArrayList<AdvanceAssigmentDTO>();
        this.listAdvanceAssigments = new ArrayList<AdvanceAssigment>();
        this.listAdvanceMeasurements = new TreeSet<AdvanceMeasurement>(new AdvanceMeasurementComparator());
        for (AdvanceAssigment advanceAssigment : this.orderElement
                    .getAdvanceAssigments()) {
            AdvanceAssigmentDTO advanceAssigmentDTO = new AdvanceAssigmentDTO(
                advanceAssigment.getAdvanceType(), advanceAssigment,
                advanceAssigment.getAdvanceMeasurements());

            for (AdvanceMeasurement advanceMeasurement : advanceAssigment.
                    getAdvanceMeasurements()) {
                AdvanceMeasurementDTO advanceMeasurementDTO = new AdvanceMeasurementDTO(
                    advanceMeasurement);

                advanceAssigmentDTO.getAdvanceMeasurementDTOs().add(advanceMeasurementDTO);
                advanceMeasurementDTO.setAdvanceAssigmentDTO(advanceAssigmentDTO);
                this.listAdvanceMeasurements.add(advanceMeasurement);
            }
            this.advanceAssigmentDTOs.add(advanceAssigmentDTO);
            this.listAdvanceAssigments.add(advanceAssigment);
        }
    }

    @Override
    public void addNewLineAdvaceAssigment() {
        AdvanceAssigmentDTO newAdvance = new AdvanceAssigmentDTO();
        this.advanceAssigmentDTOs.add(newAdvance);
    }

    @Override
    public void addNewLineAdvaceMeasurement() {
        if(this.advanceAssigmentDTO != null){
            AdvanceMeasurementDTO newAdvance = new AdvanceMeasurementDTO();
            newAdvance.setAdvanceAssigmentDTO(advanceAssigmentDTO);
            this.advanceAssigmentDTO.getAdvanceMeasurementDTOs().add(newAdvance);
        }
    }

    @Override
    public void removeLineAdvanceAssigment(AdvanceAssigmentDTO advanceDTO) {
        this.advanceAssigmentDTOs.remove(advanceDTO);
        this.advanceAssigmentDTO = null;
    }

    @Override
    public void removeLineAdvanceMeasurement(AdvanceMeasurementDTO advanceDTO) {
        this.advanceAssigmentDTO.getAdvanceMeasurementDTOs().remove(advanceDTO);
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
    public boolean isReadOnlyAdvanceMeasurementDTOs(){
        if(this.advanceAssigmentDTO == null) return true;
        return this.advanceAssigmentDTO.getType().equals(AdvanceAssigment.Type.CALCULATED);
    }

    @Override
    public void cleanAdvance(){
        if(this.advanceAssigmentDTO != null){
            this.advanceAssigmentDTO.setReportGlobalAdvance(false);
            List<AdvanceMeasurementDTO> listAdvanceMeasurementDTOs =
                    this.advanceAssigmentDTO.getAdvanceMeasurementDTOs().toListView();
            for(AdvanceMeasurementDTO advanceMeasurementDTO : listAdvanceMeasurementDTOs){
                advanceMeasurementDTO.setValue(BigDecimal.ZERO);
                advanceMeasurementDTO.setDate(null);
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
        for(AdvanceAssigmentDTO advanceAssigmentDTO : this.advanceAssigmentDTOs){
            if(advanceAssigmentDTO.getType().equals(AdvanceAssigment.Type.DIRECT))
                validateBasicData(advanceAssigmentDTO);
        }
    }

    private void updateRemoveAdvances(){
        for(AdvanceAssigment advanceAssigment : this.listAdvanceAssigments){
            AdvanceAssigmentDTO advanceAssigmentDTO = yetExistAdvanceAssigment(advanceAssigment);
            if(advanceAssigmentDTO == null){
                updateRemoveCalculatedAdvanceAssigment(orderElement,advanceAssigment);
                removeAdvanceAssigment(advanceAssigment);
            }else{
                for(AdvanceMeasurement advanceMeasurement : this.listAdvanceMeasurements){
                    if(!yetExistAdvanceMeasurement(advanceAssigmentDTO,advanceMeasurement)){
                        updateRemoveCalculatedAdvanceMeasurement(orderElement,
                            advanceAssigment,advanceMeasurement);
                        removeAdvanceMeasurement(advanceMeasurement);
                    }
                }
            }
        }
    }

    private void validateBasicData(AdvanceAssigmentDTO advanceAssigmentDTO)
            throws InstanceNotFoundException,DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{
        if(advanceAssigmentDTO.getIsNewDTO()){
            AdvanceAssigment newAdvanceAssigment = createNewAdvance(advanceAssigmentDTO);
            addCalculatedAdvanceAssigmentToAncestors(this.orderElement,
                    advanceAssigmentDTO,newAdvanceAssigment);
            addAdvanceAssigment(newAdvanceAssigment);
        }else{
            AdvanceAssigment newAdvanceAssigment = advanceAssigmentDTO.getAdvanceAssigment();
            addCalculatedAdvanceAssigmentToAncestors(this.orderElement,
                    advanceAssigmentDTO,newAdvanceAssigment);
            updateAdvanceAssigment(advanceAssigmentDTO);
        }
    }

    private void updateAdvanceAssigment(AdvanceAssigmentDTO advanceAssigmentDTO){
        //Removed the advance and add a new advanceAssigment
        AdvanceAssigment advanceAssigment = advanceAssigmentDTO.getAdvanceAssigment();
        for(AdvanceMeasurementDTO advanceMeasurementDTO :
            advanceAssigmentDTO.getAdvanceMeasurementDTOs().toListView()){
            if(advanceMeasurementDTO.getIsNewDTO()){
                AdvanceMeasurement newAdvanceMeasurement =
                        createAdvanceMeasurement(advanceMeasurementDTO);
                advanceAssigment.getAdvanceMeasurements().add(newAdvanceMeasurement);
            }else{
                AdvanceMeasurement newAdvanceMeasurement =
                        createAdvanceMeasurement(advanceMeasurementDTO);
                removeAdvanceMeasurement(advanceMeasurementDTO.getAdvanceMeasurement());
                advanceAssigment.getAdvanceMeasurements().add(newAdvanceMeasurement);
            }
        }
        //Update changes in AdvanceAssigment
        advanceAssigment.setReportGlobalAdvance(advanceAssigmentDTO.getReportGlobalAdvance());
        advanceAssigment.setMaxValue(advanceAssigmentDTO.getMaxValue());
        advanceAssigment.setAdvanceType(advanceAssigmentDTO.getAdvanceType());
    }

     private AdvanceMeasurement createAdvanceMeasurement(AdvanceMeasurementDTO advanceMeasurementDTO){
        AdvanceMeasurement newAdvanceMeasurement = AdvanceMeasurement.create(
                advanceMeasurementDTO.getDate(),advanceMeasurementDTO.getValue(),0);
        AdvanceAssigment advanceAssigment = advanceMeasurementDTO.getAdvanceAssigmentDTO().
                getAdvanceAssigment();
        newAdvanceMeasurement.setAdvanceAssigment(advanceAssigment);
        return newAdvanceMeasurement;
    }

    private AdvanceAssigmentDTO yetExistAdvanceAssigment(AdvanceAssigment advanceAssigment){
        for(AdvanceAssigmentDTO advanceDTO : this.advanceAssigmentDTOs){
            if((!advanceDTO.getIsNewDTO()) &&
                (advanceDTO.getAdvanceAssigment().getId() == advanceAssigment.getId()))
                    return advanceDTO;
        }
        return null;
    }

    private boolean yetExistAdvanceMeasurement(AdvanceAssigmentDTO advanceAssigmentDTO,
            AdvanceMeasurement advanceMeasurement){
            if(belongsToAdvanceAssigment(advanceAssigmentDTO,advanceMeasurement)){
                 for(AdvanceMeasurementDTO advanceDTO :
                    advanceAssigmentDTO.getAdvanceMeasurementDTOs().toListView()){
                    if((!advanceDTO.getIsNewDTO()) &&
                        (advanceDTO.getAdvanceMeasurement().getId() == advanceMeasurement.getId())){
                            return true;
                    }
                }
                return false;
            }
            return true;
    }

    private boolean belongsToAdvanceAssigment(AdvanceAssigmentDTO advanceAssigmentDTO,
            AdvanceMeasurement advanceMeasurement){
        AdvanceAssigment advanceAssigment = advanceAssigmentDTO.getAdvanceAssigment();
        if(advanceAssigment != null){
            if(advanceAssigment.getId() == advanceMeasurement.getAdvanceAssigment().getId())
                return true;
            else return false;
        }
        return false;
    }

    @Transactional(readOnly = true)
    private AdvanceAssigment createNewAdvance(AdvanceAssigmentDTO advanceAssigmentDTO)
        throws InstanceNotFoundException{
        //create AdvanceAssigment
        AdvanceAssigment newAdvanceAssigment = AdvanceAssigment.create(
            advanceAssigmentDTO.getReportGlobalAdvance(),
            advanceAssigmentDTO.getMaxValue());
        newAdvanceAssigment.setAdvanceType(advanceAssigmentDTO.getAdvanceType());
        newAdvanceAssigment.setOrderElement(this.orderElement);
        newAdvanceAssigment.setType(AdvanceAssigment.Type.DIRECT);

        //create AdvanceMeasurement
        for(AdvanceMeasurementDTO advanceMeasurementDTO
                 :advanceAssigmentDTO.getAdvanceMeasurementDTOs().toListView()){
            AdvanceMeasurement newAdvanceMeasurement = AdvanceMeasurement.create(
                    advanceMeasurementDTO.getDate(),advanceMeasurementDTO.getValue(),0);

            //link AdvanceMeasurement to AdvanceAssigment
            newAdvanceMeasurement.setAdvanceAssigment(newAdvanceAssigment);
            newAdvanceAssigment.getAdvanceMeasurements().add(newAdvanceMeasurement);
        }
        advanceAssigmentDTO.setAdvanceAssigment(newAdvanceAssigment);
        return newAdvanceAssigment;
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

    private void updateRemoveCalculatedAdvanceAssigment(OrderElement orderElement,
            AdvanceAssigment advanceAssigment){
        OrderElement parent = orderElement.getParent();
        if(parent != null){
            removeCalculatedAdvanceAssigment(parent,advanceAssigment);
            updateRemoveCalculatedAdvanceAssigment(parent,advanceAssigment);
        }
    }

    private void updateRemoveCalculatedAdvanceMeasurement(OrderElement orderElement,
            AdvanceAssigment advanceAssigment,AdvanceMeasurement advanceMeasurement){
        OrderElement parent = orderElement.getParent();
        if(parent != null){
            AdvanceAssigment indirectAdvanceAssigment =
                findCalculatedAdvanceInParent(parent,advanceAssigment.getAdvanceType().getId());
            if(indirectAdvanceAssigment != null){
                removeCalculatedAdvanceMeasurement(advanceMeasurement,indirectAdvanceAssigment);
                updateRemoveCalculatedAdvanceMeasurement(parent,advanceAssigment,advanceMeasurement);
            }
        }
    }

    public void addCalculatedAdvanceAssigmentToAncestors(OrderElement orderElement,
            AdvanceAssigmentDTO newAdvanceAssigmentDTO,AdvanceAssigment newAdvanceAssigment)
        throws DuplicateAdvanceAssigmentForOrderElementException,
            DuplicateValueTrueReportGlobalAdvanceException{

        if (orderElement.getParent() != null) {
            OrderElement parent = orderElement.getParent();
            if(checkChangeTheAdvanceType(newAdvanceAssigmentDTO)){
                removeCalculatedAdvanceAssigment(parent,newAdvanceAssigment);
            }

            AdvanceAssigment indirectAdvanceAssigment =
                findCalculatedAdvanceInParent(parent,newAdvanceAssigmentDTO.getAdvanceType().getId());
            if(indirectAdvanceAssigment == null){
                indirectAdvanceAssigment = initNewCalculatedAdvanceAssigment(parent,newAdvanceAssigmentDTO);
                parent.addAdvanceAssigment(indirectAdvanceAssigment);
            }
            addIncrementMaxValueToAdvanceAssigment(newAdvanceAssigmentDTO,indirectAdvanceAssigment);
            addCalculatedAdvanceMeasurements(newAdvanceAssigmentDTO,indirectAdvanceAssigment);
            addCalculatedAdvanceAssigmentToAncestors(parent,newAdvanceAssigmentDTO,newAdvanceAssigment);
        }
    }

    private void addIncrementMaxValueToAdvanceAssigment(
            AdvanceAssigmentDTO newAdvanceAssigmentDTO,
            AdvanceAssigment indirectAdvanceAssigment){
        BigDecimal incrementMaxValue = getIncrementMaxValue(newAdvanceAssigmentDTO);
        BigDecimal currentMaxValue = indirectAdvanceAssigment.getMaxValue().add(incrementMaxValue);
        indirectAdvanceAssigment.setMaxValue(currentMaxValue);
    }

    private void addCalculatedAdvanceMeasurements(
            AdvanceAssigmentDTO advanceAssigmentDTO, AdvanceAssigment indirectAdvanceAssigment){
            for(AdvanceMeasurementDTO advanceMeasurementDTO
                 :advanceAssigmentDTO.getAdvanceMeasurementDTOs().toListView()){
                 if((advanceMeasurementDTO.getIsNewDTO())
                         || (checkChangeTheAdvanceType(advanceAssigmentDTO))){
                     addNewCalculatedAdvanceMeasurement(advanceMeasurementDTO,indirectAdvanceAssigment);
                 }else{
                     _removeCalculatedAdvanceMeasurement(advanceMeasurementDTO,indirectAdvanceAssigment);
                     addNewCalculatedAdvanceMeasurement(advanceMeasurementDTO,indirectAdvanceAssigment);
                 }
            }
    }

    private void addNewCalculatedAdvanceMeasurement(
        AdvanceMeasurementDTO advanceMeasurementDTO,
        AdvanceAssigment indirectAdvanceAssigment){

        AdvanceMeasurementDTO greatNeighbor = this.getGreatNeighborDTO(advanceMeasurementDTO);
        AdvanceMeasurement lessNeighbor = this.getLessNeighbor(advanceMeasurementDTO);

        incrementLaterCalculatedAdvances(lessNeighbor,greatNeighbor,
                advanceMeasurementDTO,indirectAdvanceAssigment);

        AdvanceMeasurement previousAdvanceMeasurement =
            findPreviousIndirectAdvanceMeasurement(
        advanceMeasurementDTO.getDate(),indirectAdvanceAssigment);
        if(previousAdvanceMeasurement == null){
            //create and add a new indirect AdvanceMeasurement
            AdvanceMeasurement newIndirectAdvanceMeasurement = AdvanceMeasurement.create(
            advanceMeasurementDTO.getDate(),advanceMeasurementDTO.getValue(),0);
            newIndirectAdvanceMeasurement.setAdvanceAssigment(indirectAdvanceAssigment);
            newIndirectAdvanceMeasurement.incrementNumIndirectSons();
            indirectAdvanceAssigment.getAdvanceMeasurements().add(newIndirectAdvanceMeasurement);
        }else{
            if(previousAdvanceMeasurement.getDate().compareTo(advanceMeasurementDTO.getDate()) < 0){
                //create and add a new indirect AdvanceMeasurement
                BigDecimal incrementValue = calculateIncrementValue(lessNeighbor,advanceMeasurementDTO);
                BigDecimal currentValue = previousAdvanceMeasurement.getValue().add(incrementValue);
                AdvanceMeasurement newIndirectAdvanceMeasurement = AdvanceMeasurement.create(
                advanceMeasurementDTO.getDate(),currentValue,0);
                newIndirectAdvanceMeasurement.setAdvanceAssigment(indirectAdvanceAssigment);
                newIndirectAdvanceMeasurement.incrementNumIndirectSons();
                indirectAdvanceAssigment.getAdvanceMeasurements().add(newIndirectAdvanceMeasurement);
            }
            if(previousAdvanceMeasurement.getDate().compareTo(advanceMeasurementDTO.getDate()) == 0){
                previousAdvanceMeasurement.incrementNumIndirectSons();
            }
        }
    }

    private void removeCalculatedAdvanceMeasurement(AdvanceMeasurement advanceMeasurement,
            AdvanceAssigment indirectAdvanceAssigment){
        //find the indirect advanceMeasurement
        AdvanceMeasurement indirectAdvanceMeasurement =
            findIndirectAdvanceMeasurement(advanceMeasurement.getDate(),indirectAdvanceAssigment);
        //check if the indirect advanceMeasurement is the adding of several sons.
        indirectAdvanceMeasurement.decrementNumIndirectSons();
        if(indirectAdvanceMeasurement.getNumIndirectSons() == 0){
            indirectAdvanceAssigment.getAdvanceMeasurements().remove(indirectAdvanceMeasurement);
        }
        //update post indirect advanceMeasurement (substract the increment)
        AdvanceMeasurement[] neighbors = getOldNeighborsAdvanceMeasurement(advanceMeasurement);
        decrementLaterCalculatedAdvances(neighbors,advanceMeasurement,indirectAdvanceAssigment);
    }

    private void _removeCalculatedAdvanceMeasurement(AdvanceMeasurementDTO advanceMeasurementDTO,
            AdvanceAssigment indirectAdvanceAssigment){
        //find the indirect advanceMeasurement
        AdvanceMeasurement advanceMeasurement = advanceMeasurementDTO.getAdvanceMeasurement();
        AdvanceMeasurement indirectAdvanceMeasurement =
            findIndirectAdvanceMeasurement(advanceMeasurement.getDate(),indirectAdvanceAssigment);
        //check if the indirect advanceMeasurement is the adding of several sons.
        indirectAdvanceMeasurement.decrementNumIndirectSons();
        if(indirectAdvanceMeasurement.getNumIndirectSons() == 0){
            indirectAdvanceAssigment.getAdvanceMeasurements().remove(indirectAdvanceMeasurement);
        }
        //update post indirect advanceMeasurement (substract the increment)
        AdvanceMeasurement  lessNeighbor = getLessNeighbor(advanceMeasurementDTO);
        AdvanceMeasurementDTO  greatNeighbor = getGreatNeighborDTO(advanceMeasurementDTO);
        _decrementLaterCalculatedAdvances(lessNeighbor,greatNeighbor,advanceMeasurement,indirectAdvanceAssigment);
    }

    private void removeCalculatedAdvanceAssigment(OrderElement parent,
        AdvanceAssigment newAdvanceAssigment){
        AdvanceAssigment indirectAdvanceAssigment =
                findCalculatedAdvanceInParent(parent,newAdvanceAssigment.getAdvanceType().getId());
        if(indirectAdvanceAssigment != null){
            if(decrementMaxValue(newAdvanceAssigment,indirectAdvanceAssigment)){
                parent.removeAdvanceAssigment(indirectAdvanceAssigment);
            }else{
                for(AdvanceMeasurement advanceMeasurement :
                    newAdvanceAssigment.getAdvanceMeasurements()){
                        removeCalculatedAdvanceMeasurement(advanceMeasurement,indirectAdvanceAssigment);
                    }
            }
        }
    }

    private boolean decrementMaxValue(AdvanceAssigment newAdvanceAssigment,
            AdvanceAssigment indirectAdvanceAssigment){
        BigDecimal maxValue = newAdvanceAssigment.getMaxValue();
        BigDecimal currentMaxValue = indirectAdvanceAssigment.getMaxValue().subtract(maxValue);
        indirectAdvanceAssigment.setMaxValue(currentMaxValue);
        if(currentMaxValue.compareTo(new BigDecimal(0)) == 0){
            return true;
        }
        return false;
    }

    private boolean isAddingOfSeveralSons(AdvanceMeasurement previousAdvanceMeasurement){
        previousAdvanceMeasurement.decrementNumIndirectSons();
        if(previousAdvanceMeasurement.getNumIndirectSons() == 0)
            return false;
        else return true;
    }

    private AdvanceMeasurementDTO getGreatNeighborDTO(
            AdvanceMeasurementDTO advanceMeasurementDTO){
        AdvanceMeasurementDTO  neighbor = null;
        AdvanceAssigmentDTO advanceAssigmentDTO =
                advanceMeasurementDTO.getAdvanceAssigmentDTO();
        List<AdvanceMeasurementDTO> advanceMeasurementDTOs =
                advanceAssigmentDTO.getAdvanceMeasurementDTOs().toListView();

        for(int i=0; i < advanceMeasurementDTOs.size() ; i++){
            AdvanceMeasurementDTO advance =
                    (AdvanceMeasurementDTO) advanceMeasurementDTOs.get(i);
            if(advance.equals(advanceMeasurementDTO)){
                if(i > 0){
                    neighbor =((AdvanceMeasurementDTO)
                            advanceMeasurementDTOs.get(i-1));
                }
                return neighbor;
            }
        }
        return neighbor;
    }

    private AdvanceMeasurement getLessNeighbor(
            AdvanceMeasurementDTO advanceMeasurementDTO){
        AdvanceMeasurement  neighbor = null;

        AdvanceAssigmentDTO advanceAssigmentDTO = advanceMeasurementDTO.getAdvanceAssigmentDTO();
        AdvanceAssigment advanceAssigment = advanceAssigmentDTO.getAdvanceAssigment();
        if((advanceAssigment == null) || (advanceAssigmentDTO.getIsNewDTO())) return neighbor;

        Object[] advanceMeasurements = advanceAssigment.getAdvanceMeasurements().toArray();
        for(int i=0; i < advanceMeasurements.length;i++){
            AdvanceMeasurement advance = (AdvanceMeasurement) advanceMeasurements[i];
            if(advance.getDate().compareTo(advanceMeasurementDTO.getDate()) < 0){
                neighbor=advance;
                return neighbor;
            }
        }
        return neighbor;
    }

    private AdvanceMeasurement[] getOldNeighborsAdvanceMeasurement(
            AdvanceMeasurement advanceMeasurement){
        AdvanceMeasurement  neighbors[] = {null,null};
        AdvanceAssigment advanceAssigment = advanceMeasurement.getAdvanceAssigment();
        Object[] advanceMeasurements = advanceAssigment.getAdvanceMeasurements().toArray();

        for(int i=0; i < advanceMeasurements.length;i++){
            AdvanceMeasurement advance = (AdvanceMeasurement) advanceMeasurements[i];
            if(advance.equals(advanceMeasurement)){
                if(i > 0){
                    neighbors[1]=((AdvanceMeasurement) advanceMeasurements[i-1]);
                }
                if(i < advanceMeasurements.length-1){
                    neighbors[0]=((AdvanceMeasurement) advanceMeasurements[i+1]);
                }
                return neighbors;
            }
        }
        return neighbors;
    }

    private void incrementLaterCalculatedAdvances(AdvanceMeasurement lessNeighbor,
        AdvanceMeasurementDTO greatNeighbor,
        AdvanceMeasurementDTO advanceMeasurementDTO,
        AdvanceAssigment indirectAdvanceAssigment){

        BigDecimal incrementValue = calculateIncrementValue(lessNeighbor,advanceMeasurementDTO);

        Date dateIni = advanceMeasurementDTO.getDate();
        Date dateFin = advanceMeasurementDTO.getDate();

        for(AdvanceMeasurement indirectAdvanceMeasurement :
            indirectAdvanceAssigment.getAdvanceMeasurements()){
            if((indirectAdvanceMeasurement.getDate().compareTo(dateIni) >= 0)
                && (isIntoIntervalDateFin(greatNeighbor,dateFin,indirectAdvanceMeasurement))){
                    indirectAdvanceMeasurement.setValue(indirectAdvanceMeasurement.
                    getValue().add(incrementValue));
            }
        }
    }

    private void _decrementLaterCalculatedAdvances(AdvanceMeasurement lessNeighbor,
            AdvanceMeasurementDTO greatNeighbor,
            AdvanceMeasurement advanceMeasurement,
            AdvanceAssigment indirectAdvanceAssigment){

            BigDecimal decrementValue = calculateDecrementValue(lessNeighbor,advanceMeasurement);

            Date dateFin = advanceMeasurement.getDate();
            Date dateIni = advanceMeasurement.getDate();

            for(AdvanceMeasurement indirectAdvanceMeasurement :
            indirectAdvanceAssigment.getAdvanceMeasurements()){
                if((indirectAdvanceMeasurement.getDate().compareTo(dateIni) >= 0)
                && (isIntoIntervalDateFin(greatNeighbor,dateFin,indirectAdvanceMeasurement))){
                   indirectAdvanceMeasurement.setValue(
                            indirectAdvanceMeasurement.getValue().subtract(decrementValue));
                }
            }
    }

    private void decrementLaterCalculatedAdvances(AdvanceMeasurement[] neighbors
            ,AdvanceMeasurement advanceMeasurement,
            AdvanceAssigment indirectAdvanceAssigment){

            BigDecimal decrementValue = calculateDecrementValue(neighbors[0],advanceMeasurement);

            Date dateFin = advanceMeasurement.getDate();
            Date dateIni = advanceMeasurement.getDate();

            for(AdvanceMeasurement indirectAdvanceMeasurement :
            indirectAdvanceAssigment.getAdvanceMeasurements()){
                if((indirectAdvanceMeasurement.getDate().compareTo(dateIni) >= 0)
                && (isIntoIntervalDateFin(neighbors[1],dateFin,indirectAdvanceMeasurement))){
                   indirectAdvanceMeasurement.setValue(indirectAdvanceMeasurement.getValue().subtract(decrementValue));
                }
            }
    }

    private boolean isIntoIntervalDateFin(AdvanceMeasurementDTO neighbor,
            Date dateFin,AdvanceMeasurement advanceMeasurement){
        if(neighbor != null){
            dateFin = neighbor.getDate();
            if(advanceMeasurement.getDate().compareTo(dateFin) < 0) return true;
            else return false;
        }
        return true;
    }

    private boolean isIntoIntervalDateFin(AdvanceMeasurement neighbor,
            Date dateFin,AdvanceMeasurement advanceMeasurement){
        if(neighbor != null){
            dateFin = neighbor.getDate();
            if(advanceMeasurement.getDate().compareTo(dateFin) < 0) return true;
            else return false;
        }
        return true;
    }

    private BigDecimal calculateIncrementValue(AdvanceMeasurement neighbor
            ,AdvanceMeasurementDTO advanceMeasurementDTO){
            //Calculate the increment value
            BigDecimal incrementValue = advanceMeasurementDTO.getValue();
            if(neighbor != null){
                BigDecimal previousValue = neighbor.getValue();
                incrementValue = incrementValue.subtract(previousValue);
            }
            return incrementValue;
    }

    private BigDecimal calculateDecrementValue(AdvanceMeasurement neighbor
            ,AdvanceMeasurement advanceMeasurement){
            //Calculate the decrement value
            BigDecimal decrementValue = advanceMeasurement.getValue();
            if(neighbor != null){
                BigDecimal previousValue = neighbor.getValue();
                decrementValue = decrementValue.subtract(previousValue);
            }
            return decrementValue;

    }

    private BigDecimal getIncrementMaxValue(AdvanceAssigmentDTO advanceAssigmentDTO){
        BigDecimal incrementMaxValue= new BigDecimal(0);
        if((advanceAssigmentDTO.getIsNewDTO())
                || (checkChangeTheAdvanceType(advanceAssigmentDTO))){
            incrementMaxValue = advanceAssigmentDTO.getMaxValue();
        }else{
            AdvanceAssigment advanceAssigment = advanceAssigmentDTO.getAdvanceAssigment();
            incrementMaxValue = advanceAssigmentDTO.getMaxValue().subtract(advanceAssigment.getMaxValue());
        }
        return incrementMaxValue;
    }

    private AdvanceAssigment initNewCalculatedAdvanceAssigment(
        OrderElement orderElement,AdvanceAssigmentDTO advanceAssigmentDTO){
         //create AdvanceAssigment
        AdvanceAssigment newAdvanceAssigment = AdvanceAssigment.create(
            advanceAssigmentDTO.getReportGlobalAdvance(),new BigDecimal(0));
        newAdvanceAssigment.setAdvanceType(advanceAssigmentDTO.getAdvanceType());
        newAdvanceAssigment.setOrderElement(orderElement);
        newAdvanceAssigment.setType(AdvanceAssigment.Type.CALCULATED);

        return newAdvanceAssigment;
    }

    private AdvanceMeasurement findPreviousIndirectAdvanceMeasurement(
        Date date,AdvanceAssigment indirectAdvanceAssigment){
        Object[] arrayAdvanceMeasurements = indirectAdvanceAssigment.getAdvanceMeasurements().toArray();
        for(int i=0; i < arrayAdvanceMeasurements.length; i++){
            AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement)arrayAdvanceMeasurements[i];
            if(advanceMeasurement.getDate().compareTo(date) <= 0){
                return advanceMeasurement;
            }
        }
        return null;
    }

    private AdvanceMeasurement findIndirectAdvanceMeasurement(
        Date date,AdvanceAssigment indirectAdvanceAssigment){
        Object[] arrayAdvanceMeasurements = indirectAdvanceAssigment.getAdvanceMeasurements().toArray();
        for(int i=0; i < arrayAdvanceMeasurements.length; i++){
            AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement)arrayAdvanceMeasurements[i];
            if(advanceMeasurement.getDate().compareTo(date) == 0){
                return advanceMeasurement;
            }
        }
        return null;
    }

    private AdvanceAssigment findCalculatedAdvanceInParent(
            OrderElement orderElement, Long id){
        for(AdvanceAssigment oldAdvanceAssigment : orderElement.getAdvanceAssigments()){
            if(oldAdvanceAssigment.getAdvanceType().getId().equals(id))
                return oldAdvanceAssigment;
        }
        return null;
    }

    private boolean checkChangeTheAdvanceType(AdvanceAssigmentDTO newAdvanceAssigmentDTO){
        AdvanceAssigment advanceAssigment = newAdvanceAssigmentDTO.getAdvanceAssigment();
        AdvanceType advanceType = advanceAssigment.getAdvanceType();
        AdvanceType advanceTypeDTO = newAdvanceAssigmentDTO.getAdvanceType();
        if((newAdvanceAssigmentDTO.getIsNewObject())
            && (!advanceType.equals(advanceTypeDTO))) return true;
        return false;
    }

    @Override
    public boolean isPrecisionValid(BigDecimal value){
        if((this.advanceAssigmentDTO != null)
                && (this.advanceAssigmentDTO.getAdvanceType() != null)){
            BigDecimal precision = this.advanceAssigmentDTO.getAdvanceType().getUnitPrecision();
            BigDecimal result[] = value.divideAndRemainder(precision);
            if(result[1].compareTo(BigDecimal.ZERO) == 0) return true;
            return false;
        }
        return true;
    }

    @Override
    public boolean greatThanMaxValue(BigDecimal value){
        if((this.advanceAssigmentDTO == null)
            ||(this.advanceAssigmentDTO.getMaxValue() == null))
            return false;
        if(value.compareTo(this.advanceAssigmentDTO.getMaxValue())>0)
             return true;
        return false;
    }

    @Override
    public boolean isDistinctValidDate(Date value,AdvanceMeasurementDTO newAdvanceMeasurementDTO){
        if(this.advanceAssigmentDTO == null) return true;
        int equalsDates = 0;
        for(AdvanceMeasurementDTO advanceMeasurementDTO
                : advanceAssigmentDTO.getAdvanceMeasurementDTOs().toListView()){
                Date oldDate = advanceMeasurementDTO.getDate();
                if((oldDate != null) && (!newAdvanceMeasurementDTO.equals(advanceMeasurementDTO))
                        && (oldDate.compareTo(value) == 0))
                        return false;
        }
        return true;
    }

    @Override
    public BigDecimal getUnitPrecision(){
        if(this.advanceAssigmentDTO == null){
            return new BigDecimal(0);
        }
        return this.advanceAssigmentDTO.getAdvanceType().getUnitPrecision();
    }

    @Override
    public AdvanceMeasurementDTO getFirstAdvanceMeasurement(AdvanceAssigmentDTO advanceAssigmentDTO){
        if((advanceAssigmentDTO != null) &&
            (advanceAssigmentDTO.getAdvanceMeasurementDTOs().toListView().size()>0)){
            final AdvanceMeasurementDTO advanceMeasurementDTO =
                (AdvanceMeasurementDTO) advanceAssigmentDTO.getAdvanceMeasurementDTOs().toListView().get(0);
            return advanceMeasurementDTO;
        }
        return null;
    }

    public AdvanceMeasurement getFirstAdvanceMeasurement(AdvanceAssigment advanceAssigment){
        if((advanceAssigment != null) &&
            (advanceAssigmentDTO.getAdvanceMeasurements().size()>0)){
            SortedSet<AdvanceMeasurement> listAM = (SortedSet<AdvanceMeasurement>) advanceAssigment.getAdvanceMeasurements();
            final AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement) listAM.first();
            return advanceMeasurement;
        }
        return null;
    }

    @Override
    public void modifyListAdvanceMeasurement(AdvanceMeasurementDTO advanceMeasurementDTO){
        this.advanceAssigmentDTO.getAdvanceMeasurementDTOs().modified(advanceMeasurementDTO);
    }
}
