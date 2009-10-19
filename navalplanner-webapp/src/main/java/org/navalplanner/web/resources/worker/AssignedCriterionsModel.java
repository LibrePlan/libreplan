/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.web.resources.worker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.ClassValidator;
import static org.navalplanner.web.I18nHelper._;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Interval;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.WrongValueException;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@Service()
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedCriterionsModel implements IAssignedCriterionsModel {

    private ClassValidator<CriterionSatisfactionDTO> satisfactionDTOValidator = new ClassValidator<CriterionSatisfactionDTO>(
            CriterionSatisfactionDTO.class);

    private ClassValidator<CriterionSatisfaction> satisfactionValidator = new ClassValidator<CriterionSatisfaction>(
            CriterionSatisfaction.class);

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    private List<CriterionWithItsType> criterionsWithItsTypes = new ArrayList<CriterionWithItsType>();

    private Worker worker;

    private Set<CriterionSatisfactionDTO> criterionSatisfactionDTOs = new HashSet<CriterionSatisfactionDTO>();

    private static List<ResourceEnum> applicableResources = new ArrayList<ResourceEnum>();

    static {
        applicableResources.add(ResourceEnum.RESOURCE);
        applicableResources.add(ResourceEnum.WORKER);
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareForEdit(Worker worker) {
        this.worker = worker;
        if(worker != null){
            reattachmentWorker();
            initDTOs();
        }
    }

    public void prepareForCreate(Worker worker){
        this.worker = worker;
        this.criterionSatisfactionDTOs = new HashSet<CriterionSatisfactionDTO>();
    }

    private void initDTOs(){
        criterionSatisfactionDTOs = new HashSet<CriterionSatisfactionDTO>();
        for(CriterionSatisfaction criterionSatisfaction :
            worker.getCriterionSatisfactions()){
                if(!criterionSatisfaction.isIsDeleted()){
                    CriterionSatisfactionDTO dto =
                            new CriterionSatisfactionDTO(criterionSatisfaction);
                    criterionSatisfactionDTOs.add(dto);
                }
        }
    }

    @Override
    public Set<CriterionSatisfactionDTO> getAllCriterionSatisfactions() {
        if(worker == null){
            return new HashSet<CriterionSatisfactionDTO>();
        }
        return allSatisfactionsDTO();
    }

     @Override
    public Set<CriterionSatisfactionDTO> getFilterCriterionSatisfactions() {
        if(worker == null){
            return new HashSet<CriterionSatisfactionDTO>();
        }
        return filterSatisfactionsDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public void reattachmentWorker(){
            resourceDAO.save(worker);
            for(CriterionSatisfaction criterionSatisfaction :
                worker.getCriterionSatisfactions()){
                criterionSatisfaction.getCriterion().getName();
                criterionSatisfaction.getCriterion().getType().getName();
                if(criterionSatisfaction.getCriterion().getParent() != null){
                    criterionSatisfaction.getCriterion().getParent().getName();
                }
            }
    }

    @Override
    public void addCriterionSatisfaction(){
        CriterionSatisfactionDTO criterionSatisfactionDTO = new CriterionSatisfactionDTO();
        this.criterionSatisfactionDTOs.add(criterionSatisfactionDTO);
    }

    private Set<CriterionSatisfactionDTO> allSatisfactionsDTO(){
        Set<CriterionSatisfactionDTO> satisfactions = new HashSet<CriterionSatisfactionDTO>();
        for(CriterionSatisfactionDTO criterionSatisfactionDTO : criterionSatisfactionDTOs){
            if(!criterionSatisfactionDTO.isIsDeleted()){
                satisfactions.add(criterionSatisfactionDTO);
            }
        }
        return satisfactions;
    }

    private Set<CriterionSatisfactionDTO> filterSatisfactionsDTO(){
        Set<CriterionSatisfactionDTO> satisfactions = new HashSet<CriterionSatisfactionDTO>();
        for(CriterionSatisfactionDTO criterionSatisfactionDTO : criterionSatisfactionDTOs){
            if((!criterionSatisfactionDTO.isIsDeleted())&&
                    (criterionSatisfactionDTO.isCurrent())){
                satisfactions.add(criterionSatisfactionDTO);
            }
        }
        return satisfactions;
    }

    @Override
    public void remove(CriterionSatisfactionDTO criterionSatisfactionDTO){
        if(criterionSatisfactionDTO.isNewObject()){
            criterionSatisfactionDTOs.remove(criterionSatisfactionDTO);
        }else{
            criterionSatisfactionDTO.setIsDeleted(true);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CriterionWithItsType> getCriterionWithItsType() {
        criterionsWithItsTypes = new ArrayList<CriterionWithItsType>();
        List<CriterionType> listTypes = getCriterionTypes();
        for(CriterionType criterionType : listTypes){
            if(criterionType.isEnabled()){
                Set<Criterion> listCriterion = getDirectCriterions(criterionType);
                getCriterionWithItsType(criterionType,listCriterion );
            }
        }
        return criterionsWithItsTypes;
    }

    private List<CriterionType> getCriterionTypes() {
        return criterionTypeDAO
                .getCriterionTypesByResources(applicableResources);
    }

    private void getCriterionWithItsType(CriterionType type, Set<Criterion> children){
        for(Criterion criterion : children){
             if(criterion.isActive()){
                //Create the criterion with its criterionType and its Hierarchy label
                CriterionWithItsType criterionAndType = new CriterionWithItsType(type,criterion);
                //Add to the list
                criterionsWithItsTypes.add(criterionAndType);
                getCriterionWithItsType(type,criterion.getChildren());
             }
        }
    }

    private static Set<Criterion> getDirectCriterions(CriterionType criterionType){
        Set<Criterion> criterions = new HashSet<Criterion>();
        for(Criterion criterion : criterionType.getCriterions()){
            if(criterion.getParent() == null){
                criterions.add(criterion);
            }
        }
        return criterions;
    }

    @Override
    public void setCriterionWithItsType(CriterionSatisfactionDTO criterionSatisfactionDTO,
            CriterionWithItsType criterionAndType) throws WrongValueException{
            criterionSatisfactionDTO.setCriterionWithItsType(criterionAndType);
    }

    @Override
    public boolean checkSameCriterionAndSameInterval(CriterionSatisfactionDTO satisfaction){
        return existSameCriterionAndInterval(satisfaction);
    }

    @Override
    public boolean checkNotAllowSimultaneousCriterionsPerResource(
            CriterionSatisfactionDTO satisfaction){
        ICriterionType<?> type = satisfaction.getCriterionWithItsType().getType();
        if (type.isAllowSimultaneousCriterionsPerResource()) {
            return false;
        }
        return existSameCriterionTypeAndInterval(satisfaction);
    }


    private boolean existSameCriterionTypeAndInterval(CriterionSatisfactionDTO satisfaction){
        for(CriterionSatisfactionDTO otherSatisfaction : criterionSatisfactionDTOs){
            if((!otherSatisfaction.equals(satisfaction))&&
                    (!otherSatisfaction.isIsDeleted())&&
                    (!satisfaction.isIsDeleted())&&
                    (sameCriterionType(otherSatisfaction,satisfaction)) &&
                    (sameInterval(otherSatisfaction,satisfaction))) {
                return true;
            }
        }
        return false;
    }

    private boolean existSameCriterionAndInterval(CriterionSatisfactionDTO satisfaction){
        for(CriterionSatisfactionDTO otherSatisfaction : criterionSatisfactionDTOs){
            if((!otherSatisfaction.equals(satisfaction))&&
                    (!otherSatisfaction.isIsDeleted())&&
                    (!satisfaction.isIsDeleted())&&
                    (sameCriterion(otherSatisfaction,satisfaction)) &&
                    (sameInterval(otherSatisfaction,satisfaction))) {
                return true;
            }
        }
        return false;
    }

    private boolean sameCriterion(CriterionSatisfactionDTO otherSatisfaction,
            CriterionSatisfactionDTO satisfaction){
        if(otherSatisfaction.getCriterionWithItsType() == null) return false;
        Criterion otherCriterion = otherSatisfaction.getCriterionWithItsType().getCriterion();
        if(otherCriterion.getId().equals(satisfaction.getCriterionWithItsType().getCriterion().getId()))
            return true;
        return false;
    }

    private boolean sameCriterionType(CriterionSatisfactionDTO otherSatisfaction,
            CriterionSatisfactionDTO satisfaction){
        if(otherSatisfaction.getCriterionWithItsType() == null) return false;
        ICriterionType<?> criterionType = otherSatisfaction.getCriterionWithItsType().getType();
        if(criterionType.equals(satisfaction.getCriterionWithItsType().getType()))
            return true;
        return false;
    }

    private boolean sameInterval(CriterionSatisfactionDTO otherSatisfaction,
            CriterionSatisfactionDTO satisfaction){
        if(otherSatisfaction.getStartDate() == null) return false;
        Interval otherInterval = otherSatisfaction.getInterval();
        Interval interval = satisfaction.getInterval();
        if((satisfaction.overlapsWith(otherInterval))||
                (otherSatisfaction.overlapsWith(interval)))
            return true;
        return false;
    }

    @Override
    public void validate()
            throws ValidationException,IllegalStateException {
        validateDTOs();
    }

    @Override
    public void confirm()
            throws ValidationException,IllegalStateException {
        updateDTOs();
    }

    private void validateDTOs() throws ValidationException{
        Set<CriterionSatisfactionDTO> listDTOs =
                new HashSet<CriterionSatisfactionDTO>(criterionSatisfactionDTOs);
        for(CriterionSatisfactionDTO satisfactionDTO : listDTOs){
            InvalidValue[] invalidValues;
            invalidValues = satisfactionDTOValidator.getInvalidValues(satisfactionDTO);
            if (invalidValues.length > 0){
                throw new ValidationException(invalidValues);
            }
            Criterion criterion = satisfactionDTO.getCriterionWithItsType().getCriterion();
            if(checkSameCriterionAndSameInterval(satisfactionDTO)){
                throw new IllegalStateException(_(" The "+criterion.getName()+
                    " can not be assigned to this resource. Its interval overlap with other criterion"));
            }
            if(checkNotAllowSimultaneousCriterionsPerResource(satisfactionDTO)){
                throw new IllegalStateException(_(" The "+criterion.getName()+
                                        "is not valid, the criterionType overlap other criterionSatisfaction whith same criterionType"));
            }
        }
    }


    private void updateDTOs()throws ValidationException,IllegalStateException {
        //Create a new list of Criterion satisfaction
        Set<CriterionSatisfaction> newList = new HashSet<CriterionSatisfaction>();
        for(CriterionSatisfactionDTO satisfactionDTO :criterionSatisfactionDTOs){
            CriterionSatisfaction satisfaction;
            if(satisfactionDTO.isNewObject()){
                Criterion criterion = satisfactionDTO.getCriterionWithItsType().getCriterion();
                Interval interval = satisfactionDTO.getInterval();
                satisfaction = CriterionSatisfaction.create(criterion, worker, interval);

            }else{
                satisfaction = satisfactionDTO.getCriterionSatisfaction();
                if(satisfactionDTO.isIsDeleted()){
                     satisfaction.setIsDeleted(true);
                }else{
                    satisfaction.setStartDate(satisfactionDTO.getStartDate());
                    satisfaction.finish(satisfactionDTO.getEndDate());
                }
            }
            newList.add(satisfaction);
        }
        worker.addSatisfactions(newList);
    }
}
